package com.mk.websocket.chat;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component 
public class ChatMessageMapper {
    public ChatMessage toChatMessage(IncomingChatMessageDTO IncomingChatMessageDTO){
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setChatId(IncomingChatMessageDTO.chatId());
        chatMessage.setSenderId(IncomingChatMessageDTO.senderId());
        chatMessage.setRecipientId(IncomingChatMessageDTO.recipientId());
        chatMessage.setContent(IncomingChatMessageDTO.content());
        chatMessage.setTimeStamp(Instant.now());

        return chatMessage;
    }

    public ChatMessageDTO toDTO(ChatMessage chatMessage){
        return new ChatMessageDTO(
            chatMessage.getId(), 
            chatMessage.getChatId(), 
            chatMessage.getSenderId(), 
            chatMessage.getRecipientId(), 
            chatMessage.getContent(), 
            chatMessage.getTimeStamp()
        );
    }
}
