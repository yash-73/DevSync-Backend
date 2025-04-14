package com.github.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoGithubAccessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoGithubAccessApplication.class, args);
	}

}
