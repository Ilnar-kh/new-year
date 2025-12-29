package com.example.dedmoroz.web;

import com.example.dedmoroz.config.PaymentProperties;
import com.example.dedmoroz.service.PaymentService;
import com.example.dedmoroz.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class YooKassaWebhookController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final PaymentProperties paymentProperties;
    private final com.example.dedmoroz.telegram.DedMorozBot bot;

    @PostMapping("/yookassa/webhook")
    public ResponseEntity<String> handleWebhook(@RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
                                                @RequestBody Map<String, Object> payload) {
        if (secret == null || !secret.equals(paymentProperties.webhookSecret())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("forbidden");
        }
        String event = (String) payload.get("event");
        Map<String, Object> object = (Map<String, Object>) payload.get("object");
        if (object == null) {
            return ResponseEntity.ok("ignored");
        }
        String paymentId = (String) object.get("id");
        Map<String, Object> metadata = (Map<String, Object>) object.get("metadata");
        Long telegramId = metadata != null ? ((Number) metadata.getOrDefault("telegram_id", 0)).longValue() : null;
        if ("payment.succeeded".equals(event) && paymentId != null && telegramId != null) {
            paymentService.handleSucceededPayment(paymentId, telegramId);
            userService.addCredit(telegramId, 1);
            userService.updateState(telegramId, com.example.dedmoroz.domain.UserState.WAITING_TEXT);
            var user = userService.getByTelegramId(telegramId);
            try {
                bot.execute(SendMessage.builder()
                        .chatId(user.getChatId())
                        .text("Оплата прошла ✅ Напишите текст поздравления.")
                        .build());
            } catch (Exception e) {
                log.error("Failed to notify user about payment", e);
            }
        }
        return ResponseEntity.ok("ok");
    }
}
