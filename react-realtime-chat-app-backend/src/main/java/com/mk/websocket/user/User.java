package com.mk.websocket.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document // Tells Spring Data MongoDB that this class represents a document (record) in a database collection
public class User {
    @Id
    private String nickName;
    private String fullName;
    private Status status;
}
