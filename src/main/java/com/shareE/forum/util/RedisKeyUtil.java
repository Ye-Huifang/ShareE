package com.shareE.forum.util;

public class RedisKeyUtil {

	private static final String SPLIT = ":";
	private static final String PREFIX_ENTITY_LIKE = "like:entity";
	private static final String PREFIX_USER_LIKE = "like:user";
	private static final String PREFIX_FOLLOWEE = "followee";
	private static final String PREFIX_FOLLOWER = "follower";

	// like:entity:entityType:entityId -> set(userId)
	public static String getEntityLikeKey(int entityType, int entityId) {
		return PREFIX_ENTITY_LIKE + SPLIT + entityType + entityId;
	}

	// like:user:userId -> int
	public static String getUserLikeKey(int userId) {
		return PREFIX_USER_LIKE + SPLIT + userId;
	}

	// entity followed by a certain user
	// followee:userId:entityType -> zset(entityId, now)
	public static String getFolloweeKey(int userId, int entityType) {
		return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
	}

	// followers of a certain entity
	// follower:entityType:entityId -> zset(userId, now)
	public static String getFollowerKey(int entityType, int entityId) {
		return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
	}
}
