package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import com.aiqa.system.model.DocumentChunk;
import com.aiqa.system.model.RetrievalHit;
import com.aiqa.system.repository.InMemoryKnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class VectorRetriever {

    private final InMemoryKnowledgeRepository repository;
    private final EmbeddingService embeddingService;
    private final AiProperties properties;

    public VectorRetriever(InMemoryKnowledgeRepository repository,
                           EmbeddingService embeddingService,
                           AiProperties properties) {
        this.repository = repository;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }

    public List<RetrievalHit> search(String query) {
        List<Double> queryEmbedding = embeddingService.embed(query);
        return repository.findAllChunks().stream()
                .map(chunk -> scoreChunk(chunk, queryEmbedding))
                .sorted(Comparator.comparingDouble(RetrievalHit::score).reversed())
                .limit(properties.getRag().getVectorTopK())
                .toList();
    }

    private RetrievalHit scoreChunk(DocumentChunk chunk, List<Double> queryEmbedding) {
        double score = embeddingService.cosineSimilarity(queryEmbedding, chunk.embedding());
        return new RetrievalHit(chunk, score, "vector");
    }
}
