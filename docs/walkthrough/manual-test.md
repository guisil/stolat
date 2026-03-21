# Manual Walkthrough / Test

Manual test plan for StoLat. Covers user-facing behavior across all modules.
Organized by workflow area with checkboxes for progress tracking.

**Date:** 2026-03-21
**Status:** Awaiting project scaffold — no testable features yet.

---

## 1. Prerequisites

### Start the application

```bash
docker-compose up -d          # Start PostgreSQL
mvn spring-boot:run
```

### Access the application

- **URL:** `http://localhost:8080`

---

## 2. Application Startup

- [ ] Application starts without errors
- [ ] Main layout renders with navigation

---

_Sections for collection, birthday, and notification modules will be added
as features are implemented._
