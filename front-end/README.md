# HomeControl — Frontend

Vue 3 + TypeScript + Tailwind frontend for the HomeControl smart-home app.
This sprint covers: visual design, CRUD for 3 sensor types (curtains, light
bulb, AC) with a real command-queue-based manual override and reading
history, a user system with roles and self-service signup, a preferences
(automation rules) screen, and a mock backend built to be swapped for the
real Spring Boot API with minimal changes.

## Stack

- **Vue 3** (`<script setup>`, Composition API) + **TypeScript**
- **Tailwind CSS v4** (CSS-first config — see `src/style.css`, no `tailwind.config.js`)
- **Pinia** for state
- **Vue Router** for routing + auth/role guards
- **Axios** for HTTP
- **MSW (Mock Service Worker)** for the mock backend — intercepts real HTTP
  calls at the network level, so the app talks to a real-looking REST API
  the whole time, mock or not.

## Getting started

```bash
npm install
npm run dev
```

Open the printed local URL. You'll land on `/login`. Demo accounts (mock-only,
see below) are shown as quick-fill buttons on the login screen, or create a
new classic-user account from `/signup`:

| Role          | Email                       | Password       |
|---------------|------------------------------|----------------|
| Admin         | admin@homecontrol.io         | admin123       |
| Maintainer    | maintainer@homecontrol.io    | maintainer123  |
| Classic user  | user@homecontrol.io          | user123        |

Self-service signup (`/signup`) always creates a `classic_user` account —
admin/maintainer accounts are provisioned by an admin from the Users screen
instead. Signed-up accounts are added to the mock's credentials table (see
`src/mocks/db.ts`) and persisted the same way as everything else, so you can
log back in after a refresh.

## Project structure

```
src/
  types/            Sensor, User, Command, PreferenceRule domain types
  mocks/            MSW handlers + seed data + tiny localStorage-backed "db"
  services/         api.ts (axios instance) + sensorService/userService/
                     authService/preferenceService
  stores/           Pinia stores (auth, sensors, users, preferences)
  router/           Routes + auth/role guards
  utils/
    permissions.ts     single source of truth for role checks
    sensorVisuals.ts   icon/color mapping per sensor type
    commandSummary.ts  human-readable text for a command's payload
  layouts/          DefaultLayout.vue (sidebar + content frame)
  views/            LoginView, SignupView, SensorsView, PreferencesView, UsersView
  components/
    common/         BaseButton, BaseModal, BaseInput, ToggleSwitch, etc.
    sensors/        SensorCard, SensorFormModal, SensorSparkline, CommandLog,
                     and per-type field forms
    preferences/    PreferenceTable, PreferenceFormModal
    users/          UserTable, UserFormModal, RoleBadge
    icons/          Small custom SVG icon components
```

## How the mock <-> real backend swap works

Nothing in `views/`, `components/`, or `stores/` talks to `fetch`/`axios`
directly, and nothing in them knows mocks exist. The chain is:

```
component -> Pinia store -> service (sensorService/userService/authService/
                             preferenceService) -> axios (api.ts)
```

`src/services/api.ts` is the only place the backend URL is configured, via
env vars. Two settings control everything:

- `VITE_API_BASE_URL` — the API's base URL (default `/api`)
- `VITE_USE_MOCKS` — `"true"` starts the MSW mock worker before the app
  mounts (see `main.ts`); `"false"` skips it entirely, so no mock code is
  even loaded

**When the real backend is ready:** copy `.env.local.example` to `.env.local`
(already gitignored) and point it at your teammate's server:

```
VITE_USE_MOCKS=false
VITE_API_BASE_URL=http://localhost:8080/api
```

That's the whole swap — assuming the backend exposes matching paths and
fields. It's worth a quick sync with your backend teammate on the contract
below so that step really is a one-line change.

### Assumed API contract (mock mirrors this — confirm against the real thing)

```
GET    /api/sensors
POST   /api/sensors                  body: SensorInput -> 201 Sensor
PUT    /api/sensors/:id              body: Partial<SensorInput> -> 200 Sensor
                                      (frontend only ever sends { name } here —
                                       see "Manual override" below for state changes)
DELETE /api/sensors/:id              -> 204

GET    /api/sensors/:id/readings?limit=20   -> SensorReading[] (history)

GET    /api/commands?limit=20               -> Command[] (all sensors, newest first)
GET    /api/sensors/:id/commands            -> Command[] (one sensor, newest first)
POST   /api/sensors/:id/commands     body: CommandPayload -> 202 Command (status: PENDING)

GET    /api/preferences?userId=...
POST   /api/preferences              body: PreferenceRuleInput -> 201 PreferenceRule
PUT    /api/preferences/:id          body: Partial<PreferenceRuleInput> -> 200 PreferenceRule
DELETE /api/preferences/:id          -> 204

GET    /api/users
POST   /api/users                    body: UserInput -> 201 User (409 if email taken)
PUT    /api/users/:id                body: Partial<UserInput> -> 200 User
DELETE /api/users/:id                -> 204

POST   /api/auth/login               body: { email, password } -> { token, user }
POST   /api/auth/signup              body: { name, email, phoneNumber, password }
                                      -> 201 { token, user } (always role: classic_user)
```

Domain shapes live in `src/types/`. Sensor `data` is discriminated by `type`:

- `CURTAINS` -> `{ isOpen: boolean, openPercent: number }`
- `LIGHT_BULB` -> `{ isOn: boolean, brightness: number }`
- `AC` -> `{ mode: 'OFF' | 'HEAT' | 'COOL', targetTemp: number }`

Requests carry `Authorization: Bearer <token>` once logged in (see the axios
interceptor in `api.ts`) — the real backend's JWT should slot in here as-is.

## Manual override = a real command queue, not a direct write

This isn't just a UI label — it's modeled the way the architecture diagrams
show it: a Manual Override doesn't edit a sensor's state directly. It posts
a `Command` (`POST /sensors/:id/commands`) that starts `PENDING`, exactly
like the diagrams' `Command`/`CommandStatus` entities. Two things change as
a result of a command, not immediately from the request itself:

1. The command's own status eventually flips to `EXECUTED` or `FAILED`
   (about 10% fail, on purpose — see below).
2. Only on `EXECUTED` does the sensor's actual `status`/`data` change, and a
   new `SensorReading` gets appended to its history.

Both of these are visible in the UI: a sensor card shows "Sending to
device…" while its command is `PENDING` (see `pendingSensorIds` in
`stores/sensors.ts`), and the **Recent commands** panel below the grid
(`CommandLog.vue`) shows the live queue with status badges.

**Why it resolves on a timer instead of a real device polling for it:** the
diagrams model the ESP32 itself polling `GET /devices/{id}/commands`,
executing, then acking. Building an actual simulated device *consumer* is a
separate system (the "Simulated IoT Devices" container in the diagrams) and
is out of scope for this frontend sprint. So `POST /sensors/:id/commands`
schedules a `setTimeout` (1.2–2.4s) that plays the device's role internally
— same end state, same lifecycle, just without a second process actually
polling for it. When the real device-side polling loop exists, only this
one handler in `mocks/handlers.ts` needs replacing; `issueCommand()`,
`pendingSensorIds`, and the UI don't change.

Editing a sensor's **name** is still a plain `PUT` (metadata, not
actuation). `SensorFormModal` submits one payload; `SensorsView.handleSubmit`
is what splits it — a name change goes through `store.update`, and any
status/data change goes through `store.issueCommand`.

## Reading history

`GET /sensors/:id/readings` returns a `SensorReading[]` (mirrors the
diagrams' `DeviceReading`: `sensorId`, `timestamp`, `status`, `data`). Each
sensor card fetches and polls its own last ~12 readings and draws a small
sparkline from whichever numeric field applies (`openPercent` / `brightness`
/ `targetTemp`). Readings are appended by the mock only when a command
actually executes — so the trend line reflects real (simulated) state
changes, not a random walk. Seed sensors start with six synthetic points so
the sparkline isn't empty on first load.

## Preferences (System Configuration)

A straightforward CRUD screen over `PreferenceRule` (`ruleId`/`deviceId`/
`condition`/`action`/`strict`/`enabled` in the diagrams), scoped per user —
each person manages their own automation rules, the same way
`PreferencesController.getRules(userId)` is user-scoped in the diagrams.
No role gating: this is a personal/household feature, not an admin one
(unlike Users management).

Deliberately **not** built: anything that reads or reasons over these rules.
`condition` and `action` are free text the person types — matching the
diagram's untyped `String` fields — because parsing/acting on them is the
AI agent's job, and that's a later sprint by design. This screen only
manages the rules that agent will eventually consume.

## Roles this sprint

`admin` and `maintainer` currently have identical permissions (full access,
including user management). `classic_user` can do everything except manage
other users. This all routes through `src/utils/permissions.ts` — that's the
only file that needs to change if/when `maintainer` gets its own distinct
capabilities.

Sensor CRUD isn't scoped per-user (the agreed schema has no owner field this
sprint) — every authenticated role can see and manage every sensor.
Preferences, by contrast, *are* scoped per user (see above).

## Real-time updates (polling now, swappable for push later)

`SensorsView` starts a 5-second poll on mount (`stores/sensors.ts`:
`startPolling` / `stopPolling`), refreshing both the sensor list and the
command list (so `PENDING -> EXECUTED/FAILED` transitions surface without a
manual refresh). It skips the loading flag on background ticks so the grid
doesn't flash a skeleton, and pauses automatically when the tab isn't
visible (`document.visibilityState`).

This already works against the mock and will work unchanged against the
real REST API — it's just "fetch the list every N seconds." If the backend
later exposes push (WebSocket/SSE) instead of polling, only the *inside* of
`startPolling`/`stopPolling`/`refreshSilently` needs to change to a socket
subscription — the store's public surface (`items`, `commands`, `fetchAll`)
stays the same, so `SensorsView` and everything else is untouched.

One thing this is *not*: the `StatusSnapshot` concept from the architecture
diagrams (readings + active preferences bundled together for the AI agent's
decision cycle). That's specific to the AI/decision sprint that's
deliberately out of scope right now — this polling is purely to keep the
monitoring UI fresh.

## Known gaps / next steps

- **Polling, not push** — see above; fine for a handful of simulated ESP32s,
  worth revisiting if the fleet grows or the backend adds WebSocket/SSE.
- **No simulated device consumer** — the mock plays the ESP32's role
  internally on a timer (see "Manual override" above) rather than exposing
  a separate process that actually polls `GET /sensors/:id/commands`. Fine
  for this frontend sprint; worth building for real once actual (or
  simulated) devices exist.
- **No password reset** flow.
- **Sidebar stacks rather than collapsing** on narrow screens — usable, not
  a polished mobile nav pattern (no hamburger/drawer yet).
- **`unit` is derived, not editable** — it's set automatically per sensor
  type (curtains/bulb → `%`, AC → `°C`) rather than a free-text field, since
  it's intrinsic to the device. Flagging since the original schema listed it
  as its own field — happy to make it editable if that's actually wanted.
- **No automated tests** yet.
- **Mock tokens never expire**, and there's no global 401 handler yet — a
  real backend returning 401 on an expired/invalid token won't currently
  bounce the user back to `/login` automatically. Worth adding before real
  auth lands.
- **No toast/notification system** — create/update/delete feedback is
  inline-only (e.g. sensor/user form errors); there's no global success/error
  notification for actions.

## Mock data persistence

Mock data lives in `localStorage` (see `src/mocks/db.ts`) so a page refresh
doesn't wipe out what you were testing. To reset everything to the seed
data, clear site data in devtools or run this in the browser console:

```js
localStorage.removeItem('homecontrol_mock_sensors')
localStorage.removeItem('homecontrol_mock_users')
localStorage.removeItem('homecontrol_mock_credentials')
localStorage.removeItem('homecontrol_mock_commands')
localStorage.removeItem('homecontrol_mock_readings')
localStorage.removeItem('homecontrol_mock_preferences')
location.reload()
```
