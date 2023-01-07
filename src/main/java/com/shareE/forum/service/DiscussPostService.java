package com.shareE.forum.service;

import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

import java.util.List;

@Service
public class DiscussPostService {

	@Autowired
	private DiscussMapper discussMapper;


	@Autowired
	private SensitiveFilter sensitiveFilter;

	public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
		return discussMapper.selectDiscussPosts(userId, offset, limit, orderMode);
	}

	public int findDiscussPostRows(int userId) {
		return discussMapper.selectDiscussPostRows(userId);
	}

	public int addDiscussPost(DiscussPost post) {
		if (post == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}

		// escape HTML tag
		post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
		post.setContent(HtmlUtils.htmlEscape(post.getContent()));
		// filter sensitive words
		post.setTitle(sensitiveFilter.filter(post.getTitle()));
		post.setContent(sensitiveFilter.filter(post.getContent()));

		return discussMapper.insertDiscussPost(post);
	}

	public DiscussPost findDiscussPostById(int id) {
		return discussMapper.selectDiscussPostById(id);
	}

	public int updateCommentCount(int id, int commentCount) {
		return discussMapper.updateCommentCount(id, commentCount);
	}

	public int updateType(int id, int type) {
		return discussMapper.updateType(id, type);
	}

	public int updateStatus(int id, int status) {
		return discussMapper.updateStatus(id, status);
	}

	public int updateScore(int id, double score) {
		return discussMapper.updateScore(id, score);
	}
}
