package com.example.demo.orchestration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/orchestrator")
public class OrchestrationController {

    private final Orchestrator orchestrator;
    private final Map<String, Boolean> approvals = new ConcurrentHashMap<>();

    public OrchestrationController() {
        this.orchestrator = new Orchestrator();

        // sample nodes for demo
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

        orchestrator.addNode(n1); orchestrator.addNode(n2); orchestrator.addNode(n3);
    }

    @PostMapping("/start")
    public ResponseEntity<Object> start() {
        orchestrator.run(nodeId -> {
            // approval provider delegating to manual approvals map
            return approvals.getOrDefault(nodeId, false);
        });
        return ResponseEntity.ok(Map.of("status", "started"));
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