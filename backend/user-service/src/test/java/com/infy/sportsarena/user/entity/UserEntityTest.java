package com.infy.sportsarena.user.entity;

import com.infy.sportsarena.user.UserDataAssertions;
import org.junit.jupiter.api.Test;

class UserEntityTest {
    @Test
    void teamDataContract() {
        UserDataAssertions.verifyDataClass(Team.class);
    }

    @Test
    void customerProfileDataContract() {
        UserDataAssertions.verifyDataClass(CustomerProfile.class);
    }

    @Test
    void allSportTypesAreAvailable() {
        org.assertj.core.api.Assertions.assertThat(SportType.values()).containsExactly(
                SportType.FOOTBALL, SportType.CRICKET, SportType.BADMINTON, SportType.TENNIS, SportType.BASKETBALL);
        org.assertj.core.api.Assertions.assertThat(SportType.valueOf("FOOTBALL")).isEqualTo(SportType.FOOTBALL);
    }
}
