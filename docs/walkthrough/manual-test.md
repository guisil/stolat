# Manual Walkthrough / Test

Manual test plan for StoLat. Covers user-facing behavior across all modules.

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
- **Mailpit:** `http://localhost:8025`

---

## 2. Login & Application Startup

- [ ] Application starts without errors
- [ ] Navigating to `http://localhost:8080` redirects to login page
- [ ] Log in with `user` / `stolat`
- [ ] Redirected to Birthdays view (default route)
- [ ] Main layout renders with "StoLat" title in navbar
- [ ] Side navigation has "Birthdays" and "Collection" links
- [ ] Console shows "Seeding dev data..." log message
- [ ] Console shows "Sending birthday notification on startup" log message

---

## 3. Birthday View (`/`)

- [ ] Page shows "Album Birthdays — Today" heading
- [ ] Grid displays with columns: Artist, Album, Birthday, Year, Format
- [ ] "Today's Birthday Album" by "Test Artist" appears in the grid
- [ ] Format column shows "Digital" badges for digital albums
- [ ] "Homogenic" by Bjork shows "Digital, Vinyl"
- [ ] Date range selector works: Today, Last 7 days, Next 7 days, This week,
  Last 30 days, Next 30 days, This month
- [ ] Search field filters by artist or album name
- [ ] All columns are sortable

---

## 4. Collection View (`/collection`)

- [ ] Click "Collection" in side navigation
- [ ] Page shows "Collection" heading
- [ ] Grid displays with columns: Artist, Album, Release Date, Format
- [ ] Format filter dropdown: All / Digital / Vinyl
- [ ] All seeded albums visible when "All" selected
- [ ] "Digital" filter shows only digital albums
- [ ] "Vinyl" filter shows only vinyl albums (Pink Floyd, Bjork)
- [ ] Search field filters by artist or album name
- [ ] All columns are sortable
- [ ] "Scan Collection" button is visible

### Scan a local music folder

> **Setup:** Place a few MusicBrainz-tagged FLAC files in `~/Music/stolat-test/`
> (or adjust `stolat.collection.music-directory` in `application-dev.properties`).

- [ ] Click "Scan Collection" button
- [ ] Notification toast: "Scanning collection..."
- [ ] Albums appear in the grid progressively
- [ ] Release dates fill in as MusicBrainz lookups complete (background)
- [ ] Console shows log entries for each release date lookup
- [ ] Re-scanning does not duplicate albums
- [ ] All scanned albums show "Digital" in Format column

### Scan Discogs collection (optional)

> **Setup:** Add your Discogs credentials to `application-dev.properties`:
> ```properties
> stolat.discogs.username=your-username
> stolat.discogs.token=your-token
> ```

- [ ] "Scan Discogs" button appears when username is configured
- [ ] Click "Scan Discogs" button
- [ ] Notification toast: "Scanning Discogs collection..."
- [ ] Vinyl albums appear in the grid
- [ ] Albums that exist in both collections show "Digital, Vinyl"
- [ ] New vinyl-only albums show "Vinyl"
- [ ] Console shows MusicBrainz search matches for unmatched albums

---

## 5. Email Notification (Mailpit)

### Startup notification

- [ ] Open Mailpit at `http://localhost:8025`
- [ ] An email with subject "Album Birthdays — {today's date}" is present
- [ ] Email body is HTML with a table listing today's birthday(s)
- [ ] "Today's Birthday Album" by "Test Artist" appears with anniversary years

---

## 6. Format Tracking & Reconciliation

- [ ] Remove a FLAC folder and re-scan — album loses "Digital" format
- [ ] Remove a release from Discogs and re-scan — album loses "Vinyl" format
- [ ] Albums with no remaining formats disappear from "All" view
- [ ] Birthday data is preserved (still shows in Birthday view)

---

## 7. Duplicate Handling

- [ ] Click "Scan Collection" twice — no duplicate albums
- [ ] Click "Scan Discogs" twice — no duplicate albums
- [ ] Albums that match by artist+title across sources get both format badges

---

## 8. Cleanup

```bash
docker compose down          # Stop PostgreSQL + Mailpit
docker compose down -v       # Stop and remove data volumes (fresh start)
```
