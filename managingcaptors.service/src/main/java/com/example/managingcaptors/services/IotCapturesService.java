package com.example.managingcaptors.services;

import com.example.managingcaptors.DAO.Captures;
import com.example.managingcaptors.DAO.CaptursInputs;
import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.IotCaptures;
import com.example.managingcaptors.entities.Locations;
import com.example.managingcaptors.repo.IotCaptureRepo;
import com.example.managingcaptors.repo.LocationsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class IotCapturesService {

    @Autowired
    private IotCaptureRepo iotCaptureRepo;

    @Autowired
    private LocationsRepo locationsRepo;

    @Autowired
    private TokenService TokenService;

    public List<?> CreateIotCaptures(CaptursInputs inputs, String token) {
        List <IotCaptures> listIotCaptures = new ArrayList<>();

        System.out.println("get Token " + token);
        System.out.println("get token server " + TokenService.CallTheToken());

        if (!TokenService.VerifyTheToken(token, inputs.getCaptures().getUser().getEmail())) return null;

        Locations newLocations = new Locations();
        newLocations.setLongitude(inputs.getCaptures().getLocation().getLongitude());
        newLocations.setLatitude(inputs.getCaptures().getLocation().getLatitude());
        locationsRepo.save(newLocations);

        IotCaptures newIotCaptures = new IotCaptures();
        newIotCaptures.setName(inputs.getCaptures().getName());
        newIotCaptures.setStatus(inputs.getCaptures().getStatus());
        newIotCaptures.setTimestamp(inputs.getCaptures().getTimestamp());
        newIotCaptures.setUnit(inputs.getCaptures().getUnit());
        newIotCaptures.setLocation(newLocations);
        newIotCaptures.setUser(inputs.getCaptures().getUser());
        iotCaptureRepo.save(newIotCaptures);


        listIotCaptures = iotCaptureRepo.findByUser(inputs.getCaptures().getUser());
        return listIotCaptures;
    }

    public List<?> getAllCaptors(String token, User user) {
        if (!TokenService.VerifyTheToken(token, user.getEmail())) return null;

        return iotCaptureRepo.findByUser(user);
    }

    public List<?> ActivateCaptures (String token, User user, String IdCaprture) {
        List <IotCaptures> listIotCaptures = new ArrayList<>();

        if (!TokenService.VerifyTheToken(token, user.getEmail())) return null;

        IotCaptures isExist = iotCaptureRepo.findById(IdCaprture).get();
        if (isExist == null) return null;

        if (Objects.equals(isExist.getStatus(), "Active")) {
            isExist.setStatus("Deactivate");
            iotCaptureRepo.save(isExist);

            listIotCaptures = iotCaptureRepo.findByUser(user);
            return listIotCaptures;
        }

        isExist.setStatus("Active");
        iotCaptureRepo.save(isExist);

        listIotCaptures = iotCaptureRepo.findByUser(user);
        return listIotCaptures;
    }

    public IotCaptures UpdateCaptures (String token, Captures iot) {
        if (!TokenService.VerifyTheToken(token, iot.getEmail())) return null;

        IotCaptures isExist = iotCaptureRepo.findById(iot.getId()).get();
        if (isExist == null) return null;

        Locations locations = locationsRepo.getLocationsById(iot.getLocations().getId());
        if (locations == null) return null;

        locations.setLatitude(iot.getLocations().getLatitude());
        locations.setLongitude(iot.getLocations().getLongitude());
        locationsRepo.save(locations);

        isExist.setName(iot.getName());
        isExist.setStatus(iot.getStatus());
        isExist.setLocation(locations);
        iotCaptureRepo.save(isExist);

        return isExist;
    }

    public List<?> deleteIot(String token, User user, String IdCaprature) {
        System.out.println("IotCaptur: " + IdCaprature);

        if (user == null) return Collections.emptyList();
        if (!TokenService.VerifyTheToken(token, user.getEmail())) return Collections.emptyList();

        IotCaptures isExist = iotCaptureRepo.findById(IdCaprature).get();

        System.out.println("User: " + isExist);

        locationsRepo.delete(isExist.getLocation());
        iotCaptureRepo.delete(isExist);

        return iotCaptureRepo.findByUser(user);
    }

}
