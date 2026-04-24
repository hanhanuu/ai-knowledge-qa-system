package com.aiqa.system.controller;

import com.aiqa.system.dto.ApiResponse;
import com.aiqa.system.model.AgentTrace;
import com.aiqa.system.service.AgentObservabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/observability")
public class ObservabilityController {

    private final AgentObservabilityService agentObservabilityService;

    public ObservabilityController(AgentObservabilityService agentObservabilityService) {
        this.agentObservabilityService = agentObservabilityService;
    }

    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> metrics() {
        return ApiResponse.success(agentObservabilityService.metrics());
    }

    @GetMapping("/traces")
    public ApiResponse<List<AgentTrace>> traces() {
        return ApiResponse.success(agentObservabilityService.recentTraces());
    }
}
