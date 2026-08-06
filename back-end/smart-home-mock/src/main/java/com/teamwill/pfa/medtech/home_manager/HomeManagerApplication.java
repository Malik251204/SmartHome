package com.teamwill.pfa.medtech.home_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Mock H2 backend — no real IoT loop, no Wokwi. Sensor "liveness" comes
// entirely from SensorSimulationScheduler, hence @EnableScheduling.
@SpringBootApplication
@EnableScheduling
public class HomeManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeManagerApplication.class, args);
	}

}
