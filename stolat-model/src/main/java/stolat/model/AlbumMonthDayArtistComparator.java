package stolat.model;

import java.util.Comparator;

public class AlbumMonthDayArtistComparator implements Comparator<AlbumBirthday> {

    @Override
    public int compare(AlbumBirthday o1, AlbumBirthday o2) {
        int firstDay = Integer.MAX_VALUE;
        int firstMonth = Integer.MAX_VALUE;
        int secondDay = Integer.MAX_VALUE;
        int secondMonth = Integer.MAX_VALUE;
        if (o1.getAlbumCompleteDate() != null) {
            firstDay = o1.getAlbumCompleteDate().getDayOfMonth();
            firstMonth = o1.getAlbumCompleteDate().getMonthValue();
        } else if (o1.getAlbumYearMonth() != null) {
            firstMonth = o1.getAlbumYearMonth().getMonthValue();
        }
        if (o2.getAlbumCompleteDate() != null) {
            secondDay = o2.getAlbumCompleteDate().getDayOfMonth();
            secondMonth = o2.getAlbumCompleteDate().getMonthValue();
        } else if (o2.getAlbumYearMonth() != null) {
            secondMonth = o2.getAlbumYearMonth().getMonthValue();
        }

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
