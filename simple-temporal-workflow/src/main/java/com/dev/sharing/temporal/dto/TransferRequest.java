package com.dev.sharing.temporal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
    @NotBlank String fromAccountId,
    @NotBlank String toAccountId,
    @Positive long amount) {}
