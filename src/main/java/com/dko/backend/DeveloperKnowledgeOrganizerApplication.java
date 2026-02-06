package com.dko.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeveloperKnowledgeOrganizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeveloperKnowledgeOrganizerApplication.class, args);
		System.out.println("=================Application started successfully=================");
	}

}
