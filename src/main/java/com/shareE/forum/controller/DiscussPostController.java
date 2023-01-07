package com.shareE.forum.controller;

import com.shareE.forum.entity.Comment;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.entity.Page;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.CommentService;
import com.shareE.forum.service.DiscussPostService;
import com.shareE.forum.service.LikeService;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import com.shareE.forum.util.HostHolder;
import com.shareE.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements ForumConstant {

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private LikeService likeService;

	@Autowired
	private RedisTemplate redisTemplate;

	@RequestMapping(path = "/add", method = RequestMethod.POST)
	@ResponseBody
	public String addDiscussPost(String title, String content) {
		User user = hostHolder.getUser();
		if (user == null) {
			return ForumUtil.getJSONString(403, "You have not logged in!");
		}

		DiscussPost discussPost = new DiscussPost();
		discussPost.setContent(content);
		discussPost.setTitle(title);
		discussPost.setUserId(user.getId());
		discussPost.setCreateTime(new Date());
		discussPostService.addDiscussPost(discussPost);

		String redisKey = RedisKeyUtil.getPostScoreKey();
		redisTemplate.opsForSet().add(redisKey, discussPost.getId());

		return ForumUtil.getJSONString(0, "Success!");
	}

	@RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
	public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
		// post
		DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
		model.addAttribute("post", discussPost);
		// user
		User user = userService.findUserById(discussPost.getUserId());
//		User user = hostHolder.getUser();
		model.addAttribute("user", user);
		// like count
		long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
		model.addAttribute("likeCount", likeCount);
		// like status
		int likeStatus = hostHolder.getUser() == null ? 0 :
				likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
		model.addAttribute("likeStatus", likeStatus);

		// comment
		page.setLimit(5);
		page.setPath("/discuss/detail/" + discussPostId);
		page.setRows(discussPost.getCommentCount());

		List<Comment> commentList = commentService.findCommentsByEntity(
				ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

		List<Map<String, Object>> commentVoList = new ArrayList<>();

		if (commentList != null) {
			for (Comment comment : commentList) {
				Map<String, Object> commentVo = new HashMap<>();
				commentVo.put("comment", comment);
				commentVo.put("user", userService.findUserById(comment.getUserId()));
				// like count
				likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
				commentVo.put("likeCount", likeCount);
				// like status
				likeStatus = hostHolder.getUser() == null ? 0 :
						likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
				commentVo.put("likeStatus", likeStatus);
				// reply list (comment to comment)
				List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

				List<Map<String, Object>> replyVoList = new ArrayList<>();
				if (replyList != null) {
					for (Comment reply : replyList) {
						Map<String, Object> replyVo = new HashMap<>();
						replyVo.put("reply", reply);
						replyVo.put("user", userService.findUserById(reply.getUserId()));
						// like count
						likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
						replyVo.put("likeCount", likeCount);
						// like status
						likeStatus = hostHolder.getUser() == null ? 0 :
								likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
						replyVo.put("likeStatus", likeStatus);
						User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
						replyVo.put("target", target);
						replyVoList.add(replyVo);
					}
				}
				commentVo.put("replies", replyVoList);

				// the number of reply
				int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
				commentVo.put("replyCount", replyCount);

				commentVoList.add(commentVo);
			}
		}

		model.addAttribute("comments", commentVoList);
		return "/site/discuss-detail";
	}

	@RequestMapping(path = "/top", method = RequestMethod.POST)
	@ResponseBody
	public String setTop(int id) {
		discussPostService.updateType(id, 1);

		String redisKey = RedisKeyUtil.getPostScoreKey();
		redisTemplate.opsForSet().add(redisKey, id);

		return ForumUtil.getJSONString(0);
	}

	@RequestMapping(path = "/wonderful", method = RequestMethod.POST)
	@ResponseBody
	public String setWonderful(int id) {
		discussPostService.updateStatus(id, 1);
		return ForumUtil.getJSONString(0);
	}

	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String setDelete(int id) {
		discussPostService.updateStatus(id, 2);
		return ForumUtil.getJSONString(0);
	}
}
