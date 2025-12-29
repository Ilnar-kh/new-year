package com.example.dedmoroz.service;

import com.example.dedmoroz.client.YooKassaClient;
import com.example.dedmoroz.config.AppProperties;
import com.example.dedmoroz.domain.PaymentEntity;
import com.example.dedmoroz.domain.PaymentStatus;
import com.example.dedmoroz.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final YooKassaClient yooKassaClient;
    private final AppProperties appProperties;

    @Transactional
    public PaymentEntity createPayment(Long telegramId) {
        String idempotenceKey = UUID.randomUUID().toString();
        String description = "Video greeting for telegram user " + telegramId;
        var response = yooKassaClient.createPayment(appProperties.priceRub(), description, idempotenceKey, telegramId).block();
        if (response == null) {
            throw new IllegalStateException("Empty response from YooKassa");
        }
        String confirmationUrl = response.confirmation() != null ? (String) response.confirmation().get("confirmation_url") : null;
        PaymentEntity entity = new PaymentEntity();
        entity.setTelegramId(telegramId);
        entity.setYookassaPaymentId(response.id());
        entity.setAmount(appProperties.priceRub() * 100);
        entity.setCurrency("RUB");
        entity.setStatus(PaymentStatus.PENDING);
        entity.setIdempotenceKey(idempotenceKey);
        entity.setConfirmationUrl(confirmationUrl);
        paymentRepository.save(entity);
        log.info("Created payment {} for user {}", response.id(), telegramId);
        return entity;
    }

    @Transactional
    public void handleSucceededPayment(String paymentId, Long telegramId) {
        PaymentEntity payment = paymentRepository.findByYookassaPaymentId(paymentId).orElse(null);
        if (payment == null) {
            log.warn("Webhook for unknown payment {}", paymentId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return; // idempotent
        }
        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
    }

    public PaymentEntity findPending(Long telegramId) {
        return paymentRepository.findTopByTelegramIdAndStatusOrderByCreatedAtDesc(telegramId, PaymentStatus.PENDING).orElse(null);
    }
}
