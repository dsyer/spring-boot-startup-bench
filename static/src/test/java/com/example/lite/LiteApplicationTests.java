package com.example.lite;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = LiteApplication.class)
@DirtiesContext
public class LiteApplicationTests {

	@Autowired
	private TestRestTemplate rest;

	@Test
	public void contextLoads() throws Exception {
		ResponseEntity<String> result = rest.getForEntity(new URI("/"), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
