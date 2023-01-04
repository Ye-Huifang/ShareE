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

	// find the latest notification of a topic
	Message selectLatestNotice(int userId, String topic);

	// find the notification count of a topic
	int selectNoticeCount(int userId, String topic);

	// find the unread notification count of a topic
	int selectNoticeUnreadCount(int userId, String topic);

	// find the list of a certain entity
	List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
