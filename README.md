# SmartHome

A smart-home management system: rooms, controllable devices (AC, light
bulbs, curtains), ambient sensors, and a local AI agent that reads your
stated preferences and controls devices on your behalf.

## Quick start (Docker — recommended)

**Requirements:** Docker Desktop, running with the WSL2 backend if you're
on Windows. An NVIDIA GPU is optional but strongly recommended for the AI
agent — without one, the local model runs on CPU and is noticeably slow.

```bash
git clone <this-repo-url>
cd SmartHome/back-end
docker compose up --build
```

First run takes a few minutes (downloading base images, dependencies).
Once everything's healthy:

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Mock sensor service | http://localhost:8081 |

**One extra step to use the AI agent** — pull a model into the running
Ollama container:
```bash
docker exec -it smarthome-ollama ollama pull qwen2.5:3b-instruct
```
(swap the model name if you want a larger one and have the hardware for
it — see [AI agent](#ai-agent) below)

## Log in

The database seeds three accounts on first run:

| Email | Password | Role |
|---|---|---|
| alice@example.com | alice123 | ADMIN |
| bob@example.com | bob123 | USER |
| carol@example.com | carol123 | USER |

## What this is

Users manage rooms and the devices in them. Each room also has ambient
sensors (temperature, light, occupancy) that drift on their own over
time. Anyone can write a free-text preference for a room they're
assigned to ("keep this room cool in the afternoons") — the AI agent
periodically reads those preferences against the room's current sensor
readings and decides whether to act, logging every decision it makes.

## Architecture

- **`smart-home-manager`** — main backend: auth, rooms, users, devices,
  preferences, the AI agent. Spring Boot + PostgreSQL.
- **`smart-home-mock`** — simulates the sensor hardware; generates and
  drifts readings on a timer. Spring Boot + in-memory H2, no persistence
  by design.
- **`front-end`** — Vue 3 + TypeScript dashboard.
- **Ollama** (Docker) — runs the local LLM the agent talks to.

The manager never stores a sensor reading itself — every read is a live
call to the mock service.

## Features

- Rooms, users, and devices — full CRUD, admin-gated where it matters
- Real JWT authentication, role-based access (`ADMIN` / `USER`)
- Free-text preferences, scoped to one room or all of a user's rooms
- **AI agent**: runs on a schedule (and on demand via
  `POST /api/agent/run`), evaluates every enabled preference against its
  room's live state, validates anything it proposes before applying it,
  and logs a full audit trail of every decision — see
  `GET /api/agent/decisions`

## AI agent

Model is a config value, not hardcoded — set `OLLAMA_MODEL` to swap it:

```bash
OLLAMA_MODEL=qwen2.5:14b-instruct docker compose up
```

A 3B model is fast and fine for a quick demo on modest hardware, but has
shown real reliability limits on this task (misreading device state,
reasoning about devices that aren't in the room) — a 14B model resolves
both, if you have the VRAM for it (~9GB).

## Running without Docker

Each service can run on its own the normal way, if you'd rather iterate
locally:

```bash
cd back-end/smart-home-mock && ./mvnw spring-boot:run     # start first
cd back-end/smart-home-manager && ./mvnw spring-boot:run  # needs Postgres — docker compose up postgres
cd front-end && npm install && npm run dev
```

## Tech stack

Java 21 · Spring Boot 4 · Spring Security (JWT) · Spring Data JPA ·
PostgreSQL · Spring AI (Ollama) · Vue 3 · TypeScript · Pinia · Vite ·
Docker Compose

## Known limitations

- No global exception handler yet — a bad ID returns a raw `500`, not a
  clean `404`
- No first-admin bootstrap beyond the seeded accounts above — if the
  database ever starts genuinely empty without seeding, there's no way
  to create the first admin through the API
- If two preferences target the same device with opposite intent, the
  agent applies both in sequence — last one evaluated wins, no
  reconciliation
- The agent's "no action needed" decisions aren't independently
  verified — only proposed *actions* are validated against reality
