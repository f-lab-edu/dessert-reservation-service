package com.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class JjoncketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(JjoncketingApplication.class, args);
	}

}
