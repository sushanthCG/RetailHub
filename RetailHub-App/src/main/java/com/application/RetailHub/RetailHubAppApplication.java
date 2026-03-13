package com.application.RetailHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RetailHubAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetailHubAppApplication.class, args);
	}

}
