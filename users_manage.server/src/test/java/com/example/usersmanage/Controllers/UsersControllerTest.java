package com.example.usersmanage.Controllers;

import com.example.usersmanage.DOA.RegisterRequest;
import com.example.usersmanage.DOA.UserInfo;
import com.example.usersmanage.entities.Users;
import com.example.usersmanage.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Users Controller Tests")
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private Users user;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        user = new Users();
        user.setFirstname("John");
        user.setEmail("john.doe@example.com");
        userInfo = new UserInfo(user, "mockToken");
    }

    @Test
    @DisplayName("POST /user/register - Success")
    void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@example.com");

        when(userService.register(any(RegisterRequest.class))).thenReturn(true);

        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /user/login - Success")
    void testLogin_Success() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(userInfo);

        mockMvc.perform(post("/user/login")
                .param("email", "john.doe@example.com")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    @DisplayName("POST /user/logout - Success")
    void testLogout() throws Exception {
        mockMvc.perform(post("/user/logout")
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /user/send-code - Success")
    void testSendCode() throws Exception {
        when(userService.VerificationCode(anyString())).thenReturn("123456");

        mockMvc.perform(post("/user/send-code")
                .param("email", "john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("123456"));
    }

    @Test
    @DisplayName("GET /user/verification-code/{email} - Success")
    void testVerifyCode() throws Exception {
        when(userService.verifyCode(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(get("/user/verification-code/john.doe@example.com")
                .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /user/reset-password - Success")
    void testResetPassword() throws Exception {
        when(userService.resetPassword(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/user/reset-password")
                .param("code", "123456")
                .param("password", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
