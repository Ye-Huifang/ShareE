package com.shareE.forum;

import com.shareE.forum.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class SensitiveTests {

	@Autowired
	private SensitiveFilter sensitiveFilter;

	@Test
	public void testSensitiveFilter() {
		String text = "hello, I can drug and text. ascbf&a&bc&&";
		text = sensitiveFilter.filter(text);
		System.out.println(text);
	}
}
