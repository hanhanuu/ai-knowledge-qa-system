package com.aiqa.system.controller;

import com.aiqa.system.dto.ApiResponse;
import com.aiqa.system.dto.IngestRequest;
import com.aiqa.system.service.KnowledgeIngestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService knowledgeIngestionService;

    public KnowledgeController(KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    @PostMapping("/ingest")
    public ApiResponse<Map<String, Object>> ingest(@Valid @RequestBody IngestRequest request) {
        return ApiResponse.success(knowledgeIngestionService.ingest(request));
    }
}
