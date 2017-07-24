package com.example.func;

import java.net.URI;

import org.junit.Test;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SocketUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class FuncApplicationTests {

	private TestRestTemplate rest = new TestRestTemplate();

	@Test
	public void contextLoads() throws Exception {
		int port = SocketUtils.findAvailableTcpPort();
		ConfigurableApplicationContext context = new SpringApplicationBuilder(
				FuncApplication.class).initializers(new WebAppInitializer())
						.run("--server.port=" + port);
		ResponseEntity<String> result = rest
				.getForEntity(new URI("http://localhost:" + port + "/"), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		context.close();
	}

}
