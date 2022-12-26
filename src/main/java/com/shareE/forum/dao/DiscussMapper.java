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




}
