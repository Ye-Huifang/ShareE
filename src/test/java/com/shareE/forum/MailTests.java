package com.shareE.forum;
import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.dao.UserMapper;
import com.shareE.forum.entity.User;
import com.shareE.forum.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MailTests {

	@Autowired
	private MailClient mailClient;

	@Autowired
	private TemplateEngine templateEngine;

	@Test
	public void testTextMail() {
		mailClient.sendMail("515108593@qq.com", "TEST", "Welcome");
	}

	@Test
	public void testHtmlMail() {
		Context context = new Context();
		context.setVariable("username", "Amy");

		String content = templateEngine.process("/mail/demo", context);
		System.out.println(content);

		mailClient.sendMail("515108593@qq.com", "HTML", content);
	}
}
