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
package com.example.micro;

import com.example.config.ApplicationBuilder;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import reactor.core.publisher.Mono;

/**
 * @author Dave Syer
 *
 */
public class MicroApplication {

	public static void main(String[] args) throws Exception {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(RouterFunction.class, () -> RouterFunctions.route(GET("/"),
				request -> ok().body(Mono.just("Hello"), String.class)));
		context.registerBean("webHandler", WebHandler.class, () -> RouterFunctions
				.toWebHandler(context.getBean(RouterFunction.class)));
		context.refresh();
		ApplicationBuilder.start(context);
	}
}