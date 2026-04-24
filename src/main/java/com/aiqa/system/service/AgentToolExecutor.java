package com.aiqa.system.service;

import com.aiqa.system.agent.AgentTool;
import com.aiqa.system.agent.ToolContext;
import com.aiqa.system.agent.ToolResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgentToolExecutor {

    private final Map<String, AgentTool> tools;

    public AgentToolExecutor(List<AgentTool> tools) {
        this.tools = tools.stream().collect(Collectors.toMap(AgentTool::name, Function.identity()));
    }

    public ToolResult execute(String toolName, ToolContext context) {
        AgentTool tool = tools.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        return tool.execute(context);
    }
}
