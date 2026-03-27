package app.stolat.birthday.internal;

import java.util.concurrent.CompletableFuture;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import app.stolat.MainLayout;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.birthday.ReleaseDateSource;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import org.springframework.beans.factory.annotation.Value;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Birthdays")
@AnonymousAllowed
public class BirthdayView extends VerticalLayout {

    private static final String ALL = "All";
    private static final String TODAY = "Today";
    private static final String LAST_7_DAYS = "Last 7 days";
    private static final String NEXT_7_DAYS = "Next 7 days";
    private static final String THIS_WEEK = "This week";
    private static final String LAST_30_DAYS = "Last 30 days";
    private static final String NEXT_30_DAYS = "Next 30 days";
    private static final String THIS_MONTH = "This month";

    private static final String ALL_SOURCES = "All sources";
    private static final String SOURCE_MUSICBRAINZ = "MusicBrainz";
    private static final String SOURCE_DISCOGS = "Discogs";
    private static final String SOURCE_BANDCAMP = "Bandcamp";
    private static final String SOURCE_MB_PENDING = "MB Pending";
    private static final String SOURCE_MANUAL = "Manual";

    private final BirthdayService birthdayService;
    private final CollectionService collectionService;
    private Map<UUID, Set<AlbumFormat>> formatsByMusicBrainzId;
    private Map<UUID, Set<AlbumFormat>> formatsByAlbumId;
    private final Grid<AlbumBirthday> grid;
    private final H2 heading;
    private final Span countLabel;
    private final TextField searchField;
    private final Select<String> sourceFilter;

    public BirthdayView(BirthdayService birthdayService, CollectionService collectionService,
                        @Value("${stolat.volumio.url:}") String volumioUrl,
                        @Value("${stolat.lastfm.api-key:}") String lastFmApiKey) {
        this.birthdayService = birthdayService;
        this.collectionService = collectionService;
        this.formatsByMusicBrainzId = new HashMap<>();

        heading = new H2("Album Birthdays \u2014 Today");

        countLabel = new Span();

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        var session = VaadinSession.getCurrent();
        var savedRange = (String) session.getAttribute("birthday.range");
        var savedSearch = (String) session.getAttribute("birthday.search");
        var savedSource = (String) session.getAttribute("birthday.source");

        var rangeSelect = new Select<String>();
        rangeSelect.setItems(ALL, TODAY, LAST_7_DAYS, NEXT_7_DAYS, THIS_WEEK, LAST_30_DAYS, NEXT_30_DAYS, THIS_MONTH);
        rangeSelect.setValue(savedRange != null ? savedRange : TODAY);

        sourceFilter = new Select<>();
        sourceFilter.setItems(ALL_SOURCES, SOURCE_MUSICBRAINZ, SOURCE_DISCOGS, SOURCE_BANDCAMP, SOURCE_MANUAL);
        sourceFilter.setValue(savedSource != null ? savedSource : ALL_SOURCES);
        sourceFilter.setLabel("Source");

        var monthDayFormatter = DateTimeFormatter.ofPattern("MMM dd");

        grid = new Grid<>(AlbumBirthday.class, false);
        grid.setMultiSort(true);
        grid.addColumn(AlbumBirthday::getArtistName).setHeader("Artist").setSortable(true);
        grid.addColumn(AlbumBirthday::getAlbumTitle).setHeader("Album").setSortable(true);
        var birthdayColumn = grid.addColumn(b -> MonthDay.from(b.getReleaseDate()).format(monthDayFormatter))
                .setHeader("Birthday")
                .setSortable(true)
                .setWidth("120px").setFlexGrow(0)
                .setComparator((a, b) -> MonthDay.from(a.getReleaseDate()).compareTo(MonthDay.from(b.getReleaseDate())));
        var yearColumn = grid.addColumn(b -> b.getReleaseDate().getYear())
                .setHeader("Year")
                .setSortable(true)
                .setWidth("90px").setFlexGrow(0)
                .setComparator((a, b) -> Integer.compare(a.getReleaseDate().getYear(), b.getReleaseDate().getYear()));
        grid.addColumn(b -> switch (b.getReleaseDateSource()) {
            case MUSICBRAINZ -> SOURCE_MUSICBRAINZ;
            case MB_PENDING -> SOURCE_MB_PENDING;
            case DISCOGS -> SOURCE_DISCOGS;
            case BANDCAMP -> SOURCE_BANDCAMP;
            case MANUAL -> SOURCE_MANUAL;
        }).setHeader("Source").setSortable(true).setWidth("150px").setFlexGrow(0);
        grid.addColumn(b -> b.getPlayCount() != null ? b.getPlayCount() : "")
                .setHeader("Plays").setSortable(true).setWidth("90px").setFlexGrow(0)
                .setComparator((a, b) -> {
                    var pa = a.getPlayCount() != null ? a.getPlayCount() : 0;
                    var pb = b.getPlayCount() != null ? b.getPlayCount() : 0;
                    return Integer.compare(pa, pb);
                });
        grid.addComponentColumn(b -> {
            var formats = b.getMusicBrainzId() != null
                    ? formatsByMusicBrainzId.get(b.getMusicBrainzId())
                    : formatsByAlbumId.get(b.getAlbumId());
            if (formats == null || formats.isEmpty()) return new Span();
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

        // Only add play column if Volumio is configured
        if (volumioUrl != null && !volumioUrl.isEmpty()) {
            grid.addComponentColumn(birthday -> {
                var formats = birthday.getMusicBrainzId() != null
                        ? formatsByMusicBrainzId.get(birthday.getMusicBrainzId())
                        : formatsByAlbumId.get(birthday.getAlbumId());
                if (formats == null || !formats.contains(AlbumFormat.DIGITAL)) {
                    return new Span(); // empty for non-digital
                }
                var playIcon = VaadinIcon.PLAY.create();
                var playButton = new Button(playIcon);
                playButton.addClassName("play-button");
                playButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                playButton.setTooltipText("Play in Volumio");
                playButton.addClickListener(e -> {
                    try {
                        collectionService.playAlbumOnVolumio(birthday.getAlbumTitle(), birthday.getArtistName());
                        Notification.show("Playing '" + birthday.getAlbumTitle() + "' on Volumio");
                    } catch (Exception ex) {
                        Notification.show("Could not play on Volumio: " + ex.getMessage());
                    }
                });
                return playButton;
            }).setHeader("").setWidth("60px").setFlexGrow(0);
        }

        grid.getColumns().forEach(c -> c.setResizable(true));
        grid.sort(GridSortOrder.asc(birthdayColumn)
                .thenAsc(yearColumn).build());

        updateGrid(rangeSelect.getValue());

        if (savedSearch != null && !savedSearch.isEmpty()) {
            searchField.setValue(savedSearch);
            applySearchFilter();
        }

        searchField.addValueChangeListener(event -> {
            session.setAttribute("birthday.search", event.getValue());
            applySearchFilter();
        });

        rangeSelect.addValueChangeListener(event -> {
            searchField.clear();
            session.setAttribute("birthday.range", event.getValue());
            updateGrid(event.getValue());
        });

        sourceFilter.addValueChangeListener(event -> {
            searchField.clear();
            session.setAttribute("birthday.source", event.getValue());
            applySourceFilter();
        });

        searchField.setWidth("300px");
        var toolbar = new HorizontalLayout(searchField, rangeSelect, sourceFilter);
        toolbar.setWidthFull();
        var spacer = new Span();
        toolbar.add(spacer);
        toolbar.setFlexGrow(1, spacer);
        if (lastFmApiKey != null && !lastFmApiKey.isEmpty()) {
            var syncButton = new Button("Sync Plays");
            syncButton.addClickListener(event -> {
                syncButton.setEnabled(false);
                syncButton.setText("Syncing...");
                Notification.show("Syncing play counts...");
                var ui = UI.getCurrent();
                CompletableFuture.runAsync(() -> {
                    var synced = birthdayService.syncPlayCounts();
                    ui.access(() -> {
                        Notification.show("Synced play counts for " + synced + " albums");
                        updateGrid(rangeSelect.getValue());
                        syncButton.setEnabled(true);
                        syncButton.setText("Sync Plays");
                    });
                }).exceptionally(ex -> {
                    ui.access(() -> {
                        Notification.show("Play count sync failed: " + ex.getMessage());
                        syncButton.setEnabled(true);
                        syncButton.setText("Sync Plays");
                    });
                    return null;
                });
            });
            toolbar.add(syncButton);
        }
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        setSizeFull();
        grid.setSizeFull();

        add(heading, countLabel, toolbar, grid);
        setFlexGrow(1, grid);
    }

    private void updateGrid(String range) {
        heading.setText("Album Birthdays \u2014 " + range);
        var activeAlbums = collectionService.findAllActiveAlbums();
        this.formatsByMusicBrainzId = activeAlbums.stream()
                .filter(a -> a.getMusicBrainzId() != null)
                .collect(Collectors.toMap(Album::getMusicBrainzId, Album::getFormats, (a, b) -> a));
        this.formatsByAlbumId = activeAlbums.stream()
                .collect(Collectors.toMap(Album::getId, Album::getFormats, (a, b) -> a));
        var today = LocalDate.now();

        var birthdays = switch (range) {
            case ALL -> birthdayService.findAllBirthdays();
            case LAST_7_DAYS -> birthdayService.findBirthdaysBetween(today.minusDays(7), today);
            case NEXT_7_DAYS -> birthdayService.findBirthdaysBetween(today, today.plusDays(7));
            case THIS_WEEK -> birthdayService.findBirthdaysBetween(
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
            case LAST_30_DAYS -> birthdayService.findBirthdaysBetween(today.minusDays(30), today);
            case NEXT_30_DAYS -> birthdayService.findBirthdaysBetween(today, today.plusDays(30));
            case THIS_MONTH -> {
                var yearMonth = YearMonth.from(today);
                yield birthdayService.findBirthdaysBetween(
                        yearMonth.atDay(1), yearMonth.atEndOfMonth());
            }
            default -> birthdayService.findBirthdaysOn(today);
        };

        var dataProvider = new ListDataProvider<>(birthdays);
        grid.setItems(dataProvider);

        if (sourceFilter != null) {
            applySourceFilter();
        } else {
            updateCountLabel(birthdays.size());
        }
    }

    @SuppressWarnings("unchecked")
    private void applySourceFilter() {
        var dp = grid.getDataProvider();
        if (dp instanceof ListDataProvider<?>) {
            var listDataProvider = (ListDataProvider<AlbumBirthday>) dp;
            listDataProvider.clearFilters();

            var selectedSource = sourceFilter != null ? sourceFilter.getValue() : ALL_SOURCES;
            if (!ALL_SOURCES.equals(selectedSource)) {
                var source = switch (selectedSource) {
                    case SOURCE_MUSICBRAINZ -> ReleaseDateSource.MUSICBRAINZ;
                    case SOURCE_DISCOGS -> ReleaseDateSource.DISCOGS;
                    case SOURCE_BANDCAMP -> ReleaseDateSource.BANDCAMP;
                    case SOURCE_MANUAL -> ReleaseDateSource.MANUAL;
                    default -> null;
                };
                if (source != null) {
                    listDataProvider.addFilter(b -> b.getReleaseDateSource() == source);
                }
            }

            var filterText = searchField.getValue().trim().toLowerCase();
            if (!filterText.isEmpty()) {
                listDataProvider.addFilter(birthday ->
                        birthday.getArtistName().toLowerCase().contains(filterText) ||
                        birthday.getAlbumTitle().toLowerCase().contains(filterText));
            }

            var filter = listDataProvider.getFilter();
            var filteredCount = filter != null
                    ? (int) listDataProvider.getItems().stream().filter(filter).count()
                    : listDataProvider.getItems().size();
            updateCountLabel(filteredCount);
        }
    }

    @SuppressWarnings("unchecked")
    private void applySearchFilter() {
        applySourceFilter();
    }

    private void updateCountLabel(int count) {
        countLabel.setText(count + (count == 1 ? " birthday" : " birthdays"));
    }
}
