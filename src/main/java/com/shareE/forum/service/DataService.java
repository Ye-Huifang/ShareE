package com.shareE.forum.service;

import com.shareE.forum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Service
public class DataService {

	@Autowired
	private RedisTemplate redisTemplate;

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public void recordUV(String ip) {
		String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
		redisTemplate.opsForHyperLogLog().add(redisKey, ip);
	}


	@Temporal(TemporalType.DATE)
	public long calculateUV(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}
		if (start.after(end)) {
			throw new IllegalArgumentException("Start date cannot be after end date!");
		}


		List<String> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		while (!calendar.getTime().after(end)) {
			String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
			keyList.add(key);
			calendar.add(Calendar.DATE, 1);
		}

		String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
		redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

		return redisTemplate.opsForHyperLogLog().size(redisKey);
	}

	public void recordDAU(int userId) {
		String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
		redisTemplate.opsForValue().setBit(redisKey, userId, true);
	}

	@Temporal(TemporalType.DATE)
	public long calculateDAU(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}
		if (start.after(end)) {
			throw new IllegalArgumentException("Start date cannot be after end date!");
		}

		List<byte[]> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		while (!calendar.getTime().after(end)) {
			String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
			keyList.add(key.getBytes());
			calendar.add(Calendar.DATE, 1);
		}

		return (long) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
				redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(), keyList.toArray(new byte[0][0]));
				return redisConnection.bitCount(redisKey.getBytes());
			}
		});
	}
}
