package com.shareE.forum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
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

	@Test
	public void testHyperLogLog() {
		String redisKey = "test:hll:01";

		for (int i = 1; i <= 100000; i++) {
			redisTemplate.opsForHyperLogLog().add(redisKey, String.valueOf(i));
		}

		for (int i = 1; i <= 10000; i++) {
			int r = (int) (Math.random() * 10000 + 1);
			redisTemplate.opsForHyperLogLog().add(redisKey, String.valueOf(r));
		}

		System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
	}

	@Test
	public void testHyperLogLogUnion() {
		String redisKey2 = "test:hll:02";
		for (int i = 1; i <= 10000; i++) {
			redisTemplate.opsForHyperLogLog().add(redisKey2, String.valueOf(i));
		}

		String redisKey3 = "test:hll:03";
		for (int i = 5001; i <= 15000; i++) {
			redisTemplate.opsForHyperLogLog().add(redisKey3, String.valueOf(i));
		}

		String redisKey4 = "test:hll:04";
		for (int i = 10001; i <= 20000; i++) {
			redisTemplate.opsForHyperLogLog().add(redisKey4, String.valueOf(i));
		}

		String unionKey = "test:hll:union";
		redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

		System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
	}

	@Test
	public void testBitMap() {
		String redisKey = "test:bm:01";

		redisTemplate.opsForValue().setBit(redisKey, 1, true);
		redisTemplate.opsForValue().setBit(redisKey, 4, true);
		redisTemplate.opsForValue().setBit(redisKey, 7, true);

		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

		Object obj = redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});

		System.out.println(obj);
	}

	@Test
	public void testBitMapOperation() {
		String redisKey2 = "test:bm:02";
		redisTemplate.opsForValue().setBit(redisKey2, 0, true);
		redisTemplate.opsForValue().setBit(redisKey2, 1, true);
		redisTemplate.opsForValue().setBit(redisKey2, 2, true);

		String redisKey3 = "test:bm:03";
		redisTemplate.opsForValue().setBit(redisKey3, 2, true);
		redisTemplate.opsForValue().setBit(redisKey3, 3, true);
		redisTemplate.opsForValue().setBit(redisKey3, 4, true);

		String redisKey4 = "test:bm:04";
		redisTemplate.opsForValue().setBit(redisKey4, 5, true);
		redisTemplate.opsForValue().setBit(redisKey4, 6, true);
		redisTemplate.opsForValue().setBit(redisKey4, 4, true);
		
		String redisKey = "test:bm:or";
		Object obj = redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});

		System.out.println(obj);
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
		System.out.println(redisTemplate.opsForValue().getBit(redisKey, 7));
	}
}
