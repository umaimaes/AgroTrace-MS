package com.example.usersmanage.repositories;

import com.example.usersmanage.entities.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersSqlRepo extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email);

    Optional<Users> findFirstByEmail(String email);

    Users findByEmail(String email);
}
