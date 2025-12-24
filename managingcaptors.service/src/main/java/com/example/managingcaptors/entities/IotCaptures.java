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

@Document(collection = "IoT_Captures")
@Data
@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "user",
        "name",
        "unit",
        "timestamp",
        "status",
        "location"
})
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IotCaptures {
    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("user")
    private User user;

    @JsonProperty("name")
    private String name;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("status")
    private String status;

    @JsonProperty("location")
    private Locations location;
}
