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
@Table(name = "jobs")
public class JobEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "fal_job_id")
    private String falJobId;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
