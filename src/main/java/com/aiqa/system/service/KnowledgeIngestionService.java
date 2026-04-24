package com.aiqa.system.service;

import com.aiqa.system.dto.IngestRequest;
import com.aiqa.system.model.DocumentChunk;
import com.aiqa.system.model.KnowledgeDocument;
import com.aiqa.system.repository.InMemoryKnowledgeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeIngestionService {

    private final TextChunkingService textChunkingService;
    private final EmbeddingService embeddingService;
    private final InMemoryKnowledgeRepository repository;

    public KnowledgeIngestionService(TextChunkingService textChunkingService,
                                     EmbeddingService embeddingService,
                                     InMemoryKnowledgeRepository repository) {
        this.textChunkingService = textChunkingService;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public Map<String, Object> ingest(IngestRequest request) {
        String documentId = UUID.randomUUID().toString();
        KnowledgeDocument document = new KnowledgeDocument(
                documentId,
                request.title(),
                request.content(),
                request.metadata() == null ? Map.of() : request.metadata(),
                LocalDateTime.now()
        );
        repository.saveDocument(document);

        List<String> segments = textChunkingService.semanticChunks(request.content());
        List<DocumentChunk> chunks = new ArrayList<>();
        for (int index = 0; index < segments.size(); index++) {
            String content = segments.get(index);
            chunks.add(new DocumentChunk(
                    documentId + "-chunk-" + index,
                    documentId,
                    request.title(),
                    content,
                    document.metadata(),
                    embeddingService.embed(content)
            ));
        }
        repository.saveChunks(chunks);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("documentId", documentId);
        payload.put("chunks", chunks.size());
        payload.put("documents", repository.documentCount());
        payload.put("totalChunks", repository.chunkCount());
        return payload;
    }
}
