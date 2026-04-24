package com.aiqa.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private final Qwen qwen = new Qwen();
    private final Rag rag = new Rag();

    public Qwen getQwen() {
        return qwen;
    }

    public Rag getRag() {
        return rag;
    }

    public static class Qwen {
        private String baseUrl;
        private String apiKey;
        private String chatModel;
        private String embeddingModel;
        private int timeoutSeconds = 20;
        private boolean mockEnabled = true;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }
    }

    public static class Rag {
        private boolean rewriteEnabled = true;
        private int chunkSize = 220;
        private int chunkOverlap = 40;
        private int keywordTopK = 5;
        private int vectorTopK = 5;
        private int fusedTopK = 6;

        public boolean isRewriteEnabled() {
            return rewriteEnabled;
        }

        public void setRewriteEnabled(boolean rewriteEnabled) {
            this.rewriteEnabled = rewriteEnabled;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public int getChunkOverlap() {
            return chunkOverlap;
        }

        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }

        public int getKeywordTopK() {
            return keywordTopK;
        }

        public void setKeywordTopK(int keywordTopK) {
            this.keywordTopK = keywordTopK;
        }

        public int getVectorTopK() {
            return vectorTopK;
        }

        public void setVectorTopK(int vectorTopK) {
            this.vectorTopK = vectorTopK;
        }

        public int getFusedTopK() {
            return fusedTopK;
        }

        public void setFusedTopK(int fusedTopK) {
            this.fusedTopK = fusedTopK;
        }
    }
}
