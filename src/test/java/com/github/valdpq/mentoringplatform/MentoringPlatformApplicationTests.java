package com.github.valdpq.mentoringplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MentoringPlatformApplicationTests {

	@Test
	void contextLoads() {
	}

}
