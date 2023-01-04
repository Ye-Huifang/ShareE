package com.shareE.forum.config;

import com.shareE.forum.annotation.LoginRequired;
import com.shareE.forum.controller.interceptor.AlphaInterceptor;
import com.shareE.forum.controller.interceptor.LoginRequiredInterceptor;
import com.shareE.forum.controller.interceptor.LoginTicketInterceptor;
import com.shareE.forum.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Autowired
	private AlphaInterceptor alphaInterceptor;

	@Autowired
	private LoginTicketInterceptor loginTicketInterceptor;

	@Autowired
	private LoginRequiredInterceptor loginRequiredInterceptor;

	@Autowired
	private MessageInterceptor messageInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(alphaInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.png", "/**/*.js", "/**/*.jpg", "/**/*.jpeg")
				.addPathPatterns("/register", "/login");

		registry.addInterceptor(loginTicketInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.png", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");

		registry.addInterceptor(loginRequiredInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.png", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");

		registry.addInterceptor(messageInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.png", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");
	}
}
