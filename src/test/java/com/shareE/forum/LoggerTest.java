package com.shareE.forum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.slf4j.Logger;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class LoggerTest {

	private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

	@Test
	public void testLogger() {
		System.out.println(logger.getName());
		logger.debug("debug log");
		logger.info("info log");
		logger.warn("warn log");
		logger.error("error log");
	}
}
