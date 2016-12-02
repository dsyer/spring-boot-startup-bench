package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceRepositoryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ WebApplicationConfiguration.class, ActuatorConfiguration.class })
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@Configuration
@ImportAutoConfiguration({ EndpointAutoConfiguration.class, EndpointWebMvcAutoConfiguration.class,
		HealthIndicatorAutoConfiguration.class, InfoContributorAutoConfiguration.class,
		ManagementServerPropertiesAutoConfiguration.class, MetricFilterAutoConfiguration.class,
		PublicMetricsAutoConfiguration.class, TraceRepositoryAutoConfiguration.class,
		TraceWebFilterAutoConfiguration.class })
class ActuatorConfiguration {

}

@Configuration
@ImportAutoConfiguration({ PropertyPlaceholderAutoConfiguration.class, EmbeddedServletContainerAutoConfiguration.class,
		JacksonAutoConfiguration.class, DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		HttpMessageConvertersAutoConfiguration.class, ServerPropertiesAutoConfiguration.class,
		WebMvcAutoConfiguration.class })
class WebApplicationConfiguration {

}