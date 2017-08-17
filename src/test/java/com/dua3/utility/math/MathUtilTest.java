package com.dua3.utility.math;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author axel
 */
public class MathUtilTest {

    @BeforeClass
    public static void setUpClass() {
        // nop
    }

    @AfterClass
    public static void tearDownClass() {
        // nop
    }

    public MathUtilTest() {
    }

    @Before
    public void setUp() {
        // nop
    }

    @After
    public void tearDown() {
        // nop
    }

    /**
     * Test of findRoot method, of class MathUtil.
     */
    @Test
    public void testFindRoot() {
        System.out.println("findRoot");
        double result = MathUtil.findRoot(x -> (x - 5) * (x + 2), 4, 10, 1e-15);
        double expResult = 5;
        assertEquals(expResult, result, 1e-15);
    }

    /**
     * Test of findRootsInIntervall method, of class MathUtil.
     */
    @Test
    public void testFindRootsInIntervall() {
        System.out.println("findRootsInIntervall");
        List<Double> result = MathUtil.findRootsInInterval(x -> 3 * x * (x - 2.0 / 3.0), -10.5, +10.5, 20, 1e-15);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0), 1e-15);
        assertEquals(2.0 / 3.0, result.get(1), 1e-15);
    }

    /**
     * Test of ilog10 method, of class MathUtil.
     */
    @Test
    public void testIlog10() {
        System.out.println("ilog10");
        assertEquals(-4, MathUtil.ilog10(0.00099), 1e-15);
        assertEquals(-3, MathUtil.ilog10(0.001), 1e-15);
        assertEquals(-3, MathUtil.ilog10(0.0099), 1e-15);
        assertEquals(-2, MathUtil.ilog10(0.01), 1e-15);
        assertEquals(-2, MathUtil.ilog10(0.099), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.2), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.3), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.4), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.5), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.6), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.7), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.8), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.9), 1e-15);
        assertEquals(-1, MathUtil.ilog10(0.99), 1e-15);
        assertEquals(0, MathUtil.ilog10(1.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(2.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(3.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(4.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(5.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(6.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(7.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(8.0), 1e-15);
        assertEquals(0, MathUtil.ilog10(9.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(10.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(20.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(30.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(40.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(50.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(60.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(70.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(80.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(90.0), 1e-15);
        assertEquals(1, MathUtil.ilog10(99.0), 1e-15);
        assertEquals(2, MathUtil.ilog10(100.0), 1e-15);
    }

    /**
     * Test of pow10 method, of class MathUtil.
     */
    @Test
    public void testPow10() {
        System.out.println("pow10");
        assertEquals(0.01, MathUtil.pow10(-2), 1e-15);
        assertEquals(0.1, MathUtil.pow10(-1), 1e-15);
        assertEquals(1.0, MathUtil.pow10(0), 1e-15);
        assertEquals(10.0, MathUtil.pow10(1), 1e-15);
        assertEquals(100.0, MathUtil.pow10(2), 1e-15);
    }

    /**
     * Test of round method, of class MathUtil.
     */
    @Test
    public void testRound() {
        System.out.println("round");
        double[][] tests = {
                // arg, round(arg,2), round(arg,-2)
                { 0, 0, 0 }, { 1.234, 1.23, 0 }, { -1.234, -1.23, 0 }, { -1234.567, -1234.57, -1200 }, };

        for (double[] test : tests) {
            double arg = test[0];
            double expected = test[1];
            double result = MathUtil.round(arg, 2);
            assertEquals(expected, result, 1e-15);
        }
    }

    /**
     * Test of roundToPrecision method, of class MathUtil.
     */
    @Test
    public void testRoundToPrecision() {
        System.out.println("roundToPrecision");
        assertEquals(1.2, MathUtil.roundToPrecision(1.2345, 2), 1e-10);
        assertEquals(1.235, MathUtil.roundToPrecision(1.2345, 4), 1e-10);
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
