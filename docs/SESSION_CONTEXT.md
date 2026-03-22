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
for migrations, Testcontainers + Karibu Testing for tests.

**Branch:** `main`
**Current release:** v0.1.2
**Dev version:** 0.1.3-SNAPSHOT
**Tests:** 102 passing (`mvn test -Dsurefire.useFile=false`)
**Deployed:** Raspberry Pi (Docker, Ubuntu Server 24.04)

---

## Modules

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Done | Digital + vinyl collection, filesystem scanning (incl. non-MBID albums), Discogs import (with year capture), format tracking, Volumio playback |
| `birthday` | Done | Release date lookup (MusicBrainz API + Bandcamp), caching, date range queries, release date source tracking, missing birthdays view |
| `notification` | Done | Daily email digests via Thymeleaf templates (Gmail SMTP) |
| `discovery` | Not started (later) | Public-facing album birthday browsing |

---

## Deployment

- **Platform:** Raspberry Pi with Ubuntu Server 24.04, Docker
- **Compose:** `docker-compose.rpi.yml` runs PostgreSQL + app
- **Music:** NAS share mounted via CIFS at `/mnt/music`
- **Email:** Gmail with app password
- **Scripts:** `deploy.sh` (from Mac), `deploy-pi.sh` (on Pi)
- **Checklist:** `docs/deployment/rpi-checklist.md`

---

## Architecture Highlights

- **Scan pipeline:** filesystem walk → group by directory → read tags per directory →
  import album → commit (progressive, directory-by-directory). Albums without MusicBrainz
  tags are imported too (grouped by artist+title, year extracted from date tag).
- **Discogs import:** paginated fetch → match by discogsId/artist+title/MusicBrainz search →
  import with VINYL format. Captures release year as fallback date. Partial fetch skips
  reconciliation. Artist disambiguation stripping handles any parenthesized suffix.
- **MusicBrainz:** shared rate limiter bean (1.1s interval) across birthday lookup and
  collection search clients. Lookups are @Async (non-blocking). Search score threshold
  configurable via `stolat.musicbrainz.search-score-threshold` (default 90).
- **Release date sources:** `ReleaseDateSource` enum (MUSICBRAINZ, DISCOGS, BANDCAMP, MANUAL)
  tracked on each AlbumBirthday. Albums without MusicBrainz IDs can now have birthdays
  via albumId-based lookup (e.g., Discogs year fallback, Bandcamp user-initiated lookup).
- **Bandcamp lookup:** `BandcampLookup` component fetches album page HTML and extracts
  `datePublished` from JSON-LD. User-initiated only (no automated crawling).
- **Format tracking:** `@ElementCollection` with `Set<AlbumFormat>` (DIGITAL/VINYL).
  Soft delete via format reconciliation — empty formats = removed.
- **Vaadin Push:** @Push for progressive UI updates during scans (3s polling).
- **Views:** BirthdayView at `/` (date ranges, format icons, Volumio play button,
  multi-sort, full-height grid), CollectionView at `/collection` (format filter,
  scan buttons, search, multi-sort, split Birthday/Year columns),
  MissingBirthdaysView at `/missing-birthdays` (status filter, Bandcamp URL dialog,
  year column). Filter state persists across navigation via VaadinSession.

---

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `stolat.collection.music-directory` | (required) | Path to music collection |
| `stolat.collection.scan-cron` | `0 0 3 * * *` | Filesystem scan (3am) |
| `stolat.discogs.username` | (none, opt-in) | Discogs username |
| `stolat.discogs.token` | (none) | Discogs personal access token |
| `stolat.discogs.scan-cron` | `0 0 4 * * *` | Discogs sync (4am) |
| `stolat.musicbrainz.base-url` | `https://musicbrainz.org/ws/2` | MusicBrainz API |
| `stolat.musicbrainz.search-score-threshold` | `90` | Min score for MusicBrainz search match |
| `stolat.notification.recipient` | (required) | Email recipient |
| `stolat.notification.from` | `StoLat <stolat@noreply.com>` | Email sender |
| `stolat.notification.cron` | `0 0 8 * * *` | Daily digest (8am) |
| `stolat.notification.send-on-startup` | `false` | Send digest on startup |
| `stolat.volumio.url` | (none, opt-in) | Volumio instance URL |
| `stolat.user-agent` | `StoLat/0.1.3-SNAPSHOT (...)` | User-Agent for APIs |

---

## Key Technical Notes

- V2 migration: all tables (artists, albums, tracks, album_birthdays, album_formats)
- V3 migration: indexes on musicbrainz_id, discogs_id columns
- V4 migration: album_birthdays evolution — nullable musicbrainz_id, album_id column,
  release_date_source column (backfilled as MUSICBRAINZ), partial unique indexes
- JAudioTagger: RouHim fork 2.0.19 via JitPack, logging set to WARN
- Discogs/Volumio features conditional on config (@ConditionalOnProperty)
- Views are @AnonymousAllowed (auth deferred — TODO: DB-backed auth)
- SecurityConfig has in-memory user (user/stolat) with TODO for replacement
- Version displayed in drawer footer via BuildProperties

## What's Next

- DB-backed authentication (replace in-memory user)
- Notification view (settings, history, manual send)
- Album detail view with tracks
- Discovery module (public-facing)
- Additional release date sources (Spotify, Discogs API release dates)
- Improve Bandcamp lookup UX (batch lookups, URL suggestions)
