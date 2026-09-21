package com.mk.websocket.chat;

import com.mk.websocket.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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

    private static final int PAGE_SIZE = 50;

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

    public ChatMessageSliceDTO getMessages(String senderId, String recipientId, Instant cursor){
        var chatIdOptional = chatRoomService.getChatRoomId(senderId, recipientId, false);

        if(chatIdOptional.isEmpty()){
            return new ChatMessageSliceDTO(new ArrayList<>(), null);
        }

        String chatId = chatIdOptional.get();
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        Slice<ChatMessage> messageSlice;

        if(cursor == null) {
            messageSlice = repository.findByChatIdOrderByTimeStampDesc(chatId, pageRequest);
        } else {
            messageSlice = repository.findByChatIdAndTimeStampLessThanOrderByTimeStampDesc(chatId, cursor, pageRequest);
        }

        List<ChatMessage> messages = messageSlice.getContent();
        List<ChatMessageDTO> messageDTOs = messages.stream()
            .map(mapper::toDTO)
            .toList();

        Instant nextCursor = null;
        if(!messages.isEmpty() && messageSlice.hasNext()){
            nextCursor = messages.get(messages.size() - 1).getTimeStamp();
        }

        return new ChatMessageSliceDTO(messageDTOs, nextCursor);
    }

    // public List<ChatMessage> findChatMessage(
    //         String senderId, String recipientId
    // ){
    //     var chatId = chatRoomService.getChatRoomId(
    //             senderId,
    //             recipientId,
    //             false
    //     );

    //     // Functional programming map: If the room exists, fetch its messages from the repository.
    //     // If it doesn't exist, fall back safely to a blank ArrayList so React doesn't crash.
    //     return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    // }
}
