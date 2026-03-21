package app.stolat.birthday.internal;

import java.time.LocalDate;
import java.time.YearMonth;

import app.stolat.MainLayout;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Birthdays")
@AnonymousAllowed
public class BirthdayView extends VerticalLayout {

    private static final String TODAY = "Today";
    private static final String LAST_7_DAYS = "Last 7 days";
    private static final String NEXT_7_DAYS = "Next 7 days";
    private static final String LAST_30_DAYS = "Last 30 days";
    private static final String NEXT_30_DAYS = "Next 30 days";
    private static final String THIS_MONTH = "This month";

    private final BirthdayService birthdayService;
    private final Grid<AlbumBirthday> grid;
    private final H2 heading;
    private final TextField searchField;

    public BirthdayView(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;

        heading = new H2("Album Birthdays \u2014 Today");

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        var rangeSelect = new Select<String>();
        rangeSelect.setItems(TODAY, LAST_7_DAYS, NEXT_7_DAYS, LAST_30_DAYS, NEXT_30_DAYS, THIS_MONTH);
        rangeSelect.setValue(TODAY);
        rangeSelect.setLabel("Date range");

        grid = new Grid<>(AlbumBirthday.class, false);
        grid.addColumn(AlbumBirthday::getArtistName).setHeader("Artist").setSortable(true);
        grid.addColumn(AlbumBirthday::getAlbumTitle).setHeader("Album").setSortable(true);
        grid.addColumn(AlbumBirthday::getReleaseDate).setHeader("Release Date").setSortable(true);

        updateGrid(TODAY);

        searchField.addValueChangeListener(event -> applySearchFilter());

        rangeSelect.addValueChangeListener(event -> {
            searchField.clear();
            updateGrid(event.getValue());
        });

        var toolbar = new HorizontalLayout(rangeSelect, searchField);

        add(heading, toolbar, grid);
    }

    private void updateGrid(String range) {
        heading.setText("Album Birthdays \u2014 " + range);
        var today = LocalDate.now();

        var birthdays = switch (range) {
            case LAST_7_DAYS -> birthdayService.findBirthdaysBetween(today.minusDays(7), today);
            case NEXT_7_DAYS -> birthdayService.findBirthdaysBetween(today, today.plusDays(7));
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
