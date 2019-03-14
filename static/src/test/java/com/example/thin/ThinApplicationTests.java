package com.example.thin;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = ThinApplication.class)
@DirtiesContext
public class ThinApplicationTests {

	@LocalServerPort
	private int port;

	private TestRestTemplate rest = new TestRestTemplate();

	@Test
	public void contextLoads() throws Exception {
		ResponseEntity<String> result = rest
				.getForEntity(new URI("http://localhost:" + port + "/"), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
