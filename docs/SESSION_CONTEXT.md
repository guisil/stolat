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
**Current release:** v0.2.6
**Dev version:** 0.2.7-SNAPSHOT
**Tests:** 177 passing (`mvn test -Dsurefire.useFile=false`)
**Deployed:** Raspberry Pi (Docker, Ubuntu Server 24.04)

---

## Modules

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Done | Digital + vinyl collection, filesystem scanning (incl. non-MBID albums), Discogs import (with year capture), format tracking, Volumio playback |
| `birthday` | Done | Release date lookup (MusicBrainz API + Bandcamp + Discogs full dates), caching, date range queries, release date source tracking, missing birthdays view, Last.fm play counts |
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
  tags are imported too (grouped by artist+title, year extracted from date tag). TagReader
  prefers ALBUM_ARTIST over ARTIST (fixes compilation album grouping). Re-scan merges
  newly-added MusicBrainz IDs into existing albums matched by artist+title. Diagnostic
  logging reports per-directory tag failures and scan summary. Scan errors logged via
  CompletableFuture.exceptionally (previously swallowed silently).
- **Discogs import:** paginated fetch → match by discogsId/artist+title/MusicBrainz search →
  import with VINYL format. Captures release year as fallback date. Partial fetch skips
  reconciliation. Artist disambiguation stripping handles any parenthesized suffix.
- **MusicBrainz:** shared rate limiter bean (1.1s interval) across birthday lookup and
  collection search clients. Lookups are @Async (non-blocking). Search score threshold
  configurable via `stolat.musicbrainz.search-score-threshold` (default 90).
- **Release date sources:** `ReleaseDateSource` enum (MUSICBRAINZ, MB_PENDING, DISCOGS,
  BANDCAMP, MANUAL) tracked on each AlbumBirthday. Albums without MusicBrainz IDs can now
  have birthdays via albumId-based lookup (e.g., Discogs year fallback, Bandcamp
  user-initiated lookup). MusicBrainz dates take priority: on rescan, if an album gains an
  MBID (or its MBID changes), the system looks up the release date via MusicBrainz API and
  upgrades the existing birthday. If MusicBrainz has no date yet, the MBID is stored with
  MB_PENDING source so future rescans retry the lookup.
- **Discogs full dates:** `DiscogsReleaseDateLookup` in birthday module fetches full release
  dates from `GET /releases/{discogsId}`. Handles YYYY, YYYY-MM-00, YYYY-MM-DD formats.
  `AlbumBirthday` entity stores `discogsId` for upgrade tracking. Batch upgrade via
  `BirthdayService.upgradeDiscogsYearOnlyBirthdays()` finds Jan-1 Discogs entries and
  re-fetches from API. UI: "Upgrade Discogs Dates" button on MissingBirthdaysView toolbar,
  per-album Discogs lookup button (globe icon) for albums with discogsId.
- **Bandcamp lookup:** `BandcampLookup` component fetches album page HTML and extracts
  `datePublished` from JSON-LD. Follows 301 redirects for labels with custom domains.
  User-initiated only (no automated crawling).
  `BandcampUrlSuggester` generates candidate URLs from artist/album names and Bandcamp
  search links. Slug generation replaces `&` with `and` and strips edition suffixes (Deluxe,
  Remastered, etc.). MissingBirthdaysView dialog pre-populates suggested URL with inline
  validation and loading state. "Try Bandcamp" toolbar button batch-tries suggested URLs
  for all missing albums (async, rate-limited 1s between requests).
- **Format tracking:** `@ElementCollection` with `Set<AlbumFormat>` (DIGITAL/VINYL).
  Soft delete via format reconciliation — empty formats = removed.
- **Vaadin Push:** @Push for progressive UI updates during scans (3s polling).
- **Last.fm play counts:** `LastFmClient` fetches user play counts via `album.getinfo` API.
  Conditional on `stolat.lastfm.api-key` + `stolat.lastfm.username`. Rate-limited (200ms
  between requests). `BirthdayService.syncPlayCounts()` updates all birthdays. Scheduled
  daily (default 6am) + manual "Sync Plays" button in BirthdayView. Play counts stored on
  `AlbumBirthday` entity (`playCount`, `playCountUpdatedAt`). Shown in BirthdayView Plays
  column and email digest.
- **Views:** BirthdayView at `/` (date ranges incl. "All", source filter, play count column,
  count label, format icons, Volumio play button, conditional Sync Plays button, multi-sort,
  full-height grid), CollectionView at `/collection` (format filter, scan buttons, search,
  multi-sort, split Birthday/Year columns, total album count),
  MissingBirthdaysView at `/missing-birthdays` (status filter, Bandcamp URL dialog with
  suggested URL and search link, year column, retry button, Discogs upgrade button,
  count label with status breakdown),
  StatsView at `/stats` (totals: albums, birthdays by source, missing breakdown).
  Filter state persists across navigation via VaadinSession.

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
| `stolat.notification.cron` | `0 0 5 * * *` | Daily digest (5am) |
| `stolat.notification.send-on-startup` | `false` | Send digest on startup |
| `stolat.volumio.url` | (none, opt-in) | Volumio instance URL |
| `stolat.lastfm.api-key` | (none, opt-in) | Last.fm API key |
| `stolat.lastfm.username` | (none) | Last.fm username for play counts |
| `stolat.lastfm.sync-cron` | `0 0 6 * * *` | Last.fm play count sync (6am) |
| `stolat.user-agent` | `StoLat/{version} (...)` | User-Agent for APIs |

---

## Key Technical Notes

- V2 migration: all tables (artists, albums, tracks, album_birthdays, album_formats)
- V3 migration: indexes on musicbrainz_id, discogs_id columns
- V4 migration: album_birthdays evolution — nullable musicbrainz_id, album_id column,
  release_date_source column (backfilled as MUSICBRAINZ), partial unique indexes
- V5 migration: add discogs_id column to album_birthdays (with partial index)
- V6 migration: add play_count and play_count_updated_at columns to album_birthdays
- JAudioTagger: RouHim fork 2.0.19 via JitPack, logging set to WARN
- Discogs/Volumio/Last.fm features conditional on config (@ConditionalOnProperty)
- Views are @AnonymousAllowed (auth deferred — TODO: DB-backed auth)
- SecurityConfig has in-memory user (user/stolat) with TODO for replacement
- Version displayed in drawer footer via BuildProperties

## Known Gaps
- **Duplicate artists:** The same artist can have multiple DB rows with different
  MusicBrainz IDs (e.g., from different release groups or artist credits). The
  `findArtistByName` helper in CollectionService picks the one with an MBID when
  importing non-MBID albums, but the duplicates remain in the DB. This could cause
  albums by the same artist to be split across artist records. A future artist-merge
  feature or dedup migration may be needed if this causes inconsistencies.

---

## What's Next

- Additional release date sources (Spotify)
- Notification view (settings, history, manual send, multiple recipient emails)
- Album detail view with tracks
- DB-backed authentication (replace in-memory user)
- Discovery module (public-facing)
