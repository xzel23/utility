package com.dua3.utility.text;

import com.dua3.utility.i18n.I18N;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MessageFormatterTest {

    @Test
    void argumentFactoriesCreateExpectedArguments() {
        MessageFormatter.MessageFormatterArgs args = MessageFormatter.args("Hello, %s!", "World");

        assertEquals("Hello, %s!", args.fmt());
        assertArrayEquals(new Object[]{"World"}, args.args());
        assertEquals(new MessageFormatter.MessageFormatterArgs("Hello, %s!", "World"), args);
        assertEquals(new MessageFormatter.MessageFormatterArgs("Hello, %s!", "World").hashCode(), args.hashCode());

        assertEquals(new MessageFormatter.MessageFormatterArgs("\0", "literal"), MessageFormatter.literal("literal"));
        assertEquals(new MessageFormatter.MessageFormatterArgs("\0", (Object) null), MessageFormatter.literal(null));
        assertEquals(new MessageFormatter.MessageFormatterArgs("\0plain %s", "text"), MessageFormatter.nonI18N("plain %s", "text"));
        assertEquals(new MessageFormatter.MessageFormatterArgs(""), MessageFormatter.empty());
    }

    @Test
    void standardFormatterUsesLocaleAndSpecialArguments() {
        MessageFormatter formatter = MessageFormatter.localized(Locale.GERMANY);

        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("Hello, %s!", "World"));
        assertEquals("1.234,50", formatter.format("%,.2f", 1234.5));
        assertEquals("plain text", formatter.format(MessageFormatter.nonI18N("plain %s", "text")));
        assertEquals("100% complete", formatter.format(MessageFormatter.literal("100% complete")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
        assertEquals("unchanged", formatter.text("unchanged"));
    }

    @Test
    void messageFormatFormatterFormatsMessageFormatPatterns() {
        MessageFormatter formatter = MessageFormatter.messageFormat();

        assertSame(formatter, MessageFormatter.messageFormat());
        assertEquals(MessageFormatter.FormatStyle.MESSAGE_FORMAT, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("Hello, {0}!", "World"));
        assertEquals("There are 2 items.", formatter.format("There are {0,number} items.", 2));
        assertEquals("plain text", formatter.format(MessageFormatter.nonI18N("plain", "text")));
        assertEquals("100% complete", formatter.format(MessageFormatter.literal("100% complete")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
    }

    @Test
    void i18nFormatterLooksUpMessagesAndBypassesLookupForNonI18nText() {
        ResourceBundle bundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"greeting", "Hello, {0}!"}};
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        };
        MessageFormatter formatter = MessageFormatter.i18n(I18N.create(bundle));

        assertEquals(MessageFormatter.FormatStyle.I18N, formatter.getFormatStyle());
        assertEquals("Hello, World!", formatter.format("greeting", "World"));
        assertEquals("greeting World", formatter.format(MessageFormatter.nonI18N("greeting", "World")));
        assertEquals("greeting", formatter.format(MessageFormatter.literal("greeting")));
        assertEquals("", formatter.format(MessageFormatter.empty()));
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void standardFormatterIsSingletonAndUsesDefaultLocale() {
        assertSame(MessageFormatter.standard(), MessageFormatter.standard());
        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, MessageFormatter.standard().getFormatStyle());
    }

    @Test
    void testFactoriesAndFormatting() {
        MessageFormatter fmtStandard = MessageFormatter.standard();
        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, fmtStandard.getFormatStyle());

        MessageFormatter fmtLoc = MessageFormatter.localized(Locale.GERMAN);
        assertEquals(MessageFormatter.FormatStyle.STRING_FORMAT, fmtLoc.getFormatStyle());
        assertEquals("1,5", fmtLoc.format("%.1f", 1.5));
        assertEquals("1,5", fmtLoc.format(MessageFormatter.args("%.1f", 1.5)));

        MessageFormatter fmtMsg = MessageFormatter.messageFormat();
        assertEquals(MessageFormatter.FormatStyle.MESSAGE_FORMAT, fmtMsg.getFormatStyle());
        assertEquals("Hello Alice", fmtMsg.format("Hello {0}", "Alice"));
        assertEquals("Hello Alice", fmtMsg.format(MessageFormatter.args("Hello {0}", "Alice")));

        I18N i18n = I18N.create(new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"key", "value"}, {"test_key", "Result: {0}"}};
            }
        });
        MessageFormatter fmtI18n = MessageFormatter.i18n(i18n);
        assertEquals(MessageFormatter.FormatStyle.I18N, fmtI18n.getFormatStyle());
        assertEquals("value", fmtI18n.format("key"));
        assertEquals("Result: 123", fmtI18n.format("test_key", 123));
        assertEquals("Result: 123", fmtI18n.format(MessageFormatter.args("test_key", 123)));

        MessageFormatter defaultI18n = MessageFormatter.i18n();
        assertEquals(MessageFormatter.FormatStyle.I18N, defaultI18n.getFormatStyle());
    }

    @SuppressWarnings("java:S5845")
    @Test
    void testMessageFormatterArgsDetails() {
        MessageFormatter.MessageFormatterArgs args1 = MessageFormatter.args("fmt %s", "a");
        MessageFormatter.MessageFormatterArgs args2 = MessageFormatter.args("fmt %s", "a");
        MessageFormatter.MessageFormatterArgs args3 = MessageFormatter.args("fmt %s", "b");
        MessageFormatter.MessageFormatterArgs args4 = MessageFormatter.args("other %s", "a");

        assertEquals(args1, args2);
        assertEquals(args1.hashCode(), args2.hashCode());
        org.junit.jupiter.api.Assertions.assertNotEquals(args1, args3);
        org.junit.jupiter.api.Assertions.assertNotEquals(args1, args4);
        org.junit.jupiter.api.Assertions.assertNotEquals(null, args1);
        //noinspection AssertBetweenInconvertibleTypes
        org.junit.jupiter.api.Assertions.assertNotEquals("not an args object", args1);

        org.junit.jupiter.api.Assertions.assertTrue(args1.toString().contains("fmt %s"));
    }
}
