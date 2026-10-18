package com.infy.sportsarena.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class UserServiceApplicationTest {
    @Test
    void mainDelegatesToSpringApplication() {
        new UserServiceApplication();
        try (var springApplication = mockStatic(SpringApplication.class)) {
            UserServiceApplication.main(new String[0]);
            springApplication.verify(() -> SpringApplication.run(UserServiceApplication.class));
        }
    }
}
