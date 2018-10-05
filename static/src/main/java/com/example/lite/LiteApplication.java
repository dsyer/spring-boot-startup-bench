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

package com.example.lite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.example.config.ApplicationBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.util.UrlPathHelper;

@SpringBootConfiguration
@Import({ DispatcherServletConfiguration.class, EmbeddedTomcat.class,
		WebMvcConfiguration.class })
@EnableConfigurationProperties(WebMvcProperties.class)
@RestController
public class LiteApplication {

	@GetMapping("/")
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) {
		ApplicationBuilder.builder(LiteApplication.class).run(args)
				.registerShutdownHook();
	}
}

@Configuration
@EnableConfigurationProperties(ServerProperties.class)
class EmbeddedTomcat {

	@Bean
	public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}

	@Bean
	public ServletWebServerFactoryCustomizer serverPropertiesWebServerFactoryCustomizer(
			ServerProperties serverProperties) {
		return new ServletWebServerFactoryCustomizer(serverProperties);
	}

	@Bean
	public WebServerFactoryCustomizerBeanPostProcessor webServerFactoryCustomizerBeanPostProcessor() {
		return new WebServerFactoryCustomizerBeanPostProcessor();
	}

}

@Configuration
class DispatcherServletConfiguration {

	private final WebMvcProperties webMvcProperties;

	public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

	public DispatcherServletConfiguration(WebMvcProperties webMvcProperties) {
		this.webMvcProperties = webMvcProperties;
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setDispatchOptionsRequest(
				this.webMvcProperties.isDispatchOptionsRequest());
		dispatcherServlet
				.setDispatchTraceRequest(this.webMvcProperties.isDispatchTraceRequest());
		dispatcherServlet.setThrowExceptionIfNoHandlerFound(
				this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
		return dispatcherServlet;
	}

	@Bean
	public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(
			DispatcherServlet dispatcherServlet) {
		ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(
				dispatcherServlet,
				this.webMvcProperties.getServlet().getServletMapping());
		registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
		registration
				.setLoadOnStartup(this.webMvcProperties.getServlet().getLoadOnStartup());
		return registration;
	}

}

@Configuration
class WebMvcConfiguration implements ApplicationContextAware, ServletContextAware {

	private ApplicationContext applicationContext;

	private ServletContext servletContext;

	private PathMatchConfigurer pathMatchConfigurer;

	private ContentNegotiationManager contentNegotiationManager;

	private List<HandlerMethodReturnValueHandler> returnValueHandlers;

	private List<HttpMessageConverter<?>> messageConverters;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = createRequestMappingHandlerMapping();
		handlerMapping.setOrder(0);
		handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());

		PathMatchConfigurer configurer = getPathMatchConfigurer();
		if (configurer.isUseSuffixPatternMatch() != null) {
			handlerMapping.setUseSuffixPatternMatch(configurer.isUseSuffixPatternMatch());
		}
		if (configurer.isUseRegisteredSuffixPatternMatch() != null) {
			handlerMapping.setUseRegisteredSuffixPatternMatch(
					configurer.isUseRegisteredSuffixPatternMatch());
		}
		if (configurer.isUseTrailingSlashMatch() != null) {
			handlerMapping.setUseTrailingSlashMatch(configurer.isUseTrailingSlashMatch());
		}
		UrlPathHelper pathHelper = configurer.getUrlPathHelper();
		if (pathHelper != null) {
			handlerMapping.setUrlPathHelper(pathHelper);
		}
		PathMatcher pathMatcher = configurer.getPathMatcher();
		if (pathMatcher != null) {
			handlerMapping.setPathMatcher(pathMatcher);
		}

		return handlerMapping;
	}

	protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
		return new RequestMappingHandlerMapping();
	}

	protected PathMatchConfigurer getPathMatchConfigurer() {
		if (this.pathMatchConfigurer == null) {
			this.pathMatchConfigurer = new PathMatchConfigurer();
		}
		return this.pathMatchConfigurer;
	}

	@Bean
	public PathMatcher mvcPathMatcher() {
		PathMatcher pathMatcher = getPathMatchConfigurer().getPathMatcher();
		return (pathMatcher != null ? pathMatcher : new AntPathMatcher());
	}

	@Bean
	public UrlPathHelper mvcUrlPathHelper() {
		UrlPathHelper pathHelper = getPathMatchConfigurer().getUrlPathHelper();
		return (pathHelper != null ? pathHelper : new UrlPathHelper());
	}

	@Bean
	public ContentNegotiationManager mvcContentNegotiationManager() {
		if (this.contentNegotiationManager == null) {
			ContentNegotiationManagerFactoryBean configurer = new ContentNegotiationManagerFactoryBean();
			configurer.setServletContext(this.servletContext);
			configurer.addMediaTypes(getDefaultMediaTypes());
			try {
				configurer.afterPropertiesSet();
				this.contentNegotiationManager = configurer.getObject();
			}
			catch (Exception ex) {
				throw new BeanInitializationException(
						"Could not create ContentNegotiationManager", ex);
			}
		}
		return this.contentNegotiationManager;
	}

	protected Map<String, MediaType> getDefaultMediaTypes() {
		Map<String, MediaType> map = new HashMap<>(4);
		map.put("json", MediaType.APPLICATION_JSON);
		return map;
	}

	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
		adapter.setContentNegotiationManager(mvcContentNegotiationManager());
		adapter.setMessageConverters(getMessageConverters());
		adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
		adapter.setCustomReturnValueHandlers(getReturnValueHandlers());
		adapter.setRequestBodyAdvice(
				Collections.singletonList(new JsonViewRequestBodyAdvice()));
		adapter.setResponseBodyAdvice(
				Collections.singletonList(new JsonViewResponseBodyAdvice()));
		return adapter;
	}

	protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
		return new RequestMappingHandlerAdapter();
	}

	protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
		ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
		initializer.setConversionService(mvcConversionService());
		initializer.setValidator(mvcValidator());
		return initializer;
	}

	@Bean
	public FormattingConversionService mvcConversionService() {
		FormattingConversionService conversionService = new DefaultFormattingConversionService();
		return conversionService;
	}

	@Bean
	public Validator mvcValidator() {
		Validator validator;
		if (ClassUtils.isPresent("javax.validation.Validator",
				getClass().getClassLoader())) {
			Class<?> clazz;
			try {
				String className = "org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean";
				clazz = ClassUtils.forName(className,
						WebMvcConfiguration.class.getClassLoader());
			}
			catch (ClassNotFoundException | LinkageError ex) {
				throw new BeanInitializationException(
						"Could not find default validator class", ex);
			}
			validator = (Validator) BeanUtils.instantiateClass(clazz);
		}
		else {
			validator = new NoOpValidator();
		}
		return validator;
	}

	protected final List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
		if (this.returnValueHandlers == null) {
			this.returnValueHandlers = new ArrayList<>();
		}
		return this.returnValueHandlers;
	}

	protected final List<HttpMessageConverter<?>> getMessageConverters() {
		if (this.messageConverters == null) {
			this.messageConverters = new ArrayList<>();
			if (this.messageConverters.isEmpty()) {
				addDefaultHttpMessageConverters(this.messageConverters);
			}
		}
		return this.messageConverters;
	}

	protected final void addDefaultHttpMessageConverters(
			List<HttpMessageConverter<?>> messageConverters) {
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setWriteAcceptCharset(false);

		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(stringConverter);
		messageConverters.add(new ResourceHttpMessageConverter());
		messageConverters.add(new SourceHttpMessageConverter<>());
		messageConverters.add(new AllEncompassingFormHttpMessageConverter());

		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
				.applicationContext(this.applicationContext).build();
		messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}

	@Bean
	public CompositeUriComponentsContributor mvcUriComponentsContributor() {
		return new CompositeUriComponentsContributor(
				requestMappingHandlerAdapter().getArgumentResolvers(),
				mvcConversionService());
	}

	@Bean
	public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
		return new HttpRequestHandlerAdapter();
	}

	@Bean
	public HandlerExceptionResolver handlerExceptionResolver() {
		List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
		if (exceptionResolvers.isEmpty()) {
			addDefaultHandlerExceptionResolvers(exceptionResolvers);
		}
		HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
		composite.setOrder(0);
		composite.setExceptionResolvers(exceptionResolvers);
		return composite;
	}

	protected final void addDefaultHandlerExceptionResolvers(
			List<HandlerExceptionResolver> exceptionResolvers) {
		ExceptionHandlerExceptionResolver exceptionHandlerResolver = createExceptionHandlerExceptionResolver();
		exceptionHandlerResolver
				.setContentNegotiationManager(mvcContentNegotiationManager());
		exceptionHandlerResolver.setMessageConverters(getMessageConverters());
		exceptionHandlerResolver.setCustomReturnValueHandlers(getReturnValueHandlers());
		exceptionHandlerResolver.setResponseBodyAdvice(
				Collections.singletonList(new JsonViewResponseBodyAdvice()));
		exceptionHandlerResolver.setApplicationContext(this.applicationContext);
		exceptionHandlerResolver.afterPropertiesSet();
		exceptionResolvers.add(exceptionHandlerResolver);

		ResponseStatusExceptionResolver responseStatusResolver = new ResponseStatusExceptionResolver();
		responseStatusResolver.setMessageSource(this.applicationContext);
		exceptionResolvers.add(responseStatusResolver);

		exceptionResolvers.add(new DefaultHandlerExceptionResolver());
	}

	protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
		return new ExceptionHandlerExceptionResolver();
	}

	private static final class NoOpValidator implements Validator {

		@Override
		public boolean supports(Class<?> clazz) {
			return false;
		}

		@Override
		public void validate(Object target, Errors errors) {
		}
	}

}
