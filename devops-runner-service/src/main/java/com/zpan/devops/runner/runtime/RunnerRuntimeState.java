package com.zpan.devops.runner.runtime;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RunnerRuntimeState {

    private final AtomicInteger currentConcurrency = new AtomicInteger(0);

    public int getCurrentConcurrency() {
        return currentConcurrency.get();
    }

    public int increment() {
        return currentConcurrency.incrementAndGet();
    }

    public int decrement() {
        return currentConcurrency.decrementAndGet();
    }

}
