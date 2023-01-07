package com.shareE.forum.quartz;

import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.service.DiscussPostService;
import com.shareE.forum.service.LikeService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PostScoreRefreshJob implements Job, ForumConstant {

	private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private LikeService likeService;

	private static final Date epoch;

	static {
		try {
			epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
		} catch (ParseException e) {
			throw new RuntimeException("Initialize epoch year failed!");
		}
	}

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		String redisKey = RedisKeyUtil.getPostScoreKey();
		BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

		if (operations.size() == 0) {
			logger.info("[Task cancel] No new post to update!");
			return;
		}

		logger.info("[Task Start] Start to update score of post: " + operations.size());
		while (operations.size() > 0) {
			this.refresh((Integer)operations.pop());
		}
		logger.info("[Task End] Finish refreshing the score!");
	}

	private void refresh(int postId) {
		DiscussPost post = discussPostService.findDiscussPostById(postId);

		if (post == null) {
			logger.error("The post does not exist! Id: " + postId);
			return;
		}

		boolean wonderful = post.getStatus() == 1;
		int commentCount = post.getCommentCount();
		long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

		double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		double score = Math.log10(Math.max(weight, 1))
				+ (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
		discussPostService.updateScore(postId, score);
	}
}
