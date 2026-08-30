package com.dua3.utility.fx.controls;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPaneStructuralMarkerTest extends FxTestBase {

    @Test
    void identifiesOnlyRunsContainingSplitMarkers() {
        Run marker = RichText.valueOf((char) RichText.SPLIT_MARKER).runAt(0);
        Run text = RichText.valueOf("text").runAt(0);
        Run empty = text.subSequence(0, 0);

        assertTrue(isStructuralMarker(marker));
        assertFalse(isStructuralMarker(text));
        assertFalse(isStructuralMarker(empty));
    }

    private static boolean isStructuralMarker(Run run) {
        try {
            Method method = TextPane.class.getDeclaredMethod("isStructuralMarker", Run.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, run);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }
}
