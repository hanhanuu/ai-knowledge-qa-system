package com.aiqa.system.service;

import com.aiqa.system.dto.AskRequest;
import org.springframework.stereotype.Service;

@Service
public class AgentRouterService {

    public String route(AskRequest request) {
        String question = request.question();
        if (request.forceRetrieval()) {
            return "retrieval_qa";
        }
        if (question.contains("\u5bf9\u6bd4")
                || question.contains("\u533a\u522b")
                || question.contains("\u4e3a\u4ec0\u4e48")) {
            return "retrieval_qa";
        }
        if (question.length() < 6) {
            return "clarify";
        }
        return "direct_qa";
    }
}
