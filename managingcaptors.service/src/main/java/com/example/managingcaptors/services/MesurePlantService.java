package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.PlantInput;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.DAO.plantInfo;
import com.example.managingcaptors.entities.MesuresEachPlant;
import com.example.managingcaptors.entities.Plant;
import com.example.managingcaptors.repo.IotCaptureRepo;
import com.example.managingcaptors.repo.LocationsRepo;
import com.example.managingcaptors.repo.MesuresEachPlantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MesurePlantService {
    @Autowired
    private MesuresEachPlantRepo mesuresEachPlantRepo;

    @Autowired
    private IotCaptureRepo iotCaptureRepo;

    @Autowired
    private LocationsRepo locationsRepo;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    public List<?> InsertIotCapturesMesurePlant(PlantInput p, String token) {
        if (tokenService.VerifyTheToken(token, p.getUser().getEmail()))
            return null;

        MesuresEachPlant newP = new MesuresEachPlant();

        newP.setPlant(p.getPlant());
        newP.setUser(p.getUser());

        newP.setTemperature(p.getTemperature());
        newP.setHumidity(p.getHumidity());
        newP.setSoilMoisture(p.getSoilMoisture());
        newP.setReferenceEvapotranspiration(p.getReferenceEvapotranspiration());
        newP.setEvapotranspiration(p.getEvapotranspiration());
        newP.setCropCoefficient(p.getCropCoefficient());
        newP.setCropCoefficientStage(p.getCropCoefficientStage());
        newP.setNitrogen(p.getNitrogen());
        newP.setPhosphorus(p.getPhosphorus());
        newP.setPotassium(p.getPotassium());
        newP.setSolarRadiationGhi(p.getSolarRadiationGhi());
        newP.setWindSpeed(p.getWindSpeed());
        newP.setDaysOfPlanted(p.getDaysOfPlanted());
        newP.setPH(p.getPH());

        mesuresEachPlantRepo.save(newP);

        return mesuresEachPlantRepo.findByUserAndPlant(p.getUser(), p.getPlant());
    }

    public List<?> getIotCaptureMesurePlant(plantInfo p, String token) {
        if (tokenService.VerifyTheToken(token, p.getUser().getEmail()))
            return null;

        return mesuresEachPlantRepo.findByUserAndPlant(p.getUser(), p.getPlant());
    }

    public Object getRecommendation(com.example.managingcaptors.entities.MesuresEachPlant p) {
        String url = "http://localhost:8000/recommend";

        java.util.Map<String, Object> body = new java.util.HashMap<>();

        // Metadata for AI Service -> Notify Server flow
        body.put("plant_id", p.getPlant().getId());
        body.put("plant_name", p.getPlant().getName());
        body.put("user_email", p.getUser().getEmail());

        // Climatic Data
        body.put("stage", p.getCropCoefficientStage());
        body.put("temperature", p.getTemperature());
        body.put("humidity", p.getHumidity());
        body.put("soil_moisture", p.getSoilMoisture());
        body.put("nitrogen", p.getNitrogen());
        body.put("phosphorus", p.getPhosphorus());
        body.put("potassium", p.getPotassium());
        body.put("ph", p.getPH());
        body.put("solar_radiation", p.getSolarRadiationGhi());
        body.put("wind_speed", p.getWindSpeed());

        // Send to AI Service (which will forward to Notify Server)
        return restTemplate.postForObject(url, body, Object.class);
    }

}
