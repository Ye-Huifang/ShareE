package com.shareE.forum.controller.advice;

import com.shareE.forum.util.ForumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

	@ExceptionHandler({Exception.class})
	public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.error("Error in server: " + e.getMessage());
		for (StackTraceElement element : e.getStackTrace()) {
			logger.error(element.toString());
		}

		String xRequestedWith = request.getHeader("x-requested-with");
		if ("XMLHttpRequest".equals(xRequestedWith)) {
			response.setContentType("application/plain;charset=utf-8");
			PrintWriter writer = response.getWriter();
			writer.write(ForumUtil.getJSONString(1, "Server exception!"));
		} else {
			response.sendRedirect(request.getContextPath() + "/error");
		}
	}
}
