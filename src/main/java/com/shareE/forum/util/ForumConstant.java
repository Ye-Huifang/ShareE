package com.shareE.forum.util;

public interface ForumConstant {

	int ACTIVATION_SUCCESS = 0;

	int ACTIVATION_REPEAT = 1;

	int ACTIVATION_FAILURE = 2;

	int DEFAULT_EXPIRED_SESSIONS = 3600 * 12;

	int REMEMBER_EXPIRED_SESSIONS = 3600 * 24 * 100;

	int ENTITY_TYPE_POST = 1;

	int ENTITY_TYPE_COMMENT = 2;

	int ENTITY_TYPE_USER = 3;

	String TOPIC_COMMENT = "comment";

	String TOPIC_LIKE = "like";

	String TOPIC_FOLLOW = "follow";

	String TOPIC_PUBLISH = "publish";

	String TOPIC_DELETE = "delete";

	String TOPIC_SHARE = "share";

	int SYSTEM_USER_ID = 1;

	String AUTHORITY_USER = "user";

	String AUTHORITY_ADMIN = "admin";

	String AUTHORITY_MODERATOR = "moderator";
}
