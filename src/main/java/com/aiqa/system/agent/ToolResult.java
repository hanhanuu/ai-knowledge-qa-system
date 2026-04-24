package com.aiqa.system.agent;

import java.util.List;
import java.util.Map;

public record ToolResult(
        String toolName,
        String summary,
        List<String> evidence,
        Map<String, Object> payload
) {
}
