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

package com.example.func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.config.StartupApplicationListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DefaultServletWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
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
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.util.UrlPathHelper;

@RestController
public class FuncApplication {

	@GetMapping("/")
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(FuncApplication.class)
				.listeners(new StartupApplicationListener(FuncApplication.class))
				.initializers(new WebAppInitializer()).run(args);
	}
}

class WebAppInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

	private PathMatchConfigurer pathMatchConfigurer;

	private ContentNegotiationManager contentNegotiationManager;

	private List<HandlerMethodReturnValueHandler> returnValueHandlers;

	private List<HttpMessageConverter<?>> messageConverters;

	@Override
	public void initialize(GenericApplicationContext context) {
		context.registerBean(ConfigurationPropertiesBindingPostProcessor.class,
				() -> new ConfigurationPropertiesBindingPostProcessor());
		context.registerBean(WebMvcProperties.class, () -> new WebMvcProperties());
		context.registerBean(ServerProperties.class, () -> new ServerProperties());
		context.registerBean(DefaultServletWebServerFactoryCustomizer.class,
				() -> serverPropertiesWebServerFactoryCustomizer(
						context.getBean(ServerProperties.class)));
		context.registerBean(WebServerFactoryCustomizerBeanPostProcessor.class,
				this::webServerFactoryCustomizerBeanPostProcessor);
		context.registerBean(TomcatServletWebServerFactory.class,
				this::tomcatServletWebServerFactory);
		context.registerBean(DispatcherServlet.class,
				() -> dispatcherServlet(context.getBean(WebMvcProperties.class)));
		context.registerBean(ServletRegistrationBean.class,
				() -> dispatcherServletRegistration(
						context.getBean(DispatcherServlet.class),
						context.getBean(ServerProperties.class),
						context.getBean(WebMvcProperties.class)));
		context.registerBean(RequestMappingHandlerMapping.class,
				() -> requestMappingHandlerMapping(
						context.getBean(ContentNegotiationManager.class)));
		context.registerBean(PathMatcher.class, this::mvcPathMatcher);
		context.registerBean(UrlPathHelper.class, this::mvcUrlPathHelper);
		context.registerBean(ContentNegotiationManager.class,
				this::mvcContentNegotiationManager);
		context.registerBean(RequestMappingHandlerAdapter.class,
				() -> requestMappingHandlerAdapter(
						context.getBean(ContentNegotiationManager.class)));
		context.registerBean(FormattingConversionService.class,
				this::mvcConversionService);
		context.registerBean(Validator.class, this::mvcValidator);
		context.registerBean(CompositeUriComponentsContributor.class,
				() -> mvcUriComponentsContributor(
						context.getBean(RequestMappingHandlerAdapter.class)));
		context.registerBean(HandlerExceptionResolver.class,
				this::handlerExceptionResolver);
		context.registerShutdownHook();
	}

	public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}

	public DefaultServletWebServerFactoryCustomizer serverPropertiesWebServerFactoryCustomizer(
			ServerProperties serverProperties) {
		return new DefaultServletWebServerFactoryCustomizer(serverProperties);
	}

	public WebServerFactoryCustomizerBeanPostProcessor webServerFactoryCustomizerBeanPostProcessor() {
		return new WebServerFactoryCustomizerBeanPostProcessor();
	}

	public DispatcherServlet dispatcherServlet(WebMvcProperties webMvcProperties) {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet
				.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());
		dispatcherServlet
				.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());
		dispatcherServlet.setThrowExceptionIfNoHandlerFound(
				webMvcProperties.isThrowExceptionIfNoHandlerFound());
		return dispatcherServlet;
	}

	public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(
			DispatcherServlet dispatcherServlet, ServerProperties serverProperties,
			WebMvcProperties webMvcProperties) {
		ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(
				dispatcherServlet, serverProperties.getServlet().getServletMapping());
		registration.setName(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
		registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
		return registration;
	}

	public RequestMappingHandlerMapping requestMappingHandlerMapping(
			ContentNegotiationManager mvcContentNegotiationManager) {
		RequestMappingHandlerMapping handlerMapping = createRequestMappingHandlerMapping();
		handlerMapping.setOrder(0);
		handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager);

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

	public PathMatcher mvcPathMatcher() {
		PathMatcher pathMatcher = getPathMatchConfigurer().getPathMatcher();
		return (pathMatcher != null ? pathMatcher : new AntPathMatcher());
	}

	public UrlPathHelper mvcUrlPathHelper() {
		UrlPathHelper pathHelper = getPathMatchConfigurer().getUrlPathHelper();
		return (pathHelper != null ? pathHelper : new UrlPathHelper());
	}

	public ContentNegotiationManager mvcContentNegotiationManager() {
		if (this.contentNegotiationManager == null) {
			ContentNegotiationManagerFactoryBean configurer = new ContentNegotiationManagerFactoryBean();
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

	public RequestMappingHandlerAdapter requestMappingHandlerAdapter(
			ContentNegotiationManager mvcContentNegotiationManager) {
		RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
		adapter.setContentNegotiationManager(mvcContentNegotiationManager);
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

	public FormattingConversionService mvcConversionService() {
		FormattingConversionService conversionService = new DefaultFormattingConversionService();
		return conversionService;
	}

	public Validator mvcValidator() {
		Validator validator;
		if (ClassUtils.isPresent("javax.validation.Validator",
				getClass().getClassLoader())) {
			Class<?> clazz;
			try {
				String className = "org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean";
				clazz = ClassUtils.forName(className,
						WebAppInitializer.class.getClassLoader());
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

		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
		messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}

	public CompositeUriComponentsContributor mvcUriComponentsContributor(
			RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
		return new CompositeUriComponentsContributor(
				requestMappingHandlerAdapter.getArgumentResolvers(),
				mvcConversionService());
	}

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
		exceptionHandlerResolver.afterPropertiesSet();
		exceptionResolvers.add(exceptionHandlerResolver);

		ResponseStatusExceptionResolver responseStatusResolver = new ResponseStatusExceptionResolver();
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