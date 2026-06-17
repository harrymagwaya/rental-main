package com.xpro.rentalmain.rentalmain;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RentalmainApplication {

	public static void main(String[] args) {

		SpringApplication.run(RentalmainApplication.class, args);
	}

}
