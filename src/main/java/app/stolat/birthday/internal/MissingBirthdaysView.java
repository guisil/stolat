package app.stolat.birthday.internal;

import app.stolat.MainLayout;
import app.stolat.birthday.BirthdayService;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "missing-birthdays", layout = MainLayout.class)
@PageTitle("Missing Birthdays")
@AnonymousAllowed
public class MissingBirthdaysView extends VerticalLayout {

    private static final String ALL = "All";
    private static final String NO_MBID = "No MusicBrainz ID";
    private static final String LOOKUP_FAILED = "Lookup failed";

    private final BirthdayService birthdayService;
    private final CollectionService collectionService;
    private final Grid<Album> grid;
    private final Span countLabel;
    private final TextField searchField;
    private final Select<String> statusFilter;

    public MissingBirthdaysView(BirthdayService birthdayService, CollectionService collectionService) {
        this.birthdayService = birthdayService;
        this.collectionService = collectionService;
        setSizeFull();

        var session = VaadinSession.getCurrent();
        var savedSearch = (String) session.getAttribute("missing.search");
        var savedStatus = (String) session.getAttribute("missing.status");

        var heading = new H2("Missing Birthdays");

        countLabel = new Span();

        statusFilter = new Select<>();
        statusFilter.setItems(ALL, NO_MBID, LOOKUP_FAILED);
        statusFilter.setValue(savedStatus != null ? savedStatus : ALL);
        statusFilter.setLabel("Status");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        grid = new Grid<>(Album.class, false);
        grid.setMultiSort(true);
        var artistColumn = grid.addColumn(album -> album.getArtist().getName())
                .setHeader("Artist").setSortable(true);
        grid.addColumn(Album::getTitle).setHeader("Album").setSortable(true);
        var yearColumn = grid.addColumn(album -> album.getReleaseDate() != null ? album.getReleaseDate().getYear() : null)
                .setHeader("Year").setSortable(true).setWidth("90px").setFlexGrow(0)
                .setComparator((a, b) -> {
                    if (a.getReleaseDate() == null && b.getReleaseDate() == null) return 0;
                    if (a.getReleaseDate() == null) return 1;
                    if (b.getReleaseDate() == null) return -1;
                    return Integer.compare(a.getReleaseDate().getYear(), b.getReleaseDate().getYear());
                });
        grid.addComponentColumn(album -> {
            var formats = album.getFormats();
            if (formats.isEmpty()) return new Span();
            var layout = new HorizontalLayout();
            layout.setSpacing(false);
            layout.addClassName("format-icons");
            if (formats.contains(AlbumFormat.DIGITAL)) {
                var icon = VaadinIcon.FILE_SOUND.create();
                icon.addClassName("format-icon");
                icon.setTooltipText("Digital");
                layout.add(icon);
            }
            if (formats.contains(AlbumFormat.VINYL)) {
                var icon = VaadinIcon.DISC.create();
                icon.addClassName("format-icon");
                icon.setTooltipText("Vinyl");
                layout.add(icon);
            }
            return layout;
        }).setHeader("Format").setWidth("80px").setFlexGrow(0);
        grid.addColumn(album -> album.getMusicBrainzId() != null ? LOOKUP_FAILED : NO_MBID)
                .setHeader("Status").setSortable(true).setWidth("200px").setFlexGrow(0);
        grid.addComponentColumn(album -> {
            var actions = new HorizontalLayout();
            actions.setSpacing(false);

            var retryButton = new Button(VaadinIcon.REFRESH.create());
            retryButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            retryButton.setTooltipText("Retry MusicBrainz lookup");
            retryButton.addClickListener(e -> retryMusicBrainzLookup(album));
            actions.add(retryButton);

            if (album.getDiscogsId() != null) {
                var discogsButton = new Button(VaadinIcon.GLOBE.create());
                discogsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                discogsButton.setTooltipText("Look up on Discogs");
                discogsButton.addClickListener(e -> resolveFromDiscogs(album));
                actions.add(discogsButton);
            }

            var bandcampButton = new Button(VaadinIcon.SEARCH.create());
            bandcampButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bandcampButton.setTooltipText("Look up on Bandcamp");
            bandcampButton.addClickListener(e -> openBandcampDialog(album));
            actions.add(bandcampButton);

            return actions;
        }).setHeader("").setWidth("140px").setFlexGrow(0);
        grid.getColumns().forEach(c -> c.setResizable(true));
        grid.sort(GridSortOrder.asc(artistColumn)
                .thenAsc(yearColumn).build());
        grid.setSizeFull();

        refreshGrid();

        if (savedSearch != null && !savedSearch.isEmpty()) {
            searchField.setValue(savedSearch);
            applySearchFilter();
        }

        searchField.addValueChangeListener(event -> {
            session.setAttribute("missing.search", event.getValue());
            applySearchFilter();
        });

        statusFilter.addValueChangeListener(event -> {
            searchField.clear();
            session.setAttribute("missing.status", event.getValue());
            refreshGrid();
        });

        var retryAllButton = new Button("Retry All Lookups", VaadinIcon.REFRESH.create(), event -> retryAllMusicBrainzLookups());
        var upgradeDiscogsButton = new Button("Upgrade Discogs Dates", VaadinIcon.GLOBE.create(), event -> upgradeDiscogsYearOnlyBirthdays());

        var toolbar = new HorizontalLayout(retryAllButton, upgradeDiscogsButton, statusFilter, searchField);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        add(heading, countLabel, toolbar, grid);
        setFlexGrow(1, grid);
    }

    private void refreshGrid() {
        var albumIdsWithBirthdays = birthdayService.findAlbumIdsWithBirthdays();
        var releaseDatesByMbid = birthdayService.findReleaseDatesByMusicBrainzId();
        var selectedStatus = statusFilter != null ? statusFilter.getValue() : ALL;

        var allMissing = collectionService.findAllActiveAlbums().stream()
                .filter(album -> !albumIdsWithBirthdays.contains(album.getId()))
                .filter(album -> album.getMusicBrainzId() == null
                        || !releaseDatesByMbid.containsKey(album.getMusicBrainzId()))
                .toList();

        var noMbidCount = allMissing.stream().filter(a -> a.getMusicBrainzId() == null).count();
        var failedCount = allMissing.size() - noMbidCount;

        var missingAlbums = allMissing.stream()
                .filter(album -> switch (selectedStatus) {
                    case NO_MBID -> album.getMusicBrainzId() == null;
                    case LOOKUP_FAILED -> album.getMusicBrainzId() != null;
                    default -> true;
                })
                .toList();

        countLabel.setText(missingAlbums.size() + " albums without birthdays ("
                + noMbidCount + " without MBID, " + failedCount + " failed lookup)");
        grid.setItems(new ListDataProvider<>(missingAlbums));
    }

    private void openBandcampDialog(Album album) {
        var dialog = new Dialog();
        dialog.setHeaderTitle("Look up on Bandcamp");

        var urlField = new TextField("Bandcamp URL");
        urlField.setPlaceholder("https://artist.bandcamp.com/album/...");
        urlField.setWidthFull();

        var lookupButton = new Button("Look up", event -> {
            var url = urlField.getValue().trim();
            if (url.isEmpty()) return;

            var result = birthdayService.resolveReleaseDateFromBandcamp(
                    album.getId(), album.getTitle(), album.getArtist().getName(), url);

            if (result.isPresent()) {
                var birthday = result.get();
                collectionService.updateAlbumReleaseDateById(album.getId(), birthday.getReleaseDate());
                Notification.show("Found release date: " + birthday.getReleaseDate());
                dialog.close();
                searchField.clear();
                refreshGrid();
            } else {
                Notification.show("Could not find release date on Bandcamp");
            }
        });
        lookupButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var content = new VerticalLayout(urlField);
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()), lookupButton);
        dialog.open();
    }

    private void resolveFromDiscogs(Album album) {
        if (album.getDiscogsId() == null) {
            Notification.show("No Discogs ID for this album");
            return;
        }
        var result = birthdayService.resolveReleaseDateFromDiscogs(
                album.getId(), album.getTitle(), album.getArtist().getName(), album.getDiscogsId());

        if (result.isPresent()) {
            var birthday = result.get();
            collectionService.updateAlbumReleaseDateById(album.getId(), birthday.getReleaseDate());
            Notification.show("Found release date: " + birthday.getReleaseDate());
            searchField.clear();
            refreshGrid();
        } else {
            Notification.show("Could not find release date on Discogs");
        }
    }

    private void upgradeDiscogsYearOnlyBirthdays() {
        var upgraded = birthdayService.upgradeDiscogsYearOnlyBirthdays();
        for (var birthday : upgraded) {
            if (birthday.getAlbumId() != null) {
                collectionService.updateAlbumReleaseDateById(birthday.getAlbumId(), birthday.getReleaseDate());
            }
        }
        Notification.show("Upgraded " + upgraded.size() + " Discogs year-only birthdays to full dates");
        searchField.clear();
        refreshGrid();
    }

    private void retryAllMusicBrainzLookups() {
        if (collectionService.isScanInProgress()) {
            Notification.show("A scan is in progress — please wait until it finishes");
            return;
        }
        var albumIdsWithBirthdays = birthdayService.findAlbumIdsWithBirthdays();
        var releaseDatesByMbid = birthdayService.findReleaseDatesByMusicBrainzId();

        var missingAlbums = collectionService.findAllActiveAlbums().stream()
                .filter(album -> !albumIdsWithBirthdays.contains(album.getId()))
                .filter(album -> album.getMusicBrainzId() == null
                        || !releaseDatesByMbid.containsKey(album.getMusicBrainzId()))
                .toList();

        int resolved = 0;
        int skipped = 0;
        for (var album : missingAlbums) {
            if (album.getMusicBrainzId() == null) {
                skipped++;
                continue;
            }
            var result = birthdayService.resolveReleaseDate(album.getId(), album.getTitle(),
                    album.getArtist().getName(), album.getMusicBrainzId());
            if (result.isPresent()) {
                collectionService.updateAlbumReleaseDate(album.getMusicBrainzId(), result.get().getReleaseDate());
                resolved++;
            }
        }

        var message = new StringBuilder();
        message.append(missingAlbums.size()).append(" missing albums: ");
        message.append("resolved ").append(resolved);
        if (skipped > 0) {
            message.append(", skipped ").append(skipped).append(" without MusicBrainz ID");
        }
        Notification.show(message.toString());
        searchField.clear();
        refreshGrid();
    }

    private void retryMusicBrainzLookup(Album album) {
        if (collectionService.isScanInProgress()) {
            Notification.show("A scan is in progress — please wait until it finishes");
            return;
        }
        if (album.getMusicBrainzId() == null) {
            Notification.show("No MusicBrainz ID — re-scan your collection after tagging the files");
            return;
        }
        var result = birthdayService.resolveReleaseDate(album.getId(), album.getTitle(),
                album.getArtist().getName(), album.getMusicBrainzId());

        if (result.isPresent()) {
            var birthday = result.get();
            collectionService.updateAlbumReleaseDate(album.getMusicBrainzId(), birthday.getReleaseDate());
            Notification.show("Found release date: " + birthday.getReleaseDate());
            searchField.clear();
            refreshGrid();
        } else {
            Notification.show("MusicBrainz still has no release date for this album");
        }
    }

    @SuppressWarnings("unchecked")
    private void applySearchFilter() {
        var dp = grid.getDataProvider();
        if (dp instanceof ListDataProvider<?>) {
            var listDataProvider = (ListDataProvider<Album>) dp;
            listDataProvider.clearFilters();
            var filterText = searchField.getValue().trim().toLowerCase();
            if (!filterText.isEmpty()) {
                listDataProvider.addFilter(album ->
                        album.getArtist().getName().toLowerCase().contains(filterText) ||
                        album.getTitle().toLowerCase().contains(filterText));
            }
        }
    }
}
