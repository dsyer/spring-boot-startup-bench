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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class LazyInitBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private Class<?>[] exclusionList;

	public LazyInitBeanFactoryPostProcessor() {
	}

	public LazyInitBeanFactoryPostProcessor(Class<?>[] exclusionList) {
		this.exclusionList = exclusionList;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {

		// Iterate over all bean, mark them as lazy if they are not in the exclusion list.
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			if (isLazy(beanName, beanFactory)) {
				BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
				definition.setLazyInit(true);
			}
		}
	}

	private boolean isLazy(String beanName, ConfigurableListableBeanFactory beanFactory) {
		if (exclusionList == null || exclusionList.length == 0) {
			return true;
		}
		for (Class<?> clazz : exclusionList) {
			if (beanFactory.isTypeMatch(beanName, clazz)) {
				return false;
			}
		}
		return true;
	}
}