package com.aiqa.system.service;

import com.aiqa.system.model.ChatTurn;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMemoryService {

    private static final int MAX_TURNS = 8;

    private final Map<String, Deque<ChatTurn>> sessionMemory = new ConcurrentHashMap<>();

    public List<ChatTurn> recentTurns(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }
        Deque<ChatTurn> turns = sessionMemory.getOrDefault(sessionId, new ArrayDeque<>());
        return new ArrayList<>(turns);
    }

    public void appendUserTurn(String sessionId, String content) {
        appendTurn(sessionId, "user", content);
    }

    public void appendAssistantTurn(String sessionId, String content) {
        appendTurn(sessionId, "assistant", content);
    }

    private void appendTurn(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank() || content == null || content.isBlank()) {
            return;
        }
        Deque<ChatTurn> turns = sessionMemory.computeIfAbsent(sessionId, key -> new ArrayDeque<>());
        if (turns.size() >= MAX_TURNS) {
            turns.removeFirst();
        }
        turns.addLast(new ChatTurn(role, content, LocalDateTime.now()));
    }
}
