package stolat.dao;

import stolat.model.AlbumBirthday;

import java.time.MonthDay;
import java.util.List;

public interface AlbumBirthdayDao {

    void clearAlbumBirthdays();

    void populateAlbumBirthdays();

    default List<AlbumBirthday> getAlbumBirthdays(MonthDay fromTo) {
        return getAlbumBirthdays(fromTo, fromTo);
    }

    List<AlbumBirthday> getAlbumBirthdays(MonthDay from, MonthDay to);
}
