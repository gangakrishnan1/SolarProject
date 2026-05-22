# SolarIQ — AI-Powered Solar Feasibility Platform for Lumenor

> A 4-step solar feasibility calculator that turns a single electricity bill into a personalized 25-year savings projection, MNRE subsidy estimate, and AI-generated buying advice — with a sales dashboard on the other side.

## The problem this solves

Lumenor's sales team was spending hours on each lead manually estimating system size, payback periods, and PM Surya Ghar subsidies — usually before the customer was even qualified. Cold leads ate the same time as hot ones. SolarIQ flips that: customers self-serve a realistic estimate in under a minute, contact info is captured only when they're convinced, and every lead arrives pre-scored 0–100 so sales call the hot ones first.

## Features

- **4-step calculator** — GPS/search location API (OpenStreetMap), area-type panel sizing, monthly bill, property type, roof
- **Personalized panel recommendations** — rural (≤5 kW), semi-urban (≤8 kW), urban (≤12 kW), metro (≤20 kW) with panel count & wattage
- **Solar math engine** — 10 Indian states with per-state tariff, peak sun hours, and installation cost; commercial/industrial tariff multipliers; system size rounded to 0.5 kW increments
- **MNRE PM Surya Ghar subsidy** — exact split formula for residential (1–3 kW), capped above 3 kW, zero for commercial/industrial
- **25-year financial projection** — 3% annual tariff escalation, 0.5% panel degradation, payback-year marker
- **CO2 offset** — annual generation × Indian grid emissions factor (0.82 kg/kWh)
- **AI summary + tips** — Google Gemini (`gemini-1.5-flash`) personalizes a 2-3 sentence summary plus 3 location/property-specific tips; deterministic fallback when the API key is empty
- **Lead scoring** — 0–100 from bill (40 pts) + ownership (20) + roof (20) + solar potential (20)
- **Sales dashboard** — basic-auth protected, filter by status and minimum score, click into any lead for AI summary + status workflow

## Tech stack

| Layer    | Technology                                                  |
|----------|-------------------------------------------------------------|
| Backend  | Spring Boot 3.2.5, Java 17, Spring Data JPA, Spring Security |
| Frontend | React 19 (JSX, no TS), Vite 8, React Router 7, Recharts, Axios |
| Database | PostgreSQL 18 (H2 in-memory for tests)                      |
| AI       | Google Gemini API (`gemini-1.5-flash`) via REST             |
| Testing  | JUnit 5 + Mockito + MockMvc (backend), Vitest + React Testing Library (frontend) |

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.9+

## Backend setup

```bash
cd backend

# 1. Create the database (one time)
createdb solariq

# 2. (Optional) copy env file and edit
cp .env.example .env

# 3. Run
mvn spring-boot:run
```

Server: `http://localhost:8080`

The default config in `application.properties` points to PostgreSQL on `localhost:5433` with username `gangaKrishnan` and no password by default. Override with environment variables — see the table below.

## Frontend setup

```bash
cd frontend
npm install
npm run dev
```

Dev server: `http://localhost:5173` — proxies `/api` → `http://localhost:8080`.

## Environment variables

| Variable          | Default                                          | Description                                  |
|-------------------|--------------------------------------------------|----------------------------------------------|
| `DB_URL`          | `jdbc:postgresql://localhost:5433/solariq`       | PostgreSQL JDBC URL                          |
| `DB_USERNAME`     | `gangaKrishnan`                                  | PostgreSQL user                              |
| `DB_PASSWORD`     | (empty)                                          | PostgreSQL password                          |
| `JPA_DDL_AUTO`    | `update`                                         | Hibernate DDL strategy                       |
| `JPA_SHOW_SQL`    | `true`                                           | Log SQL statements                           |
| `GEMINI_API_KEY`  | (empty)                                          | Google Gemini API key. Empty → fallback used |
| `ADMIN_USERNAME`  | `admin`                                          | Basic-auth user for `/leads` endpoints       |
| `ADMIN_PASSWORD`  | `admin123`                                       | Basic-auth password                          |

## API endpoints

| Method | Path                                        | Auth   | Description                                                            |
|--------|---------------------------------------------|--------|------------------------------------------------------------------------|
| GET    | `/api/v1/location/search?q=`                | None   | Search Indian localities (OpenStreetMap Nominatim). |
| GET    | `/api/v1/location/reverse?lat=&lon=`        | None   | Reverse geocode coordinates to city/state + suggested area type. |
| POST   | `/api/v1/assess`                            | None   | Run a solar assessment. Body: location + `areaType` + bill + property. Returns panel recommendation + leadScore. |
| POST   | `/api/v1/assess/{assessmentId}/capture`     | None   | Attach contact info (name/email/phone) to an existing assessment.      |
| GET    | `/api/v1/leads?status=&state=&minScore=`    | Basic  | List leads (filters optional). Ordered by score descending.            |
| PATCH  | `/api/v1/leads/{leadId}/status`             | Basic  | Update lead status. Body: `{status, notes?, assignedTo?}`. Allowed status: `new`, `contacted`, `site_visit`, `converted`, `lost`. |

## Running tests

```bash
# Backend (71 tests across calculator, AI, validation, service, controller)
cd backend && mvn test

# Frontend (25 component tests via Vitest)
cd frontend && npm test
```

## Folder structure

```
karunkar_project/
├── backend/
│   ├── pom.xml
│   ├── .env.example
│   └── src/
│       ├── main/
│       │   ├── java/com/lumenor/solariq/
│       │   │   ├── SolariqApplication.java
│       │   │   ├── config/         CorsConfig, SecurityConfig
│       │   │   ├── controller/     AssessmentController
│       │   │   ├── dto/            AssessmentRequestDTO, AssessmentResponseDTO,
│       │   │   │                   ContactCaptureDTO, LeadResponseDTO,
│       │   │   │                   StatusUpdateDTO, ProjectionYearDTO
│       │   │   ├── entity/         Lead
│       │   │   ├── exception/      NotFoundException, GlobalExceptionHandler
│       │   │   ├── repository/     LeadRepository
│       │   │   └── service/        CalculatorService, AiService, LeadService,
│       │   │                       StateConfig, AssessmentResultData,
│       │   │                       ProjectionYear, AiAnalysisResult
│       │   └── resources/application.properties
│       └── test/
│           ├── java/com/lumenor/solariq/   (5 test classes, 71 tests)
│           └── resources/application.properties  (H2)
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── pages/         CalculatorPage, ResultsPage, DashboardPage
│       ├── components/    ProgressBar, StepLocation, StepBill, StepProperty,
│       │                  StepRoof, LoadingScreen, HeroBanner, MetricGrid,
│       │                  ProjectionChart, TipsList, ContactModal,
│       │                  ScoreBadge, LeadTable, LeadDetail
│       ├── services/      api.js
│       ├── utils/         formatters.js
│       ├── styles/        global.css
│       └── test/          calculator.test.jsx, results.test.jsx,
│                          dashboard.test.jsx, setup.js
└── README.md
```

## Author

**Ganga Bhavani Kunapureddy** —  Java Full Stack 
