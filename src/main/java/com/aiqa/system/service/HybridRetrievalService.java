package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import com.aiqa.system.model.DocumentChunk;
import com.aiqa.system.model.RetrievalHit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HybridRetrievalService {

    private final KeywordRetriever keywordRetriever;
    private final VectorRetriever vectorRetriever;
    private final AiProperties properties;

    public HybridRetrievalService(KeywordRetriever keywordRetriever,
                                  VectorRetriever vectorRetriever,
                                  AiProperties properties) {
        this.keywordRetriever = keywordRetriever;
        this.vectorRetriever = vectorRetriever;
        this.properties = properties;
    }

    public List<RetrievalHit> search(String query) {
        List<RetrievalHit> keywordHits = keywordRetriever.search(query);
        List<RetrievalHit> vectorHits = vectorRetriever.search(query);

        Map<String, Double> fusedScores = new LinkedHashMap<>();
        Map<String, DocumentChunk> chunkMap = new LinkedHashMap<>();
        applyRrf(keywordHits, fusedScores, chunkMap);
        applyRrf(vectorHits, fusedScores, chunkMap);

        List<RetrievalHit> merged = new ArrayList<>();
        for (Map.Entry<String, Double> entry : fusedScores.entrySet()) {
            merged.add(new RetrievalHit(chunkMap.get(entry.getKey()), entry.getValue(), "hybrid"));
        }

        return merged.stream()
                .sorted(Comparator.comparingDouble(RetrievalHit::score).reversed())
                .limit(properties.getRag().getFusedTopK())
                .toList();
    }

    private void applyRrf(List<RetrievalHit> hits,
                          Map<String, Double> fusedScores,
                          Map<String, DocumentChunk> chunkMap) {
        for (int rank = 0; rank < hits.size(); rank++) {
            RetrievalHit hit = hits.get(rank);
            fusedScores.merge(hit.chunk().chunkId(), 1.0 / (rank + 60.0), Double::sum);
            chunkMap.put(hit.chunk().chunkId(), hit.chunk());
        }
    }
}
