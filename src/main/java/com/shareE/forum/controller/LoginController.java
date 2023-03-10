package com.shareE.forum.controller;

import com.google.code.kaptcha.Producer;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import com.shareE.forum.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements ForumConstant {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private Producer kaptchaProducer;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@RequestMapping(path = "/register", method = RequestMethod.GET)
	public String getRegisterPage() {
		return "/site/register";
	}

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String getLoginPage() {
		return "/site/login";
	}

	@RequestMapping(path = "/register", method = RequestMethod.POST)
	public String register(Model model, User user) {
		Map<String, Object> map = userService.register(user);
		if (map == null || map.isEmpty()) {
			model.addAttribute("msg", "Success! We have sent you an activation email. Please activate as soon as possible!");
			model.addAttribute("target", "/index");
			return "/site/operate-result";
		} else {
			model.addAttribute("usernameMsg", map.get("usernameMsg"));
			model.addAttribute("passwordMsg", map.get("passwordMsg"));
			model.addAttribute("emailMsg", map.get("emailMsg"));
			return "/site/register";
		}
	}

	@RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
	public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
		int result = userService.activation(userId, code);
		if (result == ACTIVATION_SUCCESS) {
			model.addAttribute("msg", "Your account has been activated successfully. You can now login.");
			model.addAttribute("target", "/login");
		} else if (result == ACTIVATION_REPEAT) {
			model.addAttribute("msg", "Failed to activate - account already active.");
			model.addAttribute("target", "/index");
		} else {
			model.addAttribute("msg", "Failed to activate - account code incorrect.");
			model.addAttribute("target", "/index");
		}
		return "/site/operate-result";
	}

	@RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
	public void getKaptcha(HttpServletResponse response/*, HttpSession session */) {
		// generate verification code
		String text = kaptchaProducer.createText();
		BufferedImage image = kaptchaProducer.createImage(text);

		// store the code into session
//		session.setAttribute("kaptcha", text);

		String kaptchaOwner = ForumUtil.generateUUID();
		Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
		cookie.setMaxAge(60);
		cookie.setPath(contextPath);
		response.addCookie(cookie);
		// store verification code into redis
		String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
		redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

		// output image to the browser
		response.setContentType("image/png");
		try {
			OutputStream os = response.getOutputStream();
			ImageIO.write(image, "png", os);
		} catch (IOException e) {
			logger.error("Response verification code failed: " + e.getMessage());
		}
	}

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public String login(String username, String password, String code,
	                    boolean rememberMe, Model model, /* HttpSession session, */HttpServletResponse response,
	                    @CookieValue("kaptchaOwner") String kaptchaOwner) {
//		String kaptcha = (String) session.getAttribute("kaptcha");
		String kaptcha = null;
		if (StringUtils.isNotBlank(kaptchaOwner)) {
			String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
			kaptcha = redisTemplate.opsForValue().get(redisKey);
		}
		// check verification code
		if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
			model.addAttribute("codeMsg", "Incorrect verification code!");
			return "/site/login";
		}

		// check username and password
		int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SESSIONS : DEFAULT_EXPIRED_SESSIONS;
		Map<String, Object> map = userService.login(username, password, expiredSeconds);
		if (map.containsKey("ticket")) {
			Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
			cookie.setPath(contextPath);
			cookie.setMaxAge(expiredSeconds);
			response.addCookie(cookie);
			return "redirect:/index";
		} else {
			model.addAttribute("usernameMsg", map.get("usernameMsg"));
			model.addAttribute("passwordMsg", map.get("passwordMsg"));
			return "/site/login";
		}
	}

	@RequestMapping(path = "/logout", method = RequestMethod.GET)
	public String logout(@CookieValue("ticket") String ticket) {
		userService.logout(ticket);
		SecurityContextHolder.clearContext();
		return "redirect:/login";
	}
}
