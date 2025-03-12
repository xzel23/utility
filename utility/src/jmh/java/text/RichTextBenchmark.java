package text;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * JMH benchmarks for RichText class.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RichTextBenchmark {

    private String shortText;
    private String longText;
    private RichText shortRichText;
    private RichText longRichText;
    private RichText styledRichText;
    private Pattern pattern;

    @Setup
    public void setup() {
        shortText = "Hello, world!";
        longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        shortRichText = RichText.valueOf(shortText);
        longRichText = RichText.valueOf(longText);

        Style boldStyle = Style.BOLD;
        Style italicStyle = Style.ITALIC;
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("This is ");
        builder.push(boldStyle);
        builder.append("bold");
        builder.pop(boldStyle);
        builder.append(" and ");
        builder.push(italicStyle);
        builder.append("italic");
        builder.pop(italicStyle);
        builder.append(" text.");
        styledRichText = builder.toRichText();

        pattern = Pattern.compile("\\s+");
    }

    @Benchmark
    public void valueOf_short(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf(shortText));
    }

    @Benchmark
    public void valueOf_long(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf(longText));
    }

    @Benchmark
    public void valueOf_withStyle(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf(shortText, Style.BOLD));
    }

    @Benchmark
    public void toString_short(Blackhole blackhole) {
        blackhole.consume(shortRichText.toString());
    }

    @Benchmark
    public void toString_long(Blackhole blackhole) {
        blackhole.consume(longRichText.toString());
    }

    @Benchmark
    public void equalsText_same(Blackhole blackhole) {
        blackhole.consume(longRichText.equalsText(longText));
    }

    @Benchmark
    public void equalsText_different(Blackhole blackhole) {
        blackhole.consume(longRichText.equalsText(shortText));
    }

    @Benchmark
    public void subSequence(Blackhole blackhole) {
        blackhole.consume(longRichText.subSequence(10, 50));
    }

    @Benchmark
    public void charAt(Blackhole blackhole) {
        blackhole.consume(longRichText.charAt(50));
    }

    @Benchmark
    public void trim(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf("  " + longText + "  ").trim());
    }

    @Benchmark
    public void strip(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf("  " + longText + "  ").strip());
    }

    @Benchmark
    public void split(Blackhole blackhole) {
        blackhole.consume(longRichText.split("\\s+"));
    }

    @Benchmark
    public void split_pattern(Blackhole blackhole) {
        blackhole.consume(longRichText.split(pattern));
    }

    @Benchmark
    public void indexOf_char(Blackhole blackhole) {
        blackhole.consume(longRichText.indexOf('a'));
    }

    @Benchmark
    public void indexOf_string(Blackhole blackhole) {
        blackhole.consume(longRichText.indexOf("dolor"));
    }

    @Benchmark
    public void contains(Blackhole blackhole) {
        blackhole.consume(longRichText.contains("dolor"));
    }

    @Benchmark
    public void apply_style(Blackhole blackhole) {
        blackhole.consume(shortRichText.apply(Style.BOLD));
    }

    @Benchmark
    public void lines(Blackhole blackhole) {
        blackhole.consume(RichText.valueOf("Line 1\nLine 2\nLine 3").lines().count());
    }

    // Benchmarks for styledRichText

    @Benchmark
    public void toString_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.toString());
    }

    @Benchmark
    public void subSequence_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.subSequence(5, 15));
    }

    @Benchmark
    public void split_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.split("\\s+"));
    }

    @Benchmark
    public void apply_additional_style(Blackhole blackhole) {
        blackhole.consume(styledRichText.apply(Style.UNDERLINE));
    }

    @Benchmark
    public void stylesAt_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.stylesAt(10));
    }

    @Benchmark
    public void runAt_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.runAt(10));
    }

    @Benchmark
    public void runs_styled(Blackhole blackhole) {
        blackhole.consume(styledRichText.runs());
    }
}
