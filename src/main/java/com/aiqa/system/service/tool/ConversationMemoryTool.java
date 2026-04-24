package com.aiqa.system.service.tool;

import com.aiqa.system.agent.AgentTool;
import com.aiqa.system.agent.ToolContext;
import com.aiqa.system.agent.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ConversationMemoryTool implements AgentTool {

    @Override
    public String name() {
        return "conversation_memory";
    }

    @Override
    public ToolResult execute(ToolContext context) {
        List<String> memory = context.memory().stream()
                .map(turn -> turn.role() + ": " + turn.content())
                .toList();
        return new ToolResult(
                name(),
                "Loaded " + memory.size() + " conversation turns.",
                List.of(),
                Map.of("memory", memory)
        );
    }
}
