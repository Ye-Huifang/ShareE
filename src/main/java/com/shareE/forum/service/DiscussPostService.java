package com.shareE.forum.service;

import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

	@Autowired
	private DiscussMapper discussMapper;

	public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
		return discussMapper.selectDiscussPosts(userId, offset, limit);
	}

	public int findDiscussPostRows(int userId) {
		return discussMapper.selectDiscussPostRows(userId);
	}
}
