package com.example.dedmoroz.service;

import com.example.dedmoroz.client.FalAiClient;
import com.example.dedmoroz.config.FalProperties;
import com.example.dedmoroz.domain.JobEntity;
import com.example.dedmoroz.domain.JobStatus;
import com.example.dedmoroz.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationService {

    private final JobRepository jobRepository;
    private final FalAiClient falAiClient;
    private final FalProperties falProperties;

    @Transactional
    public JobEntity startJob(Long telegramId, String text) {
        var existing = jobRepository.findTopByTelegramIdAndStatusIn(telegramId, List.of(JobStatus.CREATED, JobStatus.RUNNING));
        if (existing.isPresent()) {
            return existing.get();
        }
        JobEntity job = new JobEntity();
        job.setTelegramId(telegramId);
        job.setInputText(text);
        job.setStatus(JobStatus.CREATED);
        return jobRepository.save(job);
    }

    @Transactional
    public void updateJob(JobEntity job) {
        jobRepository.save(job);
    }

    public VideoResult generate(JobEntity job) {
        try {
            var createResponse = falAiClient.createJob(job.getInputText()).block();
            if (createResponse == null || !StringUtils.hasText(createResponse.id())) {
                throw new IllegalStateException("FAL AI empty response");
            }
            job.setFalJobId(createResponse.id());
            job.setStatus(JobStatus.RUNNING);
            updateJob(job);
            Instant start = Instant.now();
            while (Duration.between(start, Instant.now()).getSeconds() < falProperties.timeoutSeconds()) {
                var status = falAiClient.getJob(job.getFalJobId()).block();
                if (status == null) {
                    Thread.sleep(falProperties.pollDelayMs());
                    continue;
                }
                if ("succeeded".equalsIgnoreCase(status.status())) {
                    String videoUrl = extractVideoUrl(status.result());
                    job.setVideoUrl(videoUrl);
                    job.setStatus(JobStatus.SUCCEEDED);
                    updateJob(job);
                    return new VideoResult(true, videoUrl, null);
                } else if ("failed".equalsIgnoreCase(status.status())) {
                    job.setStatus(JobStatus.FAILED);
                    job.setErrorMessage("Generation failed");
                    updateJob(job);
                    return new VideoResult(false, null, "FAL AI вернул ошибку");
                }
                Thread.sleep(falProperties.pollDelayMs());
            }
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("Timeout");
            updateJob(job);
            return new VideoResult(false, null, "Время ожидания генерации истекло. Попробуйте позже.");
        } catch (Exception e) {
            log.error("Fal generation error", e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            updateJob(job);
            return new VideoResult(false, null, "Не удалось сгенерировать видео: " + e.getMessage());
        }
    }

    private String extractVideoUrl(Map<String, Object> result) {
        if (result == null) {
            return null;
        }
        Object url = result.get("video_url");
        if (url == null) {
            url = result.get("url");
        }
        return url != null ? url.toString() : null;
    }

    public record VideoResult(boolean success, String videoUrl, String errorMessage) {}
}
