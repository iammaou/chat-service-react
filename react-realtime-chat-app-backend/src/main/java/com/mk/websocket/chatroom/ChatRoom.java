package com.mk.websocket.chatroom;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder // Implements the Builder Pattern, allowing code like: ChatRoom.builder().id("1").build()
@Document // Defines this as a collection entity inside MongoDB
@CompoundIndex(def = "{'user1': 1, 'user2': 1}", unique = true)
public class ChatRoom {
    @Id
    private String id;
    private String chatId; // The shared identifier for the conversation thread (e.g., "alice_bob")
    private String user1; 
    private String user2; 
}
