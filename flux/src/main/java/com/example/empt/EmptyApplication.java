package com.example.empt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class EmptyApplication {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(EmptyApplication.class).run(args);
	}

}
