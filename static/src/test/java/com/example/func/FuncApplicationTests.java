package com.example.func;

import java.net.URI;

import org.junit.Test;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class FuncApplicationTests {

	private TestRestTemplate rest = new TestRestTemplate();

	@Test
	public void contextLoads() throws Exception {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(
				FuncApplication.class).initializers(new WebAppInitializer()).run();
		ResponseEntity<String> result = rest
				.getForEntity(new URI("http://localhost:8080/"), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		context.close();
	}

}
