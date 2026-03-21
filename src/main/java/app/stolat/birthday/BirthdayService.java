package app.stolat.birthday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import app.stolat.birthday.internal.AlbumBirthdayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BirthdayService {

    private final AlbumBirthdayRepository albumBirthdayRepository;
    private final ReleaseDateLookup releaseDateLookup;

    public BirthdayService(AlbumBirthdayRepository albumBirthdayRepository,
                           ReleaseDateLookup releaseDateLookup) {
        this.albumBirthdayRepository = albumBirthdayRepository;
        this.releaseDateLookup = releaseDateLookup;
    }

    public List<AlbumBirthday> findBirthdaysOn(LocalDate date) {
        return albumBirthdayRepository.findByReleaseDateMonthAndDay(
                date.getMonthValue(), date.getDayOfMonth());
    }

    public Optional<AlbumBirthday> resolveReleaseDate(String albumTitle, String artistName,
                                                       UUID musicBrainzId) {
        return releaseDateLookup.lookUp(musicBrainzId)
                .map(releaseDate -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, musicBrainzId, releaseDate)));
    }
}
