package com.dua3.utility.ui;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InlineNodeTest {

    @Test
    void testBasicPropertiesAndDefensiveCopy() {
        byte[] payload = new byte[]{1, 2, 3};
        InlineNode<String> node = new InlineNode<>("wrapped", "image/png", payload);

        assertEquals("wrapped", node.getWrapped());
        assertEquals("image/png", node.getMimeType());
        assertArrayEquals(payload, node.getData());

        // Modify input array and ensure node data is unchanged
        payload[0] = 99;
        assertNotEquals(99, node.getData()[0]);

        // Modify returned array and ensure node data is unchanged
        byte[] retrieved = node.getData();
        retrieved[0] = 88;
        assertNotEquals(88, node.getData()[0]);
    }

    @Test
    void testEncodeAndDecodeArgbImageData() {
        int width = 2;
        int height = 2;
        int[] argb = new int[]{0xFF000000, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        Image image = ImageUtil.getInstance().createImage(width, height, argb);

        byte[] encoded = InlineNode.encodeArgbImageData(image);
        Image decoded = InlineNode.decodeArgbImageData(encoded);

        assertEquals(width, decoded.width());
        assertEquals(height, decoded.height());
        assertArrayEquals(argb, decoded.getArgb());
    }

    @Test
    void testDecodeArgbImageDataInvalidPayload() {
        assertThrows(IllegalArgumentException.class, () -> InlineNode.decodeArgbImageData(new byte[4])); // too small
        assertThrows(IllegalArgumentException.class, () -> InlineNode.decodeArgbImageData(new byte[]{
                0, 0, 0, 0, // width = 0
                0, 0, 0, 1  // height = 1
        }));
        assertThrows(IllegalArgumentException.class, () -> InlineNode.decodeArgbImageData(new byte[]{
                0, 0, 0, 2, // width = 2
                0, 0, 0, 2, // height = 2
                0, 0, 0, 0  // only 1 pixel instead of 4
        }));
    }

    @Test
    void testEqualsAndHashCode() {
        InlineNode<String> n1 = new InlineNode<>("w1", "image/png", new byte[]{1, 2});
        InlineNode<String> n2 = new InlineNode<>("w1", "image/png", new byte[]{1, 2});
        InlineNode<String> n3 = new InlineNode<>("w2", "image/png", new byte[]{1, 2});
        InlineNode<String> n4 = new InlineNode<>("w1", "image/jpeg", new byte[]{1, 2});
        InlineNode<String> n5 = new InlineNode<>("w1", "image/png", new byte[]{1, 3});

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
        assertNotEquals(n1, n3);
        assertNotEquals(n1, n4);
        assertNotEquals(n1, n5);
        assertNotEquals(null, n1);
        assertNotEquals("string", n1);
    }
}
