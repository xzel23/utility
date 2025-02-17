// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * @author axel
 */
public final class MathUtil {

    private static final Logger LOG = LogManager.getLogger(MathUtil.class);

    /**
     * Utility class - private constructor.
     */
    private MathUtil() {
    }

    /**
     * Clip argument to range.
     *
     * @param min minimal value
     * @param max maximum value
     * @param arg argument
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static int clamp(int min, int max, int arg) {
        assert min <= max : "invalid interval: min=" + min + ", max=" + max;
        return arg < min ? min : Math.min(arg, max);
    }

    /**
     * Clip argument to range.
     *
     * @param min minimal value
     * @param max maximum value
     * @param arg argument
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static long clamp(long min, long max, long arg) {
        assert min <= max : "invalid interval: min=" + min + ", max=" + max;
        return arg < min ? min : Math.min(arg, max);
    }

    /**
     * Clip argument to range.
     *
     * @param min minimal value
     * @param max maximum value
     * @param arg argument
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static double clamp(double min, double max, double arg) {
        assert min <= max : "invalid interval: min=" + min + ", max=" + max;
        return arg < min ? min : Math.min(arg, max);
    }

    /**
     * Clip argument to range.
     *
     * @param min minimal value
     * @param max maximum value
     * @param arg argument
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static float clamp(float min, float max, float arg) {
        assert min <= max : "invalid interval: min=" + min + ", max=" + max;
        return arg < min ? min : Math.min(arg, max);
    }

    /**
     * Clip argument to range.
     *
     * @param min        minimal value
     * @param max        maximum value
     * @param arg        argument
     * @param valueIfNaN value to return if arg is NaN
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>valueIfNaN, if arg is NaN
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static double clamp(double min, double max, double arg, double valueIfNaN) {
        return Double.isNaN(arg) ? valueIfNaN : clamp(min, max, arg);
    }

    /**
     * Clip argument to range.
     *
     * @param min        minimal value
     * @param max        maximum value
     * @param arg        argument
     * @param valueIfNaN value to return if arg is NaN
     * @return <ul>
     * <li>min, if arg &lt; min
     * <li>max, if arg &gt; max
     * <li>valueIfNaN, if arg is NaN
     * <li>else arg
     * </ul>
     * @deprecated for Java 21 and later, use {@code Math.clamp()} instead. Note that the order of arguments differs!
     */
    @Deprecated
    public static float clamp(float min, float max, float arg, float valueIfNaN) {
        return Float.isNaN(arg) ? valueIfNaN : clamp(min, max, arg);
    }

    /**
     * Find root of function.
     * The root finding uses a modified secant algorithm. The values given as
     * staring points should
     * be rough approximations of the root, i.e., the algorithm might fail if the
     * second order
     * derivative changes sign in the interval [xa,root] or [xb,root].
     *
     * @param f   the function for which the root shall be calculated
     * @param xa  first starting point
     * @param xb  second starting point
     * @param eps desired accuracy
     * @return the calculated root or Double.NaN if none is found
     */
    public static double findRoot(DoubleUnaryOperator f, double xa, double xb, double eps) {

        final int maxIterations = 50;

        if (xb < xa) {
            double tmp = xa;
            xa = xb;
            xb = tmp;
        }

        double ya = f.applyAsDouble(xa);
        double yb = f.applyAsDouble(xb);

        int iteration = 0;
        do {
            if (iteration++ > maxIterations) {
                return Double.NaN;
            }

            if (ya == 0.0) {
                return xa;
            }

            if (yb == 0.0) {
                return xb;
            }

            double xc = xa - ya * (xb - xa) / (yb - ya);

            if (Double.isNaN(xc)) {
                return Double.NaN;
            }

            if (xc < xa) {
                xb = xa;
                yb = ya;
                xa = xc;
                ya = f.applyAsDouble(xa);
            } else if (xc > xb) {
                xa = xb;
                ya = yb;
                xb = xc;
                yb = f.applyAsDouble(xb);
            } else if (2 * (xc - xa) < (xb - xa)) {
                xb = xc;
                yb = f.applyAsDouble(xb);
            } else {
                xa = xc;
                ya = f.applyAsDouble(xa);
            }
        } while (xb - xa > eps);
        return (xa + xb) / 2.0;
    }

    /**
     * Find all roots within the given interval.
     * The function is evaluated a fixed number of times to find starting values for
     * root finding.
     *
     * @param f     the function
     * @param x0    first limit of the interval
     * @param x1    second limit of the interval; it must be different from {@code x0}
     * @param steps maximum number of iterations
     * @param eps   maximum error
     * @return list of all calculated roots
     */
    @SuppressWarnings("FloatingPointEquality")
    public static List<Double> findRootsInInterval(DoubleUnaryOperator f, double x0, double x1, int steps, double eps) {
        LangUtil.check(x0 != x1, "Empty interval.");

        if (x0 > x1) {
            double tmp = x0;
            x0 = x1;
            x1 = tmp;
        }

        List<Double> roots = new ArrayList<>();
        double step = (x1 - x0) / steps;
        double xa = x0;
        double ya = f.applyAsDouble(xa);
        double dya = Double.NaN;
        for (int i = 0; i < steps; i++) {
            double x = x0 + (i + 1) * (x1 - x0) / steps;
            double y = f.applyAsDouble(x);
            double dy = ya - y;

            if (ya * y <= 0 || dya * dy <= 0) {
                try {
                    final double root = findRoot(f, xa, x, eps);
                    if (!Double.isNaN(root) && root >= x0 - eps && root <= x1 + eps) {
                        roots.add(root);
                        // avoid finding the same root twice
                        x += step / 2.0;
                        y = f.applyAsDouble(x);
                        // keep value of dy
                    }
                } catch (@SuppressWarnings("unused") Exception e) {
                    LOG.debug("no root");
                }
            }
            xa = x;
            ya = y;
            dya = dy;
        }

        return roots;
    }

    /**
     * Calculate the greatest common divisor.
     * The greatest common divisor is calculated using the Euclidean algorithm.
     *
     * @param a first argument
     * @param b second argument
     * @return greatest common divisor of a and b.
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long h = a % b;
            a = b;
            b = h;
        }
        return Math.abs(a);
    }

    /**
     * Calculate ceil(log10(x)).
     *
     * @param x argument
     * @return ceil(log10 ( x))
     */
    public static int ilog10(double x) {
        LangUtil.check(x > 0.0 && !Double.isInfinite(x), "Illegal argument: %f", x);

        int i;
        if (x >= 1.0) {
            i = 0;
            while ((x /= 10.0) >= 1.0) {
                i++;
            }
        } else {
            i = -1;
            while ((x *= 10.0) < 1.0) {
                i--;
            }
        }
        return i;
    }

    /**
     * Test if number is integral.
     *
     * @param a the number to test
     * @return true, if {@code a} is integral
     */
    @SuppressWarnings("FloatingPointEquality")
    public static boolean isIntegral(double a) {
        return !Double.isNaN(a) && a == Math.floor(a);
    }

    /**
     * Calculate powers of 10.
     *
     * @param i the exponent to use
     * @return 10 raised to the power of i
     */
    public static double pow10(int i) {
        if (i == Integer.MIN_VALUE) {
            // -Integer.MIN_VALUE will overflow
            return 1.0 / (10 * pow10(Integer.MAX_VALUE));
        } else if (i < 0) {
            return 1.0 / pow10(-i);
        }

        double result = 1.0;
        while (i-- > 0) {
            result *= 10.0;
        }
        return result;
    }

    /**
     * Round to decimal places.
     * <p><strong>Note:</strong> If this is a bulk operation, consider using {@link #roundingOperation(int, RoundingMode)}
     * instead as the scale calculation will take place only once per instance.
     * <p>
     * Round {@code x} to {@code n} decimal places according to {@link java.math.RoundingMode#HALF_UP},
     * i.e., 1.5 will be rounded to 2 and -1.5 will be rounded to -1.
     * <p>
     * The number of places {@code n} may be negative, resulting in rounding taking place before the decimal point,
     * i.e. {@code round(125, -1)=130}.
     * <p>
     * Examples rounding to two digits precision:
     * <ul>
     *     <li>0.123 -&gt; 0.12
     *     <li>12.3 -&gt; 12.3
     *     <li>123 -&gt; 123
     *     <li>0.125 -&gt; 0.13
     *     <li>-0.125 -&gt; -0.12
     * </ul>
     *
     * @param x value to round
     * @param n number of decimal places
     * @return x rounded to n decimal places
     */
    public static double round(double x, int n) {
        if (x == 0 || Double.isNaN(x) || Double.isInfinite(x)) {
            return x;
        }

        int m = Math.abs(n);
        long f = 1;
        while (m > 0) {
            f *= 10;
            m--;
        }

        double scale = n >= 0 ? f : 1.0 / f;
        return Math.round(x * scale) / scale;
    }

    /**
     * Round to precision.
     * <p>
     * Round {@code x} to {@code p} digits of precision according to {@link java.math.RoundingMode#HALF_UP}.
     * <p>
     * Examples rounding to two digits precision:
     * <ul>
     *     <li>0.123 -&gt; 0.12
     *     <li>12.3 -&gt; 12
     *     <li>123 -&gt; 120
     * </ul>
     *
     * @param x value to round
     * @param p number of digits (p must be positive)
     * @return x rounded to p digits precision
     */
    public static double roundToPrecision(double x, int p) {
        if (x == 0 || Double.isNaN(x) || Double.isInfinite(x)) {
            return x;
        }

        LangUtil.check(p > 0, "p must be positive: %d", p);

        int n = p - ilog10(Math.abs(x)) - 1;
        return round(x, n);
    }

    /**
     * Get the operation that performs rounding to a fixed number of decimal places.
     * <p>
     * Round {@code x} to {@code n} decimal places according to the supplied {@link java.math.RoundingMode}.
     * <p>
     * If {@link RoundingMode#UNNECESSARY} is used, {@code x} is returned unchanged.
     * <p>
     * The number of places {@code n} may be negative, resulting in rounding taking place before the decimal point.
     *
     * @param n    number of decimal places
     * @param mode the {@link RoundingMode} to use
     * @return operation that performs the requested rounding
     */
    public static DoubleUnaryOperator roundingOperation(int n, RoundingMode mode) {
        // special case: no rounding
        if (mode == RoundingMode.UNNECESSARY) {
            return x -> x;
        }

        // determine rounding operation to use
        DoubleUnaryOperator roundingOperation = getRoundingOperation(mode);

        // special case: rounding to integer needs no scale
        if (n == 0) {
            return roundingOperation;
        }

        // otherwise precalculate and use scale
        double scale = pow10(n);
        return x -> roundingOperation.applyAsDouble(x * scale) / scale;
    }

    /**
     * Get a DoubleUnaryOperator that implements rounding according to the requested mode.
     *
     * @param mode the {@link RoundingMode}
     * @return rounding operation
     */
    public static DoubleUnaryOperator getRoundingOperation(RoundingMode mode) {
        return switch (mode) {
            case HALF_UP -> x -> x >= 0 ? Math.floor(x + 0.5) : Math.ceil(x - 0.5);
            case HALF_DOWN -> x -> x >= 0 ? Math.ceil(x - 0.5) : Math.floor(x + 0.5);
            case HALF_EVEN -> Math::rint;
            case UP -> x -> x >= 0 ? Math.ceil(x) : Math.floor(x);
            case DOWN -> x -> x >= 0 ? Math.floor(x) : Math.ceil(x);
            case FLOOR -> Math::floor;
            case CEILING -> Math::ceil;
            case UNNECESSARY -> x -> x;
        };
    }

    /**
     * The mathematical constant π (pi), defined as the ratio of a circle's circumference
     * to its diameter. This constant is widely used in mathematics, physics, and engineering.
     * It has an approximate value of 3.141592653589793.
     */
    public static final double PI = Math.PI;

    /**
     * Represents the constant 2π (in degrees: 360°).
     */
    public static final double TWO_PI = 2.0 * Math.PI;

    /**
     * Represents the constant π/2 (in degrees: 90°).
     */
    public static final double PI_HALF = Math.PI / 2.0;

    /**
     * Represents the constant π/2 (in degrees: 45°).
     */
    public static final double PI_QUARTER = Math.PI / 4.0;

    /**
     * Represents the constant π/180 (in degrees: 1°).
     */
    public static final double PI_DIV_180 = Math.PI / 180.0;

    /**
     * Converts an angle measured in degrees to an equivalent angle measured in radians.
     *
     * @param alpha the angle in degrees
     * @return the angle in radians
     */
    public static double rad(double alpha) {
        return Math.toRadians(alpha);
    }

    /**
     * Converts an angle measured in degrees to an equivalent angle measured in radians.
     *
     * @param alpha the angle in degrees
     * @return the angle in radians
     */
    public static float radf(float alpha) {
        return (float) Math.toRadians(alpha);
    }

    /**
     * Converts an angle from radians to degrees.
     *
     * @param phi the angle in radians
     * @return the angle in degrees
     */
    public static double deg(double phi) {
        return Math.toDegrees(phi);
    }

    /**
     * Converts an angle from radians to degrees.
     *
     * @param phi the angle in radians
     * @return the angle in degrees
     */
    public static float degf(float phi) {
        return (float) Math.toDegrees(phi);
    }

    /**
     * Normalizes the given angle in radians to the range [0, 2π).
     *
     * @param phi the angle in radians to be normalized
     * @return the normalized angle in radians within the range [0, 2π)
     */
    public static double normalizeRadians(double phi) {
        phi = phi % MathUtil.TWO_PI;
        if (phi < 0.0) {
            phi += MathUtil.TWO_PI;
        }
        return phi;
    }

    /**
     * Normalizes the given angle in radians to the range [0, 360).
     *
     * @param alpha the angle in degrees to be normalized
     * @return the normalized angle in degrees within the range [0, 360)
     */
    public static double normalizeDegrees(double alpha) {
        alpha = alpha % 360.0;
        if (alpha < 0.0) {
            alpha += 360.0;
        }
        return alpha;
    }

    /**
     * Determines the quadrant number (1-based) of an angle given in radians.
     *
     * @param phi The angle in radians.
     * @return The quadrant number (1 to 4) in which the given angle lies.
     */
    public static int quadrantRadians(double phi) {
        return 1 + quadrantIndexRadians(phi);
    }

    /**
     * Determines the quadrant index (0-based) of an angle given in radians.
     *
     * @param phi The angle in radians.
     * @return The quadrant number (0 to 3) in which the given angle lies.
     */
    public static int
    quadrantIndexRadians(double phi) {
        return (int) (normalizeRadians(phi) / PI_HALF);
    }

    /**
     * Determines the quadrant number (1-based) of an angle given in degrees.
     *
     * @param alpha The angle in degrees.
     * @return The quadrant number (1 to 4) in which the given angle lies.
     */
    public static int quadrantDegrees(double alpha) {
        return 1 + quadrantIndexDegrees(alpha);
    }

    /**
     * Determines the quadrant index (0-based) of an angle given in degrees.
     *
     * @param alpha The angle in degrees.
     * @return The quadrant number (0 to 3) in which the given angle lies.
     */
    public static int quadrantIndexDegrees(double alpha) {
        return (int) (normalizeDegrees(alpha) / 90.0);
    }

    /**
     * Determines the octant number (1-based) of an angle given in radians.
     *
     * @param phi The angle in radians.
     * @return The octant number (1 to 8) in which the given angle lies.
     */
    public static int octantRadians(double phi) {
        return 1 + octantIndexRadians(phi);
    }

    /**
     * Determines the octant index (0-based) of an angle given in radians.
     *
     * @param phi The angle in radians.
     * @return The octant number (0 to 7) in which the given angle lies.
     */
    public static int
    octantIndexRadians(double phi) {
        return (int) (normalizeRadians(phi) / PI_QUARTER);
    }

    /**
     * Determines the octant number (1-based) of an angle given in degrees.
     *
     * @param alpha The angle in degrees.
     * @return The octant number (1 to 8) in which the given angle lies.
     */
    public static int octantDegrees(double alpha) {
        return 1 + octantIndexDegrees(alpha);
    }

    /**
     * Determines the octant index (0-based) of an angle given in degrees.
     *
     * @param alpha The angle in degrees.
     * @return The octant number (0 to 7) in which the given angle lies.
     */
    public static int octantIndexDegrees(double alpha) {
        return (int) (normalizeDegrees(alpha) / 45.0);
    }

}
