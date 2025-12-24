package com.example.managingcaptors.DAO;

import com.example.managingcaptors.entities.Locations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Captures {
    private String id;
    private String email;
    private String name;
    private String status;
    private Locations locations;
}
