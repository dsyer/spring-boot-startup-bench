package com.example.demo;

import com.example.config.StartupApplicationListener;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.ClassUtils;
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
		builder().run(args);
	}

	private static SpringApplicationBuilder builder() {
		// Defensive reflective builder to work with Boot 1.5 and 2.0
		if (ClassUtils.hasConstructor(SpringApplicationBuilder.class, Class[].class)) {
			return BeanUtils
					.instantiateClass(
							ClassUtils.getConstructorIfAvailable(
									SpringApplicationBuilder.class, Class[].class),
							(Object) new Class<?>[] { DemoApplication.class })
					.listeners(new StartupApplicationListener(DemoApplication.class));
		}
		return BeanUtils
				.instantiateClass(
						ClassUtils.getConstructorIfAvailable(
								SpringApplicationBuilder.class, Object[].class),
						(Object) new Object[] { DemoApplication.class.getName() })
				.listeners(
						new StartupApplicationListener(DemoApplication.class.getName()));
	}

}
