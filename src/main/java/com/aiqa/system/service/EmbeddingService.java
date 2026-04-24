package com.aiqa.system.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    public List<Double> embed(String text) {
        double[] buckets = new double[12];
        for (char current : text.toCharArray()) {
            buckets[current % buckets.length] += 1;
        }
        double norm = 0;
        for (double bucket : buckets) {
            norm += bucket * bucket;
        }
        norm = Math.sqrt(norm == 0 ? 1 : norm);

        List<Double> vector = new ArrayList<>(buckets.length);
        for (double bucket : buckets) {
            vector.add(bucket / norm);
        }
        return vector;
    }

    public double cosineSimilarity(List<Double> left, List<Double> right) {
        int size = Math.min(left.size(), right.size());
        double score = 0;
        for (int i = 0; i < size; i++) {
            score += left.get(i) * right.get(i);
        }
        return score;
    }
}
