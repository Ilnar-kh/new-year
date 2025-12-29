package com.example.dedmoroz.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;

    @Column(name = "chat_id")
    private Long chatId;

    private String username;
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "video_credits")
    private Integer videoCredits = 0;

    @Enumerated(EnumType.STRING)
    private UserState state = UserState.START_SCREEN;

    @Column(name = "last_text", columnDefinition = "TEXT")
    private String lastText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
