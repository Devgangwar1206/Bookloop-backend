package com.bookloop.chat.service;

 
import com.bookloop.chat.dto.*;

import java.util.List;

public interface ChatService {

    List<ConversationDto> getConversations(String userId);

    ConversationDto getConversationById(String chatId);

    ConversationDto getConversationById(String chatId, String userId);

    List<ChatMessageDto> getMessages(String chatId);

    ChatMessageDto sendMessage(String chatId, SendMessageRequest request);

    ConversationDto startOrGetChatWithSeller(StartChatRequest request);

    long markAsRead(String chatId, String userId);

    long markAllAsRead(String userId);

    long getUnreadCount(String userId);
}
