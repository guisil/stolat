package app.stolat.collection;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "albums")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Album {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "musicbrainz_id")
    private UUID musicBrainzId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Album(String title, UUID musicBrainzId, Artist artist) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.musicBrainzId = musicBrainzId;
        this.artist = artist;
    }

    public void updateReleaseDate(LocalDate releaseDate) {
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
