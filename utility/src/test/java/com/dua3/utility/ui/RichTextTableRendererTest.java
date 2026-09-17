package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextTableRendererTest {

    record LineCall(float x1, float y1, float x2, float y2) {}

    @Test
    void testRenderTable() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ID, 1);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ROW, 0);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER, true);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN, 0);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN_ALIGNMENT, "LEFT");
        builder.push(Style.create("bg", Map.of(Style.BACKGROUND_COLOR, Color.YELLOW)));
        builder.append("Header").append('\t');
        builder.pop(Style.create("bg", Map.of(Style.BACKGROUND_COLOR, Color.YELLOW)));
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN_ALIGNMENT);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ROW);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ID);

        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ID, 1);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ROW, 0);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER, true);
        builder.append('\n');
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ROW);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ID);

        RichText text = builder.toRichText();
        List<RichTextTableHelper.Table> tables = RichTextTableHelper.tables(text);
        assertFalse(tables.isEmpty());

        Font font = FontUtil.getInstance().getDefaultFont();
        RichTextTableHelper.TableLayout layout = RichTextTableHelper.layout(
                tables.getFirst(),
                FontUtil.getInstance(),
                font,
                200.0f,
                true,
                2.0f
        );

        List<Rectangle2f> filledRects = new ArrayList<>();
        List<LineCall> strokedLines = new ArrayList<>();
        List<String> renderedTexts = new ArrayList<>();

        Graphics graphics = (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("fillRect".equals(name) && args.length == 1 && args[0] instanceof Rectangle2f r) {
                        filledRects.add(r);
                    }
                    if ("strokeLine".equals(name) && args.length == 4) {
                        strokedLines.add(new LineCall((Float) args[0], (Float) args[1], (Float) args[2], (Float) args[3]));
                    }
                    if ("drawText".equals(name) && args.length == 5) {
                        renderedTexts.add((String) args[0]);
                    }
                    return null;
                }
        );

        RichTextTableRenderer.render(graphics, layout);

        assertFalse(filledRects.isEmpty(), "Background color should be filled");
        assertFalse(strokedLines.isEmpty(), "Grid lines should be stroked");
        assertFalse(renderedTexts.isEmpty(), "Cell text should be rendered");
        assertTrue(renderedTexts.contains("Header"));
    }
}
