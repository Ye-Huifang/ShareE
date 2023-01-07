package com.shareE.forum.config;

import com.shareE.forum.quartz.AlphaJob;
import com.shareE.forum.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

//	@Bean
	public JobDetailFactoryBean alphaJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(AlphaJob.class);
		factoryBean.setName("alphaJob");
		factoryBean.setGroup("alphaJobGroup");
		factoryBean.setDurability(true);
		factoryBean.setRequestsRecovery(true);
		return factoryBean;
	}

//	@Bean
	public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(alphaJobDetail);
		factoryBean.setName("alphaTrigger");
		factoryBean.setGroup("alphaTriggerGroup");
		factoryBean.setRepeatInterval(3000);
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}

	@Bean
	public JobDetailFactoryBean postScoreRefreshJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(PostScoreRefreshJob.class);
		factoryBean.setName("postScoreRefreshJob");
		factoryBean.setGroup("forumJobGroup");
		factoryBean.setDurability(true);
		factoryBean.setRequestsRecovery(true);
		return factoryBean;
	}

	@Bean
	public SimpleTriggerFactoryBean postScoreRefreshJobTrigger(JobDetail postScoreRefreshJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(postScoreRefreshJobDetail);
		factoryBean.setName("postScoreRefreshTrigger");
		factoryBean.setGroup("forumTriggerGroup");
		factoryBean.setRepeatInterval(1000 * 60 * 5);
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}
}
