/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.slim;

import com.example.config.ApplicationBuilder;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootConfiguration
@Import({ HttpMessageConvertersAutoConfiguration.class, JacksonAutoConfiguration.class,
		JmxAutoConfiguration.class, ValidationAutoConfiguration.class,
		RestTemplateAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
		HttpEncodingAutoConfiguration.class, MultipartAutoConfiguration.class,
		ServletWebServerFactoryAutoConfiguration.class, WebMvcAutoConfiguration.class,
		ErrorMvcAutoConfiguration.class, WebSocketServletAutoConfiguration.class,
		ConfigurationPropertiesAutoConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class, ProjectInfoAutoConfiguration.class })
@RestController
public class SlimApplication {

	@GetMapping("/")
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) {
		ApplicationBuilder.builder(SlimApplication.class).run(args);
	}
}
