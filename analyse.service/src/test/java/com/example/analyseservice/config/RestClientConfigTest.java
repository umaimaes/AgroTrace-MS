package com.example.analyseservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Rest Client Config Tests")
class RestClientConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @DisplayName("RestTemplate Bean - Should be correctly initialized")
    void testRestTemplateBean_Exists() {
        assertNotNull(restTemplate, "RestTemplate bean should not be null");

        RestTemplate beanFromContext = context.getBean(RestTemplate.class);
        assertNotNull(beanFromContext, "RestTemplate should be present in application context");
    }
}
