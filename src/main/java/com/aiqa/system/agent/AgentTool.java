package com.aiqa.system.agent;

public interface AgentTool {

    String name();

    ToolResult execute(ToolContext context);
}
