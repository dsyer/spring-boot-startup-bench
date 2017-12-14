package com.example.demo;

import com.example.config.ApplicationBuilder;

import org.springframework.boot.autoconfigure.SpringBootApplication;
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

}
