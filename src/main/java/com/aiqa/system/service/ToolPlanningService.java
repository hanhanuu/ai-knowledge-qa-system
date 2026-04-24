package com.aiqa.system.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ToolPlanningService {

    public List<String> plan(String route, String query) {
        List<String> tools = new ArrayList<>();
        tools.add("conversation_memory");
        if ("retrieval_qa".equals(route)) {
            tools.add("knowledge_search");
        }
        if ("clarify".equals(route)) {
            tools.add("clarify_question");
        }
        if (query.toLowerCase().contains("history") || query.contains("之前") || query.contains("上次")) {
            tools.add("conversation_memory");
        }
        return tools.stream().distinct().toList();
    }
}
