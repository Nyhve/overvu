# OvervU

Persönliches Life-Management-Tool zur Übersicht über alle Lebensbereiche – Ziele, Aufgaben, Habits, Fitness, Learning und Finanzen – mit Activity-Tracking und Analytics-Dashboard.

## Tech Stack

**Backend**
- Java 21, Spring Boot 4.1, Spring Security (JWT)
- PostgreSQL, Flyway (Datenbankmigrationen)
- Maven

**Frontend**
- React 18, TypeScript, Vite
- Tailwind CSS, Recharts
- React Router

**Infrastructure**
- Docker, Docker Compose
- GitHub Actions (CI/CD)

## Features

- **Ziele** – Langfristige Lebensziele verfolgen
- **Aufgaben** – Kurzfristige To-dos mit Fälligkeitsdatum
- **Habits** – Tägliche und wöchentliche Gewohnheiten tracken
- **Fitness** – Workouts mit Übungen und Sätzen erfassen
- **Learning** – Lernprojekte und Lernzeit dokumentieren
- **Finanzen** – Einnahmen und Ausgaben verwalten
- **Analytics** – Visualisierungen und Statistiken aller Bereiche
- **Dark Mode** – Helles und dunkles Farbschema
- **Profil** – Features individuell aktivieren/deaktivieren

## TO-DOS und bekannte "Bugs"

- In Fitness kann man eine Übung wiederholt eintragen und fühlt sich nicht gut an weil alle dann gleichzeitig aktualisiert werden
- In Fitness die Möglichkeit gleich mehrere Sätze einzutragen
- Ziele und Augaben mehr thematisch voneinander trennen. Möglicherweise Aufgaben als Teilziele von den großen Lebenszielen machen.
- Insgesamt glaub bessere Farben wählen besonders für Dark Mode weil da ist auch das grün und rot ein bisschen schwer für die Augen.
- Vielleicht noch dass wenn man ein Feature tatsächlich ausschaltet es komplett nciht mehr sichtbar ist auch auf der Main "OvervU" Seite.

## Lokale Entwicklung

### Voraussetzungen
- Java 21
- Node.js 24
- Docker Desktop
- Maven (oder über den mitgelieferten Maven Wrapper)

### Setup

**1. Datenbank starten:**
```bash
docker compose up -d postgres
```

**2. Backend starten:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**3. Frontend starten (separates Terminal):**
```bash
cd frontend
npm install
npm run dev
```

**4. App öffnen:**
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

### Vollständig containerisiert (optional)

```bash
# .env Datei im Root anlegen:
# JWT_SECRET=dein-secret-key-mindestens-32-zeichen

docker compose up --build
```
App läuft dann auf http://localhost

## Tests

```bash
cd backend
./mvnw test
```

CI läuft automatisch bei jedem Push auf `main` via GitHub Actions.