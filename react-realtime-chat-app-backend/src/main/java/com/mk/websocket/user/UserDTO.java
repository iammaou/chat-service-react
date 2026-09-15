package com.mk.websocket.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    @NotBlank(message = "nickName is required")
    @Size(min = 3, max = 20, message = "nickName must be 3-20 chars")
    private String nickName;

    @NotBlank(message = "fullName is required")
    @Size(max = 100)
    private String fullName;

    @NotNull (message = "status is required")
    private Status status;
}