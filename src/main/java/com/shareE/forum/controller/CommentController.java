package com.shareE.forum.controller;

import com.shareE.forum.entity.Comment;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.entity.Event;
import com.shareE.forum.event.EventProducer;
import com.shareE.forum.service.CommentService;
import com.shareE.forum.service.DiscussPostService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements ForumConstant {

	@Autowired
	private CommentService commentService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private EventProducer eventProducer;

	@Autowired
	private DiscussPostService discussPostService;

	@RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
	public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
		comment.setUserId(hostHolder.getUser().getId());
		comment.setStatus(0);
		comment.setCreateTime(new Date());
		commentService.addComment(comment);

		// fire comment event
		Event event = new Event()
				.setTopic(TOPIC_COMMENT)
				.setUserId(hostHolder.getUser().getId())
				.setEntityType(comment.getEntityType())
				.setEntityId(comment.getEntityId())
				.setData("postId", discussPostId);
		if (comment.getEntityType() == ENTITY_TYPE_POST) {
			DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
			event.setEntityUserId(target.getUserId());
		} else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
			Comment target = commentService.findCommentById(comment.getEntityId());
			event.setEntityUserId(target.getUserId());
		}
		eventProducer.fireEvent(event);

		return "redirect:/discuss/detail/" + discussPostId;
	}
}
