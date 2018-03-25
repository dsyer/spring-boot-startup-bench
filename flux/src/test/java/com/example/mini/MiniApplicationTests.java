package com.example.mini;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "server.port=0")
@DirtiesContext
public class MiniApplicationTests {

	@Test
	public void contextLoads() throws Exception {
	}

}
