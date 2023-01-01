package com.shareE.forum.dao;

import com.shareE.forum.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

	// find message list of current user
	// only return the latest message
	List<Message> selectConversations(int userId, int offset, int limit);

	// find conversation count of current user
	int selectConversationCount(int userId);

	// find all direct messages in a conversation
	List<Message> selectLetters(String conversationId, int offset, int limit);

	// find direct message count of each conversation
	int selectLetterCount(String conversationId);

	// find count of unread direct message
	int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

	// add message
	int insertMessage(Message message);

	// update status of message
	int updateStatus(List<Integer> ids, int status);

}
