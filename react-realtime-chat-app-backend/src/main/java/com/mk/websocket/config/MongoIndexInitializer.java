package com.mk.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @Bean
    public ApplicationRunner ensureChatRoomIndexes() {
        return args -> {
            IndexOperations indexOps = mongoTemplate.indexOps("chat_rooms");

            Index compoundIndex = new Index()
                    .on("user1", Sort.Direction.ASC)
                    .on("user2", Sort.Direction.ASC)
                    .unique()
                    .named("user1_1_user2_1");

            try {
                indexOps.createIndex(compoundIndex);
                log.info("✅ Unique compound index 'user1_1_user2_1' ensured on chat_rooms.");
            } catch (Exception e) {
                log.error("❌ Failed to create unique index on chat_rooms: {}", e.getMessage());
            }
        };
    }
}