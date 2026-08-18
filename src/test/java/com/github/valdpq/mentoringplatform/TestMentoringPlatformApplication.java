package com.github.valdpq.mentoringplatform;

import org.springframework.boot.SpringApplication;

public class TestMentoringPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(MentoringPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
