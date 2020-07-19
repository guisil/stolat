package stolat.bootstrap.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import stolat.bootstrap.dao.AlbumBirthdayDao;

@Component
@AllArgsConstructor
@Slf4j
public class AlbumBirthdayCommand {

    private AlbumBirthdayDao albumBirthdayDao;

    public void updateAlbumBirthdayDatabase() {
        log.info("Triggering Album Birthday update");
        albumBirthdayDao.clearAlbumBirthdays();
        albumBirthdayDao.populateAlbumBirthdays();
        log.info("Album birthday update triggered");
    }
}
