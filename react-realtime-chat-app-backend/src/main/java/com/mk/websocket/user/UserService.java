package com.mk.websocket.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDTO saveUser(UserDTO userDTO){
        userDTO.setStatus(Status.ONLINE);
        repository.save(mapper.toUser(userDTO));

        return userDTO;
    }

    public UserDTO getUser(String nickName){
        User user = repository.findByNickName(nickName);

        return mapper.toDTO(user);
    }

    public void disconnect(UserDTO userDTO){
        var storedUser = repository.findById(mapper.toUser(userDTO).getNickName())
                .orElse(null);
        if(storedUser != null){
            storedUser.setStatus(Status.OFFLINE);
            repository.save(storedUser);
        }
    }

    public List<UserDTO> findConnectedUsers(){
        List<User> users = repository.findAllByStatus(Status.ONLINE);

        return users.stream()
            .map(mapper::toDTO)
            .toList();
    }
}
