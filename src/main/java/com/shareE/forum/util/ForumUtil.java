package com.shareE.forum.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class ForumUtil {

	/**
	 * @return random UUID
	 */
	public static String generateUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * Use MD5 to encrypt password
	 * @param key password
	 * @return
	 */
	public static String md5(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return DigestUtils.md5DigestAsHex(key.getBytes());
	}

}
