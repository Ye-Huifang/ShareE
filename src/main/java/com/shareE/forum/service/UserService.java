package com.shareE.forum.service;

import com.shareE.forum.dao.UserMapper;
import com.shareE.forum.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;

	public User findUserById(int id) {
		return userMapper.selectById(id);
	}
}
