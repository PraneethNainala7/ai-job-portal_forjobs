# AI Job Portal

Next.js frontend and Spring Boot API from [FRONTEND.md](FRONTEND.md) and [BACKEND.md](BACKEND.md).

The database starts empty. Candidates and employers register through the UI. Admin accounts are not self-registered.

## Run locally

```bash
docker compose up -d postgres
cd backend
mvnw.cmd spring-boot:run
```

In another terminal:

```bash
npm install
npm run dev
```

Open http://localhost:3000. Optional first admin (created only if none exists):

```bash
set ADMIN_EMAIL=you@example.com
set ADMIN_PASSWORD=your-password
set ADMIN_NAME=Your Name
```

Set these before starting Spring Boot. `API_ORIGIN` defaults to `http://localhost:8080`.

Optional Claude for resume analysis and ranked recommendations (set before starting Spring Boot):

```bash
set ANTHROPIC_API_KEY=your-anthropic-key
```

Without the key, analysis and recommendations stay on the local heuristic.

## AI

Resume analysis and `POST /api/ai/jobs/recommend` use Claude when `ANTHROPIC_API_KEY` is set. Job-search match badges, per-job match, and interview questions stay heuristic decision support.

```bash
npm run lint
npm run build
```
