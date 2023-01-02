package com.shareE.forum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class RedisTests {

	@Autowired
	private StringRedisTemplate redisTemplate;


	@Test
	public void testStrings() {
		String redisKey = "test:counter";

		redisTemplate.opsForValue().set(redisKey, "10");

		System.out.println(redisTemplate.opsForValue().get(redisKey));
		System.out.println(redisTemplate.opsForValue().increment(redisKey));

	}

	@Test
	public void testHashes() {
		String redisKey = "test:user";

		redisTemplate.opsForHash().put(redisKey, "id", "1");
		redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

		System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
		System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));

	}

	@Test
	public void testLists() {
		String redisKey = "test:ids";

		redisTemplate.opsForList().leftPush(redisKey, "101");
		redisTemplate.opsForList().leftPush(redisKey, "102");
		redisTemplate.opsForList().leftPush(redisKey, "103");

		System.out.println(redisTemplate.opsForList().size(redisKey));
		System.out.println(redisTemplate.opsForList().index(redisKey, 0));
		System.out.println(redisTemplate.opsForList().range(redisKey, 0,2));

		System.out.println(redisTemplate.opsForList().leftPop(redisKey));
		System.out.println(redisTemplate.opsForList().leftPop(redisKey));
	}

	@Test
	public void testSets() {
		String redisKey = "test:teachers";

		redisTemplate.opsForSet().add(redisKey, "Amy", "Alice", "Lily");

		System.out.println(redisTemplate.opsForSet().size(redisKey));
		System.out.println(redisTemplate.opsForSet().pop(redisKey));
		System.out.println(redisTemplate.opsForSet().members(redisKey));

	}

	@Test
	public void testSortedSets() {
		String redisKey = "test:students";

		redisTemplate.opsForZSet().add(redisKey, "Amy", 80);
		redisTemplate.opsForZSet().add(redisKey, "Lily", 90);
		redisTemplate.opsForZSet().add(redisKey, "Alice", 100);

		System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
		System.out.println(redisTemplate.opsForZSet().score(redisKey, "Amy"));
		System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "Lily"));
		System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
		System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));

	}

	@Test
	public void testKeys() {
		redisTemplate.delete("test:user");

		System.out.println(redisTemplate.hasKey("test:user"));
		redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
	}

	@Test
	public void testBoundOperations() {
		String redisKey = "test:count";
		BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
		operations.increment();
		operations.increment();
		operations.increment();
		operations.increment();
		operations.increment();
		System.out.println(operations.get());
	}

	@Test
	public void testTransactional() {
		Object obj = redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations redisOperations) throws DataAccessException {
				String redisKey = "test:tx";
				redisOperations.multi();
				redisOperations.opsForSet().add(redisKey, "Amy");
				redisOperations.opsForSet().add(redisKey, "Emily");
				redisOperations.opsForSet().add(redisKey, "John");

				System.out.println(redisOperations.opsForSet().members(redisKey));

				return redisOperations.exec();
			}
		});
		System.out.println(obj);
	}
}
