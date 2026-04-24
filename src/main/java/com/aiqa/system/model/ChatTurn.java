package com.aiqa.system.model;

import java.time.LocalDateTime;

public record ChatTurn(
        String role,
        String content,
        LocalDateTime createdAt
) {
}
