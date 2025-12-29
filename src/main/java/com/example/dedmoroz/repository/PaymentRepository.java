package com.example.dedmoroz.repository;

import com.example.dedmoroz.domain.PaymentEntity;
import com.example.dedmoroz.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByYookassaPaymentId(String paymentId);
    Optional<PaymentEntity> findTopByTelegramIdAndStatusOrderByCreatedAtDesc(Long telegramId, PaymentStatus status);
}
