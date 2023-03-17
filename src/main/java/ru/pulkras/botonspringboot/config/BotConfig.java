package ru.pulkras.botonspringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Data;

@Configuration
@Data // add constructors, getters and setters and more
@PropertySource("classpath:application.properties")
public class BotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.key}")
    String key;

}
