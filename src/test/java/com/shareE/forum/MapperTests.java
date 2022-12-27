package com.shareE.forum;
import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.dao.LoginTicketMapper;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.dao.UserMapper;
import com.shareE.forum.entity.LoginTicket;
import com.shareE.forum.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MapperTests {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private DiscussMapper discussMapper;

	@Autowired
	private LoginTicketMapper loginTicketMapper;

	@Test
	public void testSelectById() {
		User user = userMapper.selectById(101);
		System.out.println(user);

		user = userMapper.selectByName("liubei");
		System.out.println(user);

		user = userMapper.selectByEmail("nowcoder101@sina.com");
		System.out.println(user);
	}

	@Test
	public void testInsertUser() {
		User user = new User();
		user.setUsername("test");
		user.setPassword("1234");
		user.setSalt("abc");
		user.setEmail("test@qq.com");
		user.setHeaderUrl("http://www.hello.com/101.png");
		user.setCreateTime(new Date());

		int rows = userMapper.insertUser(user);
		System.out.println(rows);
		System.out.println(user.getId());
	}

	@Test
	public void testUpdateUser() {
		int rows = userMapper.updateStatus(150, 1);
		System.out.println(rows);

		rows = userMapper.updateHeader(150, "http://www.hello.com/102.png");
		System.out.println(rows);

		rows = userMapper.updatePassword(150, "hello");
		System.out.println(rows);
	}

	@Test
	public void testSelectDiscussPosts() {
		List<DiscussPost> list = discussMapper.selectDiscussPosts(149, 0, 10);
		for (DiscussPost dp : list) {
			System.out.println(dp);
		}

		int rows = discussMapper.selectDiscussPostRows(149);
		System.out.println(rows);
	}

	@Test
	public void testInsertLoginTicket() {
		LoginTicket loginTicket = new LoginTicket();
		loginTicket.setUserId(101);
		loginTicket.setStatus(0);
		loginTicket.setTicket("abc");
		loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
		loginTicketMapper.insertLoginTicket(loginTicket);
	}

	@Test
	public void testSelectLoginTicket() {
		LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
		System.out.println(loginTicket);

		loginTicketMapper.updateStatus("abc", 1);
		loginTicket = loginTicketMapper.selectByTicket("abc");
		System.out.println(loginTicket);
	}
}