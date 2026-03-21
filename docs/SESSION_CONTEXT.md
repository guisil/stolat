# Session Context — StoLat Project

## What this file is

Standalone context for resuming work on the StoLat project. Contains everything
needed to continue even without memory files or prior conversation history.

---

## Project Overview

**StoLat** (Album Birthdays) — Spring Boot 4 + Vaadin 25 (Java Flow) + PostgreSQL
web app for tracking album birthdays from a personal music collection. Uses Spring
Modulith for modular architecture, MusicBrainz API for release date lookups, Flyway
for migrations, Testcontainers + Karibu Testing for tests. Full conventions in
`CLAUDE.md` at project root.

**Branch:** `redesign`
**Tests:** 43 passing (`mvn test -Dsurefire.useFile=false`) — verified 2026-03-21
**TDD workflow:** Two-tier (Full Cycle / Fast Cycle) — see `CLAUDE.md`

---

## Modules

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Done | Filesystem scanning, audio tag reading, album/artist/track management |
| `birthday` | Done | Release date lookup (MusicBrainz API), caching, date range queries |
| `notification` | Done | Daily email digests via Thymeleaf templates |
| `discovery` | Not started (later) | Public-facing album birthday browsing, not tied to personal collection |

---

## What's Done

- **Project scaffold** — POM, StoLatApplication (@EnableScheduling), MainLayout (SideNav),
  TestcontainersConfiguration, ModulithStructureTest, V1 migration, docker-compose, Maven wrapper
- **Collection module:**
  - Entities: Artist, Album (aggregate root), Track
  - Repositories: ArtistRepository (findByMusicBrainzId), AlbumRepository (findByMusicBrainzId),
    TrackRepository
  - CollectionService: findAllAlbums, importAlbum (with duplicate detection), scanDirectory
  - AlbumDiscoveredEvent (albumId, albumTitle, artistName, musicBrainzId)
  - FileScanner (recursive audio file discovery, 8 supported formats)
  - TagReader (JAudioTagger/RouHim fork 2.0.19 via JitPack)
  - CollectionView at /collection (Grid + Scan Collection button)
  - SecurityConfig with VaadinSecurityConfigurer
  - Tests: repository (3), service unit (7), module integration (2),
    FileScanner (2), TagReader (2), Karibu UI (3)
- **Birthday module:**
  - AlbumBirthday entity (denormalized album/artist data, release date)
  - AlbumBirthdayRepository: findByReleaseDateMonthAndDay, findByMusicBrainzId
  - BirthdayService: findBirthdaysOn, resolveReleaseDate (with duplicate detection),
    resolveReleaseDateDirect (for seeding without API call)
  - ReleaseDateLookup interface (pluggable release date sources)
  - MusicBrainzLookup: REST API call to /ws/2/release-group/{mbid},
    parses first-release-date (full, year-only, year-month)
  - MusicBrainzConfig: RestClient with User-Agent, rate limiting (1.1s interval),
    configurable base URL
  - AlbumDiscoveredListener: listens for events, triggers lookup
  - BirthdayView at /birthdays (Grid with artist, album, release date)
  - Tests: repository (2), service unit (4), listener unit (1), API unit (4),
    module integration (2), Karibu UI (2)
- **Notification module:**
  - NotificationService: sendDailyDigest (Thymeleaf HTML template, anniversary years)
  - EmailSender (JavaMailSender, configurable recipient/from)
  - NotificationScheduler: daily cron (8am default), optional send-on-startup
  - Thymeleaf template: templates/birthday-digest.html
  - Tests: service unit (2), scheduler unit (3)
- **Dev profile & local testing:**
  - application-dev.properties: DB, Mailpit, music dir, startup notify
  - DevDataInitializer: seeds 9 albums with birthdays (incl. one for today)
  - docker-compose.yml: PostgreSQL + Mailpit (SMTP :1025, Web UI :8025)
  - Duplicate handling: both collection and birthday skip already-imported data

---

## What's Next

- **Discovery module** — Public-facing views (later)
- **Remaining polish:**
  - Recipient management UI (notification)
  - Music directory config from UI (instead of property only)
  - Album detail view with tracks
  - Pagination / sorting on grids

---

## Key Technical Notes

- V2 Flyway migration contains all tables (artists, albums, tracks, album_birthdays)
  — consolidating migrations during pre-deployment phase
- JAudioTagger: using RouHim/jaudiotagger fork (2.0.19) from JitPack — actively maintained,
  Java 25 compatible
- Internal classes are Java-public but in `internal/` — Spring Modulith enforces boundaries
- MusicBrainz API: rate limit enforced via RateLimitingInterceptor (1.1s between requests),
  User-Agent required
- MainLayout uses string paths for SideNav to avoid module cycles
- BirthdayView uses route "birthdays" (not "") to avoid Karibu test issues with
  default route being created during MockVaadin.setup()

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `stolat.collection.music-directory` | (required) | Path to music collection root |
| `stolat.musicbrainz.base-url` | `https://musicbrainz.org/ws/2` | MusicBrainz API base URL |
| `stolat.notification.recipient` | (required) | Email recipient for daily digests |
| `stolat.notification.from` | `StoLat <stolat@noreply.com>` | Email sender address |
| `stolat.notification.cron` | `0 0 8 * * *` | Cron for daily digest (8am) |
| `stolat.notification.send-on-startup` | `false` | Send digest on app startup |
