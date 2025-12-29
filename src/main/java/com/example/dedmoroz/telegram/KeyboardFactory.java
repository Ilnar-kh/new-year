package com.example.dedmoroz.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class KeyboardFactory {

    public static InlineKeyboardMarkup buyButton(int priceRub) {
        InlineKeyboardButton buy = new InlineKeyboardButton("Купить за " + priceRub + " ₽");
        buy.setCallbackData("BUY_" + priceRub);
        return new InlineKeyboardMarkup(List.of(List.of(buy)));
    }

    public static InlineKeyboardMarkup payOrCancel(String url) {
        InlineKeyboardButton pay = new InlineKeyboardButton("Оплатить");
        pay.setUrl(url);
        InlineKeyboardButton check = new InlineKeyboardButton("Проверить оплату");
        check.setCallbackData("CHECK_PAYMENT");
        InlineKeyboardButton cancel = new InlineKeyboardButton("Отмена");
        cancel.setCallbackData("CANCEL");
        return new InlineKeyboardMarkup(List.of(List.of(pay), List.of(check), List.of(cancel)));
    }

    public static InlineKeyboardMarkup generateOrEdit() {
        InlineKeyboardButton gen = new InlineKeyboardButton("Сгенерировать видео");
        gen.setCallbackData("GENERATE");
        InlineKeyboardButton edit = new InlineKeyboardButton("Изменить текст");
        edit.setCallbackData("EDIT_TEXT");
        return new InlineKeyboardMarkup(List.of(List.of(gen), List.of(edit)));
    }

    public static InlineKeyboardMarkup buyMore(int priceRub) {
        InlineKeyboardButton buy = new InlineKeyboardButton("Купить ещё за " + priceRub + " ₽");
        buy.setCallbackData("BUY_" + priceRub);
        return new InlineKeyboardMarkup(List.of(List.of(buy)));
    }
}
