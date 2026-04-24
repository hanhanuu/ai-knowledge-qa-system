package com.aiqa.system.dto;

import jakarta.validation.constraints.NotBlank;

public record AskRequest(
        @NotBlank String question,
        String sessionId,
        boolean forceRetrieval
) {
}
