package com.shareE.forum.event;

import com.alibaba.fastjson.JSONObject;
import com.shareE.forum.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

	@Autowired
	private KafkaTemplate kafkaTemplate;

	public void fireEvent(Event event) {
		kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
	}
}
