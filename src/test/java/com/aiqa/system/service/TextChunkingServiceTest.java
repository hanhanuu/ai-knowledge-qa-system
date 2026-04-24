package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkingServiceTest {

    @Test
    void shouldSplitLongTextIntoMultipleChunks() {
        AiProperties properties = new AiProperties();
        properties.getRag().setChunkSize(20);
        properties.getRag().setChunkOverlap(5);

        TextChunkingService service = new TextChunkingService(properties);
        List<String> chunks = service.semanticChunks("First paragraph explains chunking. Second paragraph adds more context. Third paragraph verifies the split logic.");

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.size()).isGreaterThan(1);
    }
}
