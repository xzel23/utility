package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextPaneLayoutHelperTest {

    @Test
    void testStyleInvisibleText() {
        assertEquals(Color.TRANSPARENT_BLACK, RichTextPaneLayoutHelper.STYLE_INVISIBLE_TEXT.get(Style.COLOR));
    }

    @Test
    void testLayoutTextDataMappingAndClamping() {
        RichText text = RichText.valueOf("abc");
        int[] layoutToSource = new int[]{0, 1, 2, 3};
        int[] sourceToLayout = new int[]{0, 1, 2, 3};

        RichTextPaneLayoutHelper.LayoutTextData data = new RichTextPaneLayoutHelper.LayoutTextData(text, layoutToSource, sourceToLayout);

        assertEquals(text, data.text());
        assertEquals(0, data.layoutToSourcePosition(-5));
        assertEquals(2, data.layoutToSourcePosition(2));
        assertEquals(3, data.layoutToSourcePosition(100));

        assertEquals(0, data.sourceToLayoutPosition(-5));
        assertEquals(1, data.sourceToLayoutPosition(1));
        assertEquals(3, data.sourceToLayoutPosition(50));
    }

    @Test
    void testLayoutTextDataEqualsAndHashCode() {
        RichText text = RichText.valueOf("abc");
        int[] l2s = new int[]{0, 1, 2};
        int[] s2l = new int[]{0, 1, 2};

        RichTextPaneLayoutHelper.LayoutTextData d1 = new RichTextPaneLayoutHelper.LayoutTextData(text, l2s, s2l);
        RichTextPaneLayoutHelper.LayoutTextData d2 = new RichTextPaneLayoutHelper.LayoutTextData(text, new int[]{0, 1, 2}, new int[]{0, 1, 2});
        RichTextPaneLayoutHelper.LayoutTextData d3 = new RichTextPaneLayoutHelper.LayoutTextData(text, new int[]{0, 1, 3}, new int[]{0, 1, 2});

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(null, d1);
        assertTrue(d1.toString().contains("LayoutTextData{text=abc"));
    }

    @Test
    void testHasInlineNode() {
        Style inlineStyle = Style.create("inline", Map.of(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, "node"));
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(inlineStyle);
        builder.append("X");
        builder.pop(inlineStyle);

        RichText richText = builder.toRichText();
        Run run = richText.runs().getFirst();
        assertTrue(RichTextPaneLayoutHelper.hasInlineNode(run));

        RichText plain = RichText.valueOf("hello");
        assertFalse(RichTextPaneLayoutHelper.hasInlineNode(plain.runs().getFirst()));
    }

    @Test
    void testPrepareLayout() {
        RichText text = RichText.valueOf("Hello world\nSecond line");
        Font font = FontUtil.getInstance().getDefaultFont();

        RichTextPaneLayoutHelper.LayoutPreparation prep = RichTextPaneLayoutHelper.prepareLayout(
                text,
                font,
                true,
                300.0,
                "leading-width",
                (run, f) -> null,
                obj -> 0.0
        );

        assertNotNull(prep);
        assertNotNull(prep.layoutTextData());
        assertNotNull(prep.renderFragments());
        assertTrue(prep.renderWidth() > 0);
    }
}
