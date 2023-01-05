package com.shareE.forum;

import com.shareE.forum.dao.DiscussMapper;
//import com.shareE.forum.elasticsearch.DiscussPostRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class ElasticsearchTests {

	@Autowired
	private DiscussMapper discussMapper;

//	@Autowired
//	private DiscussPostRepository discussPostRepository;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Test
	public void testInsert() {
//		discussPostRepository.save(discussMapper.selectDiscussPostById(241));
//		discussPostRepository.save(discussMapper.selectDiscussPostById(281));
//		discussPostRepository.save(discussMapper.selectDiscussPostById(243));

	}
}
