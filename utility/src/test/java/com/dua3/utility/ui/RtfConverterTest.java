package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RtfConverterTest {

    @Test
    void testRoundTripRichText() {
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("Bold");
        builder.pop(Style.BOLD);
        builder.append(" ");
        builder.push(Style.ITALIC);
        builder.push(Style.RED);
        builder.append("ItalicRed");
        builder.pop(Style.RED);
        builder.pop(Style.ITALIC);
        builder.append("\nSecond line");

        RichText expected = builder.toRichText();

        String rtf = rtfConverter.toRtf(expected);
        RichText actual = rtfConverter.fromRtf(rtf);

        assertEquals(expected.toString(), actual.toString());

        int boldPos = actual.indexOf("Bold");
        assertEquals(Boolean.TRUE, actual.runAt(boldPos).getFontDef().getBold());

        int italicRedPos = actual.indexOf("ItalicRed");
        assertEquals(Boolean.TRUE, actual.runAt(italicRedPos).getFontDef().getItalic());
        assertEquals(Color.RED, actual.runAt(italicRedPos).getFontDef().getColor());
    }
}

