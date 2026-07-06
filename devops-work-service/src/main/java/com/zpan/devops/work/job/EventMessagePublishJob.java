package com.zpan.devops.work.job;

import com.zpan.devops.work.service.EventMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessagePublishJob {

    private final EventMessageService eventMessageService;

    @Scheduled(fixedRate = 5000)
    public void publishPendingMessage() {
        eventMessageService.publishPendingMessage();
    }
}
