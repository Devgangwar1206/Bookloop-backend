package com.bookloop.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotificationDto {
    private String id;
    private String type;
    private String title;
    private String message;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String targetUserId;
    private String targetUserEmail;
    private String actionUrl;
    private String time;
    private Long timestamp;
}
