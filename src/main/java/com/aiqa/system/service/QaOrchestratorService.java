package com.aiqa.system.service;

import com.aiqa.system.dto.AskRequest;
import com.aiqa.system.dto.AskResponse;
import com.aiqa.system.model.AgentTrace;
import com.aiqa.system.model.ChatTurn;
import com.aiqa.system.model.ToolCallRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class QaOrchestratorService {

    private final AgentRouterService agentRouterService;
    private final QueryRewriteService queryRewriteService;
    private final QwenChatService qwenChatService;
    private final ConversationMemoryService conversationMemoryService;
    private final ToolPlanningService toolPlanningService;
    private final AgentToolExecutor agentToolExecutor;
    private final AgentObservabilityService agentObservabilityService;
    private final AtomicReference<AgentTrace> latestTrace = new AtomicReference<>();

    public QaOrchestratorService(AgentRouterService agentRouterService,
                                 QueryRewriteService queryRewriteService,
                                 QwenChatService qwenChatService,
                                 ConversationMemoryService conversationMemoryService,
                                 ToolPlanningService toolPlanningService,
                                 AgentToolExecutor agentToolExecutor,
                                 AgentObservabilityService agentObservabilityService) {
        this.agentRouterService = agentRouterService;
        this.queryRewriteService = queryRewriteService;
        this.qwenChatService = qwenChatService;
        this.conversationMemoryService = conversationMemoryService;
        this.toolPlanningService = toolPlanningService;
        this.agentToolExecutor = agentToolExecutor;
        this.agentObservabilityService = agentObservabilityService;
    }

    public AskResponse ask(AskRequest request) {
        String sessionId = normalizeSessionId(request.sessionId());
        String route = agentRouterService.route(request);
        String rewrittenQuery = queryRewriteService.rewrite(request.question());
        List<ChatTurn> memory = conversationMemoryService.recentTurns(sessionId);
        List<String> steps = new ArrayList<>();
        steps.add("route=" + route);
        steps.add("rewrite=" + rewrittenQuery);
        steps.add("sessionId=" + sessionId);
        steps.add("memory_turns=" + memory.size());

        List<String> plannedTools = toolPlanningService.plan(route, request.question());
        steps.add("planned_tools=" + plannedTools);
        List<ToolCallRecord> toolCalls = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        List<String> suggestions = List.of("Ask for implementation details", "Ask for a step by step solution", "Add stack constraints and continue");

        com.aiqa.system.agent.ToolContext toolContext = new com.aiqa.system.agent.ToolContext(
                sessionId,
                request.question(),
                rewrittenQuery,
                route,
                memory
        );

        Map<String, Object> memoryPayload = Map.of();
        for (String toolName : plannedTools) {
            var result = agentToolExecutor.execute(toolName, toolContext);
            toolCalls.add(new ToolCallRecord(result.toolName(), "ok", result.summary()));
            steps.add("tool=" + result.toolName() + " -> " + result.summary());
            if ("conversation_memory".equals(toolName)) {
                memoryPayload = result.payload();
            }
            if (!result.evidence().isEmpty()) {
                evidence = new ArrayList<>(result.evidence());
            }
            if ("clarify_question".equals(toolName)) {
                @SuppressWarnings("unchecked")
                List<String> prompts = (List<String>) result.payload().getOrDefault("questions", List.of());
                suggestions = prompts.isEmpty() ? suggestions : prompts;
            }
        }

        if ("clarify".equals(route)) {
            String clarifyAnswer = "The question is still too broad. Please add a target system, scope or comparison object.";
            AgentTrace trace = new AgentTrace(route, request.question(), rewrittenQuery, steps, List.of(), LocalDateTime.now());
            latestTrace.set(trace);
            conversationMemoryService.appendUserTurn(sessionId, request.question());
            conversationMemoryService.appendAssistantTurn(sessionId, clarifyAnswer);
            agentObservabilityService.recordQuestion(route, 0, false, toolCalls, trace);
            return new AskResponse(
                    sessionId,
                    clarifyAnswer,
                    route,
                    rewrittenQuery,
                    List.of(),
                    suggestions,
                    toolCalls.stream().map(item -> item.toolName() + ": " + item.summary()).toList(),
                    memory.size()
            );
        }

        String answer;
        if ("retrieval_qa".equals(route)) {
            String context = String.join("\n---\n", evidence);
            answer = qwenChatService.answer(
                    "You are an enterprise knowledge assistant. Answer from the retrieved evidence and explain the basis.",
                    "Question: " + request.question()
                            + "\nRewritten query: " + rewrittenQuery
                            + "\nMemory: " + memoryPayload
                            + "\nEvidence:\n" + context
            );
        } else {
            answer = qwenChatService.answer(
                    "You are an enterprise knowledge assistant. Give a structured and actionable answer.",
                    "Question: " + request.question() + "\nMemory: " + memoryPayload
            );
        }

        AgentTrace trace = new AgentTrace(route, request.question(), rewrittenQuery, steps, evidence, LocalDateTime.now());
        latestTrace.set(trace);
        conversationMemoryService.appendUserTurn(sessionId, request.question());
        conversationMemoryService.appendAssistantTurn(sessionId, answer);
        agentObservabilityService.recordQuestion(route, evidence.size(), !evidence.isEmpty(), toolCalls, trace);

        return new AskResponse(
                sessionId,
                answer,
                route,
                rewrittenQuery,
                evidence,
                suggestions,
                toolCalls.stream().map(item -> item.toolName() + ": " + item.summary()).toList(),
                memory.size()
        );
    }

    public AgentTrace latestTrace() {
        return latestTrace.get();
    }

    private String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return "session-" + UUID.randomUUID();
        }
        return sessionId.trim();
    }
}
