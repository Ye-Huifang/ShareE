package com.shareE.forum.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.shareE.forum.annotation.LoginRequired;
import com.shareE.forum.entity.User;
import com.shareE.forum.service.FollowService;
import com.shareE.forum.service.LikeService;
import com.shareE.forum.service.UserService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import com.shareE.forum.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements ForumConstant {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Value("${forum.path.upload}")
	private String uploadPath;

	@Value("${forum.path.domain}")
	private String domain;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Autowired
	private UserService userService;

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private LikeService likeService;

	@Autowired
	private FollowService followService;

	@Value("${qiniu.key.access}")
	private String accessKey;

	@Value("${qiniu.key.secret}")
	private String secretKey;

	@Value("${qiniu.bucket.header.name}")
	private String headerBucketName;

	@Value("${qiniu.bucket.header.url}")
	private String headerBucketUrl;

	@LoginRequired
	@RequestMapping(path = "/setting", method = RequestMethod.GET)
	public String getSettingPage(Model model) {
		String fileName = ForumUtil.generateUUID();
		StringMap policy = new StringMap();
		policy.put("returnBody", ForumUtil.getJSONString(0));
		Auth auth = Auth.create(accessKey, secretKey);
		String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

		model.addAttribute("uploadToken", uploadToken);
		model.addAttribute("fileName", fileName);

		return "/site/setting";
	}

	@RequestMapping(path = "/header/url", method = RequestMethod.POST)
	@ResponseBody
	public String updateHeaderUrl(String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return ForumUtil.getJSONString(1, "File name cannot be null!");
		}

		String url = "http://" + headerBucketUrl + "/" + fileName;
		userService.updateHeader(hostHolder.getUser().getId(), url);

		return ForumUtil.getJSONString(0);
	}

	@LoginRequired
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public String uploadHeader(MultipartFile headerImage, Model model) {
		if (headerImage == null) {
			model.addAttribute("error", "No picture uploaded!");
			return "/site/setting";
		}

		String fileName = headerImage.getOriginalFilename();
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		if (StringUtils.isBlank(suffix)) {
			model.addAttribute("error", "Incorrect file format!");
			return "/site/setting";
		}

		// generate random file name
		fileName = ForumUtil.generateUUID() + suffix;
		File dest = new File(uploadPath + "/" + fileName);
		try {
			headerImage.transferTo(dest);
		} catch (IOException e) {
			logger.error("Upload file failed: " + e.getMessage());
			throw new RuntimeException("Upload file failed: " + e);
		}

		// http://localhost:8080/forum/user/header/xxx.png
		User user = hostHolder.getUser();
		String headerUrl = domain + contextPath + "/user/header/" + fileName;
		userService.updateHeader(user.getId(), headerUrl);

		return "redirect:/index";
	}

	@RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
	public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		// path in server
		fileName = uploadPath + "/" + fileName;
		// find suffix of file
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		response.setContentType("image/" + suffix);
		try (
				FileInputStream fis = new FileInputStream(fileName);
				OutputStream os = response.getOutputStream();
				){
			byte[] buffer = new byte[1024];
			int b = 0;
			while ((b = fis.read(buffer)) != -1) {
				os.write(buffer, 0, b);
			}
		} catch (IOException e) {
			logger.error("Failed to get header: " + e.getMessage());
		}
	}

	// profile
	@RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
	public String getProfilePage(@PathVariable("userId") int userId, Model model) {
		User user = userService.findUserById(userId);
		if (user == null) {
			throw new RuntimeException("User does not exist!");
		}

		// user
		model.addAttribute("user", user);

		// like count
		int likeCount = likeService.findUserLikeCount(userId);
		model.addAttribute("likeCount", likeCount);

		// followee count
		long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
		model.addAttribute("followeeCount", followeeCount);

		// follower count
		long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
		model.addAttribute("followerCount", followerCount);

		// is followed
		boolean hasFollowed = false;
		if (hostHolder.getUser() != null) {
			hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
		}
		model.addAttribute("hasFollowed", hasFollowed);


		return "/site/profile";
	}


}
