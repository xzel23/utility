package com.dua3.utility.ui;

import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextVisualLayoutHelperTest {

    @Test
    void testTrailingWhitespaceExtendsCaretBoundaryOnLastVisualLine() {
        RichText source = RichText.valueOf("abc  ");
        List<RichTextVisualLayoutHelper.LogicalBlock> blocks = RichTextVisualLayoutHelper.splitLogicalBlocks(source);
        FontUtil fontUtil = FontUtil.getInstance();

        List<VisualLine> lines = RichTextVisualLayoutHelper.buildVisualLines(
                blocks,
                12.0,
                fontUtil,
                block -> {
                    RichText trimmed = block.stripTrailing();
                    Run run = trimmed.runs().getFirst();
                    var runFont = fontUtil.getFont(run.getFontDef());
                    float width = (float) fontUtil.getTextWidth(run, runFont);
                    float height = (float) Math.max(1.0, fontUtil.getTextHeight(run, runFont));

                    FragmentedText.Fragment fragment = new FragmentedText.Fragment(
                            0.0f,
                            0.0f,
                            width,
                            height,
                            height,
                            runFont,
                            run
                    );

                    return new RichTextVisualLayoutHelper.BlockLayout(
                            List.of(List.of(fragment)),
                            height,
                            layoutPos -> layoutPos
                    );
                }
        );

        VisualLine line = lines.getFirst();
        assertEquals(source.length(), line.end(), "line end must include trailing whitespace");

        double xAtNonWhitespaceEnd = RichTextVisualLayoutHelper.xForIndex(line, 3);
        double xAtLineEnd = RichTextVisualLayoutHelper.xForIndex(line, source.length());
        assertTrue(xAtLineEnd > xAtNonWhitespaceEnd, "caret at line end must advance into trailing whitespace");
    }
}
