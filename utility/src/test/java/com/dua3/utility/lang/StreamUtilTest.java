package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

class StreamUtilTest {

    @Test
    void zipStreamsOfEqualLength() {
        // concatenate two streams
        List<Pair<Integer, Integer>> actual = StreamUtil.zip(
                Stream.of(1, 3, 5),
                Stream.of(2, 4, 6)
        ).toList();

        List<Pair<Integer, Integer>> expected = List.of(
                Pair.of(1, 2),
                Pair.of(3, 4),
                Pair.of(5, 6)
        );

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void zipStreamsOfDifferentLength() {
        // concatenate two streams
        List<Pair<Integer, Integer>> actual = StreamUtil.zip(
                Stream.of(1, 3, 5),
                Stream.of(2, 4)
        ).toList();

        List<Pair<Integer, Integer>> expected = List.of(
                Pair.of(1, 2),
                Pair.of(3, 4)
        );

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void concatTwoStreams() {
        List<Integer> actual = StreamUtil.concat(
                Stream.of(1, 3, 5),
                Stream.of(2, 4)
        ).toList();

        List<Integer> expected = List.of(1, 3, 5, 2, 4);

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void concatSingleStream() {
        List<Integer> actual = StreamUtil.concat(
                Stream.of(1, 3, 5)
        ).toList();

        List<Integer> expected = List.of(1, 3, 5);

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void concatNoArgs() {
        List<Integer> actual = StreamUtil.<Integer>concat().toList();

        List<Integer> expected = List.of();

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void concatThreeStreams() {
        List<Integer> actual = StreamUtil.concat(
                Stream.of(1, 3, 5),
                Stream.of(2, 4),
                Stream.of(7, 8, 9)
        ).toList();

        List<Integer> expected = List.of(1, 3, 5, 2, 4, 7, 8, 9);

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void merge() {
        List<Integer> actual = StreamUtil.merge(
                Stream.of(1, 3, 5, 8),
                Stream.of(7, 9),
                Stream.of(2, 4, 6)
        ).toList();

        List<Integer> expected = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        Assertions.assertIterableEquals(expected, actual);
    }
}
