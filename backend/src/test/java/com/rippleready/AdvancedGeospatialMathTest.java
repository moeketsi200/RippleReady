package com.rippleready;

import com.rippleready.service.DataStore;
import com.rippleready.service.RippleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AdvancedGeospatialMathTest {

    private RippleEngineService rippleEngine;

    @BeforeEach
    void setUp() {
        rippleEngine = new RippleEngineService(new DataStore());
    }

    @Test
    @DisplayName("Math: Distance between identical geographical points is exactly 0.0 km")
    void testZeroDistanceAtIdenticalCoordinates() {
        double dist = rippleEngine.calculateDistanceKm(-33.9630, 22.4610, -33.9630, 22.4610);
        assertThat(dist).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Math: Commutative symmetry — Distance(A, B) equals Distance(B, A)")
    void testCommutativeDistance() {
        double latA = -33.9630;
        double lonA = 22.4610;
        double latB = -33.9580;
        double lonB = 22.4650;

        double distAB = rippleEngine.calculateDistanceKm(latA, lonA, latB, lonB);
        double distBA = rippleEngine.calculateDistanceKm(latB, lonB, latA, lonA);

        assertThat(distAB).isEqualTo(distBA);
    }

    @Test
    @DisplayName("Math: One degree of latitude along the Prime Meridian is ~111.19 km")
    void testOneDegreeLatitudeDistance() {
        double dist = rippleEngine.calculateDistanceKm(0.0, 0.0, 1.0, 0.0);
        assertThat(dist).isCloseTo(111.19, within(0.5));
    }

    @Test
    @DisplayName("Math: Antipodal distance across the globe is half Earth circumference (~20,015 km)")
    void testAntipodalDistance() {
        // Equator to opposite side of equator
        double dist = rippleEngine.calculateDistanceKm(0.0, 0.0, 0.0, 180.0);
        assertThat(dist).isCloseTo(20015.0, within(50.0));
    }

    @Test
    @DisplayName("Math: Southern and Northern hemisphere latitude step symmetry")
    void testHemisphereSymmetry() {
        double southDist = rippleEngine.calculateDistanceKm(-33.0, 22.0, -34.0, 22.0);
        double northDist = rippleEngine.calculateDistanceKm(33.0, 22.0, 34.0, 22.0);

        assertThat(southDist).isEqualTo(northDist);
    }
}
