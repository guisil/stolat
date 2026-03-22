package app.stolat.birthday.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.stolat.birthday.AlbumBirthday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlbumBirthdayRepository extends JpaRepository<AlbumBirthday, UUID> {

    @Query("SELECT ab FROM AlbumBirthday ab WHERE MONTH(ab.releaseDate) = :month AND DAY(ab.releaseDate) = :day")
    List<AlbumBirthday> findByReleaseDateMonthAndDay(int month, int day);

    Optional<AlbumBirthday> findByMusicBrainzId(UUID musicBrainzId);

    Optional<AlbumBirthday> findByAlbumId(UUID albumId);
}
