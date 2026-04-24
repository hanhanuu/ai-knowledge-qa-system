package com.aiqa.system.service;

import com.aiqa.system.config.AiProperties;
import org.springframework.stereotype.Service;

@Service
public class QueryRewriteService {

    private final AiProperties properties;

    public QueryRewriteService(AiProperties properties) {
        this.properties = properties;
    }

    public String rewrite(String question) {
        if (!properties.getRag().isRewriteEnabled()) {
            return question;
        }
        String normalized = question == null ? "" : question.trim().replaceAll("\\s+", " ");
        if (normalized.endsWith("?") || normalized.endsWith("\uFF1F")) {
            return normalized + " \u8BF7\u7ED3\u5408\u4E0A\u4E0B\u6587\u7ED9\u51FA\u51C6\u786E\u7ED3\u8BBA\u4E0E\u4F9D\u636E";
        }
        return normalized + "\u3002\u8BF7\u8865\u5168\u4E0A\u4E0B\u6587\u5173\u952E\u8BCD\u5E76\u7ED3\u5408\u77E5\u8BC6\u5E93\u56DE\u7B54";
    }
}
