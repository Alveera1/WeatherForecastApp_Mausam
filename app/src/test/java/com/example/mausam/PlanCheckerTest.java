package com.example.mausam;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PlanCheckerTest {

    @Test
    public void outdoorPlusRain_isRisky() {
        assertEquals(PlanChecker.Verdict.RISKY,
                PlanChecker.evaluateDetailed("Outdoor Picnic", "Rain", 10, 25).verdict);
    }

    @Test
    public void travelPlusHighWind_isRisky() {
        assertEquals(PlanChecker.Verdict.RISKY,
                PlanChecker.evaluateDetailed("Travel", "Clear", 55, 25).verdict);
    }

    @Test
    public void veryHot_isCaution() {
        assertEquals(PlanChecker.Verdict.CAUTION,
                PlanChecker.evaluateDetailed("Indoor Meeting", "Clear", 5, 40).verdict);
    }

    @Test
    public void stringVersion_startsWithVerdict() {
        assertEquals(true, PlanChecker.evaluate("Outdoor Picnic", "Rain", 10, 25).startsWith("RISKY: "));
        assertEquals(true, PlanChecker.evaluate("Outdoor Picnic", "Clear", 8, 27).startsWith("SAFE: "));
    }

    @Test
    public void clearDay_isSafe() {
        PlanChecker.Result r = PlanChecker.evaluateDetailed("Outdoor Picnic", "Clear", 8, 27);
        assertEquals(PlanChecker.Verdict.SAFE, r.verdict);
        assertEquals("safe", r.toStatus());
    }

    @Test
    public void indoorPlusRain_isSafe() {
        assertEquals(PlanChecker.Verdict.SAFE,
                PlanChecker.evaluateDetailed("Movie", "Rain", 5, 24).verdict);
    }
}