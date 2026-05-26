package com.zpan.devops.pipeline.service.impl;

import com.zpan.devops.pipeline.model.vo.PipelineLogVO;
import com.zpan.devops.pipeline.service.LogStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LogStreamServiceImpl implements LogStreamService {

    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(Long pipelineRunId) {
        SseEmitter existingEmitter = emitterMap.get(pipelineRunId);
        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                log.warn("关闭旧的SSE emitter失败: pipelineRunId={}", pipelineRunId, e);
            }
        }

        SseEmitter emitter = new SseEmitter(0L); // 0表示永不超时

        emitter.onCompletion(() -> {
            log.info("SSE连接完成: pipelineRunId={}", pipelineRunId);
            emitterMap.remove(pipelineRunId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE连接超时: pipelineRunId={}", pipelineRunId);
            emitterMap.remove(pipelineRunId);
        });

        emitter.onError((ex) -> {
            log.error("SSE连接错误: pipelineRunId={}", pipelineRunId, ex);
            emitterMap.remove(pipelineRunId);
        });

        emitterMap.put(pipelineRunId, emitter);
        log.info("新的SSE连接已建立: pipelineRunId={}", pipelineRunId);

        return emitter;
    }

    @Override
    public void publishLog(Long pipelineRunId, PipelineLogVO logVO) {
        SseEmitter emitter = emitterMap.get(pipelineRunId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("log")
                        .data(logVO));
            } catch (IOException e) {
                log.error("发送日志SSE事件失败: pipelineRunId={}", pipelineRunId, e);
                emitterMap.remove(pipelineRunId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("完成emitter失败", ex);
                }
            }
        }
    }

    @Override
    public void publishComplete(Long pipelineRunId, String status) {
        SseEmitter emitter = emitterMap.get(pipelineRunId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(Map.of(
                                "pipelineRunId", pipelineRunId,
                                "status", status
                        )));
                emitter.complete();
                log.info("发送完成SSE事件: pipelineRunId={}, status={}", pipelineRunId, status);
            } catch (IOException e) {
                log.error("发送完成SSE事件失败: pipelineRunId={}", pipelineRunId, e);
                emitterMap.remove(pipelineRunId);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("完成emitter失败", ex);
                }
            }
        }
    }

    @Override
    public void close(Long pipelineRunId) {
        SseEmitter emitter = emitterMap.remove(pipelineRunId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("关闭SSE连接: pipelineRunId={}", pipelineRunId);
            } catch (Exception e) {
                log.error("关闭SSE连接失败: pipelineRunId={}", pipelineRunId, e);
            }
        }
    }
}
