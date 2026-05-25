package com.zpan.devops.pipeline.service.impl;

import com.zpan.devops.pipeline.model.vo.PipelineLogVO;
import com.zpan.devops.pipeline.model.vo.PipelineRunCompleteEventVO;
import com.zpan.devops.pipeline.service.LogStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class LogStreamServiceImpl implements LogStreamService {

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    /**
     * 当前正在订阅这次流水线日志的所有链接
     */
    private final Map<Long, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();


    @Override
    public SseEmitter subscribe(Long pipelineRunId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitterMap.computeIfAbsent(pipelineRunId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(pipelineRunId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(pipelineRunId, emitter);
        });

        emitter.onError(error -> {
            log.warn("SSE emitter error, pipelineRunId={}", pipelineRunId, error);
            removeEmitter(pipelineRunId, emitter);
        });

        try {
            emitter.send(SseEmitter.event().name("connected").data("connected"));
        } catch (IOException e) {
            log.warn("SSE emitter error, pipelineRunId={}", pipelineRunId, e);
            emitter.completeWithError(e);
            removeEmitter(pipelineRunId, emitter);
        }
        return emitter;
    }

    @Override
    public void publishLog(Long pipelineRunId, PipelineLogVO logVO) {
        List<SseEmitter> emitters = emitterMap.get(pipelineRunId);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("log").data(logVO));
            } catch (IOException e) {
                log.warn("SSE emitter error, pipelineRunId={}", pipelineRunId, e);
                emitter.completeWithError(e);
                removeEmitter(pipelineRunId, emitter);
            }
        }
    }

    @Override
    public void publishComplete(Long pipelineRunId, String status) {
        List<SseEmitter> emitters = emitterMap.get(pipelineRunId);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        PipelineRunCompleteEventVO eventVO = new PipelineRunCompleteEventVO();
        eventVO.setPipelineRunId(pipelineRunId);
        eventVO.setStatus(status);
        eventVO.setMessage("流水线运行结束，状态：" + status);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(eventVO));
                emitter.complete();
            } catch (IOException e) {
                log.warn("Send SSE complete event failed, pipelineRunId={}", pipelineRunId, e);
                emitter.completeWithError(e);
            }
        }
        emitterMap.remove(pipelineRunId);
    }

    @Override
    public void close(Long pipelineRunId) {
        List<SseEmitter> emitters = emitterMap.remove(pipelineRunId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            emitter.complete();
        }
    }

    private void removeEmitter(Long pipelineRunId, SseEmitter emitter) {

        List<SseEmitter> emitters = emitterMap.get(pipelineRunId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emitterMap.remove(pipelineRunId);
        }
    }
}
