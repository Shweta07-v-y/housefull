package com.ccm.housefull;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HousefullApplication {

	public static void main(String[] args) {
		SpringApplication.run(HousefullApplication.class, args);
	}

}
