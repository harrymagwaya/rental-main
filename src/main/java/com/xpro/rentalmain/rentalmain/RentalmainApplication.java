package com.xpro.rentalmain.rentalmain;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RentalmainApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
		SpringApplication.run(RentalmainApplication.class, args);
	}

}
