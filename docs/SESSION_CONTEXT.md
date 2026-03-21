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
**Tests:** 3 passing (`mvn test -Dsurefire.useFile=false`) — verified 2026-03-21
**TDD workflow:** Two-tier (Full Cycle / Fast Cycle) — see `CLAUDE.md`

---

## Modules Planned

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Not started | Filesystem scanning, audio tag reading, album/artist/track management |
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

---

## What's Next

1. ~~**Project scaffold**~~ — Done
2. **Collection module** — Album/Artist/Track entities, repositories, CollectionService,
   filesystem scanning, tag reading
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
