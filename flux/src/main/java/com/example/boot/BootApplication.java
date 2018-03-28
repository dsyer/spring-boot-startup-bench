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
package com.example.boot;

import java.util.Collections;

import com.example.config.ApplicationBuilder;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

/**
 * @author Dave Syer
 *
 */
@SpringBootConfiguration
@EnableWebFlux
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class BootApplication {

	public static void main(String[] args) throws Exception {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(
				BootApplication.class).web(WebApplicationType.NONE)
						.contextClass(AnnotationConfigApplicationContext.class)
						.bannerMode(Mode.OFF);
		builder.application().setListeners(Collections.emptyList());
		try (ConfigurableApplicationContext context = builder.run(args)) {
			ApplicationBuilder.start(context);
		}
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just("Hello"), String.class));
	}

}
