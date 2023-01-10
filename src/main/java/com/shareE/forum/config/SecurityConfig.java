package com.shareE.forum.config;

import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ForumConstant {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// authorize
		http.authorizeRequests()
				.antMatchers(
						"/user/setting",
						"/user/upload",
						"/discuss/add",
						"/comment/add/**",
						"/letter/**",
						"/notice/**",
						"/like",
						"/follow",
						"/unfollow"
				)
				.hasAnyAuthority(
						AUTHORITY_USER,
						AUTHORITY_MODERATOR,
						AUTHORITY_ADMIN
				)
				.antMatchers(
						"/discuss/top",
						"/discuss/wonderful"
						)
				.hasAnyAuthority(
						AUTHORITY_MODERATOR,
						AUTHORITY_ADMIN
				)
				.antMatchers(
						"/discuss/delete",
						"/data/**",
						"/actuator/**"
				)
				.hasAnyAuthority(
						AUTHORITY_ADMIN
				)
				.anyRequest().permitAll()
				.and().csrf().disable();

		// when we don't have authorization
		http.exceptionHandling()
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					// when no authority
					@Override
					public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
						String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
						if ("XMLHttpRequest".equals(xRequestedWith)) {
							// if asynchronous
							httpServletResponse.setContentType("application/plain;charset=tuf-8");
							PrintWriter writer = httpServletResponse.getWriter();
							writer.write(ForumUtil.getJSONString(403, "You haven't logged in!"));
						} else {
							httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
						}
					}
				})
				.accessDeniedHandler(new AccessDeniedHandler() {
					// when authority is not enough
					@Override
					public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
						String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
						if ("XMLHttpRequest".equals(xRequestedWith)) {
							// if asynchronous
							httpServletResponse.setContentType("application/plain;charset=tuf-8");
							PrintWriter writer = httpServletResponse.getWriter();
							writer.write(ForumUtil.getJSONString(403, "Permission denied."));
						} else {
							httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
						}
					}
				});

		http.logout().logoutUrl("/securitylogout");
	}
}
