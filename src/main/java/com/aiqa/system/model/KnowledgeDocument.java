package com.aiqa.system.model;

import java.time.LocalDateTime;
import java.util.Map;

public record KnowledgeDocument(
        String id,
        String title,
        String content,
        Map<String, String> metadata,
        LocalDateTime createdAt
) {
}
