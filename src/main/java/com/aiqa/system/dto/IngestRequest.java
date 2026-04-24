package com.aiqa.system.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record IngestRequest(
        @NotBlank String title,
        @NotBlank String content,
        Map<String, String> metadata
) {
}
