package com.shareE.forum.util;

import com.shareE.forum.entity.User;
import org.springframework.stereotype.Component;

/**
 * Hold users' information (in replace of session)
 */
@Component
public class HostHolder {

	private ThreadLocal<User> users = new ThreadLocal<>();

	public void setUser(User user) {
		users.set(user);
	}

	public User getUser() {
		return users.get();
	}

	public void clear() {
		users.remove();
	}

}
