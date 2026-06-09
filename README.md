# LoanLens AI

> AI-powered loan eligibility evaluation engine with real-time decisioning, explainability layer, and Redis caching.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-Upstash-red?style=flat-square)
![React](https://img.shields.io/badge/Frontend-React-61dafb?style=flat-square)
![Claude](https://img.shields.io/badge/AI-Claude_API-blueviolet?style=flat-square)

---

## what is this

LoanLens AI evaluates a borrower's loan application in real time using a rule-based eligibility engine, generates a weighted risk score, and produces a plain-English explanation of the decision using the Claude API. Every evaluation is persisted to PostgreSQL and cached in Redis to avoid redundant processing on repeat profiles.

Built as a portfolio project to demonstrate production-grade backend design patterns in a fintech context.

---

## architecture

React Frontend (Vite)
│
▼
Spring Boot REST API  ──────────────────────────────────────┐
│                                                    │
▼                                                    ▼
LoanEligibilityEngine                              Upstash Redis

DTI ratio check                             (evaluation cache)
Credit score check                                       │
Income floor check                                       │
Loan to income ratio                                     │
Employment type risk                                     │
│                                                    │
▼                                                    │
LoanEvaluationService  ◄────────────────────────────────────┘
│
├──► Neon PostgreSQL  (persist decision)
│
└──► ClaudeExplanationService
│
▼
Claude API  (generate plain-English explanation)

---

## features

- **rule-based eligibility engine** — evaluates DTI ratio, credit score, income floor, loan-to-income ratio, and employment type. returns APPROVED, REJECTED, or REVIEW
- **weighted scoring** — each rule contributes to a 0-100 score. risk category (LOW, MEDIUM, HIGH) derived from score bands
- **AI explanation layer** — structured prompt sent to Claude API with full decision context. returns 2-3 sentence plain-English explanation and actionable advice
- **Redis caching** — evaluations cached for 24 hours by financial fingerprint (excludes name/age). cache hit avoids engine, DB write, and AI call entirely
- **EMI calculation** — standard reducing balance formula. monthly EMI returned alongside decision
- **full persistence** — every evaluation stored in Neon PostgreSQL with UUID primary key, decision, score, checks, AI explanation, and timestamp

---

## tech stack

| layer | technology |
|---|---|
| backend | Java 17, Spring Boot 3.5 |
| database | PostgreSQL via Neon (cloud) |
| cache | Redis via Upstash (cloud) |
| ai | Claude API (Anthropic) |
| frontend | React, Vite |
| build | Maven |

---

## api

### POST /api/v1/loans/evaluate

request body:
```json
{
  "fullName": "Pratulya Tiwary",
  "age": 25,
  "monthlyIncome": 50000,
  "monthlyDebt": 10000,
  "creditScore": 750,
  "requestedLoanAmount": 500000,
  "loanTenureMonths": 60,
  "employmentType": "SALARIED"
}
```

response:
```json
{
  "applicationId": "ace909d0-3df5-49a4-96dc-c923cd49c48a",
  "fullName": "Pratulya Tiwary",
  "decision": "APPROVED",
  "score": 100,
  "riskCategory": "LOW",
  "requestedLoanAmount": 500000.0,
  "monthlyEmi": 11122.22,
  "passedChecks": [
    "dti ratio is within acceptble range at 20.0 percent",
    "credit score is strong at 750"
  ],
  "failedChecks": [],
  "aiExplanation": "application approved. strong credit score and healthy dti ratio. funds disbursed within 3-5 working days.",
  "evaluatedAt": "2026-06-09T18:31:05"
}
```

---

## eligibility rules

| rule | threshold | weight |
|---|---|---|
| DTI ratio | must be below 45% | 25 pts |
| credit score | 700+ approved, 600-699 review, below 600 rejected | 30 pts |
| monthly income | minimum Rs 15,000 | 20 pts |
| loan to income ratio | maximum 24x monthly income | 15 pts |
| employment type | salaried 10pts, self-employed 5pts, other 0pts | 10 pts |

---

## local setup

**prerequisites:** Java 17, Maven, Node.js

1. clone the repo
```bash
git clone https://github.com/codorhythm/loanlens.git
cd loanlens/loanlens
```

2. add your credentials to `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:postgresql://YOUR_NEON_HOST/neondb?sslmode=require
spring.datasource.username=YOUR_NEON_USERNAME
spring.datasource.password=YOUR_NEON_PASSWORD
spring.data.redis.host=YOUR_UPSTASH_HOST
spring.data.redis.password=YOUR_UPSTASH_PASSWORD
claude.api.key=YOUR_CLAUDE_API_KEY
```

3. run the backend
```bash
./mvnw spring-boot:run
```

4. run the frontend
```bash
cd frontend
npm install
npm run dev
```

5. open `http://localhost:5173`

---

## project structure

loanlens/
├── src/main/java/com/loanlens/
│   ├── controller/        # REST endpoints
│   ├── service/           # business logic + AI integration
│   ├── engine/            # rule-based eligibility engine
│   ├── model/
│   │   ├── entity/        # JPA entities
│   │   ├── request/       # API request models
│   │   └── response/      # API response models
│   ├── repository/        # Spring Data JPA repositories
│   ├── config/            # Redis configuration
│   └── exception/         # exception handling
└── frontend/              # React + Vite frontend


---

## key design decisions

**cache key design** — the Redis cache key is built from the financial fingerprint of the request (credit score, income, debt, loan amount, tenure, employment type). name and age are excluded because two people with identical financial profiles get the same decision. this maximises cache hit rate without compromising correctness.

**three-outcome decisioning** — most loan systems return binary approve/reject. LoanLens returns a third state REVIEW for borderline profiles (one failed check, score 50-79). this mirrors real-world credit workflows where edge cases go to a human officer.

**AI explanation as a layer** — the explanation service is decoupled from the engine. the engine produces structured data (score, checks, decision). the AI layer consumes that structure and produces human language. swapping Claude for another model or removing the AI layer entirely requires no changes to the engine or persistence layer.

---

*built by [Pratulya Tiwary](https://linkedin.com/in/pratulya-tiwary/) — backend engineer*

