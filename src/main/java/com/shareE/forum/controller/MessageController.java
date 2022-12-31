package com.shareE.forum.controller;

import com.shareE.forum.entity.Message;
import com.shareE.forum.entity.Page;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.MessageService;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private UserService userService;

	// list of direct message
	@RequestMapping(path = "/letter/list", method = RequestMethod.GET)
	public String getLetterList(Model model, Page page) {
		User user = hostHolder.getUser();
		// pagination
		page.setLimit(5);
		page.setPath("/letter/list");
		page.setRows(messageService.findConversationCount(user.getId()));

		// list of conversation
		List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
		List<Map<String, Object>> conversations = new ArrayList<>();
		if (conversationList != null) {
			for (Message message : conversationList) {
				Map<String, Object> map = new HashMap<>();
				map.put("conversation", message);
				map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
				map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
				int targetId = user.getId() == message.getFromId() ? message.getId() : message.getFromId();
				map.put("target", userService.findUserById(targetId));

				conversations.add(map);
			}
		}
		model.addAttribute("conversations", conversations);

		// find count of unread message
		int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);

		return "/site/letter";
	}

	@RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
	public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
		// pagination
		page.setLimit(5);
		page.setPath("/letter/detail/" + conversationId);
		page.setRows(messageService.findLetterCount(conversationId));

		// list of direct message
		List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
		List<Map<String, Object>> letters = new ArrayList<>();
		if (letterList != null) {
			for (Message message : letterList) {
				Map<String, Object> map = new HashMap<>();
				map.put("letter", message);
				map.put("fromUser", userService.findUserById(message.getFromId()));
				letters.add(map);
			}
		}
		model.addAttribute("letters", letters);

		// find target of direct message
		model.addAttribute("target", getLetterTarget(conversationId));

		return "/site/letter-detail";
	}

	private User getLetterTarget(String conversationId) {
		String[] ids = conversationId.split("_");
		int id0 = Integer.parseInt(ids[0]);
		int id1 = Integer.parseInt(ids[1]);

		if (hostHolder.getUser().getId() == id0) {
			return userService.findUserById(id1);
		} else {
			return  userService.findUserById(id0);
		}
	}
}
