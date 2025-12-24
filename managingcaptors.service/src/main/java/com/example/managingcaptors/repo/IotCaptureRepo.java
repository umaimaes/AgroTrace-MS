package com.example.managingcaptors.repo;


import com.example.managingcaptors.DAO.User;
import com.example.managingcaptors.entities.IotCaptures;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface IotCaptureRepo extends MongoRepository<IotCaptures, String> {

    Optional<IotCaptures> findById(String Id);

    List<IotCaptures> findByUser(User user);
}
