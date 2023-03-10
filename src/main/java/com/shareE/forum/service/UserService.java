package com.shareE.forum.service;

import com.shareE.forum.dao.LoginTicketMapper;
import com.shareE.forum.dao.UserMapper;
import com.shareE.forum.entity.LoginTicket;
import com.shareE.forum.entity.User;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import com.shareE.forum.util.MailClient;
import com.shareE.forum.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements ForumConstant {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private MailClient mailClient;

	@Autowired
	private TemplateEngine templateEngine;

	@Value("${forum.path.domain}")
	private String domain;

	@Value("${server.servlet.context-path}")
	private String contextPath;

//	@Autowired
//	private LoginTicketMapper loginTicketMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	public User findUserById(int id) {
//		return userMapper.selectById(id);
		User user = getCache(id);
		if (user == null) {
			user = initCache(id);
		}
		return user;
	}

	public Map<String, Object> register(User user) {
		Map<String, Object> map = new HashMap<>();

		// sanity check
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null!");
		}
		if (StringUtils.isBlank(user.getUsername())) {
			map.put("usernameMsg", "Username cannot be null!");
			return map;
		}
		if (StringUtils.isBlank(user.getPassword())) {
			map.put("passwordMsg", "Password cannot be null");
			return map;
		}
		if (StringUtils.isBlank(user.getEmail())) {
			map.put("emailMsg", "Email cannot be null");
			return map;
		}

		// validate username
		User u = userMapper.selectByName(user.getUsername());
		if (u != null) {
			map.put("usernameMsg", "Username already exists");
			return map;
		}

		// validate email
		u = userMapper.selectByEmail(user.getEmail());
		if (u != null) {
			map.put("usernameMsg", "Email already exists");
			return map;
		}

		// Register users
		user.setSalt(ForumUtil.generateUUID().substring(0, 5));
		user.setPassword(ForumUtil.md5(user.getPassword() + user.getSalt()));
		user.setType(0);
		user.setStatus(0);
		user.setActivationCode(ForumUtil.generateUUID());
		user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
		user.setCreateTime(new Date());
		userMapper.insertUser(user);

		// Activation email
		Context context = new Context();
		context.setVariable("email", user.getEmail());
		String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
		context.setVariable("url", url);
		String content = templateEngine.process("/mail/activation", context);
		mailClient.sendMail(user.getEmail(), "Activate Account - ShareE", content);

		return map;
	}

	public int activation(int userId, String code) {
		User user = userMapper.selectById(userId);
		if (user.getStatus() == 1) {
			return ACTIVATION_REPEAT;
		} else if (user.getActivationCode().equals(code)) {
			userMapper.updateStatus(userId, 1);
			clearCache(userId);
			return ACTIVATION_SUCCESS;
		} else {
			return ACTIVATION_FAILURE;
		}
	}

	public Map<String, Object> login(String username, String password, int expiredSeconds) {
		Map<String, Object> map = new HashMap<>();

		// sanity check
		if (StringUtils.isBlank(username)) {
			map.put("usernameMsg", "Username cannot be empty");
			return map;
		}
		if (StringUtils.isBlank(password)) {
			map.put("passwordMsg", "Password cannot be empty");
			return map;
		}

		// validate account
		User user = userMapper.selectByName(username);
		if (user == null) {
			map.put("usernameMsg", "The account does not exist!");
			return map;
		}

		// check whether account is activated
		if (user.getStatus() == 0) {
			map.put("usernameMsg", "The account has not been activated!");
			return map;
		}

		// validate password
		password = ForumUtil.md5(password + user.getSalt());
		if (!user.getPassword().equals(password)) {
			map.put("passwordMsg", "Password is incorrect!");
		}

		// generate login credentials
		LoginTicket loginTicket = new LoginTicket();
		loginTicket.setUserId(user.getId());
		loginTicket.setTicket(ForumUtil.generateUUID());
		loginTicket.setStatus(0);
		loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//		loginTicketMapper.insertLoginTicket(loginTicket);

		String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
		redisTemplate.opsForValue().set(redisKey, loginTicket);

		map.put("ticket", loginTicket.getTicket());
		return map;
	}

	public void logout(String ticket) {
//		loginTicketMapper.updateStatus(ticket, 1);
		String redisKey = RedisKeyUtil.getTicketKey(ticket);
		LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
		loginTicket.setStatus(1);
		redisTemplate.opsForValue().set(redisKey, loginTicket);
	}

	public LoginTicket findLoginTicket(String ticket) {
//		return loginTicketMapper.selectByTicket(ticket);
		String redisKey = RedisKeyUtil.getTicketKey(ticket);
		return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
	}

	public int updateHeader(int userId, String headerUrl) {
		int rows = userMapper.updateHeader(userId, headerUrl);
		clearCache(userId);
		return rows;
	}

	public User findUserByName(String username) {
		return userMapper.selectByName(username);
	}

	// we first find user via cache
	private User getCache(int userId) {
		String redisKey = RedisKeyUtil.getUserKey(userId);
		return (User) redisTemplate.opsForValue().get(redisKey);
	}

	// if we cannot find it, we initialize the cache
	private User initCache(int userId) {
		User user = userMapper.selectById(userId);
		String redisKey = RedisKeyUtil.getUserKey(userId);
		redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
		return user;
	}

	// when data change, clear the cache
	private void clearCache(int userId) {
		String redisKey = RedisKeyUtil.getUserKey(userId);
		redisTemplate.delete(redisKey);
	}

	public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
		User user = this.findUserById(userId);

		List<GrantedAuthority> list = new ArrayList<>();
		list.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				switch (user.getType()) {
					case 1:
						return AUTHORITY_ADMIN;
					case 2:
						return AUTHORITY_MODERATOR;
					default:
						return AUTHORITY_USER;
				}
			}
		});
		return list;
	}
}
