package com.example.usersmanage.repositories;

import com.example.usersmanage.entities.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Users Repository Tests")
class UsersSqlRepoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsersSqlRepo usersSqlRepo;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john.doe@example.com");
        user.setTel("123456789");
        user.setPassword("password123");
    }

    @Test
    @DisplayName("Exists By Email - Should return true when email exists")
    void testExistsByEmail_Exists() {
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = usersSqlRepo.existsByEmail("john.doe@example.com");
        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists By Email - Should return false when email does not exist")
    void testExistsByEmail_NotExists() {
        boolean exists = usersSqlRepo.existsByEmail("notfound@example.com");
        assertFalse(exists);
    }

    @Test
    @DisplayName("Find First By Email - Should return user when email matches")
    void testFindFirstByEmail_Found() {
        entityManager.persist(user);
        entityManager.flush();

        Optional<Users> found = usersSqlRepo.findFirstByEmail("john.doe@example.com");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstname());
    }

    @Test
    @DisplayName("Find First By Email - Should return empty when no match")
    void testFindFirstByEmail_NotFound() {
        Optional<Users> found = usersSqlRepo.findFirstByEmail("notfound@example.com");
        assertFalse(found.isPresent());
    }
}
