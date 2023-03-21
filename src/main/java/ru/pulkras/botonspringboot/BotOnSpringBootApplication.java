package ru.pulkras.botonspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication()

public class BotOnSpringBootApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(BotOnSpringBootApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
