// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.math;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A math utility class.
 */
@SuppressWarnings("BoundedWildcard")
class MathUtilTest {

    private static void checkRounding(Map<RoundingMode, DoubleUnaryOperator> operations, double x, double xUp, double xDown, double xCeiling, double xFloor, double xHalfUp, double xHalfDown, double xHalfEven) {
        assertEquals(xUp, operations.get(RoundingMode.UP).applyAsDouble(x));
        assertEquals(xDown, operations.get(RoundingMode.DOWN).applyAsDouble(x));
        assertEquals(xCeiling, operations.get(RoundingMode.CEILING).applyAsDouble(x));
        assertEquals(xFloor, operations.get(RoundingMode.FLOOR).applyAsDouble(x));
        assertEquals(xHalfUp, operations.get(RoundingMode.HALF_UP).applyAsDouble(x));
        assertEquals(xHalfDown, operations.get(RoundingMode.HALF_DOWN).applyAsDouble(x));
        assertEquals(xHalfEven, operations.get(RoundingMode.HALF_EVEN).applyAsDouble(x));
        assertEquals(x, operations.get(RoundingMode.UNNECESSARY).applyAsDouble(x));
    }

    /**
     * Test of findRoot method, of class MathUtil.
     */
    @Test
    void testFindRoot() {
        double result = MathUtil.findRoot(x -> (x - 5) * (x + 2), 4, 10, 1.0e-15);
        double expResult = 5;
        assertEquals(expResult, result, 1.0e-15);

        double result2 = MathUtil.findRoot(x -> (x - 5) * (x + 2), 20, -20, 1.0e-15);
        assertEquals(expResult, result2, 1.0e-15);
    }

    /**
     * Test of findRootsInInterval method, of class MathUtil.
     */
    @Test
    void testFindRootsInInterval() {
        List<Double> result = MathUtil.findRootsInInterval(x -> 3 * x * (x - 2.0 / 3.0), -10.5, +10.5, 20, 1.0e-15);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0), 1.0e-15);
        assertEquals(2.0 / 3.0, result.get(1), 1.0e-15);

        result = MathUtil.findRootsInInterval(x -> 3 * x * (x - 2.0 / 3.0), 11, -20, 20, 1.0e-15);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0), 1.0e-15);
        assertEquals(2.0 / 3.0, result.get(1), 1.0e-15);
    }

    /**
     * Test of ilog10 method, of class MathUtil.
     */
    @Test
    void testIlog10() {

        assertEquals(-4, MathUtil.ilog10(0.00099), 1.0e-15);
        assertEquals(-3, MathUtil.ilog10(0.001), 1.0e-15);
        assertEquals(-3, MathUtil.ilog10(0.0099), 1.0e-15);
        assertEquals(-2, MathUtil.ilog10(0.01), 1.0e-15);
        assertEquals(-2, MathUtil.ilog10(0.099), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.2), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.3), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.4), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.5), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.6), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.7), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.8), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.9), 1.0e-15);
        assertEquals(-1, MathUtil.ilog10(0.99), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(1.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(2.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(3.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(4.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(5.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(6.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(7.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(8.0), 1.0e-15);
        assertEquals(0, MathUtil.ilog10(9.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(10.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(20.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(30.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(40.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(50.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(60.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(70.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(80.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(90.0), 1.0e-15);
        assertEquals(1, MathUtil.ilog10(99.0), 1.0e-15);
        assertEquals(2, MathUtil.ilog10(100.0), 1.0e-15);
    }

    /**
     * Test of pow10 method, of class MathUtil.
     */
    @Test
    void testPow10() {
        long pow = 1;
        for (int i = 0; i < 19; i++) {
            assertEquals(1.0/pow, MathUtil.pow10(-i), 1.0e-15);
            assertEquals((double) pow, MathUtil.pow10(i), 1.0e-15);
            pow *= 10;
        }
    }

    /**
     * Test of round method, of class MathUtil.
     */
    @Test
    void testRound() {


        // positive n
        assertEquals(1.2, MathUtil.round(1.23, 1), 1.0e-10);
        assertEquals(12.3, MathUtil.round(12.3, 1), 1.0e-10);
        assertEquals(123.0, MathUtil.round(123, 1), 1.0e-10);

        assertEquals(1.3, MathUtil.round(1.25, 1), 1.0e-10);
        assertEquals(12.5, MathUtil.round(12.5, 1), 1.0e-10);
        assertEquals(125.0, MathUtil.round(125, 1), 1.0e-10);

        assertEquals(-1.2, MathUtil.round(-1.23, 1), 1.0e-10);
        assertEquals(-12.3, MathUtil.round(-12.3, 1), 1.0e-10);
        assertEquals(-123.0, MathUtil.round(-123, 1), 1.0e-10);

        assertEquals(-1.2, MathUtil.round(-1.25, 1), 1.0e-10);
        assertEquals(-12.5, MathUtil.round(-12.5, 1), 1.0e-10);
        assertEquals(-125.0, MathUtil.round(-125, 1), 1.0e-10);

        // negative n
        assertEquals(0.0, MathUtil.round(1.23, -1), 1.0e-10);
        assertEquals(10.0, MathUtil.round(12.3, -1), 1.0e-10);
        assertEquals(120.0, MathUtil.round(123, -1), 1.0e-10);

        assertEquals(0.0, MathUtil.round(1.25, -1), 1.0e-10);
        assertEquals(20.0, MathUtil.round(15.0, -1), 1.0e-10);
        assertEquals(130.0, MathUtil.round(125.0, -1), 1.0e-10);

        assertEquals(0.0, MathUtil.round(-1.23, -1), 1.0e-10);
        assertEquals(-10.0, MathUtil.round(-12.3, -1), 1.0e-10);
        assertEquals(-120.0, MathUtil.round(-123, -1), 1.0e-10);

        assertEquals(0.0, MathUtil.round(-1.25, -1), 1.0e-10);
        assertEquals(-10.0, MathUtil.round(-15.0, -1), 1.0e-10);
        assertEquals(-120.0, MathUtil.round(-125.0, -1), 1.0e-10);
    }

    /**
     * Test of roundToPrecision method, of class MathUtil.
     */
    @Test
    void testRoundToPrecision() {

        assertEquals(1.2, MathUtil.roundToPrecision(1.23, 2), 1.0e-10);
        assertEquals(12.0, MathUtil.roundToPrecision(12.3, 2), 1.0e-10);
        assertEquals(120, MathUtil.roundToPrecision(123, 2), 1.0e-10);

        assertEquals(1.3, MathUtil.roundToPrecision(1.25, 2), 1.0e-10);
        assertEquals(13.0, MathUtil.roundToPrecision(12.5, 2), 1.0e-10);
        assertEquals(130, MathUtil.roundToPrecision(125, 2), 1.0e-10);

        assertEquals(-1.2, MathUtil.roundToPrecision(-1.23, 2), 1.0e-10);
        assertEquals(-12.0, MathUtil.roundToPrecision(-12.3, 2), 1.0e-10);
        assertEquals(-120, MathUtil.roundToPrecision(-123, 2), 1.0e-10);

        assertEquals(-1.2, MathUtil.roundToPrecision(-1.25, 2), 1.0e-10);
        assertEquals(-12.0, MathUtil.roundToPrecision(-12.5, 2), 1.0e-10);
        assertEquals(-120, MathUtil.roundToPrecision(-125, 2), 1.0e-10);
    }

    /**
     * Test roundingOperation method, of class MathUtil.
     */
    @Test
    void testRoundingOperation() {
        // create operations
        Map<RoundingMode, DoubleUnaryOperator> operations = new EnumMap<>(RoundingMode.class);
        for (RoundingMode mode : RoundingMode.values()) {
            operations.put(mode, MathUtil.roundingOperation(0, mode));
        }

        // check results
        checkRounding(operations, 5.5, 6, 5, 6, 5, 6, 5, 6);
        checkRounding(operations, 2.5, 3, 2, 3, 2, 3, 2, 2);
        checkRounding(operations, 1.6, 2, 1, 2, 1, 2, 2, 2);
        checkRounding(operations, 1.1, 2, 1, 2, 1, 1, 1, 1);
        checkRounding(operations, 1.0, 1, 1, 1, 1, 1, 1, 1);
        checkRounding(operations, -1.0, -1, -1, -1, -1, -1, -1, -1);
        checkRounding(operations, -1.1, -2, -1, -1, -2, -1, -1, -1);
        checkRounding(operations, -1.6, -2, -1, -1, -2, -2, -2, -2);
        checkRounding(operations, -2.5, -3, -2, -2, -3, -3, -2, -2);
        checkRounding(operations, -5.5, -6, -5, -5, -6, -6, -5, -6);
    }

    @Test
    void testIsIntegral() {
        assertTrue(MathUtil.isIntegral(0.0));
        assertTrue(MathUtil.isIntegral(1.0));
        assertTrue(MathUtil.isIntegral(-10.0));
        assertTrue(MathUtil.isIntegral(1265456.0));
        assertTrue(MathUtil.isIntegral(-1265456.0));

        assertFalse(MathUtil.isIntegral(0.1));
        assertFalse(MathUtil.isIntegral(1.1));
        assertFalse(MathUtil.isIntegral(-10.1));
        assertFalse(MathUtil.isIntegral(1265456.1));
        assertFalse(MathUtil.isIntegral(-1265456.1));

        assertFalse(MathUtil.isIntegral(0.9999999999999));
        assertFalse(MathUtil.isIntegral(1.9999999999999));
        assertFalse(MathUtil.isIntegral(-10.9999999999999));
        assertFalse(MathUtil.isIntegral(1265456.9999999));
        assertFalse(MathUtil.isIntegral(-1265456.9999999));

        assertFalse(MathUtil.isIntegral(0.0000000000001));
        assertFalse(MathUtil.isIntegral(1.0000000000001));
        assertFalse(MathUtil.isIntegral(-10.0000000000001));
        assertFalse(MathUtil.isIntegral(1265456.0000001));
        assertFalse(MathUtil.isIntegral(-1265456.0000001));
    }

    @Test
    void testGcd() {
        assertEquals(6L, MathUtil.gcd(48L, 18L));
        assertEquals(6L, MathUtil.gcd(-48L, 18L));
        assertEquals(6L, MathUtil.gcd(48L, -18L));
        assertEquals(6L, MathUtil.gcd(-48L, -18L));
    }

    @Test
    void testRad() {
        assertEquals(0.0, MathUtil.rad(0.0), 1.0e-15);
        assertEquals(Math.PI / 4, MathUtil.rad(45.0), 1.0e-15);
        assertEquals(Math.PI / 2, MathUtil.rad(90.0), 1.0e-15);
        assertEquals(Math.PI, MathUtil.rad(180.0), 1.0e-15);
        assertEquals(3 * Math.PI / 2, MathUtil.rad(270.0), 1.0e-15);
        assertEquals(2 * Math.PI, MathUtil.rad(360.0), 1.0e-15);
        assertEquals(-Math.PI / 2, MathUtil.rad(-90.0), 1.0e-15);
    }

    @Test
    void testRadf() {
        assertEquals(0.0f, MathUtil.radf(0.0f), 1.0e-6);
        assertEquals((float) Math.PI / 4, MathUtil.radf(45.0f), 1.0e-6);
        assertEquals((float) Math.PI / 2, MathUtil.radf(90.0f), 1.0e-6);
        assertEquals((float) Math.PI, MathUtil.radf(180.0f), 1.0e-6);
        assertEquals(3 * (float) Math.PI / 2, MathUtil.radf(270.0f), 1.0e-6);
        assertEquals(2 * (float) Math.PI, MathUtil.radf(360.0f), 1.0e-6);
        assertEquals(-(float) Math.PI / 2, MathUtil.radf(-90.0f), 1.0e-6);
    }

    @Test
    void testDeg() {
        assertEquals(0.0, MathUtil.deg(0.0), 1.0e-15);
        assertEquals(45.0, MathUtil.deg(Math.PI / 4), 1.0e-15);
        assertEquals(90.0, MathUtil.deg(Math.PI / 2), 1.0e-15);
        assertEquals(180.0, MathUtil.deg(Math.PI), 1.0e-15);
        assertEquals(270.0, MathUtil.deg(3 * Math.PI / 2), 1.0e-15);
        assertEquals(360.0, MathUtil.deg(2 * Math.PI), 1.0e-15);
        assertEquals(-90.0, MathUtil.deg(-Math.PI / 2), 1.0e-15);
    }

    @Test
    void testDegf() {
        assertEquals(0.0f, MathUtil.degf(0.0f), 1.0e-6);
        assertEquals(45.0f, MathUtil.degf((float) Math.PI / 4), 1.0e-6);
        assertEquals(90.0f, MathUtil.degf((float) Math.PI / 2), 1.0e-6);
        assertEquals(180.0f, MathUtil.degf((float) Math.PI), 1.0e-6);
        assertEquals(270.0f, MathUtil.degf(3 * (float) Math.PI / 2), 1.0e-6);
        assertEquals(360.0f, MathUtil.degf(2 * (float) Math.PI), 1.0e-6);
        assertEquals(-90.0f, MathUtil.degf(-(float) Math.PI / 2), 1.0e-6);
    }

    @Test
    void testNormalizeRadians() {
        assertEquals(0.0, MathUtil.normalizeRadians(0.0), 1.0e-15);
        assertEquals(Math.PI / 4, MathUtil.normalizeRadians(Math.PI / 4), 1.0e-15);
        assertEquals(Math.PI / 2, MathUtil.normalizeRadians(Math.PI / 2), 1.0e-15);
        assertEquals(Math.PI, MathUtil.normalizeRadians(Math.PI), 1.0e-15);
        assertEquals(3 * Math.PI / 2, MathUtil.normalizeRadians(3 * Math.PI / 2), 1.0e-15);
        assertEquals(0.0, MathUtil.normalizeRadians(2 * Math.PI), 1.0e-15);
        assertEquals(Math.PI / 2, MathUtil.normalizeRadians(5 * Math.PI / 2), 1.0e-15);
        assertEquals(Math.PI / 2, MathUtil.normalizeRadians(-3 * Math.PI / 2), 1.0e-15);
        assertEquals(3 * Math.PI / 2, MathUtil.normalizeRadians(-Math.PI / 2), 1.0e-15);
    }

    @Test
    void testNormalizeDegrees() {
        assertEquals(0.0, MathUtil.normalizeDegrees(0.0), 1.0e-15);
        assertEquals(45.0, MathUtil.normalizeDegrees(45.0), 1.0e-15);
        assertEquals(90.0, MathUtil.normalizeDegrees(90.0), 1.0e-15);
        assertEquals(180.0, MathUtil.normalizeDegrees(180.0), 1.0e-15);
        assertEquals(270.0, MathUtil.normalizeDegrees(270.0), 1.0e-15);
        assertEquals(0.0, MathUtil.normalizeDegrees(360.0), 1.0e-15);
        assertEquals(90.0, MathUtil.normalizeDegrees(450.0), 1.0e-15);
        assertEquals(90.0, MathUtil.normalizeDegrees(-270.0), 1.0e-15);
        assertEquals(270.0, MathUtil.normalizeDegrees(-90.0), 1.0e-15);
    }

    @Test
    void testQuadrantRadians() {
        assertEquals(1, MathUtil.quadrantRadians(0.0));
        assertEquals(1, MathUtil.quadrantRadians(Math.PI / 4));
        assertEquals(2, MathUtil.quadrantRadians(Math.PI / 2));
        assertEquals(2, MathUtil.quadrantRadians(3 * Math.PI / 4));
        assertEquals(3, MathUtil.quadrantRadians(Math.PI));
        assertEquals(3, MathUtil.quadrantRadians(5 * Math.PI / 4));
        assertEquals(4, MathUtil.quadrantRadians(3 * Math.PI / 2));
        assertEquals(4, MathUtil.quadrantRadians(7 * Math.PI / 4));
        assertEquals(1, MathUtil.quadrantRadians(2 * Math.PI));

        // Test with angles outside [0, 2π)
        assertEquals(1, MathUtil.quadrantRadians(8 * Math.PI / 4));
        assertEquals(2, MathUtil.quadrantRadians(10 * Math.PI / 4));
        assertEquals(1, MathUtil.quadrantRadians(-8 * Math.PI / 4));
        assertEquals(4, MathUtil.quadrantRadians(-Math.PI / 4));
    }

    @Test
    void testQuadrantIndexRadians() {

        assertEquals(0, MathUtil.quadrantIndexRadians(0.0));
        assertEquals(0, MathUtil.quadrantIndexRadians(Math.PI / 4));
        assertEquals(1, MathUtil.quadrantIndexRadians(Math.PI / 2));
        assertEquals(1, MathUtil.quadrantIndexRadians(3 * Math.PI / 4));
        assertEquals(2, MathUtil.quadrantIndexRadians(Math.PI));
        assertEquals(2, MathUtil.quadrantIndexRadians(5 * Math.PI / 4));
        assertEquals(3, MathUtil.quadrantIndexRadians(3 * Math.PI / 2));
        assertEquals(3, MathUtil.quadrantIndexRadians(7 * Math.PI / 4));
        assertEquals(0, MathUtil.quadrantIndexRadians(2 * Math.PI));

        // Test with angles outside [0, 2π)
        assertEquals(0, MathUtil.quadrantIndexRadians(8 * Math.PI / 4));
        assertEquals(1, MathUtil.quadrantIndexRadians(10 * Math.PI / 4));
        assertEquals(0, MathUtil.quadrantIndexRadians(-8 * Math.PI / 4));
        assertEquals(3, MathUtil.quadrantIndexRadians(-Math.PI / 4));
    }

    @Test
    void testQuadrantDegrees() {

        assertEquals(1, MathUtil.quadrantDegrees(0.0));
        assertEquals(1, MathUtil.quadrantDegrees(45.0));
        assertEquals(2, MathUtil.quadrantDegrees(90.0));
        assertEquals(2, MathUtil.quadrantDegrees(135.0));
        assertEquals(3, MathUtil.quadrantDegrees(180.0));
        assertEquals(3, MathUtil.quadrantDegrees(225.0));
        assertEquals(4, MathUtil.quadrantDegrees(270.0));
        assertEquals(4, MathUtil.quadrantDegrees(315.0));
        assertEquals(1, MathUtil.quadrantDegrees(360.0));

        // Test with angles outside [0, 360)
        assertEquals(1, MathUtil.quadrantDegrees(720.0));
        assertEquals(2, MathUtil.quadrantDegrees(450.0));
        assertEquals(1, MathUtil.quadrantDegrees(-720.0));
        assertEquals(4, MathUtil.quadrantDegrees(-45.0));
    }

    @Test
    void testQuadrantIndexDegrees() {

        assertEquals(0, MathUtil.quadrantIndexDegrees(0.0));
        assertEquals(0, MathUtil.quadrantIndexDegrees(45.0));
        assertEquals(1, MathUtil.quadrantIndexDegrees(90.0));
        assertEquals(1, MathUtil.quadrantIndexDegrees(135.0));
        assertEquals(2, MathUtil.quadrantIndexDegrees(180.0));
        assertEquals(2, MathUtil.quadrantIndexDegrees(225.0));
        assertEquals(3, MathUtil.quadrantIndexDegrees(270.0));
        assertEquals(3, MathUtil.quadrantIndexDegrees(315.0));
        assertEquals(0, MathUtil.quadrantIndexDegrees(360.0));

        // Test with angles outside [0, 360)
        assertEquals(0, MathUtil.quadrantIndexDegrees(720.0));
        assertEquals(1, MathUtil.quadrantIndexDegrees(450.0));
        assertEquals(0, MathUtil.quadrantIndexDegrees(-720.0));
        assertEquals(3, MathUtil.quadrantIndexDegrees(-45.0));
    }

    @Test
    void testOctantRadians() {

        assertEquals(1, MathUtil.octantRadians(0.0));
        assertEquals(1, MathUtil.octantRadians(Math.PI / 8));
        assertEquals(2, MathUtil.octantRadians(Math.PI / 4));
        assertEquals(2, MathUtil.octantRadians(3 * Math.PI / 8));
        assertEquals(3, MathUtil.octantRadians(Math.PI / 2));
        assertEquals(3, MathUtil.octantRadians(5 * Math.PI / 8));
        assertEquals(4, MathUtil.octantRadians(3 * Math.PI / 4));
        assertEquals(4, MathUtil.octantRadians(7 * Math.PI / 8));
        assertEquals(5, MathUtil.octantRadians(Math.PI));
        assertEquals(5, MathUtil.octantRadians(9 * Math.PI / 8));
        assertEquals(6, MathUtil.octantRadians(5 * Math.PI / 4));
        assertEquals(6, MathUtil.octantRadians(11 * Math.PI / 8));
        assertEquals(7, MathUtil.octantRadians(3 * Math.PI / 2));
        assertEquals(7, MathUtil.octantRadians(13 * Math.PI / 8));
        assertEquals(8, MathUtil.octantRadians(7 * Math.PI / 4));
        assertEquals(8, MathUtil.octantRadians(15 * Math.PI / 8));
        assertEquals(1, MathUtil.octantRadians(2 * Math.PI));

        // Test with angles outside [0, 2π)
        assertEquals(1, MathUtil.octantRadians(16 * Math.PI / 8));
        assertEquals(2, MathUtil.octantRadians(18 * Math.PI / 8));
        assertEquals(1, MathUtil.octantRadians(-16 * Math.PI / 8));
        assertEquals(8, MathUtil.octantRadians(-Math.PI / 8));
    }

    @Test
    void testOctantIndexRadians() {

        assertEquals(0, MathUtil.octantIndexRadians(0.0));
        assertEquals(0, MathUtil.octantIndexRadians(Math.PI / 8));
        assertEquals(1, MathUtil.octantIndexRadians(Math.PI / 4));
        assertEquals(1, MathUtil.octantIndexRadians(3 * Math.PI / 8));
        assertEquals(2, MathUtil.octantIndexRadians(Math.PI / 2));
        assertEquals(2, MathUtil.octantIndexRadians(5 * Math.PI / 8));
        assertEquals(3, MathUtil.octantIndexRadians(3 * Math.PI / 4));
        assertEquals(3, MathUtil.octantIndexRadians(7 * Math.PI / 8));
        assertEquals(4, MathUtil.octantIndexRadians(Math.PI));
        assertEquals(4, MathUtil.octantIndexRadians(9 * Math.PI / 8));
        assertEquals(5, MathUtil.octantIndexRadians(5 * Math.PI / 4));
        assertEquals(5, MathUtil.octantIndexRadians(11 * Math.PI / 8));
        assertEquals(6, MathUtil.octantIndexRadians(3 * Math.PI / 2));
        assertEquals(6, MathUtil.octantIndexRadians(13 * Math.PI / 8));
        assertEquals(7, MathUtil.octantIndexRadians(7 * Math.PI / 4));
        assertEquals(7, MathUtil.octantIndexRadians(15 * Math.PI / 8));
        assertEquals(0, MathUtil.octantIndexRadians(2 * Math.PI));

        // Test with angles outside [0, 2π)
        assertEquals(0, MathUtil.octantIndexRadians(16 * Math.PI / 8));
        assertEquals(1, MathUtil.octantIndexRadians(18 * Math.PI / 8));
        assertEquals(0, MathUtil.octantIndexRadians(-16 * Math.PI / 8));
        assertEquals(7, MathUtil.octantIndexRadians(-Math.PI / 8));
    }

    @Test
    void testOctantDegrees() {

        assertEquals(1, MathUtil.octantDegrees(0.0));
        assertEquals(1, MathUtil.octantDegrees(22.5));
        assertEquals(2, MathUtil.octantDegrees(45.0));
        assertEquals(2, MathUtil.octantDegrees(67.5));
        assertEquals(3, MathUtil.octantDegrees(90.0));
        assertEquals(3, MathUtil.octantDegrees(112.5));
        assertEquals(4, MathUtil.octantDegrees(135.0));
        assertEquals(4, MathUtil.octantDegrees(157.5));
        assertEquals(5, MathUtil.octantDegrees(180.0));
        assertEquals(5, MathUtil.octantDegrees(202.5));
        assertEquals(6, MathUtil.octantDegrees(225.0));
        assertEquals(6, MathUtil.octantDegrees(247.5));
        assertEquals(7, MathUtil.octantDegrees(270.0));
        assertEquals(7, MathUtil.octantDegrees(292.5));
        assertEquals(8, MathUtil.octantDegrees(315.0));
        assertEquals(8, MathUtil.octantDegrees(337.5));
        assertEquals(1, MathUtil.octantDegrees(360.0));

        // Test with angles outside [0, 360)
        assertEquals(1, MathUtil.octantDegrees(720.0));
        assertEquals(2, MathUtil.octantDegrees(405.0));
        assertEquals(1, MathUtil.octantDegrees(-720.0));
        assertEquals(8, MathUtil.octantDegrees(-22.5));
    }

    @Test
    void testOctantIndexDegrees() {
        assertEquals(0, MathUtil.octantIndexDegrees(0.0));
        assertEquals(0, MathUtil.octantIndexDegrees(22.5));
        assertEquals(1, MathUtil.octantIndexDegrees(45.0));
        assertEquals(1, MathUtil.octantIndexDegrees(67.5));
        assertEquals(2, MathUtil.octantIndexDegrees(90.0));
        assertEquals(2, MathUtil.octantIndexDegrees(112.5));
        assertEquals(3, MathUtil.octantIndexDegrees(135.0));
        assertEquals(3, MathUtil.octantIndexDegrees(157.5));
        assertEquals(4, MathUtil.octantIndexDegrees(180.0));
        assertEquals(4, MathUtil.octantIndexDegrees(202.5));
        assertEquals(5, MathUtil.octantIndexDegrees(225.0));
        assertEquals(5, MathUtil.octantIndexDegrees(247.5));
        assertEquals(6, MathUtil.octantIndexDegrees(270.0));
        assertEquals(6, MathUtil.octantIndexDegrees(292.5));
        assertEquals(7, MathUtil.octantIndexDegrees(315.0));
        assertEquals(7, MathUtil.octantIndexDegrees(337.5));
        assertEquals(0, MathUtil.octantIndexDegrees(360.0));

        // Test with angles outside [0, 360)
        assertEquals(0, MathUtil.octantIndexDegrees(720.0));
        assertEquals(1, MathUtil.octantIndexDegrees(405.0));
        assertEquals(0, MathUtil.octantIndexDegrees(-720.0));
        assertEquals(7, MathUtil.octantIndexDegrees(-22.5));
    }

    @Test
    void testGetRoundingOperation() {
        // Test each rounding mode
        assertEquals(1.0, MathUtil.getRoundingOperation(RoundingMode.HALF_UP).applyAsDouble(0.5), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.HALF_DOWN).applyAsDouble(0.5), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.HALF_EVEN).applyAsDouble(0.5), 1.0e-15);
        assertEquals(1.0, MathUtil.getRoundingOperation(RoundingMode.UP).applyAsDouble(0.1), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.DOWN).applyAsDouble(0.9), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.FLOOR).applyAsDouble(0.9), 1.0e-15);
        assertEquals(1.0, MathUtil.getRoundingOperation(RoundingMode.CEILING).applyAsDouble(0.1), 1.0e-15);
        assertEquals(0.5, MathUtil.getRoundingOperation(RoundingMode.UNNECESSARY).applyAsDouble(0.5), 1.0e-15);

        // Test negative numbers
        assertEquals(-1.0, MathUtil.getRoundingOperation(RoundingMode.HALF_UP).applyAsDouble(-0.5), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.HALF_DOWN).applyAsDouble(-0.5), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.HALF_EVEN).applyAsDouble(-0.5), 1.0e-15);
        assertEquals(-1.0, MathUtil.getRoundingOperation(RoundingMode.UP).applyAsDouble(-0.1), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.DOWN).applyAsDouble(-0.9), 1.0e-15);
        assertEquals(-1.0, MathUtil.getRoundingOperation(RoundingMode.FLOOR).applyAsDouble(-0.1), 1.0e-15);
        assertEquals(0.0, MathUtil.getRoundingOperation(RoundingMode.CEILING).applyAsDouble(-0.9), 1.0e-15);
        assertEquals(-0.5, MathUtil.getRoundingOperation(RoundingMode.UNNECESSARY).applyAsDouble(-0.5), 1.0e-15);
    }
}
