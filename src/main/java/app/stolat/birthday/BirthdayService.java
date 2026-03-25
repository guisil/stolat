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
import app.stolat.birthday.internal.DiscogsReleaseDateLookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Transactional
@Validated
public class BirthdayService {

    private final AlbumBirthdayRepository albumBirthdayRepository;
    private final ReleaseDateLookup releaseDateLookup;
    private final BandcampLookup bandcampLookup;
    private final DiscogsReleaseDateLookup discogsReleaseDateLookup;

    public BirthdayService(AlbumBirthdayRepository albumBirthdayRepository,
                           ReleaseDateLookup releaseDateLookup,
                           BandcampLookup bandcampLookup,
                           @Nullable DiscogsReleaseDateLookup discogsReleaseDateLookup) {
        this.albumBirthdayRepository = albumBirthdayRepository;
        this.releaseDateLookup = releaseDateLookup;
        this.bandcampLookup = bandcampLookup;
        this.discogsReleaseDateLookup = discogsReleaseDateLookup;
    }

    public List<AlbumBirthday> findAllBirthdays() {
        return albumBirthdayRepository.findAll();
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

        var existingByAlbum = albumBirthdayRepository.findByAlbumId(albumId);
        if (existingByAlbum.isPresent()) {
            return existingByAlbum;
        }

        return releaseDateLookup.lookUp(musicBrainzId)
                .map(releaseDate -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, musicBrainzId,
                                releaseDate, ReleaseDateSource.MUSICBRAINZ)));
    }

    public AlbumBirthday resolveReleaseDateDirect(UUID albumId, String albumTitle,
                                                   String artistName, UUID musicBrainzId,
                                                   LocalDate releaseDate, ReleaseDateSource source) {
        var existing = albumBirthdayRepository.findByMusicBrainzId(musicBrainzId);
        if (existing.isPresent()) {
            return existing.get();
        }

        var existingByAlbum = albumBirthdayRepository.findByAlbumId(albumId);
        if (existingByAlbum.isPresent()) {
            return existingByAlbum.get();
        }

        return albumBirthdayRepository.save(
                new AlbumBirthday(albumTitle, artistName, albumId, musicBrainzId,
                        releaseDate, source));
    }

    public AlbumBirthday resolveReleaseDateForAlbum(UUID albumId, String albumTitle,
                                                     String artistName, LocalDate releaseDate,
                                                     ReleaseDateSource source) {
        return resolveReleaseDateForAlbum(albumId, albumTitle, artistName, releaseDate, source, null);
    }

    public AlbumBirthday resolveReleaseDateForAlbum(UUID albumId, String albumTitle,
                                                     String artistName, LocalDate releaseDate,
                                                     ReleaseDateSource source, Long discogsId) {
        return albumBirthdayRepository.findByAlbumId(albumId)
                .orElseGet(() -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, null,
                                discogsId, releaseDate, source)));
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

    public Optional<AlbumBirthday> resolveReleaseDateFromDiscogs(UUID albumId, String albumTitle,
                                                                   String artistName, long discogsId) {
        if (discogsReleaseDateLookup == null) {
            return Optional.empty();
        }

        var existing = albumBirthdayRepository.findByAlbumId(albumId);
        if (existing.isPresent()) {
            var birthday = existing.get();
            if (birthday.isYearOnlyDate() && birthday.getDiscogsId() != null) {
                var fullDate = discogsReleaseDateLookup.lookUp(discogsId);
                if (fullDate.isPresent() && !fullDate.get().equals(birthday.getReleaseDate())) {
                    birthday.updateReleaseDate(fullDate.get(), ReleaseDateSource.DISCOGS);
                    albumBirthdayRepository.save(birthday);
                    return Optional.of(birthday);
                }
            }
            return existing;
        }

        return discogsReleaseDateLookup.lookUp(discogsId)
                .map(releaseDate -> albumBirthdayRepository.save(
                        new AlbumBirthday(albumTitle, artistName, albumId, null,
                                discogsId, releaseDate, ReleaseDateSource.DISCOGS)));
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

    public List<AlbumBirthday> upgradeDiscogsYearOnlyBirthdays() {
        if (discogsReleaseDateLookup == null) {
            log.warn("Discogs release date lookup is not configured — skipping upgrade");
            return List.of();
        }

        var yearOnlyBirthdays = albumBirthdayRepository.findDiscogsYearOnlyBirthdays(ReleaseDateSource.DISCOGS);
        log.info("Found {} Discogs birthdays with year-only dates to upgrade", yearOnlyBirthdays.size());

        var upgraded = new java.util.ArrayList<AlbumBirthday>();
        for (var birthday : yearOnlyBirthdays) {
            var fullDate = discogsReleaseDateLookup.lookUp(birthday.getDiscogsId());
            if (fullDate.isPresent() && !fullDate.get().equals(birthday.getReleaseDate())) {
                var oldDate = birthday.getReleaseDate();
                birthday.updateReleaseDate(fullDate.get(), ReleaseDateSource.DISCOGS);
                albumBirthdayRepository.save(birthday);
                log.info("Upgraded '{}' by '{}' from {} to {}",
                        birthday.getAlbumTitle(), birthday.getArtistName(),
                        oldDate, fullDate.get());
                upgraded.add(birthday);
            }
        }

        log.info("Upgraded {} of {} Discogs year-only birthdays", upgraded.size(), yearOnlyBirthdays.size());
        return upgraded;
    }
}
