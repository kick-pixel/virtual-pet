package com.ruoyi.virtual.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Telegram Keyboard Factory
 *
 * @author RuoYi
 */
public class KeyboardFactory {

    private static final String CMD_GENERATE_EN = "🎨 Generate Video";
    private static final String CMD_BALANCE_EN = "💰 My Balance";
    private static final String CMD_BUY_EN = "💎 Buy Credits";
    private static final String CMD_HISTORY_EN = "📜 History";
    private static final String CMD_HELP_EN = "❓ Help";

    private static final String CMD_GENERATE_CN = "🎨 生成视频";
    private static final String CMD_BALANCE_CN = "💰 我的积分";
    private static final String CMD_BUY_CN = "💎 购买积分";
    private static final String CMD_HISTORY_CN = "📜 历史记录";
    private static final String CMD_HELP_CN = "❓ 帮助";

    public static ReplyKeyboardMarkup getMainMenuKeyboard(boolean isChinese) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Row 1
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(isChinese ? CMD_GENERATE_CN : CMD_GENERATE_EN));
        row1.add(new KeyboardButton(isChinese ? CMD_BALANCE_CN : CMD_BALANCE_EN));
        keyboard.add(row1);

        // Row 2
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(isChinese ? CMD_BUY_CN : CMD_BUY_EN));
        row2.add(new KeyboardButton(isChinese ? CMD_HISTORY_CN : CMD_HISTORY_EN));
        keyboard.add(row2);

        // Row 3
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(isChinese ? CMD_HELP_CN : CMD_HELP_EN));
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static ReplyKeyboardMarkup getCancelKeyboard(boolean isChinese) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(isChinese ? "❌ 取消" : "❌ Cancel"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
