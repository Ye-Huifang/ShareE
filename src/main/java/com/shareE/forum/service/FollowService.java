package com.shareE.forum.service;

import com.shareE.forum.entity.User;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements ForumConstant {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserService userService;

	public void follow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations redisOperations) throws DataAccessException {
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

				redisOperations.multi();

				redisOperations.opsForZSet().add(followeeKey, String.valueOf(entityId), System.currentTimeMillis());
				redisOperations.opsForZSet().add(followerKey, String.valueOf(userId), System.currentTimeMillis());
				return redisOperations.exec();
			}
		});
	}

	public void unfollow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations redisOperations) throws DataAccessException {
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

				redisOperations.multi();

				redisOperations.opsForZSet().remove(followeeKey, String.valueOf(entityId));
				redisOperations.opsForZSet().remove(followerKey, String.valueOf(userId));
				return redisOperations.exec();
			}
		});
	}

	// find count of followed entity
	public long findFolloweeCount(int userId, int entityType) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().zCard(followeeKey);
	}

	// find count of followers of a certain entity
	public long findFollowerCount(int entityType, int entityId) {
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return redisTemplate.opsForZSet().zCard(followerKey);
	}

	// check whether the current user follows the entity or not
	public boolean hasFollowed(int userId, int entityType, int entityId) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().score(followeeKey, String.valueOf(entityId)) != null;
	}

	public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
		Set<String> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
		if (targetIds == null) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();
		for (String targetId : targetIds) {
			Map<String, Object> map = new HashMap<>();
			User user = userService.findUserById(Integer.valueOf(targetId));
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}

		return list;
	}

	public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
		String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
		Set<String> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
		if (targetIds == null) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();
		for (String targetId : targetIds) {
			Map<String, Object> map = new HashMap<>();
			User user = userService.findUserById(Integer.valueOf(targetId));
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}

		return list;
	}
}
