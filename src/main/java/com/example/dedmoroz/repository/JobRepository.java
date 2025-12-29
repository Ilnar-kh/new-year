package com.example.dedmoroz.repository;

import com.example.dedmoroz.domain.JobEntity;
import com.example.dedmoroz.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    Optional<JobEntity> findTopByTelegramIdAndStatusIn(Long telegramId, Iterable<JobStatus> statuses);
}
