package com.mk.websocket.user;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserWebSocketController {

    private final UserService userService;

    @MessageMapping("/user.addUser")
    @SendTo("/topic/public")
    public UserDTO addUser(@Payload UserDTO userDTO) {
        return userService.saveUser(userDTO);
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/topic/public")
    public UserDTO disconnectUser(@Payload UserDTO userDTO) {
        userService.disconnect(userDTO);
        return userDTO;
    }
}