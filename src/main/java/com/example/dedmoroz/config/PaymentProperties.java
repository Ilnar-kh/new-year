package com.example.dedmoroz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "payment.yookassa")
public record PaymentProperties(
        @NotBlank String shopId,
        @NotBlank String secretKey,
        @NotBlank String returnUrl,
        @NotBlank String webhookSecret
) {
}
