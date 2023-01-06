package com.shareE.forum.controller.interceptor;

import com.shareE.forum.entity.User;
import com.shareE.forum.service.DataService;
import com.shareE.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

	@Autowired
	private DataService dataService;

	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String ip = request.getRemoteHost();
		dataService.recordUV(ip);

		User user = hostHolder.getUser();
		if (user != null) {
			dataService.recordDAU(user.getId());
		}

		return true;
	}
}
