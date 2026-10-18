package com.infy.sportsarena.user.dto;

import com.infy.sportsarena.user.UserDataAssertions;
import org.junit.jupiter.api.Test;

class UserDTOTest {
    @Test
    void customerProfileDtoDataContract() {
        UserDataAssertions.verifyDataClass(CustomerProfileDTO.class);
    }

    @Test
    void teamDtoDataContract() {
        UserDataAssertions.verifyDataClass(TeamDTO.class);
    }
}
