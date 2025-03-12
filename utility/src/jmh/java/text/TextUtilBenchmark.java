package text;

import com.dua3.utility.text.TextUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for TextUtil class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class TextUtilBenchmark {

    private String shortText;
    private String longText;
    private String htmlText;
    private String templateText;
    private Map<String, String> substitutions;

    @Setup
    public void setup() {
        shortText = "Hello, world!";
        longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        htmlText = "<p>This is a <strong>test</strong> with some <em>HTML</em> tags & special characters.</p>";
        templateText = "Hello, ${name}! Welcome to ${place}.";
        substitutions = Map.of("name", "John", "place", "Wonderland");
    }

    @Benchmark
    public void escapeHTML_short(Blackhole blackhole) {
        blackhole.consume(TextUtil.escapeHTML(shortText));
    }

    @Benchmark
    public void escapeHTML_long(Blackhole blackhole) {
        blackhole.consume(TextUtil.escapeHTML(longText));
    }

    @Benchmark
    public void escapeHTML_withHtmlTags(Blackhole blackhole) {
        blackhole.consume(TextUtil.escapeHTML(htmlText));
    }

    @Benchmark
    public void escape_short(Blackhole blackhole) {
        blackhole.consume(TextUtil.escape(shortText));
    }

    @Benchmark
    public void escape_long(Blackhole blackhole) {
        blackhole.consume(TextUtil.escape(longText));
    }

    @Benchmark
    public void transform_template(Blackhole blackhole) {
        blackhole.consume(TextUtil.transform(templateText, substitutions));
    }

    @Benchmark
    public void normalizeLineEnds(Blackhole blackhole) {
        blackhole.consume(TextUtil.normalizeLineEnds(longText));
    }

    @Benchmark
    public void toUnixLineEnds(Blackhole blackhole) {
        blackhole.consume(TextUtil.toUnixLineEnds(longText));
    }

    @Benchmark
    public void toWindowsLineEnds(Blackhole blackhole) {
        blackhole.consume(TextUtil.toWindowsLineEnds(longText));
    }

    @Benchmark
    public void contentEquals_same(Blackhole blackhole) {
        blackhole.consume(TextUtil.contentEquals(longText, longText));
    }

    @Benchmark
    public void contentEquals_different(Blackhole blackhole) {
        blackhole.consume(TextUtil.contentEquals(longText, shortText));
    }

    @Benchmark
    public void indexOf_first(Blackhole blackhole) {
        blackhole.consume(TextUtil.indexOf(longText, "Lorem"));
    }

    @Benchmark
    public void indexOf_middle(Blackhole blackhole) {
        blackhole.consume(TextUtil.indexOf(longText, "dolor"));
    }

    @Benchmark
    public void indexOf_last(Blackhole blackhole) {
        blackhole.consume(TextUtil.indexOf(longText, "laborum"));
    }

    @Benchmark
    public void indexOf_notFound(Blackhole blackhole) {
        blackhole.consume(TextUtil.indexOf(longText, "xyz"));
    }

    @Benchmark
    public void getMD5String(Blackhole blackhole) {
        blackhole.consume(TextUtil.getMD5String(longText));
    }

    @Benchmark
    public void base64Encode(Blackhole blackhole) {
        blackhole.consume(TextUtil.base64Encode(longText.getBytes(StandardCharsets.UTF_8)));
    }

    @Benchmark
    public void wrap_text(Blackhole blackhole) {
        blackhole.consume(TextUtil.wrap(longText, 80, com.dua3.utility.text.Alignment.LEFT, false));
    }
}