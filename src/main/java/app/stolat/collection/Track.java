package app.stolat.collection;

import java.time.Instant;
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
@Table(name = "tracks")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Track {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "track_number", nullable = false)
    private int trackNumber;

    @Column(name = "disc_number", nullable = false)
    private int discNumber;

    @Column(name = "musicbrainz_id")
    private UUID musicBrainzId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Track(String title, int trackNumber, int discNumber, UUID musicBrainzId, Album album) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.musicBrainzId = musicBrainzId;
        this.album = album;
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
