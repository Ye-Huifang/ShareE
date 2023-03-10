package com.shareE.forum.service;

import com.shareE.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	public void like(int userId, int entityType, int entityId, int entityUserId) {
		String userIdToString = String.valueOf(userId);
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations redisOperations) throws DataAccessException {
				String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
				String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

				boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userIdToString);

				redisOperations.multi();
				if (isMember) {
					redisOperations.opsForSet().remove(entityLikeKey, userIdToString);
					redisOperations.opsForValue().decrement(userLikeKey);
				} else {
					redisOperations.opsForSet().add(entityLikeKey, userIdToString);
					redisOperations.opsForValue().increment(userLikeKey);
				}


				return redisOperations.exec();
			}
		});
	}

	public long findEntityLikeCount(int entityType, int entityId) {
		String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
		return redisTemplate.opsForSet().size(entityLikeKey);
	}

	public int findEntityLikeStatus(int userId, int entityType, int entityId) {
		String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
		return redisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId)) ? 1 : 0;
	}

	public int findUserLikeCount(int userId) {
		String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
		String count = redisTemplate.opsForValue().get(userLikeKey);
		return count == null ? 0 : Integer.valueOf(count);

	}
}
