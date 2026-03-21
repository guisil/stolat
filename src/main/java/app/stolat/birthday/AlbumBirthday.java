package app.stolat.birthday;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "album_birthdays")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AlbumBirthday {

    @Id
    private UUID id;

    @Column(name = "album_title", nullable = false)
    private String albumTitle;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "musicbrainz_id", nullable = false, unique = true)
    private UUID musicBrainzId;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AlbumBirthday(String albumTitle, String artistName, UUID musicBrainzId, LocalDate releaseDate) {
        this.id = UUID.randomUUID();
        this.albumTitle = albumTitle;
        this.artistName = artistName;
        this.musicBrainzId = musicBrainzId;
        this.releaseDate = releaseDate;
    }

    @PrePersist
    void onPrePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
