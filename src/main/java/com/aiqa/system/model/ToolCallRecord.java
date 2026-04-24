package com.aiqa.system.model;

public record ToolCallRecord(
        String toolName,
        String status,
        String summary
) {
}
