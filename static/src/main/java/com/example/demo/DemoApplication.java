package com.example.demo;

import com.example.config.ApplicationBuilder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {

	@GetMapping("/")
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) throws Exception {
		ApplicationBuilder.builder(DemoApplication.class).run(args);
	}

	@Bean
	@ConditionalOnNotWebApplication
	public CommandLineRunner runner() {
		return args -> {
			Thread t = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(100L);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			});
			t.setDaemon(false);
			t.start();
		};
	}

}
