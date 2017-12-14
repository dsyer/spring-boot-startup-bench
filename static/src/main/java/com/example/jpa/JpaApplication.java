package com.example.jpa;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.example.config.ApplicationBuilder;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
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
		ApplicationBuilder.builder(JpaApplication.class).run(args);
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
