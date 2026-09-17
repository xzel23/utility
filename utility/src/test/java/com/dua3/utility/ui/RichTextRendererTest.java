package com.dua3.utility.ui;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichTextRendererTest {

    record DrawTextCall(String text, float x, float y, HAnchor hAnchor, VAnchor vAnchor) {}

    @Test
    void testRenderFragmentLines() {
        List<DrawTextCall> drawCalls = new ArrayList<>();
        List<Font> setFonts = new ArrayList<>();

        Graphics graphics = (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class},
                (proxy, method, args) -> {
                    if ("setFont".equals(method.getName())) {
                        setFonts.add((Font) args[0]);
                        return null;
                    }
                    if ("drawText".equals(method.getName()) && args.length == 5) {
                        drawCalls.add(new DrawTextCall((String) args[0], (Float) args[1], (Float) args[2], (HAnchor) args[3], (VAnchor) args[4]));
                        return null;
                    }
                    return null;
                }
        );

        Font font = FontUtil.getInstance().getDefaultFont();
        FragmentedText.Fragment frag1 = new FragmentedText.Fragment(0f, 0f, 20f, 12f, 10f, font, RichText.valueOf("Hello"));
        FragmentedText.Fragment frag2 = new FragmentedText.Fragment(20f, 0f, 20f, 12f, 10f, font, RichText.valueOf("world"));

        List<List<FragmentedText.Fragment>> lines = List.of(List.of(frag1, frag2));

        RichTextRenderer.renderFragmentLines(graphics, lines);

        assertEquals(List.of(font, font), setFonts);

        assertEquals(2, drawCalls.size());
        assertEquals("Hello", drawCalls.get(0).text());
        assertEquals(0f, drawCalls.get(0).x());
        assertEquals(HAnchor.LEFT, drawCalls.get(0).hAnchor());
        assertEquals(VAnchor.BASELINE, drawCalls.get(0).vAnchor());

        assertEquals("world", drawCalls.get(1).text());
        assertEquals(20f, drawCalls.get(1).x());
    }

    @Test
    void testRenderFragmentLinesWithExcludedFromBaseline() {
        List<DrawTextCall> drawCalls = new ArrayList<>();

        Graphics graphics = (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class},
                (proxy, method, args) -> {
                    if ("drawText".equals(method.getName()) && args.length == 5) {
                        drawCalls.add(new DrawTextCall((String) args[0], (Float) args[1], (Float) args[2], (HAnchor) args[3], (VAnchor) args[4]));
                    }
                    return null;
                }
        );

        Font font = FontUtil.getInstance().getDefaultFont();
        FragmentedText.Fragment frag1 = new FragmentedText.Fragment(0f, 0f, 20f, 12f, 10f, font, RichText.valueOf("A"));
        FragmentedText.Fragment frag2 = new FragmentedText.Fragment(20f, 0f, 20f, 12f, 10f, font, RichText.valueOf("B"));

        List<List<FragmentedText.Fragment>> lines = List.of(List.of(frag1, frag2));

        RichTextRenderer.renderFragmentLines(graphics, lines, f -> f == frag1);

        assertEquals(2, drawCalls.size());
    }
}
