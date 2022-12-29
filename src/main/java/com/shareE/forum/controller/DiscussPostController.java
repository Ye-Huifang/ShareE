package com.shareE.forum.controller;

import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.DiscussPostService;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.ForumUtil;
import com.shareE.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;

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
		discussPost.setId(user.getId());
		discussPost.setCreateTime(new Date());
		discussPostService.addDiscussPost(discussPost);

		return ForumUtil.getJSONString(0, "Success!");
	}

	@RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
	public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
		// post
		DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
		model.addAttribute("post", discussPost);
		// user
		User user = userService.findUserById(discussPost.getUserId());
		model.addAttribute("user", user);
		return "/site/discuss-detail";
	}
}
