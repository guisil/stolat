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
**Tests:** 35 passing (`mvn test -Dsurefire.useFile=false`) — verified 2026-03-21
**TDD workflow:** Two-tier (Full Cycle / Fast Cycle) — see `CLAUDE.md`

---

## Modules Planned

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Done (core) | Filesystem scanning, audio tag reading, album/artist/track management |
| `birthday` | Done (core) | Release date lookup (MusicBrainz API), caching, date range queries |
| `notification` | Not started | Daily email digests, recipient management |
| `discovery` | Not started (later) | Public-facing album birthday browsing, not tied to personal collection |

---

## What's Done

- Project redesign decisions finalized (see `CLAUDE.md`)
- Old multi-module code removed (preserved in git history on `main`)
- `CLAUDE.md` written with full architecture, conventions, and TDD workflow
- **Project scaffold** — POM, StoLatApplication, MainLayout, TestcontainersConfiguration,
  ModulithStructureTest, V1 migration, docker-compose, Maven wrapper
- **Collection module (complete):**
  - Entities: Artist, Album (aggregate root), Track
  - Repositories: ArtistRepository, AlbumRepository, TrackRepository
  - CollectionService: findAllAlbums, importAlbum, scanDirectory
  - AlbumDiscoveredEvent (enriched with albumTitle, artistName)
  - FileScanner (recursive audio file discovery)
  - TagReader (JAudioTagger/RouHim fork 2.0.19 via JitPack)
  - CollectionView at /collection (Grid with artist/album)
  - SecurityConfig with VaadinSecurityConfigurer
  - Tests: repository (3), service unit (6), module integration (2),
    FileScanner (2), TagReader (2), Karibu UI (2)
- **Birthday module (complete):**
  - AlbumBirthday entity (denormalized album/artist data, release date)
  - AlbumBirthdayRepository with findByReleaseDateMonthAndDay query
  - BirthdayService: findBirthdaysOn, resolveReleaseDate
  - ReleaseDateLookup interface (pluggable release date sources)
  - MusicBrainzLookup: REST API call to /ws/2/release-group/{mbid},
    parses first-release-date (full, year-only, year-month)
  - MusicBrainzConfig: RestClient bean with User-Agent, configurable base URL
  - AlbumDiscoveredListener: listens for events, triggers lookup
  - BirthdayView at /birthdays (Grid with artist, album, release date)
  - Tests: repository (2), service unit (3), listener unit (1), API unit (4),
    module integration (2), Karibu UI (2)

---

## What's Next

1. ~~**Project scaffold**~~ — Done
2. ~~**Collection module**~~ — Done (core)
3. ~~**Birthday module**~~ — Done (core)
4. **Notification module** — Email sending, scheduling, recipient management
5. **Discovery module** — Public-facing views (later)

---

## Key Technical Notes

- V2 Flyway migration contains all tables (artists, albums, tracks, album_birthdays)
  — consolidating migrations during pre-deployment phase
- JAudioTagger: using RouHim/jaudiotagger fork (2.0.19) from JitPack
- Internal classes are Java-public but in `internal/` — Spring Modulith enforces boundaries
- MusicBrainz API: rate limit 1 req/sec, User-Agent required
- MainLayout uses string path for SideNav ("collection") to avoid module cycles
- RestClient bean named musicBrainzRestClient, base URL configurable via
  `stolat.musicbrainz.base-url` property
