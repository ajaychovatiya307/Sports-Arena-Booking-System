package com.infy.sportsarena.catalog.entity;

import com.infy.sportsarena.catalog.CatalogDataAssertions;
import org.junit.jupiter.api.Test;

class CatalogEntityTest {
    @Test
    void arenaCategoryDataContract() {
        CatalogDataAssertions.verifyDataClass(ArenaCategory.class);
    }

    @Test
    void arenaPackageDataContract() {
        CatalogDataAssertions.verifyDataClass(ArenaPackage.class);
    }

    @Test
    void slotAvailabilityDataContract() {
        CatalogDataAssertions.verifyDataClass(SlotAvailability.class);
    }

    @Test
    void availabilityStatusValuesAreAvailable() {
        org.assertj.core.api.Assertions.assertThat(AvailabilityStatus.values())
                .containsExactly(AvailabilityStatus.ACTIVE, AvailabilityStatus.INACTIVE);
        org.assertj.core.api.Assertions.assertThat(AvailabilityStatus.valueOf("ACTIVE"))
                .isEqualTo(AvailabilityStatus.ACTIVE);
    }
}
