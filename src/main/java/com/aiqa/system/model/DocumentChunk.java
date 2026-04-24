package com.aiqa.system.model;

import java.util.List;
import java.util.Map;

public record DocumentChunk(
        String chunkId,
        String documentId,
        String title,
        String content,
        Map<String, String> metadata,
        List<Double> embedding
) {
}
