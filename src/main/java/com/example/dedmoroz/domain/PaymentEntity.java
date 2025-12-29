package com.example.dedmoroz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "yookassa_payment_id", unique = true, nullable = false)
    private String yookassaPaymentId;

    private Integer amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "idempotence_key", nullable = false)
    private String idempotenceKey;

    @Column(name = "confirmation_url")
    private String confirmationUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
