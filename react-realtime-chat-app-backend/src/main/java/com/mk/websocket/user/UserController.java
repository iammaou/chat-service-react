package com.mk.websocket.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    // Listens for WebSocket packets sent from React to the routing destination "/app/user.addUser"
    @MessageMapping("/user.addUser")
    // Megaphone effect: Whatever this method RETURNS is instantly broadcasted to everyone listening to "/user/public"
    @SendTo("/user/public")
    public User addUser(@Payload User user){ // @Payload extracts the incoming JSON packet and maps it to a Java User object v
        userService.saveUser(user);
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/public")
    public User disconnectUser(
            @Payload User user
    ){
        userService.disconnect(user);
        return user;
    }
    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers(){
        return ResponseEntity.ok(userService.findConnectedUsers());
    }
}
