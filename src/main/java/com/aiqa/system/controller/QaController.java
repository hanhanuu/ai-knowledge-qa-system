package com.aiqa.system.controller;

import com.aiqa.system.dto.ApiResponse;
import com.aiqa.system.dto.AskRequest;
import com.aiqa.system.dto.AskResponse;
import com.aiqa.system.model.AgentTrace;
import com.aiqa.system.service.QaOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qa")
public class QaController {

    private final QaOrchestratorService qaOrchestratorService;

    public QaController(QaOrchestratorService qaOrchestratorService) {
        this.qaOrchestratorService = qaOrchestratorService;
    }

    @PostMapping("/ask")
    public ApiResponse<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        return ApiResponse.success(qaOrchestratorService.ask(request));
    }

    @GetMapping("/trace")
    public ApiResponse<AgentTrace> trace() {
        return ApiResponse.success(qaOrchestratorService.latestTrace());
    }
}
