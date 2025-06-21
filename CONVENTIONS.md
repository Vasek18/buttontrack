# Button Track – Project Conventions

This document defines the structure, coding conventions, technologies, and design principles for the **Button Track** project.

---

## 🧠 Project Vision

Button Track is a personal behavior-tracking web app where users can create and press custom buttons (e.g., “Shaved”, “Mood: Good”, “Woke up”). Each press is timestamped and visualized in a timeline view (heatmap, calendar, graph). The goal is to promote self-awareness through small daily actions.

---

## 🗂 Folder Structure

├── backend/ ← Kotlin backend (Ktor)
│ ├── src/ ← Application code
│ ├── test/ ← Unit and integration tests
│ └── Dockerfile ← Ktor Docker build
├── frontend/ ← React frontend (TypeScript)
│ ├── src/ ← App source code
│ ├── public/ ← Static files
│ └── .env ← Contains REACT_APP_API_URL
├── docker-compose.yml ← Dev environment (Postgres + backend)
├── CONVENTIONS.md ← Project documentation and rules
├── aider.yml ← Aider configuration

---

## ⚙️ Technologies Used

- **Backend**: Kotlin + Ktor
- **Frontend**: React (Create React App, TypeScript)
- **Database**: PostgreSQL
- **Auth**: Google OAuth only
- **Infrastructure**: Docker + docker-compose

---

## 🔐 Authentication

- Only Google OAuth is supported (via OAuth2 client setup)
- All actions (pressing, viewing, creating buttons) require login
- Frontend uses Google sign-in button and sends the ID token to the backend for verification
- Backend uses Google public keys to verify tokens and extract user identity

---

## 📚 Backend Guidelines

- Ktor handles HTTP routing and middleware
- Use **Exposed DAO** for database access
- Access DB via `jdbc:postgresql://db:5432/buttontrack` with environment-based credentials
- Use service classes for business logic and keep routes/controllers thin
- Use dependency injection (e.g., Koin) if scaling logic
- Serve a REST API, JSON only

### Backend Endpoints (sample)

POST /api/buttons ← Create a button
GET /api/buttons ← List user’s buttons
PUT /api/buttons/:id ← Edit a button
DELETE /api/buttons/:id ← Delete a button
POST /api/press/:id ← Record a button press
GET /api/stats ← Get button press history

### Conventions

- File/Class names: `PascalCase`
- Function names: `camelCase`
- Package structure: feature-oriented
- Use DTOs for request/response models
- Log only meaningful events (e.g., button press, auth failure)

### Testing

- Tests reside in `backend/test`
- Use **JUnit 5**
- Test services and routes in isolation
- Use mock DB / test containers

---

## 🎨 Frontend Guidelines

- React functional components with hooks
- Google Sign-In handled via `react-oauth/google`
- Global state (e.g., user info, button list) stored via `useContext`
- API base URL comes from `.env` via `REACT_APP_API_URL`

### Screens

- `/` – Main button pressing screen
- `/manage` – Manage (CRUD) custom buttons
- `/stats` – Timeline visualization of button presses
- `/login` – Google login UI (optional depending on flow)

### Conventions

- TypeScript
- Component names: `PascalCase`
- Use TailwindCSS
- All API calls through a typed API service wrapper
- Keep components small and composable

### Testing

- Use **React Testing Library + Jest**
- Store tests next to components as `MyComponent.test.tsx`
