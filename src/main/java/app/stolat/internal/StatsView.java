package app.stolat.internal;

import java.util.Map;
import java.util.stream.Collectors;

import app.stolat.MainLayout;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.birthday.ReleaseDateSource;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "stats", layout = MainLayout.class)
@PageTitle("Stats")
@AnonymousAllowed
public class StatsView extends VerticalLayout {

    public StatsView(CollectionService collectionService, BirthdayService birthdayService) {
        var activeAlbums = collectionService.findAllActiveAlbums();
        var allBirthdays = birthdayService.findAllBirthdays();
        var albumIdsWithBirthdays = birthdayService.findAlbumIdsWithBirthdays();
        var releaseDatesByMbid = birthdayService.findReleaseDatesByMusicBrainzId();

        // Collection stats
        add(new H2("Stats"));

        add(new H3("Collection"));
        add(new Span("Total albums: " + activeAlbums.size()));

        // Birthday stats
        var countsBySource = allBirthdays.stream()
                .collect(Collectors.groupingBy(AlbumBirthday::getReleaseDateSource, Collectors.counting()));

        add(new H3("Birthdays"));
        add(new Span("Total birthdays: " + allBirthdays.size()));
        for (var source : ReleaseDateSource.values()) {
            var count = countsBySource.getOrDefault(source, 0L);
            var label = switch (source) {
                case MUSICBRAINZ -> "MusicBrainz";
                case DISCOGS -> "Discogs";
                case BANDCAMP -> "Bandcamp";
                case MANUAL -> "Manual";
            };
            add(new Span(label + ": " + count));
        }

        // Missing stats
        var missingAlbums = activeAlbums.stream()
                .filter(album -> !albumIdsWithBirthdays.contains(album.getId()))
                .filter(album -> album.getMusicBrainzId() == null
                        || !releaseDatesByMbid.containsKey(album.getMusicBrainzId()))
                .toList();

        var noMbidCount = missingAlbums.stream()
                .filter(a -> a.getMusicBrainzId() == null).count();
        var lookupFailedCount = missingAlbums.size() - noMbidCount;

        add(new H3("Missing Birthdays"));
        add(new Span("Missing birthdays: " + missingAlbums.size()));
        add(new Span("No MusicBrainz ID: " + noMbidCount));
        add(new Span("Lookup failed: " + lookupFailedCount));
    }
}
