package com.shareE.forum.quartz;

import com.shareE.forum.util.ForumConstant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PostScoreRefreshJob implements Job, ForumConstant {


	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

	}
}
