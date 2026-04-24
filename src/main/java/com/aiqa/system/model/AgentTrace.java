package com.aiqa.system.model;

import java.time.LocalDateTime;
import java.util.List;

public record AgentTrace(
        String route,
        String originalQuery,
        String rewrittenQuery,
        List<String> steps,
        List<String> evidence,
        LocalDateTime createdAt
) {
}
