package com.mk.websocket.user;

import org.springframework.stereotype.Component;

@Component 
public class UserMapper {
    public User toUser(UserDTO userDTO){
        User user = new User();

        user.setNickName(userDTO.getNickName());
        user.setFullName(userDTO.getFullName());
        user.setStatus(userDTO.getStatus());

        return user;
    }

    public UserDTO toDTO(User user){
        UserDTO userDTO = new UserDTO();

        userDTO.setNickName(user.getNickName());
        userDTO.setFullName(user.getFullName());
        userDTO.setStatus(user.getStatus());

        return userDTO;
    }
}
