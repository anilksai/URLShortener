package com.example.demo.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prototype orchestration layer demonstrating an agentic execution model with simple dependency graph,
 * bounded retries, audit logging and human approval checkpoint hooks. This is a small, testable prototype
 * for integration into CI/CD or a higher-level controller.
 */
public class Orchestrator {
    private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    public static class Node {
        public final String id;
        public final Runnable action;
        public final List<String> dependsOn = new ArrayList<>();
        public int maxRetries = 1;

        public Node(String id, Runnable action) { this.id = id; this.action = action; }
    }

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, String> status = new HashMap<>();

    public void addNode(Node n) { nodes.put(n.id, n); }

    public void run(ApprovalProvider approvalProvider) {
        log.info("Orchestration started at {}", Instant.now());
        for (Node n : nodes.values()) {
            // wait for dependencies
            boolean depsOk = n.dependsOn.stream().allMatch(d -> "SUCCESS".equals(status.get(d)));
            if (!depsOk) { status.put(n.id, "SKIPPED"); log.warn("Skipping {} due to unsatisfied dependencies", n.id); continue; }

           // approval checkpoint
            if (approvalProvider != null && !approvalProvider.approve(n.id)) {
                status.put(n.id, "HOLD"); log.warn("Node {} put on hold by approver", n.id); break; }

            AtomicInteger attempt = new AtomicInteger();
            boolean ok = false;
            while (attempt.incrementAndGet() <= Math.max(1, n.maxRetries)) {
                try {
                    log.info("Executing {} attempt {}", n.id, attempt.get());
                    n.action.run();
                    ok = true; status.put(n.id, "SUCCESS"); break;
                } catch (Throwable t) {
                    log.error("Node {} failed on attempt {}: {}", n.id, attempt.get(), t.getMessage());
                    status.put(n.id, "RETRYING");
                }
            }
            if (!ok) { status.put(n.id, "FAILED"); log.error("Node {} ultimately failed", n.id); break; }
        }
        log.info("Orchestration completed with statuses: {}", status);
    }

    public interface ApprovalProvider { boolean approve(String nodeId); }
}