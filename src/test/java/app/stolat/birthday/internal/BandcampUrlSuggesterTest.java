package app.stolat.birthday.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BandcampUrlSuggesterTest {

    @Test
    void shouldGenerateUrlFromSimpleArtistAndAlbum() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Anushka", "Kisses");

        assertThat(url).isEqualTo("https://anushka.bandcamp.com/album/kisses");
    }

    @Test
    void shouldReplaceSpacesWithHyphens() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Bonobo", "Black Sands");

        assertThat(url).isEqualTo("https://bonobo.bandcamp.com/album/black-sands");
    }

    @Test
    void shouldRemoveSpecialCharacters() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("AC/DC", "Back in Black!");

        assertThat(url).isEqualTo("https://acdc.bandcamp.com/album/back-in-black");
    }

    @Test
    void shouldStripThePrefixFromArtistName() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("The National", "Sleep Well Beast");

        assertThat(url).isEqualTo("https://national.bandcamp.com/album/sleep-well-beast");
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Bjork", "Homogenic");

        assertThat(url).isEqualTo("https://bjork.bandcamp.com/album/homogenic");
    }

    @Test
    void shouldHandleAccentedCharacters() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Sigur Ros", "Agaetis Byrjun");

        assertThat(url).isEqualTo("https://sigur-ros.bandcamp.com/album/agaetis-byrjun");
    }

    @Test
    void shouldNormalizeDiacritics() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Beyonce", "Lemonade");

        assertThat(url).isEqualTo("https://beyonce.bandcamp.com/album/lemonade");
    }

    @Test
    void shouldHandleDiacriticsInInput() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Beyonce\u0301", "Lemonade");

        assertThat(url).isEqualTo("https://beyonce.bandcamp.com/album/lemonade");
    }

    @Test
    void shouldCollapseMultipleHyphens() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("King Gizzard & The Lizard Wizard", "L.W.");

        assertThat(url).isEqualTo("https://king-gizzard-the-lizard-wizard.bandcamp.com/album/lw");
    }

    @Test
    void shouldNotRetainThePrefixInAlbumTitle() {
        // "The" stripping only applies to artist name, not album title
        var url = BandcampUrlSuggester.suggestAlbumUrl("Radiohead", "The Bends");

        assertThat(url).isEqualTo("https://radiohead.bandcamp.com/album/the-bends");
    }

    @Test
    void shouldGenerateSearchUrl() {
        var url = BandcampUrlSuggester.searchUrl("Bonobo", "Black Sands");

        assertThat(url).isEqualTo("https://bandcamp.com/search?q=Bonobo+Black+Sands");
    }

    @Test
    void shouldEncodeSpecialCharactersInSearchUrl() {
        var url = BandcampUrlSuggester.searchUrl("AC/DC", "Back in Black!");

        assertThat(url).isEqualTo("https://bandcamp.com/search?q=AC%2FDC+Back+in+Black%21");
    }

    @Test
    void shouldStripLeadingAndTrailingHyphensFromSlug() {
        // Input that starts/ends with special chars
        var slug = BandcampUrlSuggester.toSlug("--test--");

        assertThat(slug).isEqualTo("test");
    }

    @Test
    void shouldHandleParenthesesInAlbumTitle() {
        var url = BandcampUrlSuggester.suggestAlbumUrl("Radiohead", "OK Computer (Deluxe Edition)");

        assertThat(url).isEqualTo("https://radiohead.bandcamp.com/album/ok-computer-deluxe-edition");
    }
}
