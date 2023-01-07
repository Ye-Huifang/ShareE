package com.shareE.forum.event;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.shareE.forum.entity.DiscussPost;
import com.shareE.forum.entity.Event;
import com.shareE.forum.entity.Message;
import com.shareE.forum.service.DiscussPostService;
import com.shareE.forum.service.MessageService;
import com.shareE.forum.util.ForumConstant;
import com.shareE.forum.util.ForumUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements ForumConstant {

	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

	@Autowired
	private MessageService messageService;

	@Autowired
	private DiscussPostService discussPostService;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${wk.image.command}")
	private String wkImageCommand;

	@Value("${qiniu.key.access}")
	private String accessKey;

	@Value("${qiniu.key.secret}")
	private String secretKey;

	@Value("${qiniu.bucket.share.name}")
	private String shareBucketName;

	@Value("${qiniu.bucket.share.url}")
	private String shareBucketUrl;

	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;

	@KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
	public void handleCommentMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			logger.error("Message is empty!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			logger.error("Message format error!");
			return;
		}

		// send notification
		Message message = new Message();
		message.setFromId(SYSTEM_USER_ID);
		message.setToId(event.getEntityUserId());
		message.setConversationId(event.getTopic());
		message.setCreateTime(new Date());

		Map<String, Object> content = new HashMap<>();
		content.put("userId", event.getUserId());
		content.put("entityType", event.getEntityType());
		content.put("entityId", event.getEntityId());

		if (!event.getData().isEmpty()) {
			for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
				content.put(entry.getKey(), entry.getValue());
			}
		}

		message.setContent(JSONObject.toJSONString(content));
		messageService.addMessage(message);
	}

	@KafkaListener(topics = TOPIC_SHARE)
	public void handleShareMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			logger.error("Message is empty!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			logger.error("Message format error!");
			return;
		}

		String htmlUrl = (String) event.getData().get("htmlUrl");
		String fileName = (String) event.getData().get("fileName");
		String suffix = (String) event.getData().get("suffix");

		String cmd = wkImageCommand + " --quality 75 "
				+ htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
		try {
			Runtime.getRuntime().exec(cmd);
			logger.info("Generate image successfully: " + cmd);
		} catch (IOException e) {
			logger.error("Generate image failed: " + e.getMessage());
		}

		// use scheduler to monitor the image
		// once generated, upload it to the qiniu server
		UploadTask task = new UploadTask(fileName, suffix);
		Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task, 500);
		task.setFuture(future);

	}

	class UploadTask implements Runnable {

		private String fileName;
		private String suffix;
		private Future future;
		private long startTime;
		private int uploadTimes;

		public void setFuture(Future future) {
			this.future = future;
		}

		public UploadTask(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
			this.startTime = System.currentTimeMillis();
		}

		/**
		 * When an object implementing interface {@code Runnable} is used
		 * to create a thread, starting the thread causes the object's
		 * {@code run} method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method {@code run} is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			// generation failed
			if (System.currentTimeMillis() - startTime > 30000) {
				logger.error("Processing time is too long: " + fileName);
				future.cancel(true);
				return;
			}

			// upload failed
			if (uploadTimes >= 3) {
				logger.error("Too many upload times: " + fileName);
				future.cancel(true);
				return;
			}

			String path = wkImageStorage + "/" + fileName + suffix;
			File file = new File(path);
			if (file.exists()) {
				logger.info(String.format("No.[%d] start to upload [%s]", ++uploadTimes, fileName));
				StringMap policy = new StringMap();
				policy.put("returnBody", ForumUtil.getJSONString(0));
				Auth auth = Auth.create(accessKey, secretKey);
				String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
				UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
				try {
					Response response = manager.put(
							path, fileName, uploadToken, null, "image/" + suffix.substring(suffix.lastIndexOf(".") + 1), false
					);
					JSONObject json = JSONObject.parseObject(response.bodyString());
					if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
						logger.info(String.format("No.[%d] upload failed [%s].", uploadTimes, fileName));
					} else {
						logger.info(String.format("No.[%d] upload successfully [%s].", uploadTimes, fileName));
						future.cancel(true);
					}
				} catch (QiniuException e) {
					logger.info(String.format("No.[%d] upload failed [%s].", uploadTimes, fileName));
				}
			} else {
				logger.info("Wait for the image to generate [" + fileName + "]");
			}
		}
	}

}
