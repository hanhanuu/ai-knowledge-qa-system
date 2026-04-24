package com.aiqa.system.model;

public record RetrievalHit(
        DocumentChunk chunk,
        double score,
        String source
) {
}
