package com.zpan.devops.pipeline.controller;

import com.zpan.devops.pipeline.service.LogStreamService;
import com.zpan.devops.pipeline.service.PipelineRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class PipelineLogStreamController {

    private final PipelineRunService pipelineRunService;

    private final LogStreamService logStreamService;

    @GetMapping(
            value = "/pipeline-runs/{id}/logs/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamLogs(@PathVariable Long id) {
        pipelineRunService.getById(id);
        return logStreamService.subscribe(id);
    }
}
