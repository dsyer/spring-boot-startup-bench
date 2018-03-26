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
package com.example.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import reactor.ipc.netty.http.server.HttpServer;

/**
 * @author Dave Syer
 *
 */
public class ApplicationBuilder {

	private static final String SHUTDOWN_LISTENER = "SHUTDOWN_LISTENER";
	public static final String STARTUP = "Benchmark app started";
	private static Log logger = LogFactory.getLog(StartupApplicationListener.class);

	public static void start(ConfigurableApplicationContext context) {
		((DefaultListableBeanFactory) context.getBeanFactory()).registerDisposableBean(
				SHUTDOWN_LISTENER, new ShutdownApplicationListener());
		new BeanCountingApplicationListener().log(context);
		logger.info(STARTUP);

		HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
		HttpServer httpServer = HttpServer.create("localhost",
				context.getEnvironment().getProperty("server.port", Integer.class, 8080));
		httpServer.startAndAwait(adapter);
	}

}
