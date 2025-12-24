package com.example.usersmanage.DOA;

import com.example.usersmanage.entities.Users;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
    private Users user;
    private String token;

    public UserInfo(Users user, String token) {
        this.user = user;
        this.token = token;
    }
}
