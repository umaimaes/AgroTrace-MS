package com.example.usersmanage.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "firstname",
        "lastname",
        "email",
        "tel",
        "localisation_id",
        "captors_id"
})
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("email")
    @Column(unique = true)
    private String email;

    @JsonProperty("tel")
    @Column(unique = true)
    private String tel;

    @JsonProperty("password")
    private String password;

    @JsonProperty("localisation_id")
    private String localisation_id;

    @JsonProperty("captors_id")
    private String captors_id;

    // Password reset fields
    private String resetToken;
    private Long resetTokenExpiry;
}
