package stolat.bootstrap.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import stolat.bootstrap.dao.AlbumBirthdayDao;

@Component
public class AlbumBirthdayCommand {

    @Autowired
    private AlbumBirthdayDao albumBirthdayDao;

    public void updateAlbumBirthdayDatabase() {
        albumBirthdayDao.clearAlbumBirthdays();
        albumBirthdayDao.populateAlbumBirthdays();
    }
}
