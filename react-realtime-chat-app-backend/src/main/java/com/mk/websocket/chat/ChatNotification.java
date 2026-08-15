package com.mk.websocket.chat;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

// Notice there is NO @Document annotation here! This is a simple Data Transfer Object (DTO).
// We don't save notifications to MongoDB. We only use this class as a lightweight web packet
// to quickly alert a user's React frontend: "Hey, you've got a message!"
public class ChatNotification {

    private String id;
    private String senderId;
    private String recipientId;
    private String content;
}
