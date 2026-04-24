package com.aiqa.system.service;

import com.aiqa.system.model.AgentTrace;
import com.aiqa.system.model.ToolCallRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AgentObservabilityService {

    private static final int MAX_RECENT_TRACES = 10;

    private final AtomicLong totalQuestions = new AtomicLong();
    private final AtomicLong retrievalQuestions = new AtomicLong();
    private final AtomicLong knowledgeHits = new AtomicLong();
    private final AtomicLong totalRetrievedChunks = new AtomicLong();
    private final Map<String, AtomicLong> routeCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> toolCounters = new ConcurrentHashMap<>();
    private final Deque<AgentTrace> recentTraces = new ArrayDeque<>();

    public void recordQuestion(String route, int retrievedChunks, boolean hit, List<ToolCallRecord> toolCalls, AgentTrace trace) {
        totalQuestions.incrementAndGet();
        routeCounters.computeIfAbsent(route, key -> new AtomicLong()).incrementAndGet();
        if ("retrieval_qa".equals(route)) {
            retrievalQuestions.incrementAndGet();
        }
        if (hit) {
            knowledgeHits.incrementAndGet();
        }
        totalRetrievedChunks.addAndGet(retrievedChunks);
        for (ToolCallRecord toolCall : toolCalls) {
            toolCounters.computeIfAbsent(toolCall.toolName(), key -> new AtomicLong()).incrementAndGet();
        }
        synchronized (recentTraces) {
            if (recentTraces.size() >= MAX_RECENT_TRACES) {
                recentTraces.removeFirst();
            }
            recentTraces.addLast(trace);
        }
    }

    public Map<String, Object> metrics() {
        Map<String, Object> payload = new LinkedHashMap<>();
        long total = totalQuestions.get();
        long retrieval = retrievalQuestions.get();
        payload.put("totalQuestions", total);
        payload.put("retrievalQuestions", retrieval);
        payload.put("knowledgeHitRatePct", retrieval == 0 ? 0 : round(knowledgeHits.get() * 100.0 / retrieval));
        payload.put("avgRetrievedChunks", total == 0 ? 0 : round((double) totalRetrievedChunks.get() / total));
        payload.put("routeCounters", asNumberMap(routeCounters));
        payload.put("toolCounters", asNumberMap(toolCounters));
        return payload;
    }

    public List<AgentTrace> recentTraces() {
        synchronized (recentTraces) {
            return List.copyOf(recentTraces);
        }
    }

    private Map<String, Long> asNumberMap(Map<String, AtomicLong> source) {
        Map<String, Long> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
