package com.aiqa.system.agent;

import com.aiqa.system.model.ChatTurn;

import java.util.List;

public record ToolContext(
        String sessionId,
        String originalQuery,
        String rewrittenQuery,
        String route,
        List<ChatTurn> memory
) {
}
