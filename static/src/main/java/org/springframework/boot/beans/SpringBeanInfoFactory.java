/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeanInfoFactory;
import org.springframework.core.annotation.Order;

/**
 * {@link BeanInfoFactory} that uses a simpler algorithm than {@link Introspector} since
 * we know that our beans don't implement any of the more exotic specifications and Spring
 * only really needs {@link PropertyDescriptor PropertyDescriptors}.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
@Order
public class SpringBeanInfoFactory implements BeanInfoFactory {

	private static String SPRING_PACKAGE = "org.springframework";

	private static Set<String> SUPPORTED_PACKAGES;

	static {
		Set<String> supportedPackages = new LinkedHashSet<>();
		supportedPackages.add("org.springframework.boot.");
		supportedPackages.add("org.springframework.context.");
		supportedPackages.add("org.springframework.http.");
		supportedPackages.add("org.springframework.jmx.");
		supportedPackages.add("org.springframework.web.");
		SUPPORTED_PACKAGES = Collections.unmodifiableSet(supportedPackages);
	}

	@Override
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		String name = beanClass.getName();
		if (name.startsWith(SPRING_PACKAGE) && isSupportedSpringPackage(name)) {
			if (beanClass.getName().startsWith("org.springframework.boot")) {
				return new SpringBootBeanInfo(beanClass);
			}
		}
		return null;
	}

	private boolean isSupportedSpringPackage(String name) {
		for (String candidate : SUPPORTED_PACKAGES) {
			if (name.startsWith(candidate)) {
				return true;
			}
		}
		return false;
	}

}
