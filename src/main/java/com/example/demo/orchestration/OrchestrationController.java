package com.example.demo.orchestration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/orchestrator")
public class OrchestrationController {

    private final OrchestratorEngine engine;
    private final Map<String, Boolean> approvals = new ConcurrentHashMap<>();

    @Autowired
    public OrchestrationController(OrchestratorEngine engine) {
        this.engine = engine;

        // sample nodes for demo (enqueue to engine)
        Orchestrator.Node n1 = new Orchestrator.Node("generate-artifact", () -> {
            // simulate work
            System.out.println("Generating artifact");
        });
        Orchestrator.Node n2 = new Orchestrator.Node("run-tests", () -> {
            System.out.println("Running tests");
        });
        n2.dependsOn.add("generate-artifact");
        Orchestrator.Node n3 = new Orchestrator.Node("deploy", () -> { System.out.println("Deploying"); });
        n3.dependsOn.add("run-tests");

        // Enqueue in logical order; engine uses queue + retries so order is preserved for simple DAGs
        engine.enqueue(n1);
        engine.enqueue(n2);
        engine.enqueue(n3);
    }

    @PostMapping("/metrics")
    public ResponseEntity<Object> metrics() {
        return ResponseEntity.ok(engine.metrics());
    }

    @PostMapping("/approve/{nodeId}")
    public ResponseEntity<Object> approve(@PathVariable String nodeId) {
        approvals.put(nodeId, true);
        return ResponseEntity.ok(Map.of("approved", nodeId));
    }

    @PostMapping("/reject/{nodeId}")
    public ResponseEntity<Object> reject(@PathVariable String nodeId) {
        approvals.put(nodeId, false);
        return ResponseEntity.ok(Map.of("rejected", nodeId));
    }
}