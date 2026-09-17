package com.mk.websocket.chatroom;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // Finds or creates a unique chat ID
    public Optional<String> getChatRoomId(
            String senderId,
            String recipientId,
            boolean createNewRoomIfNotExists
    ){
        String user1 = (senderId.compareTo(recipientId) > 0) ? recipientId : senderId;
        String user2 = (senderId.compareTo(recipientId) > 0) ? senderId : recipientId;

        // 1. Check the database to see if this sender-to-recipient record exists
        return chatRoomRepository.findByUser1AndUser2(user1, user2)
                .map(room -> room.getChatId()) // 2. Functional Mapping: If found, extract just the String 'chatId' from the ChatRoom object
                .or(() -> { // 3. Lazy Evaluation: If the Optional is empty (no record found), run this backup block
                    if( createNewRoomIfNotExists ){
                        // Create a brand-new pair of records and get the generated ID
                        var chatId = createChatId(user1, user2);
                        return Optional.of(chatId);
                    }
                    return Optional.empty(); // If we weren't allowed to create a new room, return empty
                });
    }

    private String createChatId(String user1, String user2) {
        // Generates a predictable string format (e.g., "alice_bob")
        var chatId = String.format("%s_%s", user1, user2);

        ChatRoom chatRoom = ChatRoom.builder()
                .chatId(chatId)
                .user1(user1)
                .user2(user2)
                .build();

        try{
            chatRoomRepository.save(chatRoom);
        } catch (DuplicateKeyException e){
            log.debug("Chat room {} already exists, reusing.", chatId);
        }

        return chatId;
    }
}
