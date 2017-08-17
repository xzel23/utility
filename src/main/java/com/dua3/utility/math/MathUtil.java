package com.dua3.utility.math;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author axel
 */
public final class MathUtil {

    /**
     * Clip argument to range.
     *
     * @param min
     *            minimal value
     * @param max
     *            maximum value
     * @param arg
     *            argument
     * @return
     *         <ul>
     *         <li>min, if arg &lt; min
     *         <li>max, if arg &gt; max
     *         <li>else arg
     *         </ul>
     */
    public static int clamp(int min, int max, int arg) {
        assert min <= max;

        if (arg < min) {
            return min;
        }

        if (arg > max) {
            return max;
        }

        return arg;
    }

    /**
     * Find root of function.
     *
     * The root finding uses a modified secant algorithm. The values given as staring points should
     * be rough approximations of the root, ie the algorithm might fail if the second order
     * derivative changes sign in the interval [xa,root] or [xb,root].
     *
     * @param f
     *            the function for which the root shall be calculated
     * @param xa
     *            first starting point
     * @param xb
     *            second starting point
     * @param eps
     *            desired accuracy
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
     *
     * The function is evaluated a fixed number of times to find starting values for root finding.
     *
     * @param f
     *            the function
     * @param x0
     *            first limit of interval
     * @param x1
     *            second limit of interval, must be different from {@code x0}
     * @param steps
     *            maximum number of iterations
     * @param eps
     *            maximum error
     * @return list of all calculated roots
     */
    public static List<Double> findRootsInInterval(DoubleUnaryOperator f, double x0, double x1, int steps, double eps) {
        if (x0 == x1) {
            throw new IllegalArgumentException("Empty interval.");
        }

        if (x0 > x1) {
            double tmp = x0;
            x0 = x1;
            x1 = tmp;
        }

        ArrayList<Double> roots = new ArrayList<>();
        double step = (x1 - x0) / steps;
        double xa = x0, ya = f.applyAsDouble(xa);
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
                } catch (Exception e) {
                    // nop
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
     * @param a
     *            first argument
     * @param b
     *            second argument
     * @return greatest common divisor of a and b.
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long h = a % b;
            a = b;
            b = h;
        }
        return a;
    }

    /**
     * Calculate ceil(log10(x)).
     *
     * @param x
     *            argument
     * @return ceil(log10(x))
     */
    public static int ilog10(double x) {
        if (x <= 0 || Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException(Double.toString(x));
        } else if (x >= 1.0) {
            int i = 0;
            while ((x /= 10.0) >= 1) {
                i++;
            }
            return i;
        } else {
            int i = -1;
            while ((x *= 10.0) < 1) {
                i--;
            }
            return i;
        }
    }

    /**
     * Test if number is integral.
     * 
     * @param a
     *            the number to test
     * @return
     *         true, if {@code a} is integral
     */
    public static boolean isIntegral(double a) {
        return !Double.isNaN(a) && a == Math.floor(a);
    }

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
     *
     * @param x
     *            value to round
     * @param n
     *            number of decimal places
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

    public static double roundToPrecision(double x, int p) {
        if (x == 0 || Double.isNaN(x) || Double.isInfinite(x)) {
            return x;
        }

        int n = p - ilog10(Math.abs(x)) - 1;
        return round(x, n);
    }

    public static String toDecimalString(double xm, int digits) {
        StringBuilder sb = new StringBuilder();

        // sign
        final String sign;
        if (xm < 0) {
            sign = "-";
            xm = -xm;
        } else {
            sign = "";
        }

        // avoid error due to imprecise decimal representation
        final double korr = pow10(-(digits + 1));
        xm += korr;

        // integer part
        final int xi = (int) round(xm, digits);
        sb.append(xi);
        xm -= xi;

        boolean showSign = xi != 0;
        if (digits > 0) {
            sb.append(".");
            int k = 10;
            for (int i = 0; i < digits; i++) {
                final int d = ((int) (xm * k)) % 10;
                sb.append((char) ('0' + d));
                k *= 10;
                showSign |= d != 0;
            }
        }

        return showSign ? sign + sb.toString() : sb.toString();
    }

    /**
     * Utility class - private constructor.
     */
    private MathUtil() {
    }
}
