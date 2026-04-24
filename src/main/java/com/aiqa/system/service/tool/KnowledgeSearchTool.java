package com.aiqa.system.service.tool;

import com.aiqa.system.agent.AgentTool;
import com.aiqa.system.agent.ToolContext;
import com.aiqa.system.agent.ToolResult;
import com.aiqa.system.model.RetrievalHit;
import com.aiqa.system.service.HybridRetrievalService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgeSearchTool implements AgentTool {

    private final HybridRetrievalService hybridRetrievalService;

    public KnowledgeSearchTool(HybridRetrievalService hybridRetrievalService) {
        this.hybridRetrievalService = hybridRetrievalService;
    }

    @Override
    public String name() {
        return "knowledge_search";
    }

    @Override
    public ToolResult execute(ToolContext context) {
        List<RetrievalHit> hits = hybridRetrievalService.search(context.rewrittenQuery());
        List<String> evidence = hits.stream()
                .map(hit -> hit.chunk().title() + " | " + hit.chunk().content())
                .limit(4)
                .toList();
        return new ToolResult(
                name(),
                "Retrieved " + hits.size() + " candidate chunks by hybrid retrieval.",
                evidence,
                Map.of("hitCount", hits.size())
        );
    }
}
