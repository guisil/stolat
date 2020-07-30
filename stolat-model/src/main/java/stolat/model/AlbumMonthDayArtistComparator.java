package stolat.model;

import java.util.Comparator;

public class AlbumMonthDayArtistComparator implements Comparator<AlbumBirthday> {

    @Override
    public int compare(AlbumBirthday o1, AlbumBirthday o2) {
        int firstDay = o1.getAlbumDay() > 0 ? o1.getAlbumDay() : Integer.MAX_VALUE;
        int firstMonth = o1.getAlbumMonth() > 0 ? o1.getAlbumMonth() : Integer.MAX_VALUE;
        int secondDay = o2.getAlbumDay() > 0 ? o2.getAlbumDay() : Integer.MAX_VALUE;
        int secondMonth = o2.getAlbumMonth() > 0 ? o2.getAlbumMonth() : Integer.MAX_VALUE;

        if (firstMonth > secondMonth) {
            return 1;
        }
        if (firstMonth < secondMonth) {
            return -1;
        }

        if (firstDay > secondDay) {
            return 1;
        }
        if (firstDay < secondDay) {
            return -1;
        }

        return o1.getAlbum().getArtistName().compareTo(o2.getAlbum().getArtistName());
    }
}
