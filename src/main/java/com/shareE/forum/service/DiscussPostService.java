package com.shareE.forum.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.shareE.forum.dao.DiscussMapper;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

	private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

	@Autowired
	private DiscussMapper discussMapper;

	@Autowired
	private SensitiveFilter sensitiveFilter;

	@Value("${caffeine.post.max-size}")
	private int maxSize;

	@Value("${caffeine.post.expire-second}")
	private int expireSeconds;

	private LoadingCache<String, List<DiscussPost>> postListCache;

	private LoadingCache<Integer, Integer> postRowsCache;

	@PostConstruct
	public void init() {
		// initialize cache for post lists
		postListCache = Caffeine.newBuilder()
				.maximumSize(maxSize)
				.expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<DiscussPost>>() {
					@Override
					public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
						if (key == null || key.length() == 0) {
							throw new IllegalArgumentException("Argument illegal!");
						}

						String[] params = key.split(":");
						if (params == null || params.length != 2) {
							throw new IllegalArgumentException("Argument illegal!");
						}

						int offset = Integer.valueOf(params[0]);
						int limit = Integer.valueOf(params[1]);

						logger.debug("load list rows from DB");
						return discussMapper.selectDiscussPosts(0, offset, limit, 1);
					}
				});

		// initialize cache for post row
		postRowsCache = Caffeine.newBuilder()
				.maximumSize(maxSize)
				.expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
				.build(new CacheLoader<Integer, Integer>() {
					       @Override
					       public @Nullable Integer load(@NonNull Integer key) throws Exception {
						       logger.debug("load post rows from DB");
						       return discussMapper.selectDiscussPostRows(key);
					       }
				       }
				);
	}

	public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
		if (userId == 0 && orderMode == 1) {
			return postListCache.get(offset + ":" + limit);
		}

		logger.debug("load post list from DB");
		return discussMapper.selectDiscussPosts(userId, offset, limit, orderMode);
	}

	public int findDiscussPostRows(int userId) {
		if (userId == 0) {
			return postRowsCache.get(userId);
		}

		logger.debug("load post rows from DB");
		return discussMapper.selectDiscussPostRows(userId);
	}

	public int addDiscussPost(DiscussPost post) {
		if (post == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}

		// escape HTML tag
		post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
		post.setContent(HtmlUtils.htmlEscape(post.getContent()));
		// filter sensitive words
		post.setTitle(sensitiveFilter.filter(post.getTitle()));
		post.setContent(sensitiveFilter.filter(post.getContent()));

		return discussMapper.insertDiscussPost(post);
	}

	public DiscussPost findDiscussPostById(int id) {
		return discussMapper.selectDiscussPostById(id);
	}

	public int updateCommentCount(int id, int commentCount) {
		return discussMapper.updateCommentCount(id, commentCount);
	}

	public int updateType(int id, int type) {
		return discussMapper.updateType(id, type);
	}

	public int updateStatus(int id, int status) {
		return discussMapper.updateStatus(id, status);
	}

	public int updateScore(int id, double score) {
		return discussMapper.updateScore(id, score);
	}
}
