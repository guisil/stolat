# CLAUDE.md — StoLat Project Development Guide

## Project Overview

**StoLat** (Album Birthdays) is a Spring Boot 4 web application for tracking and celebrating
album birthdays — the anniversaries of album release dates from a personal music collection.
Built with Vaadin 25 (Java Flow, server-side), PostgreSQL (Flyway-managed), and Spring Modulith
for modular architecture. The system scans local audio files, looks up release dates via the
MusicBrainz API, and sends daily email notifications.

> **Status:** This project is being redesigned and modernized from a legacy multi-app architecture
> (Spring Boot 2.6.4, 5 Maven modules, local MusicBrainz database) into a single Modulith
> application. The old code is in the repository for reference but will be replaced.

---

## Tech Stack (Target)

- **Java 25**, Spring Boot 4, Spring Modulith 2.0.x, Jakarta Bean Validation
- **Vaadin 25** (Java Flow — server-side, NOT React/Hilla)
- **PostgreSQL**, Flyway (managed by Boot)
- **Testcontainers**, Karibu Testing, Mockito
- **MusicBrainz API** (primary release date source, replaces local MusicBrainz database)
- **JAudioTagger** (audio file metadata reading)
- **Spring Mail** + **Thymeleaf** (email notifications)
- **JUnit 5**, AssertJ

---

## Architecture — Spring Modulith

### Package Layout

```
app.stolat                            ← @SpringBootApplication (root module)
├── StoLatApplication.java
└── MainLayout.java                   ← Vaadin AppLayout wrapper

app.stolat.collection                 ← Collection module: filesystem scanning, tag reading
├── Album.java                        ← Entity / aggregate root
├── Artist.java                       ← Entity
├── Track.java                        ← Entity
├── CollectionService.java            ← Application service (public API)
├── AlbumDiscoveredEvent.java         ← Spring application event (triggers MusicBrainz lookup)
├── AlbumReleaseDateResolvedEvent.java ← Event for non-MusicBrainz release dates (e.g., Discogs year)
└── internal/
    ├── FileScanner.java              ← Filesystem crawler
    ├── TagReader.java                ← Audio file tag reading (JAudioTagger)
    ├── repositories...
    └── views...

app.stolat.birthday                   ← Birthday module: release date lookup, caching, queries
├── AlbumBirthday.java                ← Entity (supports nullable musicBrainzId, tracks source)
├── BirthdayService.java              ← Application service (public API)
├── ReleaseDateLookup.java            ← Interface for release date sources
├── ReleaseDateSource.java            ← Enum: MUSICBRAINZ, DISCOGS, BANDCAMP, MANUAL
└── internal/
    ├── MusicBrainzLookup.java        ← Primary: MusicBrainz API (first-release-date)
    ├── BandcampLookup.java           ← User-initiated: JSON-LD datePublished from Bandcamp pages
    ├── AlbumDiscoveredListener.java  ← Listens for new albums, triggers MusicBrainz lookup
    ├── AlbumReleaseDateResolvedListener.java ← Listens for resolved dates (e.g., Discogs year)
    ├── LastFmConfig.java             ← Conditional config for Last.fm API (@ConditionalOnProperty)
    ├── LastFmClient.java             ← Last.fm API client (album.getinfo, user play counts)
    ├── LastFmSyncService.java        ← Syncs play counts for all albums with birthdays
    ├── BirthdayView.java             ← Birthday grid at `/` (includes play count column)
    ├── MissingBirthdaysView.java     ← Missing birthdays at `/missing-birthdays` (Bandcamp dialog)
    ├── repositories...
    └── ...

app.stolat.notification               ← Notification module: email digests
├── NotificationService.java          ← Application service (public API)
└── internal/
    ├── EmailSender.java
    ├── NotificationScheduler.java    ← Scheduled daily email task
    └── views...
```

### Module Rules

- Each direct sub-package of `app.stolat` is an **application module**.
- Module root package = **public API**. Other modules can reference these classes.
- `internal/` sub-package = **module-private**. No outside access.
- Inter-module communication = **Spring application events**, not direct calls to internals.
- Verify with `ApplicationModules.of(StoLatApplication.class).verify()`.

### Module Dependencies

```
collection ──(AlbumDiscoveredEvent)──────────► birthday
collection ──(AlbumReleaseDateResolvedEvent)─► birthday
birthday   ──(birthday data)─────────────────► notification
```

### Data Flow

1. **Collection module** scans local audio files, reads MusicBrainz tags (JAudioTagger)
2. New/updated albums → publishes `AlbumDiscoveredEvent`
3. **Birthday module** listens, looks up `first-release-date` via MusicBrainz API
4. Release dates cached in DB (one-time per album, incremental)
5. **Birthday views** query cached data by date range
6. **Notification module** sends daily email digests of today's birthdays

### Release Date Lookup Strategy

- **Primary:** MusicBrainz API — `GET /ws/2/release-group/{mbid}` returns `first-release-date`
- **Fallback (untagged files):** MusicBrainz search — `releasegroup:X AND artist:Y`
- **Future:** Spotify, Discogs as additional sources behind `ReleaseDateLookup` interface

---

## Module Map

| Module | Status | Description |
|--------|--------|-------------|
| `collection` | Planned | Filesystem scanning, audio tag reading, album/artist/track management |
| `birthday` | Planned | Release date lookup (MusicBrainz API), caching, date range queries |
| `notification` | Planned | Daily email digests, recipient management |
| `discovery` | Planned (later) | Public-facing album birthday browsing, not tied to personal collection |

---

## Workflow — TWO-TIER TDD

Before starting any code change, follow the TDD workflow below.

### Choosing the Cycle

| | Full Cycle | Fast Cycle |
|---|---|---|
| **When** | New behavior, no existing test covers it | Existing tests already cover the change |
| **Examples** | New features, bug fixes, new entities/services | Renames, config changes, formatting |
| **Responses** | 3 separate responses with confirmation gates | Single response |
| **Decision rule** | Can you point to an existing test that would catch a regression? **No** → full cycle | **Yes** → fast cycle |

When uncertain, default to **full cycle**.

### Full Cycle (3 responses)

**Step 1: RED** — Write one failing test
- Decide which test type fits (see Testing Conventions below).
- Write ONE test method. No production code.
- Run: `mvn test -Dtest=ClassName#methodName -Dsurefire.useFile=false`
- **STOP. Wait for confirmation before Step 2.**

**Step 2: GREEN** — Minimum code to pass
- Write the LEAST production code that makes the test pass.
- If a Flyway migration is needed, create it now.
- Run: `mvn test -Dtest=ClassName -Dsurefire.useFile=false`
- **STOP. Wait for confirmation before Step 3.**

**Step 3: REFACTOR**
- Review both test and production code.
- Run: `mvn test -Dsurefire.useFile=false` (full suite)
- Suggest a commit message. State what to test next.
- **STOP. Wait for confirmation before next cycle.**

### Fast Cycle (1 response)

1. State which existing test(s) cover the change.
2. Make the change.
3. Run: `mvn test -Dsurefire.useFile=false` (full suite)
4. If any test breaks, stop and escalate to full cycle.
5. Suggest a commit message.

### Rules

- NEVER create production code in Step 1 (full cycle). The test must fail first.
- NEVER write multiple tests before making them pass. One test per cycle.
- NEVER skip Step 3 (full cycle). Always review, always run the full suite.
- NEVER use fast cycle for genuinely new behavior. When in doubt, full cycle.
- If a step produces unexpected results, investigate before moving on.

---

## Testing Conventions

### Test Types

| Test Type | Annotation / Tool | When |
|---|---|---|
| Unit test | `@ExtendWith(MockitoExtension.class)` | Domain logic, no Spring context |
| Repository test | `@SpringBootTest` + `@Transactional` | Persistence, schema correctness |
| Module integration test | `@ApplicationModuleTest` | One module with Spring context + DB |
| Vaadin UI test | `@SpringBootTest` + Karibu | View rendering, form actions |
| Modulith structure test | `ApplicationModules.verify()` | Module boundary validation |
| Async event test | `Scenario` or `Awaitility` | Event publication & cross-module handling |

### Test Naming

`should{Behavior}When{Condition}` — e.g., `shouldLookUpReleaseDateWhenAlbumDiscovered()`

### Testcontainers Setup

- `@TestConfiguration(proxyBeanMethods = false)` with `@ServiceConnection`
- PostgreSQL container, shared across test classes
- Import via `@Import(TestcontainersConfiguration.class)` on integration tests

### Mocking Strategy

- **Unit tests:** `@Mock` + `@InjectMocks`, BDDMockito (`given(...).willReturn(...)`)
- **Integration tests:** Real beans, real DB (Testcontainers), no mocks
- **UI tests:** Real Spring context + real DB + MockVaadin (no browser)

---

## Sequencing for Multi-Layer Features

When a feature needs new UI, service, entity, and database table, work in this order.
Each item below is a **full RED-GREEN-REFACTOR cycle** (multiple responses).

1. **Unit test** for domain logic (service behavior with mocks).
2. **Repository test** for persistence (drives entity + Flyway migration creation).
3. **Module integration test** for the wired-up module (verifies events if any).
4. **UI test** for the Vaadin view (Karibu).

Do not jump ahead. Complete cycle N before starting cycle N+1.

---

## Bug Fix Sequence

1. **Step 1 (RED):** Write a test that reproduces the bug — it asserts correct behavior
   and fails against current code.
2. **Step 2 (GREEN):** Fix the production code with minimum change.
3. **Step 3 (REFACTOR):** Review, run full suite, check for related edge cases.

---

## Database & Migrations

- **Location:** `src/main/resources/db/migration/V{N}__{description}.sql`
- **Naming:** `V{next}__{snake_case_description}.sql` (double underscore)
- Migrations are created in **Step 2** (GREEN), when a repository test needs a table.
- **Never edit existing migrations.** Always create new ones.

---

## Commands

```bash
# TDD workflow
mvn test -Dtest=Class#method -Dsurefire.useFile=false   # one test (Step 1/2)
mvn test -Dtest=Class -Dsurefire.useFile=false           # one class
mvn test -Dsurefire.useFile=false                         # full suite (Step 3)

# Build & verify
mvn verify                                                # compile + test + package
mvn clean test                                            # clean rebuild

# Architecture
mvn test -Dtest=ModulithStructureTest -Dsurefire.useFile=false  # module boundaries

# Development
mvn spring-boot:run                                       # start app (needs PostgreSQL)
```

---

## Code Conventions (from meads patterns)

### Entity Pattern
- JPA `@Entity` with `@Table(name = "...")`
- `UUID` primary key, self-generated in constructor via `UUID.randomUUID()`
- `@Getter` (Lombok) — no manual getters, no setters
- `Instant` for timestamps with `TIMESTAMP WITH TIME ZONE` in DB
- `@PrePersist` / `@PreUpdate` for automatic timestamps
- Domain methods on the entity for state changes — no setters

### Repository Pattern
- Interface extending `JpaRepository<Entity, UUID>`
- Package-private (in `internal/`) — never accessed outside the module

### Service Pattern
- `@Service` + `@Transactional` + `@Validated`
- Public class in module root (part of public API)
- Constructor injection only (no `@Autowired` field injection)

### View Pattern
- `@Route(value = "path", layout = MainLayout.class)`
- Role-based access via `@RolesAllowed` or `@PermitAll` + `beforeEnter()`

---

## Do NOT List

- **No production code in TDD Step 1.** The test must fail first.
- **No multiple tests before making them pass.** One per cycle.
- **No skipping Step 3.** Always refactor and run full suite.
- **No `@Autowired` field injection.** Use constructor injection only.
- **No `@Data` or `@Builder` on entities.** Use `@Getter` only. No setters.
- **No mocking the database in integration tests.** Use Testcontainers.
- **No editing existing Flyway migrations.** Always create new versioned files.
- **No cross-module repository access.** Repositories are `internal/`. Use events or services.
- **No making `internal/` classes public for test access.** Test through the module's public API.
- **No React/Hilla views.** This project uses Vaadin Java Flow exclusively.
- **No local MusicBrainz database.** Use the MusicBrainz API.

---

## Resuming Work (New Session)

1. **Read `docs/SESSION_CONTEXT.md` first** — this is the primary bootstrap file. It contains
   the current state of all modules, what's done, what's next, in-progress work details,
   and key technical notes. Everything needed to continue is here.
2. **`CLAUDE.md`** (this file) is auto-loaded and provides conventions, architecture, and
   workflow rules.

---

## Commit Hygiene — Documentation Freshness & Session Portability

Doc updates are part of Step 3 (REFACTOR) and Fast Cycle step 5 — not a post-commit task.
**Do NOT suggest a commit message until all affected docs are updated.** The goal is
**session portability**: after every commit-and-push, anyone must be able to resume work
with no loss of context by reading `docs/SESSION_CONTEXT.md` and `CLAUDE.md`.

Checklist — update each if affected:

1. **`docs/SESSION_CONTEXT.md`** — Test count, module status, what's next, in-progress work
2. **`CLAUDE.md`** — If conventions, module map, package layout, or migrations changed
3. **`docs/walkthrough/manual-test.md`** — If any UI or API changed

**Self-check before committing:** _"If I clear my context right now and start fresh,
can I resume this work by reading `docs/SESSION_CONTEXT.md`?"_ If no, update it.

---

## Common Pitfalls

- Creating production classes during Step 1. **The test must fail first.**
- Writing multiple tests before making any pass. **One per cycle.**
- Skipping Step 3. **Always refactor and run full suite.**
- Mocking the database in integration tests. Use Testcontainers.
- Editing existing Flyway migration files. **Create new versioned files.**
- Referencing another module's `internal` package. Use events.
- Using generic Spring/Vaadin patterns instead of checking what the existing modules actually do.
