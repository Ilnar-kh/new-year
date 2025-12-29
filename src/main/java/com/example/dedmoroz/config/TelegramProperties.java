package com.example.dedmoroz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramProperties(
        @NotBlank String token,
        @NotBlank String username,
        String sampleVideoFileId,
        String sampleVideoUrl
) {
}
