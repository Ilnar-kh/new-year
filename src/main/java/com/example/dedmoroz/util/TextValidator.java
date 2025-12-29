package com.example.dedmoroz.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TextValidator {
    private static final int MIN = 20;
    private static final int MAX = 400;

    public boolean isValid(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        var trimmed = text.trim();
        return trimmed.length() >= MIN && trimmed.length() <= MAX;
    }
}
