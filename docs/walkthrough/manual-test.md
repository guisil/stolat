# Manual Walkthrough / Test

Manual test plan for StoLat. Covers user-facing behavior across all modules.
Organized by workflow area with checkboxes for progress tracking.

**Date:** 2026-03-21

---

## 1. Prerequisites

### Start infrastructure

```bash
docker compose up -d       # Start PostgreSQL + Mailpit
```

Verify services are running:
- PostgreSQL: `localhost:5432`
- Mailpit Web UI: `http://localhost:8025`

### Start the application (dev profile)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Access the application

- **App:** `http://localhost:8080`
- **Mailpit:** `http://localhost:8025` (captured emails)

---

## 2. Application Startup

- [ ] Application starts without errors
- [ ] Main layout renders with "StoLat" title in navbar
- [ ] Side navigation has "Birthdays" and "Collection" links
- [ ] Console shows "Seeding dev data..." log message
- [ ] Console shows "Sending birthday notification on startup" log message

---

## 3. Birthday View (`/birthdays`)

- [ ] Click "Birthdays" in side navigation
- [ ] Page shows "Album Birthdays — {today's date}" heading
- [ ] Grid displays with columns: Artist, Album, Release Date
- [ ] "Today's Birthday Album" by "Test Artist" appears in the grid
  (seeded by DevDataInitializer with today's month/day)

---

## 4. Collection View (`/collection`)

- [ ] Click "Collection" in side navigation
- [ ] Page shows "Collection" heading
- [ ] Grid displays with columns: Artist, Album
- [ ] All 9 seeded albums are visible (Radiohead, Portishead, Massive Attack,
  Bjork, Boards of Canada, Aphex Twin, Test Artist)
- [ ] "Scan Collection" button is visible

### Scan a local music folder

> **Setup:** Place a few MusicBrainz-tagged FLAC files in `~/Music/stolat-test/`
> (or adjust `stolat.collection.music-directory` in `application-dev.properties`).
> Files should be organized in `Artist/Album/Track.flac` structure.

- [ ] Click "Scan Collection" button
- [ ] Notification toast appears: "Scan complete: N albums imported"
- [ ] Newly scanned albums appear in the grid
- [ ] Clicking "Scan Collection" again does not duplicate albums

---

## 5. Email Notification (Mailpit)

### Startup notification

- [ ] Open Mailpit at `http://localhost:8025`
- [ ] An email with subject "Album Birthdays — {today's date}" is present
- [ ] Email body is HTML with a table listing today's birthday(s)
- [ ] "Today's Birthday Album" by "Test Artist" appears with anniversary years

### Verify no email when no birthdays

> To test this, temporarily change the release date of the "Today's Birthday Album"
> in `DevDataInitializer` to a different month, rebuild, and restart.

- [ ] No email is sent when there are no birthdays for today

---

## 6. MusicBrainz Integration

> This test requires actual MusicBrainz-tagged audio files (tagged with Picard).

- [ ] Scan a folder with properly tagged FLAC files
- [ ] Albums appear in the collection grid
- [ ] After a short delay (rate-limited: 1 request per 1.1 seconds per album),
  birthdays for the scanned albums appear in the birthday view
- [ ] Release dates match the expected first-release-date from MusicBrainz

---

## 7. Duplicate Handling

- [ ] Click "Scan Collection" twice — no duplicate albums in the grid
- [ ] Check the birthday view — no duplicate birthday entries
- [ ] Import the same album via scan after a restart — data is not duplicated

---

## 8. Cleanup

```bash
docker compose down          # Stop PostgreSQL + Mailpit
docker compose down -v       # Stop and remove data volumes (fresh start)
```
