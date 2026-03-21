package app.stolat;

import java.time.LocalDate;
import app.stolat.birthday.AlbumBirthday;
import app.stolat.birthday.BirthdayService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Birthdays")
@AnonymousAllowed
public class BirthdayView extends VerticalLayout {

    public BirthdayView(BirthdayService birthdayService) {
        var today = LocalDate.now();
        var heading = new H2("Album Birthdays — " + today);

        var grid = new Grid<>(AlbumBirthday.class, false);
        grid.addColumn(AlbumBirthday::getArtistName).setHeader("Artist");
        grid.addColumn(AlbumBirthday::getAlbumTitle).setHeader("Album");
        grid.addColumn(AlbumBirthday::getReleaseDate).setHeader("Release Date");
        grid.setItems(birthdayService.findBirthdaysOn(today));

        add(heading, grid);
    }
}
