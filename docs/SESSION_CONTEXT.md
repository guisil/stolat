# Session Context — StoLat Project

## What this file is

Standalone context for resuming work on the StoLat project. Contains everything
needed to continue even without memory files or prior conversation history.

---

## Project Overview

**StoLat** (Album Birthdays) — Spring Boot 4 + Vaadin 25 (Java Flow) + PostgreSQL
web app for tracking album birthdays from a personal music collection. Supports
both digital (local FLAC files) and vinyl (Discogs) collections. Uses Spring
Modulith for modular architecture, MusicBrainz API for release date lookups, Flyway
for migrations, Testcontainers + Karibu Testing for tests. Full conventions in
`CLAUDE.md` at project root.

**Branch:** `redesign`
**Tests:** 73 passing (`mvn test -Dsurefire.useFile=false`) — verified 2026-03-21
**TDD workflow:** Two-tier (Full Cycle / Fast Cycle) — see `CLAUDE.md`

---

## Modules

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Done | Digital + vinyl collection management, filesystem scanning, Discogs import, format tracking |
| `birthday` | Done | Release date lookup (MusicBrainz API), caching, date range queries |
| `notification` | Done | Daily email digests via Thymeleaf templates |
| `discovery` | Not started (later) | Public-facing album birthday browsing |

---

## What's Done

- **Project scaffold** — POM, StoLatApplication (@EnableScheduling, @EnableAsync, @Push),
  MainLayout, LoginView, SecurityConfig, TestcontainersConfiguration, ModulithStructureTest
- **Collection module:**
  - Entities: Artist, Album (aggregate root, formats, discogsId), Track, AlbumFormat enum
  - Album format tracking: @ElementCollection with Set<AlbumFormat> (DIGITAL, VINYL)
  - Soft delete: albums with empty formats = removed, findAllActive() filters
  - Repositories: ArtistRepository, AlbumRepository (findByMusicBrainzId, findByDiscogsId,
    findAllActive, findByFormat, findByTitleAndArtistNameIgnoreCase), TrackRepository
  - CollectionService: findAllActiveAlbums, findAlbumsByFormat, importAlbum (with format),
    scanDirectory (with reconciliation), scanDiscogs (fetch, match, import, reconcile)
  - AlbumDiscoveredEvent (albumId, albumTitle, artistName, musicBrainzId)
  - FileScanner, TagReader (JAudioTagger/RouHim fork 2.0.19 via JitPack)
  - DiscogsClient (paginated collection fetch, artist disambiguation stripping)
  - DiscogsConfig (@ConditionalOnProperty), DiscogsScanScheduler (4am default)
  - MusicBrainzSearchClient (search release groups by artist+title, score >= 90)
  - MusicBrainzSearchConfig (RestClient with shared rate limiter)
  - CollectionScanScheduler (3am default)
  - CollectionView at /collection (format filter, search, format column,
    Scan Collection + Scan Discogs buttons, async scan with polling)
- **Birthday module:**
  - AlbumBirthday entity, AlbumBirthdayRepository
  - BirthdayService: findBirthdaysOn, findBirthdaysBetween, findReleaseDatesByMusicBrainzId,
    resolveReleaseDate, resolveReleaseDateDirect
  - ReleaseDateLookup interface, MusicBrainzLookup (REST API, rate limited)
  - AlbumDiscoveredListener (@Async, updates Album.releaseDate via CollectionService)
  - BirthdayView at / (date range selector: Today/Last-Next 7-30 days/This week/This month,
    search, Birthday + Year columns, format badges)
- **Notification module:**
  - NotificationService (Thymeleaf HTML template), EmailSender (MimeMessage)
  - NotificationScheduler (8am cron, optional send-on-startup)
- **Infrastructure:**
  - MusicBrainzRateLimiter: shared interceptor bean in root module (1.1s interval)
  - Both MusicBrainz RestClients share the same rate limiter
  - Dev profile: DevDataInitializer (9 digital + 1 vinyl + 1 both format albums),
    Mailpit, startup notification
  - SecurityConfig: in-memory user (user/stolat), LoginView

---

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `stolat.collection.music-directory` | (required) | Path to music collection root |
| `stolat.collection.scan-cron` | `0 0 3 * * *` | Cron for filesystem scan (3am) |
| `stolat.discogs.username` | (none, opt-in) | Discogs username |
| `stolat.discogs.token` | (none) | Discogs personal access token |
| `stolat.discogs.scan-cron` | `0 0 4 * * *` | Cron for Discogs sync (4am) |
| `stolat.musicbrainz.base-url` | `https://musicbrainz.org/ws/2` | MusicBrainz API base URL |
| `stolat.notification.recipient` | (required) | Email recipient for daily digests |
| `stolat.notification.from` | `StoLat <stolat@noreply.com>` | Email sender address |
| `stolat.notification.cron` | `0 0 8 * * *` | Cron for daily digest (8am) |
| `stolat.notification.send-on-startup` | `false` | Send digest on app startup |

---

## Key Technical Notes

- V2 Flyway migration contains all tables (artists, albums, tracks, album_birthdays,
  album_formats) — consolidating during pre-deployment phase
- MusicBrainz rate limit enforced globally via shared RateLimitingInterceptor bean
- Discogs feature is opt-in: @ConditionalOnProperty("stolat.discogs.username")
- Discogs matching: by discogsId → by artist+title → by MusicBrainz search → new album
- Album reconciliation: each scan removes its format from albums not found in source
- BirthdayView at route "/" (default), format badges from CollectionService
- @Async on AlbumDiscoveredListener, Awaitility in integration tests
- Cross-module views that need both services go in root module (currently none)
