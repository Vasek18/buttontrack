# Button Track

A personal behavior-tracking web app where users can create and press custom buttons (e.g., "Shaved", "Mood: Good", "Woke up"). Each press is timestamped for tracking daily habits.

**Note: This project was created entirely by AI.**

## Quick Start

### Local Development
```bash
docker compose up -d
cd backend
./gradlew run
```

## API Testing

**Create a button:**
```bash
curl -X POST "http://localhost:8080/api/buttons" \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","title":"Morning Exercise","color":"#4CAF50"}'
```

**List user's buttons:**
```bash
curl -X GET "http://localhost:8080/api/buttons?userId=1"
```

**Get specific button:**
```bash
curl -X GET "http://localhost:8080/api/buttons/{button-id}"
```

**Update button:**
```bash
curl -X PUT "http://localhost:8080/api/buttons/{button-id}" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Title","color":"#FF5722"}'
```

**Delete button:**
```bash
curl -X DELETE "http://localhost:8080/api/buttons/{button-id}"
```

## Tech Stack

- **Backend:** Kotlin + Ktor
- **Database:** PostgreSQL with Flyway migrations
- **Frontend:** React + TypeScript (planned)
- **Auth:** Google OAuth (planned)