package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import com.aiqa.system.model.DocumentChunk;
import com.aiqa.system.model.RetrievalHit;
import com.aiqa.system.repository.InMemoryKnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class KeywordRetriever {

    private final InMemoryKnowledgeRepository repository;
    private final AiProperties properties;

    public KeywordRetriever(InMemoryKnowledgeRepository repository, AiProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public List<RetrievalHit> search(String query) {
        List<String> tokens = Arrays.stream(query.toLowerCase().split("[\\s,\uFF0C\u3002\uFF1F\uFF01;]+"))
                .filter(token -> !token.isBlank())
                .toList();

        return repository.findAllChunks().stream()
                .map(chunk -> new RetrievalHit(chunk, keywordScore(chunk, tokens), "keyword"))
                .filter(hit -> hit.score() > 0)
                .sorted(Comparator.comparingDouble(RetrievalHit::score).reversed())
                .limit(properties.getRag().getKeywordTopK())
                .toList();
    }

    private double keywordScore(DocumentChunk chunk, List<String> tokens) {
        String corpus = (chunk.title() + " " + chunk.content()).toLowerCase();
        return tokens.stream().filter(corpus::contains).count();
    }
}
