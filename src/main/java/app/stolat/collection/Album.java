package app.stolat.collection;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "album_formats", joinColumns = @JoinColumn(name = "album_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private Set<AlbumFormat> formats = new HashSet<>();

    @Column(name = "discogs_id")
    private Long discogsId;

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

    public Album(String title, Artist artist, Long discogsId) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.artist = artist;
        this.discogsId = discogsId;
    }

    public void addFormat(AlbumFormat format) { this.formats.add(format); }
    public void removeFormat(AlbumFormat format) { this.formats.remove(format); }
    public boolean hasFormat(AlbumFormat format) { return this.formats.contains(format); }
    public boolean hasAnyFormat() { return !this.formats.isEmpty(); }
    public void assignMusicBrainzId(UUID musicBrainzId) { this.musicBrainzId = musicBrainzId; }
    public void assignDiscogsId(Long discogsId) { this.discogsId = discogsId; }

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
