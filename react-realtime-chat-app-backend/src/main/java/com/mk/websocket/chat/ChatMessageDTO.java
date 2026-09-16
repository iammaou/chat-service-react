package com.mk.websocket.chat;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageDTO(
    String id,
    @NotBlank 
    String chatId,
    @NotBlank 
    String senderId,
    @NotBlank 
    String recipientId,
    @NotBlank 
    @Size (min = 1, max = 5000)
    String content,
    Instant timeStamp
) {
    
}
