package com.splitwithease.bot;

import com.splitwithease.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SplitWithEaseBot extends TelegramLongPollingBot {

    @Autowired
    private BotService botService;
    
    @Value("${BOT_TOKEN}")
    private String botToken;
    
    @Value("${BOT_USERNAME}")
    private String botUserName;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
        	SendMessage message = botService.handleUpdate(update);
//            SendMessage message = new SendMessage();
//            message.setChatId(String.valueOf(update.getMessage().getChatId()));
//            message.setText(response);
            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName; // Replace with your actual bot username
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
