package com.example.managingcaptors.DAO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long id;

    private String firstname;

    private String lastname;

    private String email;

    private String tel;

    private String password;

    private String localisation_id;

    private String captors_id;

    private String resetToken;

    private Long resetTokenExpiry;

    public User(Long id, String firstname, String lastname, String email, String tel, String password, String localisation_id, String captors_id, String resetToken) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.tel = tel;
        this.password = password;
        this.localisation_id = localisation_id;
        this.captors_id = captors_id;
        this.resetToken = resetToken;
    }
}
