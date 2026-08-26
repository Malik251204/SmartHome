package com.tw.medtech.pfa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartHomeManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHomeManagerApplication.class, args);
	}

}
