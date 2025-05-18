package com.cbs.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommentaryBroadcastSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommentaryBroadcastSystemApplication.class, args);
	}

}
