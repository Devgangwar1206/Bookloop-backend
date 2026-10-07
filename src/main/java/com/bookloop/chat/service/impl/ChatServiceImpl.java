package com.bookloop.chat.service.impl;

import com.bookloop.chat.dto.*;
import com.bookloop.chat.model.ChatMessage;
import com.bookloop.chat.model.Conversation;
import com.bookloop.chat.repository.ChatMessageRepository;
import com.bookloop.chat.repository.ConversationRepository;
import com.bookloop.chat.service.ChatService;
import com.bookloop.user.entity.User;
import com.bookloop.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final Optional<SimpMessagingTemplate> messagingTemplate;

    @Autowired
    public ChatServiceImpl(ConversationRepository conversationRepository,
                           ChatMessageRepository chatMessageRepository,
                           UserRepository userRepository,
                           @Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = Optional.ofNullable(messagingTemplate);
    }

    private User resolveUser(String identifier) {
        if (identifier == null || identifier.isBlank() || "u-me".equals(identifier)) {
            return null;
        }
        try {
            Long uid = Long.parseLong(identifier);
            Optional<User> u = userRepository.findById(uid);
            if (u.isPresent()) return u.get();
        } catch (NumberFormatException ignored) {}

        return userRepository.findByEmail(identifier).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(String userId) {
        if (userId == null || userId.isBlank() || "u-me".equals(userId)) {
            return new ArrayList<>();
        }

        User resolved = resolveUser(userId);
        String idStr = resolved != null ? resolved.getId().toString() : userId;
        String emailStr = resolved != null ? resolved.getEmail() : userId;

        List<Conversation> convs = conversationRepository.findByParticipant(idStr, emailStr);

        return convs.stream().map(c -> toDtoWithoutMessages(c, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversationById(String chatId) {
        return getConversationById(chatId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversationById(String chatId, String userId) {
        Optional<Conversation> convOpt = conversationRepository.findById(chatId);
        if (convOpt.isEmpty()) {
            return null;
        }
        Conversation conv = convOpt.get();
        ConversationDto dto = toDtoWithoutMessages(conv, userId);
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(chatId);
        dto.setMessages(messages.stream().map(this::toMessageDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(String chatId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(chatId);
        return messages.stream().map(this::toMessageDto).collect(Collectors.toList());
    }

    @Override
    public ChatMessageDto sendMessage(String chatId, SendMessageRequest request) {
        String msgId = "m-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5);
        String senderId = (request.getSenderId() != null && !request.getSenderId().isBlank()) 
                ? request.getSenderId() : "u-me";
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        ChatMessage message = new ChatMessage(
                msgId,
                chatId,
                senderId,
                request.getText(),
                formattedTime,
                request.getMessageType()
        );

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update conversation summary
        Optional<Conversation> convOpt = conversationRepository.findById(chatId);
        if (convOpt.isPresent()) {
            Conversation conv = convOpt.get();
            conv.setLastMessage(request.getText());
            conv.setLastMessageTime("Just now");
            conv.setUpdatedAt(LocalDateTime.now());
            
            // Increment unread count for the other party
            conv.setUnreadCount((conv.getUnreadCount() == null ? 0 : conv.getUnreadCount()) + 1);
            conversationRepository.save(conv);
        }

        ChatMessageDto dto = toMessageDto(savedMessage);

        // Real-time broadcast via WebSocket / STOMP across all matching destinations
        messagingTemplate.ifPresent(template -> {
            try {
                // Topic subscribed by frontend chat component
                template.convertAndSend("/topic/chat/" + chatId, dto);
                template.convertAndSend("/topic/conversations/" + chatId, dto);
                template.convertAndSend("/topic/conversation." + chatId, dto);
                // Global notifications topic for desktop alerts and header badges
                template.convertAndSend("/topic/notifications", dto);
            } catch (Exception e) {
                // Ignore if STOMP broker is buffering
            }
        });

        return dto;
    }

    @Override
    public ConversationDto startOrGetChatWithSeller(StartChatRequest request) {
        String bookId = request.getBookId();
        String sellerId = (request.getSeller() != null) ? request.getSeller().getId() : null;
        String currentUserId = (request.getCurrentUserId() != null && !request.getCurrentUserId().isBlank())
                ? request.getCurrentUserId() : "u-me";

        // Strictly check if an existing 1-to-1 conversation exists between THESE TWO SPECIFIC USERS
        if (bookId != null && sellerId != null && !currentUserId.equals("u-me")) {
            List<Conversation> existing = conversationRepository.findExistingChatsBetweenUsers(bookId, currentUserId, sellerId);
            if (!existing.isEmpty()) {
                Conversation conv = existing.get(0);
                conv.setUnreadCount(0);
                conversationRepository.save(conv);
                return toDtoWithoutMessages(conv, currentUserId);
            }
        }

        // Create a completely new isolated WhatsApp-like chat for this user pair
        String chatId = "c-" + System.currentTimeMillis();
        Conversation conv = new Conversation();
        conv.setId(chatId);
        conv.setBookId(request.getBookId());
        conv.setBookTitle(request.getBookTitle() != null ? request.getBookTitle() : "Book Exchange");
        conv.setBookPrice(request.getBookPrice() != null ? request.getBookPrice() : 0.0);
        conv.setBookImage(request.getBookImage() != null ? request.getBookImage() : "");
        conv.setCurrentUserId(currentUserId);

        // Populate Buyer info
        if (request.getBuyer() != null) {
            conv.setCurrentUserName(request.getBuyer().getName());
            conv.setCurrentUserAvatar(request.getBuyer().getAvatar());
            conv.setCurrentUserVerified(request.getBuyer().getVerified());
            conv.setCurrentUserLocation(request.getBuyer().getLocation());
        } else {
            populateBuyerInfoFromDb(conv, currentUserId);
        }

        // Populate Seller info
        if (request.getSeller() != null) {
            conv.setOtherUserId(request.getSeller().getId());
            conv.setOtherUserName(request.getSeller().getName());
            conv.setOtherUserAvatar(request.getSeller().getAvatar());
            conv.setOtherUserVerified(request.getSeller().getVerified());
            conv.setOtherUserLocation(request.getSeller().getLocation());
        } else {
            conv.setOtherUserId(sellerId != null ? sellerId : "s-" + System.currentTimeMillis());
            conv.setOtherUserName("Seller");
            conv.setOtherUserVerified(true);
        }

        String initialText = (request.getInitialMessage() != null && !request.getInitialMessage().isBlank())
                ? request.getInitialMessage()
                : "Hi, is \"" + conv.getBookTitle() + "\" still available?";

        conv.setLastMessage(initialText);
        conv.setLastMessageTime("Just now");
        conv.setUnreadCount(0);
        Conversation savedConv = conversationRepository.save(conv);

        // Save initial first message
        ChatMessage firstMsg = new ChatMessage(
                "m-" + System.currentTimeMillis(),
                chatId,
                currentUserId,
                initialText,
                "Just now",
                "TEXT"
        );
        chatMessageRepository.save(firstMsg);

        ConversationDto dto = toDtoWithoutMessages(savedConv, currentUserId);
        dto.getMessages().add(toMessageDto(firstMsg));
        return dto;
    }

    private void populateBuyerInfoFromDb(Conversation conv, String userId) {
        User u = resolveUser(userId);
        if (u != null) {
            conv.setCurrentUserName(u.getName());
            conv.setCurrentUserAvatar(u.getAvatar());
            conv.setCurrentUserLocation(u.getCity() != null ? u.getCity() : "Local");
            conv.setCurrentUserVerified(u.isVerified());
        } else {
            conv.setCurrentUserName("Buyer");
        }
    }

    @Override
    public long markAsRead(String chatId, String userId) {
        String currentUserId = (userId != null && !userId.isBlank()) ? userId : "u-me";
        chatMessageRepository.markMessagesAsRead(chatId, currentUserId);

        Optional<Conversation> convOpt = conversationRepository.findById(chatId);
        if (convOpt.isPresent()) {
            Conversation conv = convOpt.get();
            conv.setUnreadCount(0);
            conversationRepository.save(conv);
        }

        return conversationRepository.getTotalUnreadCount(currentUserId);
    }

    @Override
    public long markAllAsRead(String userId) {
        String currentUserId = (userId != null && !userId.isBlank()) ? userId : "u-me";
        chatMessageRepository.markAllMessagesAsReadForUser(currentUserId);

        List<Conversation> all = conversationRepository.findAll();
        for (Conversation c : all) {
            c.setUnreadCount(0);
        }
        conversationRepository.saveAll(all);

        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        String currentUserId = (userId != null && !userId.isBlank()) ? userId : "u-me";
        return conversationRepository.getTotalUnreadCount(currentUserId);
    }

    /**
     * Converts a Conversation to ConversationDto tailored to the viewing user (WhatsApp style):
     * - If viewer is the Seller, the counterparty shown is the Buyer.
     * - If viewer is the Buyer, the counterparty shown is the Seller.
     */
    private ConversationDto toDtoWithoutMessages(Conversation conv, String viewerId) {
        User resolvedViewer = resolveUser(viewerId);
        String vId = resolvedViewer != null ? resolvedViewer.getId().toString() : viewerId;
        String vEmail = resolvedViewer != null ? resolvedViewer.getEmail() : "";

        // Check if the viewer is the seller (otherUser)
        boolean isViewerSeller = false;
        if (vId != null && !vId.isBlank()) {
            if (vId.equalsIgnoreCase(conv.getOtherUserId()) || 
                (vEmail != null && vEmail.equalsIgnoreCase(conv.getOtherUserId()))) {
                isViewerSeller = true;
            }
        }

        UserSummaryDto counterparty;
        UserSummaryDto buyerSummary = new UserSummaryDto(
                conv.getCurrentUserId(),
                (conv.getCurrentUserName() != null && !conv.getCurrentUserName().isBlank()) ? conv.getCurrentUserName() : "Buyer",
                (conv.getCurrentUserAvatar() != null && !conv.getCurrentUserAvatar().isBlank()) ? conv.getCurrentUserAvatar() : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80",
                conv.getCurrentUserVerified() != null ? conv.getCurrentUserVerified() : false,
                conv.getCurrentUserLocation() != null ? conv.getCurrentUserLocation() : "Local"
        );

        UserSummaryDto sellerSummary = new UserSummaryDto(
                conv.getOtherUserId(),
                (conv.getOtherUserName() != null && !conv.getOtherUserName().isBlank()) ? conv.getOtherUserName() : "Seller",
                (conv.getOtherUserAvatar() != null && !conv.getOtherUserAvatar().isBlank()) ? conv.getOtherUserAvatar() : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
                conv.getOtherUserVerified() != null ? conv.getOtherUserVerified() : false,
                conv.getOtherUserLocation() != null ? conv.getOtherUserLocation() : "Local"
        );

        if (isViewerSeller) {
            // Seller sees the Buyer
            counterparty = buyerSummary;
        } else {
            // Buyer sees the Seller
            counterparty = sellerSummary;
        }

        ConversationDto dto = new ConversationDto(
                conv.getId(),
                conv.getBookId(),
                conv.getBookTitle(),
                conv.getBookPrice(),
                conv.getBookImage(),
                counterparty,
                conv.getLastMessage(),
                conv.getLastMessageTime(),
                conv.getUnreadCount() != null ? conv.getUnreadCount() : 0
        );
        dto.setBuyer(buyerSummary);
        dto.setCurrentUserId(conv.getCurrentUserId());

        return dto;
    }

    private ChatMessageDto toMessageDto(ChatMessage msg) {
        return new ChatMessageDto(
                msg.getId(),
                msg.getConversationId(),
                msg.getSenderId(),
                msg.getText(),
                msg.getTime(),
                msg.getMessageType(),
                msg.isRead(),
                msg.getCreatedAt()
        );
    }
}
