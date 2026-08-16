package com.mk.websocket.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Extending MongoRepository gives this interface built-in CRUD capabilities (save, findById, delete) automatically
public interface UserRepository extends MongoRepository<User, String> {

    // Dynamic Query Derivation: Spring parses this method name automatically.
    // It generates a real MongoDB query under the hood: { status: "ONLINE" } without you writing any database code.
    List<User> findAllByStatus(Status status);

    User findByNickName(String nickName);
}
