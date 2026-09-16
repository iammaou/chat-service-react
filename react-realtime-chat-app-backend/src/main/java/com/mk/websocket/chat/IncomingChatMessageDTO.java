package com.mk.websocket.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncomingChatMessageDTO(
    @NotBlank String chatId,       // or omit if server derives it
    @NotBlank String senderId,
    @NotBlank String recipientId,
    @NotBlank @Size(max = 4000) String content
) {}