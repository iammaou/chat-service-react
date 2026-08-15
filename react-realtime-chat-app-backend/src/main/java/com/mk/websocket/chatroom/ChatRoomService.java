package com.mk.websocket.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // Finds or creates a unique chat ID for a conversation between two users.
    public Optional<String> getChatRoomId(
            String senderId,
            String recipientId,
            boolean createNewRoomIfNotExists
    ){
        // 1. Check the database to see if this sender-to-recipient record exists
        return chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(ChatRoom::getChatId) // 2. Functional Mapping: If found, extract just the String 'chatId' from the ChatRoom object
                .or(() -> { // 3. Lazy Evaluation: If the Optional is empty (no record found), run this backup block
                    if( createNewRoomIfNotExists ){
                        // Create a brand-new pair of records and get the generated ID
                        var chatId = createChatId(senderId, recipientId);
                        return Optional.of(chatId);
                    }
                    return Optional.empty(); // If we weren't allowed to create a new room, return empty
                });
    }

    // Internal Helper Method: Generates a shared Chat ID and builds BOTH directions in the database.
    private String createChatId(String senderId, String recipientId) {
        // Generates a predictable string format (e.g., "alice_bob")
        var chatId = String.format("%s_%s", senderId, recipientId);

        // PERSPECTIVE 1: From the sender's viewpoint
        ChatRoom senderRecipient = ChatRoom.builder()
                .chatId(chatId)
                .senderId(senderId)
                .recipientId(recipientId)
                .build();

        // PERSPECTIVE 2: From the recipient's viewpoint (The Inverse)
        // This is crucial so that when the recipient searches for their chats, they can find it instantly too!
        ChatRoom recipientSender = ChatRoom.builder()
                .chatId(chatId)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }
}
