# HomeControl — Mock Backend (sensor data simulator)

A standalone Spring Boot service with exactly one job: simulate live
sensor readings in software. No Postgres, no Wokwi, no real device loop.

This is **not** a general-purpose backend and is not meant to be one. It
used to also carry Rooms/Users/Preferences, back when it was the
frontend's only backend to develop against. It doesn't anymore — the real
(Postgres) backend already owns all of that business data for real, so
this service was stripped down to only what's genuinely hard to have
without real hardware: a sensor's live, changing data. See the handoff
doc for the reasoning and what happens next.

## Run it

```
./mvnw spring-boot:run
```

Starts on `http://localhost:8081` (the real backend runs on `8080` — kept
apart on purpose so both can run at once). Uses an in-memory H2 database
(data resets every restart — that's expected for a mock). H2 console (if
needed) is at `http://localhost:8081/h2-console`, JDBC URL
`jdbc:h2:mem:homemanager`, user `sa`, empty password.

No Docker required.

## API

- `POST/GET/PUT/DELETE /api/sensors`, `/api/sensors/{id}` — one unified
  resource for all three sensor types, with `type` in the payload
  (`CURTAINS` / `LIGHT_BULB` / `AC`).
- `GET /api/sensors/{id}/readings?limit=20` — history for the sparkline,
  newest first.

That's the whole API. No Rooms, Users, or Preferences here — see the
handoff doc.

There is **no command queue**. `PUT /api/sensors/{id}` updates state
directly and synchronously (the whole payload, same overwrite-everything
convention as before) — no PENDING/EXECUTED/FAILED lifecycle to poll.

## Simulated "liveness"

A `@Scheduled` job ticks every ~1s and nudges each **on** sensor's data
(curtains' `roomLightLux`, bulb `brightness`, AC `targetTemp`) with small
random drift, then appends a snapshot to that sensor's reading history
(capped at the last 30 readings per sensor). Off sensors don't drift.

## What this intentionally is NOT

No real device integration, no auth, no business data (rooms/users/
preferences — that's the real backend's job). This service exists purely
to be called for sensor readings, either directly by a frontend during
isolated dev/testing, or by the real backend once it's wired up to pull
readings from here.
