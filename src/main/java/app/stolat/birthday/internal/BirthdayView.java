package app.stolat.birthday.internal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import app.stolat.MainLayout;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import app.stolat.collection.Album;
import app.stolat.collection.AlbumFormat;
import app.stolat.collection.CollectionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Birthdays")
@AnonymousAllowed
public class BirthdayView extends VerticalLayout {

    private static final String TODAY = "Today";
    private static final String LAST_7_DAYS = "Last 7 days";
    private static final String NEXT_7_DAYS = "Next 7 days";
    private static final String THIS_WEEK = "This week";
    private static final String LAST_30_DAYS = "Last 30 days";
    private static final String NEXT_30_DAYS = "Next 30 days";
    private static final String THIS_MONTH = "This month";

    private final BirthdayService birthdayService;
    private final CollectionService collectionService;
    private final Map<UUID, Set<AlbumFormat>> formatsByMusicBrainzId;
    private final Grid<AlbumBirthday> grid;
    private final H2 heading;
    private final TextField searchField;

    public BirthdayView(BirthdayService birthdayService, CollectionService collectionService,
                        @Value("${stolat.volumio.url:}") String volumioUrl) {
        this.birthdayService = birthdayService;
        this.collectionService = collectionService;
        this.formatsByMusicBrainzId = collectionService.findAllActiveAlbums().stream()
                .filter(a -> a.getMusicBrainzId() != null)
                .collect(Collectors.toMap(Album::getMusicBrainzId, Album::getFormats, (a, b) -> a));

        heading = new H2("Album Birthdays \u2014 Today");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        var rangeSelect = new Select<String>();
        rangeSelect.setItems(TODAY, LAST_7_DAYS, NEXT_7_DAYS, THIS_WEEK, LAST_30_DAYS, NEXT_30_DAYS, THIS_MONTH);
        rangeSelect.setValue(TODAY);

        var monthDayFormatter = DateTimeFormatter.ofPattern("MMM dd");

        grid = new Grid<>(AlbumBirthday.class, false);
        grid.addColumn(AlbumBirthday::getArtistName).setHeader("Artist").setSortable(true);
        grid.addColumn(AlbumBirthday::getAlbumTitle).setHeader("Album").setSortable(true);
        grid.addColumn(b -> MonthDay.from(b.getReleaseDate()).format(monthDayFormatter))
                .setHeader("Birthday")
                .setSortable(true)
                .setComparator((a, b) -> MonthDay.from(a.getReleaseDate()).compareTo(MonthDay.from(b.getReleaseDate())));
        grid.addColumn(b -> b.getReleaseDate().getYear())
                .setHeader("Year")
                .setSortable(true)
                .setComparator((a, b) -> Integer.compare(a.getReleaseDate().getYear(), b.getReleaseDate().getYear()));
        grid.addColumn(b -> {
            var formats = formatsByMusicBrainzId.get(b.getMusicBrainzId());
            if (formats == null || formats.isEmpty()) return "";
            return formats.stream()
                    .map(f -> f == AlbumFormat.DIGITAL ? "Digital" : "Vinyl")
                    .sorted()
                    .reduce((a, c) -> a + ", " + c)
                    .orElse("");
        }).setHeader("Format").setSortable(true);

        // Only add play column if Volumio is configured
        if (volumioUrl != null && !volumioUrl.isEmpty()) {
            grid.addComponentColumn(birthday -> {
                var formats = formatsByMusicBrainzId.get(birthday.getMusicBrainzId());
                if (formats == null || !formats.contains(AlbumFormat.DIGITAL)) {
                    return new Span(); // empty for non-digital
                }
                var playButton = new Button(VaadinIcon.PLAY.create());
                playButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
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

        updateGrid(TODAY);

        searchField.addValueChangeListener(event -> applySearchFilter());

        rangeSelect.addValueChangeListener(event -> {
            searchField.clear();
            updateGrid(event.getValue());
        });

        var toolbar = new HorizontalLayout(rangeSelect, searchField);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        add(heading, toolbar, grid);
    }

    private void updateGrid(String range) {
        heading.setText("Album Birthdays \u2014 " + range);
        var today = LocalDate.now();

        var birthdays = switch (range) {
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
    }

    @SuppressWarnings("unchecked")
    private void applySearchFilter() {
        var dp = grid.getDataProvider();
        if (dp instanceof ListDataProvider<?>) {
            var listDataProvider = (ListDataProvider<AlbumBirthday>) dp;
            listDataProvider.clearFilters();
            var filterText = searchField.getValue().trim().toLowerCase();
            if (!filterText.isEmpty()) {
                listDataProvider.addFilter(birthday ->
                        birthday.getArtistName().toLowerCase().contains(filterText) ||
                        birthday.getAlbumTitle().toLowerCase().contains(filterText));
            }
        }
    }
}
