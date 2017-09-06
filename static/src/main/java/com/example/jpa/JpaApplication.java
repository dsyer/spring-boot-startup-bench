package com.example.jpa;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JpaApplication {

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
			return BeanUtils.instantiateClass(
					ClassUtils.getConstructorIfAvailable(SpringApplicationBuilder.class,
							Class[].class),
					(Object) new Class<?>[] { JpaApplication.class });
		}
		return BeanUtils.instantiateClass(
				ClassUtils.getConstructorIfAvailable(SpringApplicationBuilder.class,
						Object[].class),
				(Object) new Object[] { JpaApplication.class.getName() });
	}

}

interface GreetingRepository extends JpaRepository<Greeting, String> {
}

@Entity
class Greeting {

	@Id
	private String id = UUID.randomUUID().toString();

	private String msg;

	@SuppressWarnings("unused")
	private Greeting() {
	}

	public Greeting(String msg) {
		this.msg = msg;
	}

	public String getId() {
		return id;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Greeting [msg=" + msg + "]";
	}

}
