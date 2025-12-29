package com.example.dedmoroz.client;

import com.example.dedmoroz.config.PaymentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class YooKassaClient {

    private final WebClient webClient;
    private final PaymentProperties properties;

    public Mono<CreatePaymentResponse> createPayment(int amountRub, String description, String idempotenceKey, Long telegramId) {
        var payload = Map.of(
                "amount", Map.of("value", String.format("%d.00", amountRub), "currency", "RUB"),
                "payment_method_data", Map.of("type", "sbp"),
                "confirmation", Map.of("type", "redirect", "return_url", properties.returnUrl()),
                "capture", true,
                "description", description,
                "metadata", Map.of("telegram_id", telegramId)
        );

        return webClient.post()
                .uri("https://api.yookassa.ru/v3/payments")
                .header("Idempotence-Key", idempotenceKey)
                .headers(this::auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(CreatePaymentResponse.class);
    }

    public Mono<PaymentStatusResponse> getPaymentStatus(String paymentId) {
        return webClient.get()
                .uri("https://api.yookassa.ru/v3/payments/{id}", paymentId)
                .headers(this::auth)
                .retrieve()
                .bodyToMono(PaymentStatusResponse.class);
    }

    private void auth(HttpHeaders headers) {
        String auth = properties.shopId() + ":" + properties.secretKey();
        headers.setBasicAuth(Base64Utils.encodeToString(auth.getBytes()));
    }

    public record CreatePaymentResponse(String id, Map<String, Object> confirmation, Map<String, Object> amount, String status) {}
    public record PaymentStatusResponse(String id, String status, Map<String, Object> amount, Map<String, Object> confirmation) {}
}
