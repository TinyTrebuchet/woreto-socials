package com.woreto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    ChromeBot chromeBot() {
        ChromeBot bot = new ChromeBot();
        bot.setupChromeDriver();
        return bot;
    }
}
