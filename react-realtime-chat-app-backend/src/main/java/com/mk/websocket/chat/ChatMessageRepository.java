package com.mk.websocket.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 1. Initial Load
    Slice<ChatMessage> findByChatIdOrderByTimeStampDesc(String chatId, Pageable pageable);

    // 2. Scroll Load
    Slice<ChatMessage> findByChatIdAndTimeStampLessThanOrderByTimeStampDesc(
            String chatId, 
            Instant cursorTimestamp, 
            Pageable pageable
    );
}