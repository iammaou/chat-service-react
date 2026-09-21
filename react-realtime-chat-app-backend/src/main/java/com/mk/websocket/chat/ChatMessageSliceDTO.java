package com.mk.websocket.chat;

import java.time.Instant;
import java.util.List;

public record ChatMessageSliceDTO(
    List<ChatMessageDTO> messages,
    Instant nextCursor
) {}