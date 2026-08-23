# PingLet (Persistent Ambient Message Widget)

PingLet rotates a personal + system message on the Android home screen.

## Product framing (v1)
- Android only.
- The message is persistent on the widget surface for the selected interval.
- Content changes follow deterministic slots (30/60/120 minute cycles).
- Personal items are preferred; system catalogs fill gaps.

## Backend
- NestJS 10
- PostgreSQL + Prisma
- Redis
- REST API under `/api/v1`
- OpenAPI/Swagger at `/api/docs`
- Docker Compose included (`backend`, `postgres`, `redis`)

## Android
- Kotlin + Jetpack Compose + Glance
- Offline-first flow with local queue + Room
- WorkManager sync worker stub + scheduling scaffold
- Share target for `text/plain`

## Important folders
- `backend/src/{auth,users,devices,content,catalogs,feed,ingestion,events,admin,queue,sync}`
- `android/app/src/main/java/com/linger/app/{data,domain,ui,widget,worker,di}`

## Bootstrapping

### Backend (local)
1. `cp backend/.env.example backend/.env`
2. `docker compose up`

### Android
- Open `android/` in Android Studio and run `app`.

## Notes
- This is an MVP scaffold intended to be completed in implementation milestones.
- Future iOS support was kept in mind via platform-agnostic API and shared schema naming.
