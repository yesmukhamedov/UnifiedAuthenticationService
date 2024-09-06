package com.project.unifiedauthenticationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UnifiedAuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnifiedAuthenticationServiceApplication.class, args);
	}

}