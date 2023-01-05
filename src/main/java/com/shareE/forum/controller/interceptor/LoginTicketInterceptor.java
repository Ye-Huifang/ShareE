package com.shareE.forum.controller.interceptor;

import com.shareE.forum.entity.LoginTicket;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.CookieUtil;
import com.shareE.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

	@Autowired
	private UserService userService;

	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// acquire credential from cookie
		String ticket = CookieUtil.getValue(request, "ticket");

		if (ticket != null) {
			LoginTicket loginTicket = userService.findLoginTicket(ticket);
			// check validity of credential
			if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
				// find user by credential
				User user = userService.findUserById(loginTicket.getUserId());
				// hold user in this request
				hostHolder.setUser(user);
				// get user's authority level and store it in SecurityContext
				Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), userService.getAuthorities(user.getId()));
				SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
			}
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if (user != null && modelAndView != null) {
			modelAndView.addObject("loginUser", user);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		hostHolder.clear();
		SecurityContextHolder.clearContext();
	}
}
