package com.example.dedmoroz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "fal")
public record FalProperties(
        @NotBlank String apiKey,
        @NotBlank String model,
        @Positive long pollDelayMs,
        @Positive long timeoutSeconds
) { }
