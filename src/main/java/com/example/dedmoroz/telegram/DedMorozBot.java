package com.example.dedmoroz.telegram;

import com.example.dedmoroz.config.AppProperties;
import com.example.dedmoroz.config.TelegramProperties;
import com.example.dedmoroz.domain.JobEntity;
import com.example.dedmoroz.domain.PaymentEntity;
import com.example.dedmoroz.domain.UserEntity;
import com.example.dedmoroz.domain.UserState;
import com.example.dedmoroz.service.PaymentService;
import com.example.dedmoroz.service.UserService;
import com.example.dedmoroz.service.VideoGenerationService;
import com.example.dedmoroz.util.TextValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DedMorozBot extends TelegramLongPollingCommandBot {

    private final TelegramProperties telegramProperties;
    private final UserService userService;
    private final PaymentService paymentService;
    private final VideoGenerationService videoGenerationService;
    private final TextValidator textValidator;
    private final AppProperties appProperties;

    @PostConstruct
    public void registerBot() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(this);
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.username();
    }

    @Override
    public String getBotToken() {
        return telegramProperties.token();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    private void handleMessage(Update update) {
        var msg = update.getMessage();
        Long tgId = msg.getFrom().getId();
        Long chatId = msg.getChatId();
        String username = msg.getFrom().getUserName();
        String firstName = msg.getFrom().getFirstName();
        UserEntity user = userService.getOrCreate(tgId, chatId, username, firstName);

        if ("/start".equalsIgnoreCase(msg.getText())) {
            sendStartScreen(user);
            return;
        }

        if (user.getState() == UserState.WAITING_TEXT) {
            var text = msg.getText();
            if (!textValidator.isValid(text)) {
                sendText(chatId, "Текст должен быть от 20 до 400 символов. Попробуйте снова.");
                return;
            }
            userService.setLastText(tgId, text.trim());
            userService.updateState(tgId, UserState.CONFIRM_TEXT);
            sendTextWithKeyboard(chatId, "Вот ваш текст:\n" + text.trim(), KeyboardFactory.generateOrEdit());
        } else {
            sendText(chatId, "Нажмите /start чтобы начать или используйте кнопки.");
        }
    }

    private void handleCallback(Update update) {
        var callback = update.getCallbackQuery();
        var data = callback.getData();
        Long tgId = callback.getFrom().getId();
        Long chatId = callback.getMessage().getChatId();
        UserEntity user = userService.getOrCreate(tgId, chatId, callback.getFrom().getUserName(), callback.getFrom().getFirstName());
        switch (data) {
            case "CHECK_PAYMENT" -> checkPayment(user);
            case "CANCEL" -> {
                userService.updateState(tgId, UserState.START_SCREEN);
                sendText(chatId, "Отменено. Нажмите купить, чтобы начать заново.");
            }
            case "GENERATE" -> startGeneration(user);
            case "EDIT_TEXT" -> {
                userService.updateState(tgId, UserState.WAITING_TEXT);
                sendText(chatId, "Напишите новый текст поздравления.");
            }
            default -> {
                if (data.startsWith("BUY_")) {
                    createPayment(user);
                }
            }
        }
    }

    public void sendStartScreen(UserEntity user) {
        Long chatId = user.getChatId();
        sendSampleVideo(chatId);
        String text = "Привет! Это бот для генерации видео-поздравлений от Деда Мороза. Для детей, семьи, друзей — кому угодно." +
                "\nЦена: " + appProperties.priceRub() + " ₽ за одно видео." +
                "\nНажмите кнопку ниже, чтобы купить.";
        sendTextWithKeyboard(chatId, text, KeyboardFactory.buyButton(appProperties.priceRub()));
        userService.updateState(user.getTelegramId(), UserState.START_SCREEN);
    }

    private void sendSampleVideo(Long chatId) {
        try {
            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(chatId);
            if (telegramProperties.sampleVideoFileId() != null && !telegramProperties.sampleVideoFileId().isEmpty()) {
                sendVideo.setVideo(new InputFile(telegramProperties.sampleVideoFileId()));
            } else {
                sendVideo.setVideo(new InputFile(telegramProperties.sampleVideoUrl()));
            }
            execute(sendVideo);
        } catch (Exception e) {
            log.warn("Cannot send sample video", e);
        }
    }

    private void createPayment(UserEntity user) {
        PaymentEntity payment = paymentService.createPayment(user.getTelegramId());
        userService.updateState(user.getTelegramId(), UserState.WAITING_PAYMENT);
        String msg = "Оплата: " + appProperties.priceRub() + " ₽. Нажмите «Оплатить», затем вернитесь сюда — я пойму, когда платеж пройдет.";
        sendTextWithKeyboard(user.getChatId(), msg, KeyboardFactory.payOrCancel(payment.getConfirmationUrl()));
    }

    private void checkPayment(UserEntity user) {
        var pending = paymentService.findPending(user.getTelegramId());
        if (pending == null) {
            sendText(user.getChatId(), "Нет ожидающих платежей. Нажмите Купить.");
            return;
        }
        if (pending.getStatus() == com.example.dedmoroz.domain.PaymentStatus.SUCCEEDED) {
            sendText(user.getChatId(), "Оплата уже прошла ✅ Напишите текст поздравления.");
            userService.updateState(user.getTelegramId(), UserState.WAITING_TEXT);
        } else {
            sendText(user.getChatId(), "Ждём оплату. Если уже оплатили, попробуйте чуть позже.");
        }
    }

    private void startGeneration(UserEntity user) {
        if (user.getState() != UserState.CONFIRM_TEXT) {
            sendText(user.getChatId(), "Сначала отправьте текст поздравления.");
            return;
        }
        if (!userService.consumeCredit(user.getTelegramId())) {
            sendText(user.getChatId(), "Недостаточно кредитов. Купите ещё.");
            return;
        }
        userService.updateState(user.getTelegramId(), UserState.GENERATING);
        sendText(user.getChatId(), "Принято! Генерируем видео, это займет немного времени…");
        JobEntity job = videoGenerationService.startJob(user.getTelegramId(), user.getLastText());
        CompletableFuture.runAsync(() -> {
            var result = videoGenerationService.generate(job);
            if (result.success() && result.videoUrl() != null) {
                sendGeneratedVideo(user.getChatId(), result.videoUrl());
                userService.updateState(user.getTelegramId(), UserState.DONE);
                sendTextWithKeyboard(user.getChatId(), "Видео готово ✅ Хотите ещё одно?", KeyboardFactory.buyMore(appProperties.priceRub()));
            } else {
                userService.addCredit(user.getTelegramId(), 1); // refund
                userService.updateState(user.getTelegramId(), UserState.CONFIRM_TEXT);
                sendText(user.getChatId(), result.errorMessage() != null ? result.errorMessage() : "Не удалось сгенерировать. Попробуйте снова.");
            }
        });
    }

    private void sendGeneratedVideo(Long chatId, String url) {
        try {
            SendVideo sendVideo = new SendVideo();
            sendVideo.setChatId(chatId);
            sendVideo.setVideo(new InputFile(url));
            execute(sendVideo);
        } catch (Exception e) {
            sendText(chatId, "Не удалось отправить видео: " + e.getMessage());
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    private void sendTextWithKeyboard(Long chatId, String text, org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup keyboard) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(keyboard).build());
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }
}
