package com.aiqa.system.service.tool;

import com.aiqa.system.agent.AgentTool;
import com.aiqa.system.agent.ToolContext;
import com.aiqa.system.agent.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ClarifyQuestionTool implements AgentTool {

    @Override
    public String name() {
        return "clarify_question";
    }

    @Override
    public ToolResult execute(ToolContext context) {
        return new ToolResult(
                name(),
                "Generated clarification prompts for a broad question.",
                List.of(),
                Map.of(
                        "questions", List.of(
                                "Which module or domain do you want to focus on?",
                                "What output format do you expect?",
                                "Do you want architecture, implementation or troubleshooting details?"
                        )
                )
        );
    }
}
