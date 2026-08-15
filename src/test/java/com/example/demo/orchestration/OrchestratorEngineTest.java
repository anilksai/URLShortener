package com.example.demo.orchestration;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class OrchestratorEngineTest {

    @Test
    void nodeProcessingIncrementsMetrics() throws InterruptedException {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrchestratorEngine engine = new OrchestratorEngine(registry);

        CountDownLatch latch = new CountDownLatch(1);
        Orchestrator.Node node = new Orchestrator.Node("test-node", () -> {
            // do quick work
            latch.countDown();
        });
        node.maxRetries = 1;

        engine.enqueue(node);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "Node action should complete within 2s");

        // verify metrics
        double processed = registry.get("orchestrator.processed.total").counter().count();
        assertTrue(processed >= 1.0);

        double nodeProcessed = registry.get("orchestrator.processed.total").tag("node", "test-node").counter().count();
        assertTrue(nodeProcessed >= 1.0);

        engine.shutdown();
    }
}
