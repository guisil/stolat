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
**Tests:** 19 passing (`mvn test -Dsurefire.useFile=false`) — verified 2026-03-21
**TDD workflow:** Two-tier (Full Cycle / Fast Cycle) — see `CLAUDE.md`

---

## Modules Planned

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | In progress | Filesystem scanning, audio tag reading, album/artist/track management |
| `birthday` | Not started | Release date lookup (MusicBrainz API), caching, date range queries |
| `notification` | Not started | Daily email digests, recipient management |
| `discovery` | Not started (later) | Public-facing album birthday browsing, not tied to personal collection |

---

## What's Done

- Project redesign decisions finalized (see `CLAUDE.md`)
- Old multi-module code removed (preserved in git history on `main`)
- Branch structure cleaned up (`main` as default, `redesign` for new work)
- `CLAUDE.md` written with full architecture, conventions, and TDD workflow
- **Project scaffold created:**
  - POM with Spring Boot 4.0.2, Vaadin 25.0.5, Spring Modulith 2.0.2, Testcontainers 2.0.3
  - `StoLatApplication` entry point with `@Theme("stolat")`
  - `MainLayout` (AppLayout with drawer navigation)
  - `TestcontainersConfiguration` (PostgreSQL 18 Alpine)
  - `ModulithStructureTest` (verify + PlantUML docs)
  - `StoLatApplicationTest` (context loads)
  - V1 Flyway migration (Spring Modulith event publication table)
  - `docker-compose.yml` for local PostgreSQL
  - Maven wrapper (3.9.12)
- **Collection module — domain + scanning layer:**
  - `Artist` entity (name, musicBrainzId, timestamps)
  - `Album` entity / aggregate root (title, musicBrainzId, ManyToOne Artist, timestamps)
  - `Track` entity (title, trackNumber, discNumber, musicBrainzId, ManyToOne Album, timestamps)
  - `ArtistRepository`, `AlbumRepository`, `TrackRepository` (internal, Java-public)
  - V2 Flyway migration (`artists`, `albums`, `tracks` tables)
  - `CollectionService` (public API): `findAllAlbums()`, `importAlbum(...)`, `scanDirectory(...)`
  - `AlbumDiscoveredEvent` (record, published on album import)
  - `TrackData` record (for passing track metadata to importAlbum)
  - `FileScanner` (internal): recursive audio file discovery (flac, mp3, ogg, m4a, etc.)
  - `TagReader` (internal): reads MusicBrainz tags via JAudioTagger (RouHim fork 2.0.19)
  - `AudioFileMetadata` (internal record): parsed tag data
  - `SecurityConfig` with `VaadinSecurityConfigurer`
  - `.mcp.json` with Vaadin MCP server
  - **Tests:** 3 repository tests, 6 service unit tests, 2 module integration tests,
    2 FileScanner tests, 2 TagReader tests, 2 scaffold tests (ModulithStructure + AppContext)

---

## What's Next

1. ~~**Project scaffold**~~ — Done
2. **Collection module** — remaining:
   - UI view (Karibu test) for browsing/triggering scan
   - Configuration for music directory path
3. **Birthday module** — AlbumBirthday entity, `ReleaseDateLookup` interface,
   MusicBrainz API implementation, event listener
4. **Notification module** — Email sending, scheduling, recipient management
5. **Discovery module** — Public-facing views (later)

---

## Key Technical Notes

- MusicBrainz API returns `first-release-date` on release group lookups — no local
  MusicBrainz database needed
- Audio files are tagged with MusicBrainz IDs (user maintains tags via Picard)
- Fuzzy matching (artist + album name search) planned as fallback for untagged files
- `ReleaseDateLookup` interface allows adding Spotify/Discogs as future sources
- Stack matches the meads sibling project: Java 25, Spring Boot 4, Vaadin 25,
  Spring Modulith 2.0.x
- JAudioTagger: using RouHim/jaudiotagger fork (2.0.19) from JitPack — actively maintained,
  Java 25 compatible. Original net.jthink:jaudiotagger 3.0.1 was last updated Oct 2021.
- Internal classes (repositories, FileScanner, TagReader, AudioFileMetadata) are Java-public
  but in `internal/` package — Spring Modulith enforces module boundaries.
