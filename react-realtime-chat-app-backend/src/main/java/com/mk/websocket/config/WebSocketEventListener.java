package com.mk.websocket.config;

import com.mk.websocket.user.UserDTO;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.mk.websocket.user.UserService;

@Component
@AllArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        var sessionAttributes = accessor.getSessionAttributes();
        if(sessionAttributes != null){
            String nickName = (String) sessionAttributes.get("nickName");

            if(nickName != null) {
                UserDTO disconnectedUser = userService.getUser(nickName);
                userService.disconnect(disconnectedUser);

                messagingTemplate.convertAndSend("/topic/public", disconnectedUser);
            }
        }


    }
}
