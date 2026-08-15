package com.mk.websocket.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    // Spring's internal engine companion for programmatic WebSocket routing.
    // Gives you complete manual control over who receives packets, moving past generic @SendTo broadcast annotations.
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    // WEBSOCKET: Processes an active text message sent from React to "/app/chat"
    @MessageMapping("/chat")
    public void processMessage(
            @Payload ChatMessage chatMessage
    ){
        // Step 1: Pass the message to the service layer to bind its room ID and write it to MongoDB
        ChatMessage savedMessage = chatMessageService.save(chatMessage);

        // Step 2: The Target Delivery Magic
        // convertAndSendToUser intercepts the destination and handles it securely behind the scenes.
        // It appends the recipient's personal username session dynamically to the path.
        messagingTemplate.convertAndSendToUser(
                savedMessage.getRecipientId(),
                "/queue/messages",
                ChatNotification.builder()
                        .id(savedMessage.getId())
                        .senderId(savedMessage.getSenderId())
                        .recipientId(savedMessage.getRecipientId())
                        .content(savedMessage.getContent())
                        .build()
        );
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable("senderId") String senderId,
            @PathVariable("recipientId") String recipientId
    ){
        return ResponseEntity.ok(chatMessageService.findChatMessage(senderId, recipientId));
    }
}
