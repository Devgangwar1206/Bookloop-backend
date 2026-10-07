package com.bookloop.chat.repository;

 
import com.bookloop.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    long countByConversationIdAndSenderIdNotAndIsReadFalse(String conversationId, String senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.senderId <> :userId")
    int markMessagesAsRead(@Param("conversationId") String conversationId, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.senderId <> :userId")
    int markAllMessagesAsReadForUser(@Param("userId") String userId);

    void deleteByConversationId(String conversationId);
}
