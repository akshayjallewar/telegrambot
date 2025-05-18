package com.splitwithease.bot;

import com.splitwithease.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class SplitWithEaseBot extends TelegramLongPollingBot {

    @Autowired
    private BotService botService;

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
        return "splitwithease_bot"; // Replace with your actual bot username
    }

    @Override
    public String getBotToken() {
        return "8121257287:AAEltpujBvtbVe--Wa6MB3tApc7XFAn6k6c"; // Replace with your actual bot token
    }
}
