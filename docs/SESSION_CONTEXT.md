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
**Current release:** v0.3.5
**Dev version:** 0.3.6-SNAPSHOT
**Tests:** 192 passing (`mvn test -Dsurefire.useFile=false`)
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
  Remastered, etc.). MissingBirthdaysView dialog pre-populates with the stored `Album.bandcampUrl`
  (read from audio file comment tag during scan) when available, falling back to the generated
  slug guess. Hint text indicates when the URL came from the file. "Try Bandcamp" toolbar button
  batch-tries stored URLs first, then generated slugs for albums without one (async,
  rate-limited 1s between requests).
- **Format tracking:** `@ElementCollection` with `Set<AlbumFormat>` (DIGITAL/VINYL).
  Soft delete via format reconciliation — empty formats = removed.
- **Vaadin Push:** @Push for progressive UI updates during scans (3s polling).
- **Last.fm play counts:** `LastFmClient` fetches user play counts via `album.getinfo` API.
  Conditional on `stolat.lastfm.api-key` + `stolat.lastfm.username`. Rate-limited (200ms
  between requests). `BirthdayService.syncPlayCounts()` updates all birthdays. Scheduled
  daily (default 6am) + manual "Sync Plays" button in BirthdayView. Play counts stored on
  `AlbumBirthday` entity (`playCount`, `playCountUpdatedAt`). Shown in BirthdayView Plays
  column and email digest.
- **Volumio playback:** `Album.folderPath` (relative to music root) stored during filesystem
  scan. `CollectionService.playAlbumOnVolumio` / `addAlbumToVolumioQueue` look up the album's
  folder path and pass it to `VolumioClient`, which prepends `stolat.volumio.music-library-uri`
  (default: `music-library`) to construct the Volumio browse URI, then either
  `POST /api/v1/replaceAndPlay` (play now) or `POST /api/v1/addToQueue` (queue — takes the bare
  browsed items array/object per Volumio's REST API, no wrapper key). Volumio 4 search API no
  longer returns local library results, so direct browse replaces the former search-based
  approach. After upgrading to Volumio 4, set `stolat.volumio.music-library-uri=music-library/NAS`
  (or whatever your top-level browse path is) and re-run a filesystem scan to populate folder
  paths.
- **Views:** BirthdayView at `/` (date ranges incl. "All", source filter, play count column,
  count label, format icons, Volumio "Play Now" and "Add to Queue" buttons, conditional Sync
  Plays button, multi-sort, full-height grid), CollectionView at `/collection` (format filter,
  scan buttons, search, multi-sort, split Birthday/Year columns, total album count),
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
| `stolat.volumio.music-library-uri` | `music-library` | Volumio browse URI prefix for the music library (e.g. `music-library/NAS`) |
| `stolat.lastfm.api-key` | (none, opt-in) | Last.fm API key |
| `stolat.lastfm.username` | (none) | Last.fm username for play counts |
| `stolat.lastfm.sync-cron` | `0 0 6 * * *` | Last.fm play count sync (6am) |
| `stolat.user-agent` | `StoLat/{version} (...)` | User-Agent for APIs |

---

## Key Technical Notes

- **Dependency versions (as of 2026-11-21, commit 56c9e35 "Updated dependencies."):**
  Spring Boot 4.2.0, Vaadin 25.3.5, Spring Modulith 2.2.0, JAudioTagger 2.0.41,
  Karibu Testing 2.7.3, Testcontainers 2.0.5, Awaitility 4.3.0. `tsconfig.json`
  and `vite.config.ts`'s `vite.generated` import are auto-regenerated by the
  vaadin-maven-plugin whenever the Vaadin version changes — commit them alongside
  `pom.xml` version bumps, no manual edits needed. The full suite has NOT been
  re-run by Claude since this bump (user did the update) — run
  `mvn test -Dsurefire.useFile=false` first thing in a new session.
- V2 migration: all tables (artists, albums, tracks, album_birthdays, album_formats)
- V3 migration: indexes on musicbrainz_id, discogs_id columns
- V4 migration: album_birthdays evolution — nullable musicbrainz_id, album_id column,
  release_date_source column (backfilled as MUSICBRAINZ), partial unique indexes
- V5 migration: add discogs_id column to album_birthdays (with partial index)
- V6 migration: add play_count and play_count_updated_at columns to album_birthdays
- V7 migration: add folder_path column to albums (populated during filesystem scan)
- V8 migration: add bandcamp_url column to albums (populated from COMMENT audio tag during scan)
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

## Last.fm API Pro requirement (deadline 2026-12-01) — DECISION PENDING

Announced 2026-11-18 on the official thread
(https://support.last.fm/t/api-keys-will-require-pro-from-1st-december/121030):
from 1 Dec 2026, **holding a Last.fm API key requires a Pro subscription** on the
key-owning account. Existing keys are not grandfathered; keys without Pro are
rejected until the owner subscribes. End users of someone else's app don't need Pro.
Personal/non-commercial use = Pro; commercial/research needs a licence
(partners@last.fm). Only page 1 of the thread was read; page 2 and the related thread
"Free API tier for self-hosted scrobblers" (…/t/free-api-tier-for-self-hosted-scrobblers/121044)
may contain exemptions — worth checking before paying.

**Impact on StoLat** (`stolat.lastfm.api-key`, used by `LastFmClient.album.getinfo`):
- Fails gracefully. `LastFmClient.fetchPlayCount` catches all errors → `OptionalInt.empty()`
  (one warn log per album); `BirthdayService.syncPlayCounts()` skips empties, so stored
  counts stay stale (not reset to 0). Email digest reads only stored `playCount` (never
  calls Last.fm) and hides the "— N plays" text when null, so **the email is still sent**.
  Not tested against a real rejected key — conclusion is from reading the code.
- Daily 6am sync would log a warning per album and run slowly (200ms delay each).

**Options:** (1) subscribe to Pro on the key-owning account (no code change);
(2) remove `stolat.lastfm.api-key` + `stolat.lastfm.username` (feature is opt-in via
`@ConditionalOnProperty`; scheduler/button disappear, last-synced counts stay visible);
(3) alternative source (Libre.fm = same Audioscrobbler API but little data; ListenBrainz
unverified for per-album play counts) — would be a new feature. Optional hardening:
short-circuit the sync after the first auth error instead of warning per album.

**Open doubt — third-party scrobblers (e.g. Jeselnik/rb-scrobbler, Rockbox):** user's own
Last.fm session is unaffected, but the scrobbler's *developer* key is compiled in. README
says source builds need your own key (→ needs Pro on your account); unknown whether the
release binaries ship the author's key (then it depends on the author keeping Pro). Repo
doesn't mention the change. Check: did the user build from source or use a release
binary? Test a scrobble after 1 Dec; consider asking in the repo's issues.

**Status:** no decision made yet, no code changed. Update this section once decided.

---

## What's Next

- **Scheduled refresh of release dates across all sources.** Today lookups
  are one-shot per album. Add a recurring job (cron-based, configurable) that
  retries dates from all sources for albums that might benefit: newly-tagged
  albums gaining an MBID, MB_PENDING entries, Discogs year-only entries
  (already partly covered by `upgradeDiscogsYearOnlyBirthdays`), and albums
  where source websites may have gained data since the last lookup. Consider
  per-source policy (cadence, max-age, retry budget) and how to surface
  results without spamming notifications.
- Additional release date sources (Spotify)
- Notification view (settings, history, manual send, multiple recipient emails)
- Album detail view with tracks
- DB-backed authentication (replace in-memory user)
- Discovery module (public-facing)
