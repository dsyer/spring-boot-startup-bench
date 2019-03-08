package com.example.mini;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = "server.port=0")
@DirtiesContext
public class MiniApplicationTests {

	@Test
	public void contextLoads() throws Exception {
	}

}
