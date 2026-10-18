package com.infy.sportsarena.catalog.dto;

import com.infy.sportsarena.catalog.CatalogDataAssertions;
import org.junit.jupiter.api.Test;

class CatalogDTOTest {
    @Test
    void arenaCategoryDtoDataContract() {
        CatalogDataAssertions.verifyDataClass(ArenaCategoryDTO.class);
    }

    @Test
    void arenaPackageDtoDataContract() {
        CatalogDataAssertions.verifyDataClass(ArenaPackageDTO.class);
    }

    @Test
    void slotAvailabilityDtoDataContract() {
        CatalogDataAssertions.verifyDataClass(SlotAvailabilityDTO.class);
    }
}
