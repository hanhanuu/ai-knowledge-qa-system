package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private final AiProperties properties;

    public TextChunkingService(AiProperties properties) {
        this.properties = properties;
    }

    public List<String> semanticChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int chunkSize = properties.getRag().getChunkSize();
        int overlap = properties.getRag().getChunkOverlap();
        int start = 0;
        String normalized = text.replace("\r", "").trim();

        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());
            int boundary = findBoundary(normalized, end);
            if (boundary <= start) {
                boundary = end;
            }
            chunks.add(normalized.substring(start, boundary).trim());
            if (boundary == normalized.length()) {
                break;
            }
            start = Math.max(boundary - overlap, boundary);
        }
        return chunks.stream().filter(item -> !item.isBlank()).toList();
    }

    private int findBoundary(String text, int candidate) {
        for (int i = candidate; i > Math.max(0, candidate - 30); i--) {
            char current = text.charAt(i - 1);
            if (current == '\u3002' || current == '\uFF01' || current == '\uFF1F' || current == '\n') {
                return i;
            }
        }
        return candidate;
    }
}
