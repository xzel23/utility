package text;

import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.TextUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for TextUtil.wrap method.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class TextWrapBenchmark {

    private String shortText;
    private String longText;
    private String textWithParagraphs;

    @Setup
    public void setup() {
        shortText = "Hello, world!";

        longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        textWithParagraphs = """
                First paragraph with some text that should be wrapped.
                
                Second paragraph with different content that also needs to be wrapped properly.
                
                Third paragraph with even more text to ensure we have enough content for testing the line splitter functionality.""";
    }

    @Benchmark
    public void wrap_shortText_noWrap(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(shortText, 80, Alignment.LEFT, false));
    }

    @Benchmark
    public void wrap_longText_noWrap(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, Alignment.LEFT, false));
    }

    @Benchmark
    public void wrap_longText_hardWrap(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, Alignment.LEFT, true));
    }

    @Benchmark
    public void wrap_longText_narrowWidth(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 40, Alignment.LEFT, false));
    }

    @Benchmark
    public void wrap_textWithParagraphs(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(textWithParagraphs, 80, Alignment.LEFT, false));
    }

    @Benchmark
    public void wrap_textWithParagraphs_hardWrap(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(textWithParagraphs, 80, Alignment.LEFT, true));
    }

    @Benchmark
    public void wrap_longText_center(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, Alignment.CENTER, false));
    }

    @Benchmark
    public void wrap_longText_right(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, Alignment.RIGHT, false));
    }

    @Benchmark
    public void wrap_longText_justify(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, Alignment.JUSTIFY, false));
    }
}