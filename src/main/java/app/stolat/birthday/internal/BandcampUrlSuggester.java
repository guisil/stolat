package app.stolat.birthday.internal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Generates suggested Bandcamp URLs and search links from artist name and album title.
 */
class BandcampUrlSuggester {

    private static final Pattern NON_ALPHANUMERIC_OR_HYPHEN = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");
    private static final Pattern LEADING_THE = Pattern.compile("^the\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern EDITION_SUFFIX = Pattern.compile(
            "\\s*[\\(\\[][^)\\]]*(?:edition|remaster(?:ed)?|deluxe|bonus|anniversary|expanded|special)[^)\\]]*[\\)\\]]",
            Pattern.CASE_INSENSITIVE);

    private BandcampUrlSuggester() {
    }

    /**
     * Generates a candidate Bandcamp album URL from artist name and album title.
     * Format: https://{artist-slug}.bandcamp.com/album/{album-slug}
     */
    static String suggestAlbumUrl(String artistName, String albumTitle) {
        var artistSlug = toSlug(stripThePrefix(artistName != null ? artistName : ""));
        var albumSlug = toSlug(stripEditionSuffix(albumTitle != null ? albumTitle : ""));
        return "https://" + artistSlug + ".bandcamp.com/album/" + albumSlug;
    }

    /**
     * Generates a Bandcamp search URL for the given artist and album.
     * Format: https://bandcamp.com/search?q={artist}+{album}
     */
    static String searchUrl(String artistName, String albumTitle) {
        var query = (artistName != null ? artistName : "") + " " + (albumTitle != null ? albumTitle : "");
        return "https://bandcamp.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    private static String stripThePrefix(String name) {
        return LEADING_THE.matcher(name).replaceFirst("");
    }

    private static String stripEditionSuffix(String title) {
        return EDITION_SUFFIX.matcher(title).replaceAll("").trim();
    }

    private static String toSlug(String input) {
        // Replace "&" with "and" (Bandcamp convention)
        var withAnd = input.replace("&", "and");
        // Normalize unicode (e.g., accented chars) to ASCII equivalents
        var normalized = Normalizer.normalize(withAnd, Normalizer.Form.NFD);
        var asciiOnly = DIACRITICS.matcher(normalized).replaceAll("");

        var slug = asciiOnly.toLowerCase()
                .replace(' ', '-');
        slug = NON_ALPHANUMERIC_OR_HYPHEN.matcher(slug).replaceAll("");
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        // Strip leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }
}
