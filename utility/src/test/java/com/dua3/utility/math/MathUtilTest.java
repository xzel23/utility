// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author axel
 */
public class MathUtilTest {

    @BeforeAll
    public static void setUpClass() {
        // nop
    }

    @AfterAll
    public static void tearDownClass() {
        // nop
    }

    public MathUtilTest() {
    }

    @BeforeEach
    public void setUp() {
        // nop
    }

    @AfterEach
    public void tearDown() {
        // nop
    }

    /**
     * Test of findRoot method, of class MathUtil.
     */
    @Test
    public void testFindRoot() {
        System.out.println("findRoot");
        double result = MathUtil.findRoot(x -> (x - 5) * (x + 2), 4, 10, 1.0e-15);
        double expResult = 5;
        assertEquals(expResult, result, 1.0e-15);
    }

    /**
     * Test of findRootsInInterval method, of class MathUtil.
     */
    @Test
    public void testFindRootsInInterval() {
        System.out.println("findRootsInInterval");
        List<Double> result = MathUtil.findRootsInInterval(x -> 3 * x * (x - 2.0 / 3.0), -10.5, +10.5, 20, 1.0e-15);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0), 1.0e-15);
        assertEquals(2.0 / 3.0, result.get(1), 1.0e-15);
    }

    /**
     * Test of ilog10 method, of class MathUtil.
     */
    @Test
    public void testIlog10() {
        System.out.println("ilog10");
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
    public void testPow10() {
        System.out.println("pow10");
        assertEquals(0.01, MathUtil.pow10(-2), 1.0e-15);
        assertEquals(0.1, MathUtil.pow10(-1), 1.0e-15);
        assertEquals(1.0, MathUtil.pow10(0), 1.0e-15);
        assertEquals(10.0, MathUtil.pow10(1), 1.0e-15);
        assertEquals(100.0, MathUtil.pow10(2), 1.0e-15);
    }

    /**
     * Test of round method, of class MathUtil.
     */
    @Test
    public void testRound() {
        System.out.println("round");

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
    public void testRoundToPrecision() {
        System.out.println("roundToPrecision");
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
    public void testRoundingOperation() {
        System.out.println("roundingOperation");

        // create operations
        Map<RoundingMode, DoubleUnaryOperator> operations = new EnumMap<>(RoundingMode.class);
        for (var mode: RoundingMode.values()) {
            operations.put(mode, MathUtil.roundingOperation(0, mode));
        }

        // check results
        checkRounding(operations, 5.5, 6, 5, 6, 5, 6, 5, 6);
        checkRounding(operations, 2.5, 3, 2, 3, 2, 3, 2, 2);
        checkRounding(operations, 1.6, 2, 1, 2, 1, 2, 2, 2);
        checkRounding(operations, 1.1, 2, 1, 2, 1, 1, 1, 1);
        checkRounding(operations, 1.0, 1, 1, 1, 1, 1, 1, 1);
        checkRounding(operations, -1.0, -1, -1,	-1, -1, -1, -1, -1);
        checkRounding(operations, -1.1, -2, -1,	-1, -2, -1, -1, -1);
        checkRounding(operations, -1.6, -2, -1,	-1, -2, -2, -2, -2);
        checkRounding(operations, -2.5, -3, -2,	-2, -3, -3, -2, -2);
        checkRounding(operations, -5.5, -6, -5,	-5, -6, -6, -5, -6);
    }

    private boolean checkRounding(Map<RoundingMode,DoubleUnaryOperator> operations, double x, double xUP, double xDOWN, double xCEILING, double xFLOOR, double xHALF_UP, double xHALF_DOWN, double xHALF_EVEN) {
        assertEquals(xUP, operations.get(RoundingMode.UP).applyAsDouble(x));
        assertEquals(xDOWN, operations.get(RoundingMode.DOWN).applyAsDouble(x));
        assertEquals(xCEILING, operations.get(RoundingMode.CEILING).applyAsDouble(x));
        assertEquals(xFLOOR, operations.get(RoundingMode.FLOOR).applyAsDouble(x));
        assertEquals(xHALF_UP, operations.get(RoundingMode.HALF_UP).applyAsDouble(x));
        assertEquals(xHALF_DOWN, operations.get(RoundingMode.HALF_DOWN).applyAsDouble(x));
        assertEquals(xHALF_EVEN, operations.get(RoundingMode.HALF_EVEN).applyAsDouble(x));
        assertEquals(x, operations.get(RoundingMode.UNNECESSARY).applyAsDouble(x));
        return true;
    }

    /**
     * Test of toDecimalString method, of class MathUtil.
     */
    @Test
    public void testToDecimalString() {
        System.out.println("toDecimalString");
        assertEquals("1.0", MathUtil.toDecimalString(1.0, 1));
    }

}
