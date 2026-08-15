package com.mk.websocket.chat;

import com.mk.websocket.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService; // Injecting across packages to connect rooms with messages

    public  ChatMessage save(ChatMessage chatMessage){ // Prepares and saves an incoming message sent over WebSockets.
        // Intercepts the message and looks up the shared conversation thread ID.
        // If this is the first time these two are chatting, 'true' forces the creation of those two inverse room records!
        var chatId = chatRoomService.getChatRoomId(
                chatMessage.getSenderId(),
                chatMessage.getRecipientId(),
                true
        ).orElseThrow();

        chatMessage.setChatId(chatId); // Stamps the valid thread ID onto the message record
        return repository.save(chatMessage); // Commits the message permanently to MongoDB
    }

    public List<ChatMessage> findChatMessage(
            String senderId, String recipientId
    ){
        var chatId = chatRoomService.getChatRoomId(
                senderId,
                recipientId,
                false
        );

        // Functional programming map: If the room exists, fetch its messages from the repository.
        // If it doesn't exist, fall back safely to a blank ArrayList so React doesn't crash.
        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }
}
