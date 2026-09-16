package com.mk.websocket.chat;

import com.mk.websocket.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService; // Injecting across packages to connect rooms with messages
    private final ChatMessageMapper mapper;

    public ChatMessageDTO save(IncomingChatMessageDTO IncomingChatMessageDTO){ // Prepares and saves an incoming message sent over WebSockets.
        // Intercepts the message and looks up the shared conversation thread ID.
        // If this is the first time these two are chatting, 'true' forces the creation of those two inverse room records!
        var chatId = chatRoomService.getChatRoomId(
                IncomingChatMessageDTO.senderId(),
                IncomingChatMessageDTO.recipientId(),
                true
        ).orElseThrow();
        
        ChatMessage chatMessage = ChatMessage.builder()
            .chatId(chatId)
            .senderId(IncomingChatMessageDTO.senderId())
            .recipientId(IncomingChatMessageDTO.recipientId())
            .content(IncomingChatMessageDTO.content())
            .timeStamp(Instant.now())
            .build();

        repository.save(chatMessage); // Commits the message permanently to MongoDB
        
        return mapper.toDTO(chatMessage);
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
