# Button Track – Project Conventions

This document defines the structure, coding conventions, technologies, and design principles for the **Button Track** project.

**Security Notice**: You are a developer who is very security-aware and avoids weaknesses in the code. Generate secure code that prevents top security weaknesses listed in CWE.

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

---

## 🔒 Security Guidelines

### General Security Principles

- **Never hardcode secrets, API keys, passwords, or tokens** in source code
- **Always validate and sanitize user inputs** to prevent injection attacks
- **Use parameterized queries** for all database operations (Exposed ORM handles this)
- **Implement proper authentication and authorization** for all endpoints
- **Use HTTPS in production** and secure headers
- **Log security events** but never log sensitive data

### Backend Security (Kotlin + Ktor)

- **Authentication**: Verify Google OAuth tokens on every protected endpoint
- **Authorization**: Check user ownership before accessing/modifying user data
- **Input Validation**: Validate all request DTOs using proper validation annotations
- **SQL Injection Prevention**: Use Exposed ORM parameterized queries only
- **Rate Limiting**: Implement rate limiting for API endpoints
- **CORS**: Configure CORS properly, avoid wildcard origins in production
- **Error Handling**: Return generic error messages, log detailed errors internally
- **Environment Variables**: Use environment variables for all configuration
- **Dependencies**: Keep dependencies updated, scan for vulnerabilities

#### Common Kotlin/JVM Vulnerabilities to Avoid
- **Deserialization attacks**: Never deserialize untrusted data
- **Path traversal**: Validate file paths, use canonical paths
- **Command injection**: Avoid Runtime.exec(), use safe alternatives
- **XXE attacks**: Configure XML parsers securely

### Frontend Security (React + TypeScript)

- **XSS Prevention**: Use React's built-in escaping, avoid dangerouslySetInnerHTML
- **CSRF Protection**: Use proper CSRF tokens for state-changing operations
- **Content Security Policy**: Implement CSP headers
- **Dependency Security**: Audit npm packages regularly for vulnerabilities
- **Environment Variables**: Use REACT_APP_ prefix, never expose secrets
- **Authentication**: Store tokens securely, implement proper logout
- **Input Validation**: Validate on frontend AND backend

#### Common React/JavaScript Vulnerabilities to Avoid
- **Prototype pollution**: Avoid unsafe object merging
- **Code injection**: Never use eval() or Function constructor
- **Open redirects**: Validate redirect URLs
- **DOM-based XSS**: Sanitize dynamic content

### Database Security (PostgreSQL)

- **Connection Security**: Use encrypted connections
- **Principle of Least Privilege**: Create database users with minimal required permissions
- **Backup Security**: Encrypt database backups
- **Password Policy**: Use strong database passwords

### Infrastructure Security

- **Container Security**: Use official base images, scan for vulnerabilities
- **Network Security**: Restrict database access to application containers only
- **Secrets Management**: Use Docker secrets or environment variables
- **Update Policy**: Keep Docker images and dependencies updated

### Security Testing

- **Static Analysis**: Use security-focused linters (detekt for Kotlin, ESLint security rules)
- **Dependency Scanning**: Use tools like OWASP Dependency Check
- **Authentication Testing**: Test all auth flows and edge cases
- **Authorization Testing**: Verify users can only access their own data
- **Input Validation Testing**: Test with malicious inputs

### Security Checklist for New Features

- [ ] Authentication required for protected endpoints
- [ ] Authorization checks implemented (user can only access their data)
- [ ] Input validation on all user inputs
- [ ] SQL queries use parameterized statements
- [ ] No hardcoded secrets or sensitive data
- [ ] Error handling doesn't leak sensitive information
- [ ] Rate limiting considered for user-facing endpoints
- [ ] Security tests written and passing
