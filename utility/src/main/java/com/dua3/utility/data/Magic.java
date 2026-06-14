package com.dua3.utility.data;

import com.dua3.utility.lang.LangUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The Magic class provides functionality to determine the MIME type of a file or data
 * based on its magic number. It compares the provided magic number against a list of
 * predefined magic numbers to identify MIME types.
 */
public class Magic {

    /**
     * A constant representing the default MIME type used when a file's or data's MIME type
     * cannot be determined. This typically serves as a fallback MIME type for unknown or
     * unrecognized formats.
     * <p>
     * The value is set to "application/octet-stream", which is commonly used to indicate
     * binary data or general-purpose files without a specific MIME designation.
     */
    public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";

    private final List<MagicNumber> magicNumbers;

    /**
     * Represents a magic number used to identify file types by their
     * numeric signatures. Each magic number includes a MIME type, the
     * expected magic number value, and a bitmask for matching.
     */
    interface MagicNumber{
        static MagicNumber of(String mimeType, long magicNumber, long mask) {
            return new SimpleMagicNumber(mimeType, magicNumber, mask);
        }

        static MagicNumber of(String mimeType, String pattern) {
            LangUtil.checkArg(pattern.length() <= 8, "pattern must be at most 8 characters long");

            byte[] bytes = pattern.getBytes(StandardCharsets.UTF_8);
            LangUtil.checkArg(bytes.length == pattern.length(), "pattern contains non-ASCII characters");

            long mask = 0;
            long magicNumber = 0;
            for (int i = 0; i < bytes.length; i++) {
                // Calculate the shift for Big Endian alignment (56, 48, 40, 32...)
                int shift = (7 - i) * 8;

                // Accumulate a full 8-bit mask (0xFF) for this character position
                mask |= 0xFFL << shift;

                // Shift the byte into its proper position
                magicNumber |= ((long) bytes[i] & 0xFFL) << shift;
            }

            return new SimpleMagicNumber(mimeType, magicNumber, mask);
        }

        boolean matches(long magicNumber);

        String mimeType();
    }

    /**
     * Represents a simple implementation of the {@link Magic.MagicNumber} interface for handling
     * file type identification through magic numbers. This class encapsulates the following:
     * <ul>
     * <li> MIME type: A string representing the media type of the associated file.
     * <li> Magic number: A long value representing the unique numeric signature of the file type.
     * <li> Mask: A bitmask value used to match the magic number's relevant bits.
     * </ul>
     * This class provides a concrete implementation to determine whether a given magic number
     * matches the expected signature using the specified mask.
     */
    private record SimpleMagicNumber(String mimeType, long magicNumber, long mask) implements MagicNumber{
        @Override
        public boolean matches(long magicNumber) {
            return (magicNumber & mask) == this.magicNumber;
        }
    }

    /**
     * Constructs a new Magic instance with a provided list of magic numbers.
     * The magic numbers are used to identify the MIME type of files or data
     * based on their numeric signatures. The provided list is copied to ensure
     * the immutability of the internal state of the Magic class.
     *
     * @param magicNumbers the list of {@link Magic.MagicNumber} instances used
     *                     for identifying file types. Each magic number must
     *                     include a MIME type, a numeric signature, and a
     *                     bitmask for matching.
     */
    public Magic(List<MagicNumber> magicNumbers) {
        this.magicNumbers = List.copyOf(magicNumbers);
    }

    /**
     * Determines the MIME type of a file or data based on its numeric signature.
     *
     * @param magicNumber the numeric signature of the file or data used to identify its MIME type
     * @return the MIME type as a string if a match is found; otherwise, returns a default value
     */
    public String getMimeType(long magicNumber) {
        for (MagicNumber mn : magicNumbers) {
            if (mn.matches(magicNumber)) {
                return mn.mimeType();
            }
        }
        return UNKNOWN_MIME_TYPE;
    }
}
