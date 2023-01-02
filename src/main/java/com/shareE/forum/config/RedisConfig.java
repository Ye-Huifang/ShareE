package com.shareE.forum.config;


import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisConfig {

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(RedisSerializer.string());
		template.setValueSerializer(RedisSerializer.json());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setHashValueSerializer(RedisSerializer.json());

		template.afterPropertiesSet();
		return template;

//		RedisTemplate<Object, Object> template = new RedisTemplate<>();
//		// 2.关联 redisConnectionFactory
//		template.setConnectionFactory(factory);
//		// 3.创建 序列化类
//		GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);
//		// 6.序列化类，对象映射设置
//		// 7.设置 value 的转化格式和 key 的转化格式
//		template.setValueSerializer(genericToStringSerializer);
//		template.setKeySerializer(new StringRedisSerializer());
//		template.afterPropertiesSet();
//		return template;
	}
}
