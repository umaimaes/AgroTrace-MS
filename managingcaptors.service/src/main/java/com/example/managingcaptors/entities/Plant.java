package com.example.managingcaptors.entities;


import com.example.managingcaptors.DAO.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Document(collection = "Plant")
@Data
@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "user",
        "name",
        "location"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private String id;

    @JsonProperty("user")
    private User user;

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private Locations location;
}
