package com.shareE.forum.service;

import com.shareE.forum.dao.CommentMapper;
import com.shareE.forum.entity.Comment;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements ForumConstant {

	@Autowired
	private CommentMapper commentMapper;

	@Autowired
	private SensitiveFilter sensitiveFilter;

	@Autowired
	private DiscussPostService discussPostService;

	public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
		return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
	}

	public int findCommentCount(int entityType, int entityId) {
		return commentMapper.selectCountByEntity(entityType, entityId);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public int addComment(Comment comment) {
		if (comment == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}

		// add comment
		comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
		comment.setContent(sensitiveFilter.filter(comment.getContent()));
		int rows = commentMapper.insertComment(comment);

		// update the number of reply to post
		 if (comment.getEntityType() == ENTITY_TYPE_POST) {
			 int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
			 discussPostService.updateCommentCount(comment.getEntityId(), count);
		 }

		return rows;
	}

	public Comment findCommentById(int id) {
		return commentMapper.selectCommentById(id);
	}
}
