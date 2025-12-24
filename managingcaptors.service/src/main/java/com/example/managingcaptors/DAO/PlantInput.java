package com.example.managingcaptors.DAO;

import com.example.managingcaptors.entities.Plant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantInput {
    private String id;
    private Plant plant;
    private User user;
    private Double temperature;
    private Double humidity;
    private Double soilMoisture;
    private Double referenceEvapotranspiration;
    private Double evapotranspiration;
    private Double cropCoefficient;
    private Double cropCoefficientStage;
    private Double nitrogen;
    private Double phosphorus;
    private Double potassium;
    private Double solarRadiationGhi;
    private Double windSpeed;
    private Integer daysOfPlanted;
    private Double pH;
}
