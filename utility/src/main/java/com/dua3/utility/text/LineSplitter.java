package com.dua3.utility.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@SuppressWarnings("MagicCharacter")
final class LineSplitter<S extends CharSequence, R extends Appendable> {
    private final S seq;

    /** The current codepoint. */
    private int codepoint = 0;
    /** The current position in the input sequence. */
    private int pos = 0;
    /** The position of the last split candidate. */
    private int charsForCodepoint = 0;

    private enum ChunkType {
        WHITESPACE,
        WORD,
        PARAGRAPH_BREAK,
        EOT
    }

    /**
     * Represents a chunk of text with a specific type.
     *
     * @param <S> the type of the text chunk
     */
    private record Chunk<S extends CharSequence>(ChunkType type, S text, int start, int end) {
        public int length() {
            return end - start;
        }

        public CharSequence asCharSequence() {
            return text.subSequence(start, end);
        }

        @Override
        public String toString() {
            return String.valueOf(asCharSequence());
        }
    }

    /**
     * Construct a new instance and initialize state.
     */
    private LineSplitter(S s) {
        this.seq = s;
        get();
    }

    /**
     * Checks if the end of the sequence has been reached.
     *
     * @return {@code true} if the end of the sequence has been reached, {@code false} otherwise
     */
    private boolean isEOT() {
        return codepoint == -1;
    }

    /**
     * Get the current codepoint().
     *
     * @return the current codepoint
     */
    private int current() {
        return codepoint;
    }

    /**
     * Read the next codepoint from the sequence.
     *
     * @return the coepoint, or -1 if the end of the sequence is reached
     */
    private int get() {
        pos += charsForCodepoint;
        if (pos < seq.length()) {
            codepoint = Character.codePointAt(seq, pos);
            charsForCodepoint = Character.charCount(codepoint);
        } else {
            codepoint = -1;
            charsForCodepoint = Character.charCount(0);
        }
        return codepoint;
    }

    /**
     * Tokenizes the input sequence into chunks.
     *
     * @return A list of Chunk objects representing the tokenized chunks of the input sequence.
     */
    private List<Chunk<S>> tokenize() {
        List<Chunk<S>> chunks = new ArrayList<>();
        while (!isEOT()) {
            chunks.add(readChunk());
        }
        return chunks;
    }

    /**
     * Determines the type of the given codepoint.
     *
     * @param cp the codepoint to determine the type of
     * @return the type of the codepoint
     */
    private static ChunkType type(int cp) {
        if (cp == -1) {
            return ChunkType.EOT;
        } else if (isLineBreak(cp)) {
            return ChunkType.PARAGRAPH_BREAK;
        } else if (isWhitespace(cp)) {
            return ChunkType.WHITESPACE;
        } else {
            return ChunkType.WORD;
        }
    }

    /**
     * Checks if the given code point represents a line break character.
     *
     * @param cp the code point to check
     * @return {@code true} if the code point represents a line break character, {@code false} otherwise
     */
    private static boolean isLineBreak(int cp) {
        return switch (cp) {
            case '\n', '\u000B', '\u000C', '\r', '\u0085', '\u2028', '\u2029' -> true;
            default -> false;
        };
    }

    /**
     * Determines if the given code point is a whitespace character.
     *
     * @param cp the code point to be checked
     * @return {@code true} if the code point is a whitespace character, {@code false} otherwise
     */
    private static boolean isWhitespace(int cp) {
        return Character.isWhitespace(cp);
    }

    /**
     * Reads a chunk of characters from the input sequence.
     *
     * @return the chunk of characters read from the input sequence
     */
    private Chunk<S> readChunk() {
        int start = pos;
        ChunkType type = type(current());
        //noinspection StatementWithEmptyBody
        while (type(get()) == type) {
            // continue looping until type changes
        }
        return new Chunk<>(type, seq, start, pos);
    }

    /**
     * Splits a single string of text into multiple lines based on a given maxWidth and additional parameters.
     *
     * @param <S>           the type of the input string
     * @param <R>           the type of the buffer used to store the lines
     * @param inputText     the input string to be broken into lines
     * @param maxWidth      the maximum width of each line
     * @param isHardWrap    specifies whether the text should be hard-wrapped (chopped) or soft-wrapped (wrapped naturally)
     * @param spaceChar     the space character to be used for line breaks
     * @param bufferFactory a supplier function that creates a buffer of type R
     * @param readBuffer    a function that reads the buffer and returns a string of type S
     * @param bufferLength  a function that calculates the length of the buffer
     * @return a list of strings representing the broken lines
     * @throws IOException if an I/O error occurs
     */
    public static <S extends CharSequence, R extends Appendable> List<List<S>> process(
            S inputText,
            int maxWidth,
            boolean isHardWrap,
            S spaceChar,
            Supplier<R> bufferFactory,
            Function<R, S> readBuffer,
            ToIntFunction<R> bufferLength
    ) throws IOException {
        // tokenize the text
        LineSplitter<S, R> splitter = new LineSplitter<>(inputText);
        List<Chunk<S>> chunks = splitter.tokenize();
        //
        List<List<S>> paragraphs = new ArrayList<>();
        List<S> currentLines = new ArrayList<>();
        paragraphs.add(currentLines);
        R buffer = bufferFactory.get();

        for (Chunk<S> chunk : chunks) {
            switch (chunk.type()) {
                case EOT -> {
                    handleEndOfText(readBuffer, currentLines, buffer);
                    return paragraphs;
                }
                case PARAGRAPH_BREAK -> {
                    Result<R, S> result = handleParagraphBreak(readBuffer, bufferFactory, paragraphs, currentLines, buffer);
                    buffer = result.newBuffer();
                    currentLines = result.newLines();
                }
                case WORD ->
                        buffer = handleWordChunk(maxWidth, bufferLength, isHardWrap, bufferFactory, readBuffer, currentLines, buffer, chunk);
                case WHITESPACE -> buffer.append(spaceChar);
            }
        }

        if (!paragraphs.isEmpty() && paragraphs.getLast().isEmpty()) {
            paragraphs.removeLast();
        }
        return paragraphs;
    }

    private static <S extends CharSequence, R extends Appendable> void handleEndOfText(Function<R, S> readBuffer, List<S> currentLines, R buffer) {
        currentLines.add(readBuffer.apply(buffer));
    }

    private static <S extends CharSequence, R extends Appendable> Result<R, S> handleParagraphBreak(
            Function<R, S> readBuffer, Supplier<R> bufferFactory, List<List<S>> paragraphs, List<S> currentLines, R buffer) {
        currentLines.add(readBuffer.apply(buffer));
        List<S> newLines = new ArrayList<>();
        paragraphs.add(newLines);
        R newBuffer = bufferFactory.get();
        return new Result<>(newBuffer, newLines);
    }

    private static <S extends CharSequence, R extends Appendable> R handleWordChunk(
            int maxWidth, ToIntFunction<R> bufferLength, boolean isHardWrap, Supplier<R> bufferFactory, Function<R, S> readBuffer, List<S> currentLines, R buffer, Chunk<S> chunk) throws IOException {
        CharSequence cs = chunk.asCharSequence();
        if (bufferLength.applyAsInt(buffer) + chunk.length() <= maxWidth) {
            // chunk fits on the current line, just add it
            buffer.append(cs);
        } else {
            // chunk does not fit on the current line
            if (bufferLength.applyAsInt(buffer) > 0) {
                // line is not empty => wrap here, so that we are the beginning of a new line
                currentLines.add(readBuffer.apply(buffer));
                buffer = bufferFactory.get();
                if (isHardWrap) {
                    while (cs.length() > maxWidth) {
                        // split word
                        CharSequence part = cs.subSequence(0, maxWidth);
                        cs = cs.subSequence(maxWidth, cs.length());
                        buffer.append(part);
                        currentLines.add(readBuffer.apply(buffer));
                        buffer = bufferFactory.get();
                    }
                }
                buffer.append(cs);
            } else {
                // the line is empty
                assert bufferLength.applyAsInt(buffer) == 0 : "buffer should be empty!";
                if (bufferLength.applyAsInt(buffer) <= maxWidth || isHardWrap) {
                    // chop and add after maxWidth characters until the rest fits into a line
                    while (cs.length() > maxWidth) {
                        // split word
                        CharSequence part = cs.subSequence(0, maxWidth);
                        cs = cs.subSequence(maxWidth, cs.length());
                        buffer.append(part);
                        currentLines.add(readBuffer.apply(buffer));
                        buffer = bufferFactory.get();
                    }
                    // append the rest
                    buffer.append(cs);
                    currentLines.add(readBuffer.apply(buffer));
                    buffer = bufferFactory.get();
                } else {
                    // just add and wrap after adding
                    buffer = bufferFactory.get();
                    buffer.append(cs);
                    currentLines.add(readBuffer.apply(buffer));
                    buffer = bufferFactory.get();
                }
            }
        }
        return buffer;
    }

    private record Result<R, S>(R newBuffer, List<S> newLines) {
    }
}
