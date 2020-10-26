/**
 *    Auth:riozenc
 *    Date:2018年12月11日 下午4:49:31
 *    Title:security.web.WebConfig.java
 **/
package com.riozenc.task.configuration;

import com.riozenc.titanTool.spring.context.SpringContextHolder;
import com.riozenc.titanTool.spring.web.client.TitanTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		// TODO Auto-generated method stub
		converters.add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
//		converters.add(2, new MessageConverter());
		WebMvcConfigurer.super.configureMessageConverters(converters);
	}

	@Bean
	public SpringContextHolder springContextHolder() {
		return new SpringContextHolder();
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}



	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		return new SchedulerFactoryBean();
	}

	@Bean
	public TitanTemplate titanTemplate(RestTemplate restTemplate) {
		return new TitanTemplate(restTemplate);

	}
}
