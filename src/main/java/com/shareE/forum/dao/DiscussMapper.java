package com.shareE.forum.dao;

import com.shareE.forum.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussMapper {

	List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

	// @Param is used to alias the parameter
	// Necessary when there is only one parameter and used in <if>
	int selectDiscussPostRows(@Param("userId") int userId);

	int insertDiscussPost(DiscussPost discussPost);

	DiscussPost selectDiscussPostById(int id);

	int updateCommentCount(int id, int commentCount);

	int updateType(int id, int type);

	int updateStatus(int id, int status);

}
