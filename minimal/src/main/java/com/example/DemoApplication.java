package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.AuditAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.HealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceRepositoryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.TraceWebFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
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
@ImportAutoConfiguration({ EndpointAutoConfiguration.class,
		EndpointWebMvcAutoConfiguration.class, HealthIndicatorAutoConfiguration.class,
		InfoContributorAutoConfiguration.class,
		ManagementServerPropertiesAutoConfiguration.class,
		MetricFilterAutoConfiguration.class, PublicMetricsAutoConfiguration.class,
		TraceRepositoryAutoConfiguration.class, TraceWebFilterAutoConfiguration.class })
class ActuatorConfiguration {

}

@Configuration
@ImportAutoConfiguration({ PropertyPlaceholderAutoConfiguration.class,
		EmbeddedServletContainerAutoConfiguration.class, JacksonAutoConfiguration.class,
		DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		HttpMessageConvertersAutoConfiguration.class,
		ServerPropertiesAutoConfiguration.class, WebMvcAutoConfiguration.class })
class WebApplicationConfiguration {

}

@Configuration
@ImportAutoConfiguration({ ConfigurationPropertiesAutoConfiguration.class,
		WebClientAutoConfiguration.class, JmxAutoConfiguration.class,
		MultipartAutoConfiguration.class, HttpEncodingAutoConfiguration.class,
		CacheAutoConfiguration.class, WebSocketAutoConfiguration.class,
		ProjectInfoAutoConfiguration.class, ValidationAutoConfiguration.class })
class ExtraConfiguration {
	// Extra stuff, not originally in the minimal sample, but present in a vanilla spring
	// boot app. Doesn't add much (maybe 50ms) to start up if included.
}

@Configuration
@Import({ AuditAutoConfiguration.class, EndpointMBeanExportAutoConfiguration.class,
		MetricExportAutoConfiguration.class, MetricRepositoryAutoConfiguration.class })
class ExtraActuatorConfiguration {
	// Extra stuff, not originally in the minimal sample, but present in a vanilla spring
	// boot app with actuator. Doesn't add much (maybe 100ms) to start up if included.
}