package com.example.demo.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Queue-backed Orchestrator engine with retries, exponential backoff and micrometer metrics using injected MeterRegistry.
 */
@Component
public class OrchestratorEngine {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorEngine.class);

    private final BlockingQueue<Orchestrator.Node> queue = new LinkedBlockingQueue<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "orchestrator-worker");
        t.setDaemon(true);
        return t;
    });

    private final MeterRegistry registry;
    // Micrometer metrics
    private final Counter queuedCounter;
    private final Counter processedCounter;
    private final Counter successesCounter;
    private final Counter failuresCounter;
    // per-node counters registered lazily using injected registry
    private final ConcurrentMap<String, Counter> nodeQueued = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> nodeProcessed = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> nodeSuccess = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> nodeFailure = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> nodeAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public OrchestratorEngine(MeterRegistry registry) {
        this.registry = registry;
        this.queuedCounter = Counter.builder("orchestrator.queued.total").description("Total nodes enqueued").register(registry);
        this.processedCounter = Counter.builder("orchestrator.processed.total").description("Total nodes processed").register(registry);
        this.successesCounter = Counter.builder("orchestrator.successes.total").description("Total successful nodes").register(registry);
        this.failuresCounter = Counter.builder("orchestrator.failures.total").description("Total failed nodes").register(registry);

        // Gauge for current queue size
        Gauge.builder("orchestrator.queue.size", queue, BlockingQueue::size).description("Current queue size").register(registry);

        // Start worker loop
        worker.submit(this::processLoop);
    }

    private Counter nodeCounter(ConcurrentMap<String, Counter> map, String name, String nodeId) {
        return map.computeIfAbsent(nodeId, id -> Counter.builder(name).description(name + " per node").tag("node", nodeId).register(registry));
    }

    public void enqueue(Orchestrator.Node node) {
        queue.offer(node);
        queuedCounter.increment();
        // per-node queued counter
        nodeCounter(nodeQueued, "orchestrator.queued.total", node.id).increment();
        log.info("Enqueued node {}", node.id);
    }

    private void processLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Orchestrator.Node node = queue.take();
                processNode(node);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                log.error("Orchestrator loop error", t);
            }
        }
    }

    private void processNode(Orchestrator.Node node) {
        processedCounter.increment();
        nodeCounter(nodeProcessed, "orchestrator.processed.total", node.id).increment();
        // check dependencies are not enforced here; node should be enqueued in dependency order or be self-contained
        int maxRetries = Math.max(1, node.maxRetries);
        int attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            attempts.computeIfAbsent(node.id, k -> new AtomicInteger()).incrementAndGet();
            nodeCounter(nodeAttempts, "orchestrator.attempts.total", node.id).increment();
            try {
                log.info("Processing node {} attempt {}/{}", node.id, attempt, maxRetries);
                node.action.run();
                successesCounter.increment();
                nodeCounter(nodeSuccess, "orchestrator.successes.total", node.id).increment();
                log.info("Node {} succeeded", node.id);
                return;
            } catch (Throwable ex) {
                log.warn("Node {} failed attempt {}/{}: {}", node.id, attempt, maxRetries, ex.getMessage());
                if (attempt >= maxRetries) {
                    failuresCounter.increment();
                    nodeCounter(nodeFailure, "orchestrator.failures.total", node.id).increment();
                    log.error("Node {} ultimately failed after {} attempts", node.id, attempt);
                } else {
                    // exponential backoff with jitter
                    long backoffMs = (long) (Math.pow(2, attempt) * 100L);
                    long jitter = ThreadLocalRandom.current().nextLong(0, 100);
                    try {
                        Thread.sleep(backoffMs + jitter);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    public Map<String, Number> metrics() {
        // keep returning aggregated view for controller compatibility
        return Map.of(
                "queued", (Number) queuedCounter.count(),
                "processed", (Number) processedCounter.count(),
                "successes", (Number) successesCounter.count(),
                "failures", (Number) failuresCounter.count()
        );
    }

    public int getAttempts(String nodeId) {
        AtomicInteger a = attempts.get(nodeId);
        return a == null ? 0 : a.get();
    }

    public void shutdown() {
        worker.shutdownNow();
    }
}
