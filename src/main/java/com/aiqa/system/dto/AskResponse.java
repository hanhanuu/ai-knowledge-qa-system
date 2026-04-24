package com.aiqa.system.dto;

import java.util.List;

public record AskResponse(
        String sessionId,
        String answer,
        String route,
        String rewrittenQuery,
        List<String> evidence,
        List<String> suggestions,
        List<String> toolCalls,
        int memoryTurnCount
) {
}
