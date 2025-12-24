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
import java.util.Date;

@Document(collection = "MesureEachPlant")
@Data
@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "Plant",
        "User",
        "Temperature",
        "Humidity",
        "Soil_moisture",
        "Reference_evapotranspiration",
        "Evapotranspiration",
        "Crop_Coefficient",
        "Crop_Coefficient_stage",
        "Nitrogen",
        "Phosphorus",
        "Potassium",
        "Solar_Radiation_ghi",
        "Wind_Speed",
        "Days_of_planted",
        "pH",
        "datetime"
})
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MesuresEachPlant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private String id;

    @JsonProperty("Plant")
    private Plant plant;

    @JsonProperty("User")
    private User user;

    @JsonProperty("Temperature")
        private Double temperature;  // Temperature (°C)

    @JsonProperty("Humidity")
    private Double humidity;  // Humidity (%)

    @JsonProperty("Soil_moisture")
    private Double soilMoisture;  // Soil moisture (%)

    @JsonProperty("Reference_evapotranspiration")
    private Double referenceEvapotranspiration;  // Reference evapotranspiration (mm/day)

    @JsonProperty("Evapotranspiration")
    private Double evapotranspiration;  // Actual evapotranspiration (mm/day)

    @JsonProperty("Crop_Coefficient")
    private Double cropCoefficient;  // Crop coefficient (dimensionless)

    @JsonProperty("Crop_Coefficient_stage")
    private Double cropCoefficientStage;  // Crop coefficient for specific stage (dimensionless)

    @JsonProperty("Nitrogen")
    private Double nitrogen;  // Nitrogen level (ppm or percentage)

    @JsonProperty("Phosphorus")
    private Double phosphorus;  // Phosphorus level (ppm or percentage)

    @JsonProperty("Potassium")
    private Double potassium;  // Potassium level (ppm or percentage)

    @JsonProperty("Solar_Radiation_ghi")
    private Double solarRadiationGhi;  // Solar radiation (Global Horizontal Irradiance, W/m²)

    @JsonProperty("Wind_Speed")
    private Double windSpeed;  // Wind speed (m/s)

    @JsonProperty("Days_of_planted")
    private Integer daysOfPlanted;  // Number of days since planting

    @JsonProperty("pH")
    private Double pH;  // Soil pH level

    @JsonProperty("datetime")
    private Date dateTime;
}
