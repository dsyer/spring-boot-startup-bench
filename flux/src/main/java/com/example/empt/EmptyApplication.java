package com.example.empt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class EmptyApplication {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(EmptyApplication.class).run(args);
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(100L);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		thread.setDaemon(false);
		thread.start();
	}

}
