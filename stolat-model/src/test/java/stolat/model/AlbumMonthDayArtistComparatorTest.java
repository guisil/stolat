package stolat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlbumMonthDayArtistComparatorTest {

    private static final AlbumBirthday EXPECTED_FIRST = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Some Album",
                    List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
            2000, 1, 22);
    private static final AlbumBirthday EXPECTED_SECOND = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Some Album",
                    List.of(new Artist(UUID.randomUUID(), "Artist"))),
            2000, 2, 17);
    private static final AlbumBirthday EXPECTED_THIRD = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Some Album",
                    List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
            2000, 2, 18);
    private static final AlbumBirthday EXPECTED_FOURTH = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Album with Multiple Artists",
                    List.of(new Artist(UUID.randomUUID(), "Artistic Artist"),
                            new Artist(UUID.randomUUID(), "Another Artistic Artist"))),
            2000, 2, 18);
    private static final AlbumBirthday EXPECTED_FIFTH = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Album with Multiple Artists",
                    List.of(new Artist(UUID.randomUUID(), "Second Artistic Artist"),
                            new Artist(UUID.randomUUID(), "Another Artistic Artist"))),
            2000, 2, 18);
    private static final AlbumBirthday EXPECTED_SIXTH = new AlbumBirthday(
            new Album(
                    UUID.randomUUID(), "Some Album",
                    List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
            2000, 2, 18);
    private static final List<AlbumBirthday> EXPECTED_SORTED_LIST =
            List.of(EXPECTED_FIRST, EXPECTED_SECOND, EXPECTED_THIRD, EXPECTED_FOURTH, EXPECTED_FIFTH, EXPECTED_SIXTH);

    private Comparator<AlbumBirthday> comparator;

    @BeforeEach
    void setUp() {
        comparator = new AlbumMonthDayArtistComparator();
    }

    @Test
    void shouldCompareWhenBirthdaysHaveNoDay() {
        final var expectedFirst = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 2, null);
        final var expectedSecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
                2000, 3, null);
        final var expectedThird = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, null);
        final var expectedEquallyThird = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Other Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, null);

        assertTrue(comparator.compare(expectedFirst, expectedSecond) < 0);
        assertTrue(comparator.compare(expectedSecond, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedFirst, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedSecond, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedSecond) > 0);
        assertEquals(0, comparator.compare(expectedThird, expectedEquallyThird));
        assertEquals(0, comparator.compare(expectedEquallyThird, expectedThird));
    }

    @Test
    void shouldCompareWhenSomeBirthdaysHaveNoDay() {
        final var expectedFirst = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 2, null);
        final var expectedSecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, 1);
        final var expectedEquallySecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Other Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, 1);
        final var expectedThird = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
                2000, 3, null);

        assertTrue(comparator.compare(expectedFirst, expectedSecond) < 0);
        assertTrue(comparator.compare(expectedSecond, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedFirst, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedFirst) > 0);
        assertEquals(0, comparator.compare(expectedSecond, expectedEquallySecond));
        assertEquals(0, comparator.compare(expectedEquallySecond, expectedSecond));
        assertTrue(comparator.compare(expectedSecond, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedSecond) > 0);
    }

    @Test
    void shouldCompareWhenBirthdaysHaveNoMonthAndNoDay() {
        final var expectedFirst = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
                2000, null, null);
        final var expectedSecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, null, null);
        final var expectedEquallySecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, null, null);

        assertTrue(comparator.compare(expectedFirst, expectedSecond) < 0);
        assertTrue(comparator.compare(expectedSecond, expectedFirst) > 0);
        assertEquals(0, comparator.compare(expectedSecond, expectedEquallySecond));
        assertEquals(0, comparator.compare(expectedEquallySecond, expectedSecond));
    }

    @Test
    void shouldCompareWhenSomeBirthdaysHaveNoMonthAndNoDay() {
        final var expectedFirst = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 2, null);
        final var expectedSecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, 1);
        final var expectedEquallySecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Other Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 3, 1);
        final var expectedThird = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
                1999, null, null);

        assertTrue(comparator.compare(expectedFirst, expectedSecond) < 0);
        assertTrue(comparator.compare(expectedSecond, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedFirst, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedFirst) > 0);
        assertEquals(0, comparator.compare(expectedSecond, expectedEquallySecond));
        assertEquals(0, comparator.compare(expectedEquallySecond, expectedSecond));
        assertTrue(comparator.compare(expectedSecond, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedSecond) > 0);
    }

    @Test
    void shouldCompareWhenSomeAlbumsHaveMultipleArtists() {
        final var expectedFirst = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"))),
                2000, 2, 23);
        final var expectedSecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"),
                                new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 2, 23);
        final var expectedEquallySecond = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"),
                                new Artist(UUID.randomUUID(), "Some Artist"))),
                2000, 2, 23);
        final var expectedThird = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Another Artist"),
                                new Artist(UUID.randomUUID(), "Some Other Artist"))),
                2000, 2, 23);
        final var expectedFourth = new AlbumBirthday(
                new Album(
                        UUID.randomUUID(), "Some Album",
                        List.of(new Artist(UUID.randomUUID(), "Some Artist"),
                                new Artist(UUID.randomUUID(), "Another Artist"))),
                2000, 2, 23);

        assertTrue(comparator.compare(expectedFirst, expectedSecond) < 0);
        assertTrue(comparator.compare(expectedSecond, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedFirst, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedFirst) > 0);
        assertTrue(comparator.compare(expectedFirst, expectedFourth) < 0);
        assertTrue(comparator.compare(expectedFourth, expectedFirst) > 0);
        assertEquals(0, comparator.compare(expectedSecond, expectedEquallySecond));
        assertEquals(0, comparator.compare(expectedEquallySecond, expectedSecond));
        assertTrue(comparator.compare(expectedSecond, expectedThird) < 0);
        assertTrue(comparator.compare(expectedThird, expectedSecond) > 0);
        assertTrue(comparator.compare(expectedSecond, expectedFourth) < 0);
        assertTrue(comparator.compare(expectedFourth, expectedSecond) > 0);
        assertTrue(comparator.compare(expectedThird, expectedFourth) < 0);
        assertTrue(comparator.compare(expectedFourth, expectedThird) > 0);
    }

    @Test
    void shouldSortLists() {
        final var firstList = List.of(EXPECTED_FIRST, EXPECTED_SECOND, EXPECTED_THIRD, EXPECTED_FOURTH, EXPECTED_FIFTH, EXPECTED_SIXTH)
                .stream().sorted(comparator).collect(Collectors.toList());
        assertEquals(EXPECTED_SORTED_LIST, firstList);

        final var secondList = List.of(EXPECTED_SIXTH, EXPECTED_FIFTH, EXPECTED_FOURTH, EXPECTED_THIRD, EXPECTED_SECOND, EXPECTED_FIRST)
                .stream().sorted(comparator).collect(Collectors.toList());
        assertEquals(EXPECTED_SORTED_LIST, secondList);

        final var thirdList = List.of(EXPECTED_SECOND, EXPECTED_THIRD, EXPECTED_FIFTH, EXPECTED_FOURTH, EXPECTED_SIXTH, EXPECTED_FIRST)
                .stream().sorted(comparator).collect(Collectors.toList());
        assertEquals(EXPECTED_SORTED_LIST, thirdList);

        final var fourthList = List.of(EXPECTED_THIRD, EXPECTED_FIRST, EXPECTED_SECOND, EXPECTED_SIXTH, EXPECTED_FOURTH, EXPECTED_FIFTH)
                .stream().sorted(comparator).collect(Collectors.toList());
        assertEquals(EXPECTED_SORTED_LIST, fourthList);
    }
}