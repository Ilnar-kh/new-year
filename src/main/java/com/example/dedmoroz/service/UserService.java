package com.example.dedmoroz.service;

import com.example.dedmoroz.domain.UserEntity;
import com.example.dedmoroz.domain.UserState;
import com.example.dedmoroz.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserEntity getOrCreate(Long telegramId, Long chatId, String username, String firstName) {
        return userRepository.findByTelegramId(telegramId)
                .map(existing -> {
                    existing.setChatId(chatId);
                    existing.setUsername(username);
                    existing.setFirstName(firstName);
                    return existing;
                })
                .orElseGet(() -> {
                    UserEntity entity = new UserEntity();
                    entity.setTelegramId(telegramId);
                    entity.setChatId(chatId);
                    entity.setUsername(username);
                    entity.setFirstName(firstName);
                    entity.setState(UserState.START_SCREEN);
                    return userRepository.save(entity);
                });
    }

    @Transactional
    public UserEntity updateState(Long telegramId, UserState state) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElseThrow();
        user.setState(state);
        return user;
    }

    @Transactional
    public void addCredit(Long telegramId, int amount) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElseThrow();
        user.setVideoCredits(user.getVideoCredits() + amount);
    }

    @Transactional
    public boolean consumeCredit(Long telegramId) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElseThrow();
        if (user.getVideoCredits() > 0) {
            user.setVideoCredits(user.getVideoCredits() - 1);
            return true;
        }
        return false;
    }

    @Transactional
    public void setLastText(Long telegramId, String text) {
        UserEntity user = userRepository.findByTelegramId(telegramId).orElseThrow();
        user.setLastText(text);
    }

    public UserEntity getByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElseThrow();
    }
}
