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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class StartupApplicationListener
		implements ApplicationListener<ApplicationReadyEvent> {

	public static final String MARKER = "Benchmark app started";
	private static Log logger = LogFactory.getLog(StartupApplicationListener.class);
	private Class<?> source;

	public StartupApplicationListener(Class<?> source) {
		this.source = source;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if (sources(event).contains(source)) {
			try {
				logger.info(MARKER);
			}
			catch (Exception e) {
			}
		}
	}

	private Set<Class<?>> sources(ApplicationReadyEvent event) {
		Method method = ReflectionUtils.findMethod(SpringApplication.class,
				"getAllSources");
		if (method == null) {
			method = ReflectionUtils.findMethod(SpringApplication.class, "getSources");
		}
		ReflectionUtils.makeAccessible(method);
		@SuppressWarnings("unchecked")
		Set<Object> sources = (Set<Object>) ReflectionUtils.invokeMethod(method,
				event.getSpringApplication());
		Set<Class<?>> result = new HashSet<>();
		for (Object object : sources) {
			if (object instanceof String) {
				result.add(ClassUtils.resolveClassName(object.toString(), null));
			} else if (object instanceof Class) {
				result.add((Class<?>) object);
			}
		}
		return result;
	}

}
