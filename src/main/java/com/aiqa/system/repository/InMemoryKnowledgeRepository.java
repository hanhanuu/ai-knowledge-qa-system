package com.aiqa.system.repository;

import com.aiqa.system.model.DocumentChunk;
import com.aiqa.system.model.KnowledgeDocument;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryKnowledgeRepository {

    private final Map<String, KnowledgeDocument> documents = new ConcurrentHashMap<>();
    private final Map<String, DocumentChunk> chunks = new ConcurrentHashMap<>();

    public void saveDocument(KnowledgeDocument document) {
        documents.put(document.id(), document);
    }

    public void saveChunks(List<DocumentChunk> documentChunks) {
        for (DocumentChunk chunk : documentChunks) {
            chunks.put(chunk.chunkId(), chunk);
        }
    }

    public List<DocumentChunk> findAllChunks() {
        return new ArrayList<>(chunks.values());
    }

    public int documentCount() {
        return documents.size();
    }

    public int chunkCount() {
        return chunks.size();
    }
}
