package app.stolat.birthday;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import app.stolat.birthday.internal.AlbumBirthdayRepository;
import app.stolat.birthday.internal.BandcampLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
public class BirthdayService {

    private final AlbumBirthdayRepository albumBirthdayRepository;
    private final ReleaseDateLookup releaseDateLookup;
    private final BandcampLookup bandcampLookup;

    public BirthdayService(AlbumBirthdayRepository albumBirthdayRepository,
                           ReleaseDateLookup releaseDateLookup,
                           BandcampLookup bandcampLookup) {
        this.albumBirthdayRepository = albumBirthdayRepository;
        this.releaseDateLookup = releaseDateLookup;
        this.bandcampLookup = bandcampLookup;
    }

    public Map<UUID, LocalDate> findReleaseDatesByMusicBrainzId() {
        return albumBirthdayRepository.findAll().stream()
                .filter(b -> b.getMusicBrainzId() != null)
                .collect(Collectors.toMap(AlbumBirthday::getMusicBrainzId, AlbumBirthday::getReleaseDate));
    }

    public List<AlbumBirthday> findBirthdaysOn(LocalDate date) {
        return albumBirthdayRepository.findByReleaseDateMonthAndDay(
                date.getMonthValue(), date.getDayOfMonth());
    }

    public List<AlbumBirthday> findBirthdaysBetween(LocalDate from, LocalDate to) {
        return albumBirthdayRepository.findAll().stream()
                .filter(b -> {
                    var md = MonthDay.from(b.getReleaseDate());
                    var fromMd = MonthDay.from(from);
                    var toMd = MonthDay.from(to);
                    if (fromMd.compareTo(toMd) <= 0) {
                        return md.compareTo(fromMd) >= 0 && md.compareTo(toMd) <= 0;
                    } else {
                        // wraps around year boundary (e.g., Dec 15 to Jan 15)
                        return md.compareTo(fromMd) >= 0 || md.compareTo(toMd) <= 0;
                    }
                })
                .toList();
    }

    public Optional<AlbumBirthday> resolveReleaseDate(UUID albumId, String albumTitle,
                                                       String artistName, UUID musicBrainzId) {
        var existing = albumBirthdayRepository.findByMusicBrainzId(musicBrainzId);
        if (existing.isPresent()) {
            return existing;
        }

        return releaseDateLookup.lookUp(musicBrainzId)
                .map(releaseDate -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, musicBrainzId,
                                releaseDate, ReleaseDateSource.MUSICBRAINZ)));
    }

    public AlbumBirthday resolveReleaseDateDirect(UUID albumId, String albumTitle,
                                                   String artistName, UUID musicBrainzId,
                                                   LocalDate releaseDate, ReleaseDateSource source) {
        return albumBirthdayRepository.findByMusicBrainzId(musicBrainzId)
                .orElseGet(() -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, musicBrainzId,
                                releaseDate, source)));
    }

    public AlbumBirthday resolveReleaseDateForAlbum(UUID albumId, String albumTitle,
                                                     String artistName, LocalDate releaseDate,
                                                     ReleaseDateSource source) {
        return albumBirthdayRepository.findByAlbumId(albumId)
                .orElseGet(() -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, null,
                                releaseDate, source)));
    }

    public Optional<AlbumBirthday> resolveReleaseDateFromBandcamp(UUID albumId,
                                                                    String albumTitle,
                                                                    String artistName,
                                                                    String bandcampUrl) {
        var existing = albumBirthdayRepository.findByAlbumId(albumId);
        if (existing.isPresent()) {
            return existing;
        }

        return bandcampLookup.lookUp(bandcampUrl)
                .map(releaseDate -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, null,
                                releaseDate, ReleaseDateSource.BANDCAMP)));
    }

    public Set<UUID> findAlbumIdsWithBirthdays() {
        var result = new HashSet<UUID>();
        for (var birthday : albumBirthdayRepository.findAll()) {
            if (birthday.getAlbumId() != null) {
                result.add(birthday.getAlbumId());
            }
        }
        return result;
    }
}
