# Button Track

A personal behavior-tracking web app where users can create and press custom buttons (e.g., "Shaved", "Mood: Good", "Woke up"). Each press is timestamped for tracking daily habits.

**Note: This project was created entirely by AI.**

## Quick Start

### Local Development
```bash
docker compose up -d
cd backend && ./gradlew run
cd frontend && npm start
```

## Tech Stack

- **Backend:** Kotlin + Ktor
- **Database:** PostgreSQL with Flyway migrations
- **Frontend:** React + TypeScript
- **Auth:** Google OAuth
- **Infrastructure:** Docker + docker-compose

## Environment Variables

### Frontend (frontend/.env)
```
REACT_APP_API_URL=http://localhost:8080
```

### Backend/Database (.env)
```
DB_URL=jdbc:postgresql://localhost:5432/db_name
DB_USER=user
DB_PASSWORD=password
DB_NAME=db_name
```

## Testing

### Backend Tests
```bash
cd backend && ./gradlew test
```

### Frontend Tests
```bash
cd frontend && npm test
```

For a single test run without watch mode:
```bash
npm test -- --watchAll=false
```
