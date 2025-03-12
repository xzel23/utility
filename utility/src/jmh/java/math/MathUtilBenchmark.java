package math;

import com.dua3.utility.math.MathUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

/**
 * JMH benchmarks for MathUtil class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class MathUtilBenchmark {

    // Test data
    private double smallValue;
    private double largeValue;
    private double negativeValue;
    private double angleInDegrees;
    private double angleInRadians;
    private DoubleUnaryOperator testFunction;
    private DoubleUnaryOperator roundingOp;

    @Setup
    public void setup() {
        smallValue = 0.12345;
        largeValue = 12345.6789;
        negativeValue = -123.456;
        angleInDegrees = 45.0;
        angleInRadians = Math.PI / 4;

        // Function for root finding: f(x) = x^2 - 4
        testFunction = x -> x * x - 4;

        // Pre-create a rounding operation
        roundingOp = MathUtil.roundingOperation(2, RoundingMode.HALF_UP);
    }

    @Benchmark
    public void clamp_int(Blackhole blackhole) {
        blackhole.consume(MathUtil.clamp(0, 100, 50));
        blackhole.consume(MathUtil.clamp(0, 100, -10));
        blackhole.consume(MathUtil.clamp(0, 100, 200));
    }

    @Benchmark
    public void clamp_double(Blackhole blackhole) {
        blackhole.consume(MathUtil.clamp(0.0, 100.0, 50.5));
        blackhole.consume(MathUtil.clamp(0.0, 100.0, -10.5));
        blackhole.consume(MathUtil.clamp(0.0, 100.0, 200.5));
    }

    @Benchmark
    public void findRoot(Blackhole blackhole) {
        // Find the root of x^2 - 4 = 0 (should be 2)
        blackhole.consume(MathUtil.findRoot(testFunction, 1.0, 3.0, 1e-10));
    }

    @Benchmark
    public void findRootsInInterval(Blackhole blackhole) {
        // Find the roots of x^2 - 4 = 0 in [-3, 3] (should be -2 and 2)
        blackhole.consume(MathUtil.findRootsInInterval(testFunction, -3.0, 3.0, 100, 1e-10));
    }

    @Benchmark
    public void gcd(Blackhole blackhole) {
        blackhole.consume(MathUtil.gcd(48, 18));
        blackhole.consume(MathUtil.gcd(1071, 462));
    }

    @Benchmark
    public void ilog10_small(Blackhole blackhole) {
        // Values between 0 and 1 to test the x < 1.0 branch
        blackhole.consume(MathUtil.ilog10(0.1));
        blackhole.consume(MathUtil.ilog10(0.01));
        blackhole.consume(MathUtil.ilog10(0.001));
    }

    @Benchmark
    public void ilog10_large(Blackhole blackhole) {
        // Values > 1 to test the x >= 1.0 branch
        blackhole.consume(MathUtil.ilog10(10.0));
        blackhole.consume(MathUtil.ilog10(100.0));
        blackhole.consume(MathUtil.ilog10(1000.0));
    }

    @Benchmark
    public void isIntegral(Blackhole blackhole) {
        blackhole.consume(MathUtil.isIntegral(123.0));
        blackhole.consume(MathUtil.isIntegral(123.45));
    }

    @Benchmark
    public void pow10(Blackhole blackhole) {
        for (int i = -5; i <= 5; i++) {
            blackhole.consume(MathUtil.pow10(i));
        }
    }

    @Benchmark
    public void round_small(Blackhole blackhole) {
        blackhole.consume(MathUtil.round(smallValue, 2));
    }

    @Benchmark
    public void round_large(Blackhole blackhole) {
        blackhole.consume(MathUtil.round(largeValue, 2));
    }

    @Benchmark
    public void round_negative(Blackhole blackhole) {
        blackhole.consume(MathUtil.round(negativeValue, 2));
    }

    @Benchmark
    public void roundToPrecision(Blackhole blackhole) {
        blackhole.consume(MathUtil.roundToPrecision(largeValue, 3));
    }

    @Benchmark
    public void roundingOperation_apply(Blackhole blackhole) {
        blackhole.consume(roundingOp.applyAsDouble(smallValue));
        blackhole.consume(roundingOp.applyAsDouble(largeValue));
        blackhole.consume(roundingOp.applyAsDouble(negativeValue));
    }

    @Benchmark
    public void getRoundingOperation(Blackhole blackhole) {
        blackhole.consume(MathUtil.getRoundingOperation(RoundingMode.HALF_UP).applyAsDouble(1.5));
    }

    @Benchmark
    public void rad(Blackhole blackhole) {
        blackhole.consume(MathUtil.rad(angleInDegrees));
    }

    @Benchmark
    public void deg(Blackhole blackhole) {
        blackhole.consume(MathUtil.deg(angleInRadians));
    }

    @Benchmark
    public void normalizeRadians(Blackhole blackhole) {
        blackhole.consume(MathUtil.normalizeRadians(3 * Math.PI));
    }

    @Benchmark
    public void normalizeDegrees(Blackhole blackhole) {
        blackhole.consume(MathUtil.normalizeDegrees(370.0));
    }

    @Benchmark
    public void quadrantRadians(Blackhole blackhole) {
        blackhole.consume(MathUtil.quadrantRadians(angleInRadians));
        blackhole.consume(MathUtil.quadrantRadians(angleInRadians + Math.PI / 2));
        blackhole.consume(MathUtil.quadrantRadians(angleInRadians + Math.PI));
        blackhole.consume(MathUtil.quadrantRadians(angleInRadians + 3 * Math.PI / 2));
    }

    @Benchmark
    public void quadrantDegrees(Blackhole blackhole) {
        blackhole.consume(MathUtil.quadrantDegrees(angleInDegrees));
        blackhole.consume(MathUtil.quadrantDegrees(angleInDegrees + 90));
        blackhole.consume(MathUtil.quadrantDegrees(angleInDegrees + 180));
        blackhole.consume(MathUtil.quadrantDegrees(angleInDegrees + 270));
    }

    @Benchmark
    public void octantRadians(Blackhole blackhole) {
        blackhole.consume(MathUtil.octantRadians(angleInRadians));
    }

    @Benchmark
    public void octantDegrees(Blackhole blackhole) {
        blackhole.consume(MathUtil.octantDegrees(angleInDegrees));
    }
}
