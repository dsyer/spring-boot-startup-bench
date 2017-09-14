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

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShutdownApplicationListener
		implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {

	/**
	 * 
	 */
	private static final String SHUTDOWN_LISTENER = "SHUTDOWN_LISTENER";
	public static final String MARKER = "Benchmark app stopped";
	private static Log logger = LogFactory.getLog(ShutdownApplicationListener.class);
	private Object source;

	public ShutdownApplicationListener(Object source) {
		this.source = source;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Set<Object> sources = sources(event);
		if (sources.contains(source)) {
			((DefaultListableBeanFactory) event.getApplicationContext().getBeanFactory())
					.registerDisposableBean(SHUTDOWN_LISTENER, this);
		}
	}

	@Override
	public void destroy() throws Exception {
		try {
			logger.info(MARKER);
		}
		catch (Exception e) {
		}
	}

	private Set<Object> sources(ApplicationReadyEvent event) {
		event.getSpringApplication().getMainApplicationClass();
		Method method = ReflectionUtils.findMethod(SpringApplication.class,
				"getAllSources");
		if (method == null) {
			method = ReflectionUtils.findMethod(SpringApplication.class, "getSources");
		}
		ReflectionUtils.makeAccessible(method);
		@SuppressWarnings("unchecked")
		Set<Object> result = (Set<Object>) ReflectionUtils.invokeMethod(method,
				event.getSpringApplication());
		return result;
	}

}
