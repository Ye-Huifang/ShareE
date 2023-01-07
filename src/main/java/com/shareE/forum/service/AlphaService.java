package com.shareE.forum.service;

import com.shareE.forum.dao.AlphaDao;
import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.dao.UserMapper;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.entity.User;
import com.shareE.forum.util.ForumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {

	private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

	@Autowired
	private AlphaDao alphaDao;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private DiscussMapper discussMapper;

	@Autowired
	private TransactionTemplate transactionTemplate;

	public AlphaService() {
		System.out.println("Constructor of AlphaService");
	}

	@PostConstruct
	public void init() {
		System.out.println("Init AlphaService");
	}

	@PreDestroy
	public void destroy() {
		System.out.println("Destroy AlphaService");
	}

	public String find() {
		return alphaDao.select();
	}

	/**
	 *
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public Object save1() {
		// create user
		User user = new User();
		user.setUsername("alpha");
		user.setSalt(ForumUtil.generateUUID().substring(0, 5));
		user.setPassword(ForumUtil.md5("123" + user.getSalt()));
		user.setEmail("alpha@qq.com");
		user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
		user.setCreateTime(new Date());
		userMapper.insertUser(user);

		// create post
		DiscussPost post = new DiscussPost();
		post.setUserId(user.getId());
		post.setTitle("Hello");
		post.setContent("test");
		post.setCreateTime(new Date());
		discussMapper.insertDiscussPost(post);

		Integer.valueOf("abc");

		return "ok";
	}

	public Object save2() {
		transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

		return transactionTemplate.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus transactionStatus) {
				// create user
				User user = new User();
				user.setUsername("beta");
				user.setSalt(ForumUtil.generateUUID().substring(0, 5));
				user.setPassword(ForumUtil.md5("123" + user.getSalt()));
				user.setEmail("beta@qq.com");
				user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
				user.setCreateTime(new Date());
				userMapper.insertUser(user);

				// create post
				DiscussPost post = new DiscussPost();
				post.setUserId(user.getId());
				post.setTitle("Hello");
				post.setContent("test2");
				post.setCreateTime(new Date());
				discussMapper.insertDiscussPost(post);

				Integer.valueOf("abc");

				return "ok";
			}
		});
	}

	@Async
	public void execute1() {
		logger.debug("execute1");
	}

//	@Scheduled(initialDelay = 10000, fixedRate = 1000)
	public void execute2() {
		logger.debug("execute2");
	}
}
