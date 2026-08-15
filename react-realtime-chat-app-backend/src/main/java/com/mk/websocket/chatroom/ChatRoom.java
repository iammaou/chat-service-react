package com.mk.websocket.chatroom;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder // Implements the Builder Pattern, allowing code like: ChatRoom.builder().id("1").build()
@Document // Defines this as a collection entity inside MongoDB
public class ChatRoom {
    @Id
    private String id;
    private String chatId; // The shared identifier for the conversation thread (e.g., "alice_bob")
    private String senderId; // The user who initiates or views this specific record perspective
    private String recipientId; // The user on the receiving end of the chat thread
}
