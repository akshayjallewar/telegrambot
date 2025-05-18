package com.splitwithease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.splitwithease.bot.SplitWithEaseBot;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SplitWithEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitWithEaseApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(SplitWithEaseBot bot) throws Exception {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
