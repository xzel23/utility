package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.RtfConverter;
import com.dua3.utility.text.Run;
import com.dua3.utility.ui.InlineNode;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextBuilderFxRtfRoundTripTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRoundTripInlineImageViaRtf() throws Exception {
        runOnFxThreadAndWait(() -> {
            RtfConverter converter = RtfConverter.get().orElse(null);
            Assumptions.assumeTrue(converter != null, "RtfConverter not available");

            Image image = ImageUtil.getInstance().createImage(
                    2,
                    2,
                    new int[]{
                            Color.RED.argb(), Color.GREEN.argb(),
                            Color.BLUE.argb(), Color.YELLOW.argb()
                    }
            );

            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("A");
            builder.appendImage(image);
            builder.append("B");
            RichText expected = builder.toRichText();

            String rtf = converter.fromRichText(expected);
            RichText actual = converter.toRichText(rtf);

            assertEquals(expected.toString(), actual.toString());
            assertTrue(rtf.contains("\\pict"));

            int inlinePos = actual.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER);
            assertTrue(inlinePos >= 0, "missing inline node marker after RTF round trip");
            Run run = actual.runAt(inlinePos);

            Object inlineAttribute = run.getStyles().stream()
                    .map(style -> style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow();

            InlineNode<?> imported = assertInstanceOf(InlineNode.class, inlineAttribute);
            Image importedImage = InlineNode.decodeArgbImageData(imported.getData());

            assertEquals(image.width(), importedImage.width());
            assertEquals(image.height(), importedImage.height());
            assertTrue(Arrays.equals(image.getArgb(), importedImage.getArgb()));
        });
    }
}
