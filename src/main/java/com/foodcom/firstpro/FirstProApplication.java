package com.foodcom.firstpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FirstProApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirstProApplication.class, args);
	}

}
