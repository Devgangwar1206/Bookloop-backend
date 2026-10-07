package com.bookloop.chat.repository;

import com.bookloop.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findAllByOrderByUpdatedAtDesc();

    List<Conversation> findByCurrentUserIdOrderByUpdatedAtDesc(String currentUserId);

    Optional<Conversation> findByBookIdAndOtherUserId(String bookId, String otherUserId);

    @Query("SELECT c FROM Conversation c WHERE c.currentUserId = :userId OR c.otherUserId = :userId OR c.currentUserId = :email OR c.otherUserId = :email ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipant(@Param("userId") String userId, @Param("email") String email);

    @Query("SELECT c FROM Conversation c WHERE c.currentUserId = :userId OR c.otherUserId = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantUserIdOrderByUpdatedAtDesc(@Param("userId") String userId);

    @Query("SELECT c FROM Conversation c WHERE ((c.currentUserId = :buyerId AND c.otherUserId = :sellerId) OR (c.currentUserId = :sellerId AND c.otherUserId = :buyerId)) AND (:bookId IS NULL OR c.bookId = :bookId)")
    List<Conversation> findExistingChatsBetweenUsers(@Param("bookId") String bookId, @Param("buyerId") String buyerId, @Param("sellerId") String sellerId);

    @Query("SELECT c FROM Conversation c WHERE c.bookId = :bookId AND (c.otherUserId = :otherUserId OR c.currentUserId = :otherUserId)")
    List<Conversation> findExistingChats(@Param("bookId") String bookId, @Param("otherUserId") String otherUserId);

    @Query("SELECT COALESCE(SUM(c.unreadCount), 0) FROM Conversation c WHERE c.currentUserId = :userId OR c.otherUserId = :userId")
    long getTotalUnreadCount(@Param("userId") String userId);
}
