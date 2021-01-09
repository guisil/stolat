package stolat.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        String firstJoinedArtistNames = getJoinedArtistNames(o1.getAlbum().getArtists());
        String secondJoinedArtistNames = getJoinedArtistNames(o2.getAlbum().getArtists());

        return firstJoinedArtistNames.compareTo(secondJoinedArtistNames);
    }

    private String getJoinedArtistNames(List<Artist> artists) {
        return artists.stream().map(Artist::getArtistName).collect(Collectors.joining());
    }
}
