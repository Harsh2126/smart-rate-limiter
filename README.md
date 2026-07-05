# 🚦 Smart Rate Limiter

A production-style **adaptive rate limiter** built with Java (Spring Boot) and a live monitoring dashboard. Implements the **Token Bucket algorithm**, backed by **Redis** for persistence, with IP-based tracking, structured logging, config-driven limits, and API key authentication.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![Redis](https://img.shields.io/badge/Redis-persistence-red)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## ✨ Features

- **Token Bucket algorithm** — smooth, spike-resistant rate limiting (the same approach used by Stripe, AWS, and GitHub's public APIs)
- **IP-based client tracking** — automatically identifies clients by request IP when no explicit ID is given
- **Redis-backed persistence** — rate limit state survives server restarts and can scale across multiple instances
- **API Key authentication** — protects endpoints via an `X-API-Key` header
- **Structured logging** — every allowed/blocked request is logged with client ID and remaining tokens
- **Config-driven limits** — bucket capacity and refill rate are set in `application.properties`, no code changes needed
- **Live monitoring dashboard** — a dark, console-style frontend showing per-client token levels, request counts, and pass/block activity in real time

---

## 🏗️ Architecture

```
┌─────────────────┐        HTTP + X-API-Key        ┌──────────────────────┐
│   Dashboard      │ ──────────────────────────────▶│   Spring Boot API     │
│   (HTML/JS)      │ ◀────────────────────────────── │  (Token Bucket logic) │
└─────────────────┘         JSON response            └──────────┬───────────┘
                                                                  │
                                                                  ▼
                                                        ┌──────────────────┐
                                                        │      Redis        │
                                                        │  (bucket state)   │
                                                        └──────────────────┘
```

**How the Token Bucket works:** each client gets a bucket that holds a maximum number of tokens. Every request consumes one token. Tokens refill continuously at a fixed rate over time. If the bucket is empty, the request is blocked — but as soon as enough time passes for a token to regenerate, requests are allowed again. This avoids the "burst at window boundary" problem that simple fixed-window counters suffer from.

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.1.0 |
| Data store | Redis (via Spring Data Redis) |
| Frontend | Vanilla HTML/CSS/JS |
| Build tool | Maven |
| Containerization | Docker (for Redis) |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven
- Docker Desktop (for running Redis)

### 1. Start Redis
```bash
docker run --name redis-rate-limiter -p 6379:6379 -d redis
```

### 2. Configure
Edit `src/main/resources/application.properties` if you want to change defaults:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379

ratelimiter.max-tokens=10
ratelimiter.refill-rate=1.0

apikey.valid-keys=key-abc123,key-xyz789,key-demo001
```

### 3. Run the backend
```bash
mvn clean spring-boot:run
```
The API will be available at `http://localhost:8080`.

### 4. Open the dashboard
Open `frontend/index.html` in a browser (or serve it with VS Code's Live Server). It talks to the backend at `localhost:8080` by default.

---

## 🔌 API Reference

### `GET /api/check`
Checks whether a request from a client is allowed, consuming one token if so.

**Headers**
| Header | Required | Description |
|---|---|---|
| `X-API-Key` | Yes | Must match one of the keys in `apikey.valid-keys` |

**Query Params**
| Param | Required | Description |
|---|---|---|
| `clientId` | No | Explicit client identifier. If omitted, the caller's IP address is used. |

**Response — allowed**
```json
{ "status": "allowed", "clientId": "user1" }
```

**Response — blocked (HTTP 429)**
```json
{ "status": "blocked", "message": "Rate limit exceeded", "clientId": "user1" }
```

**Response — missing/invalid key (HTTP 401)**
```json
{ "status": "unauthorized", "message": "Missing or invalid API key" }
```

### `GET /api/stats`
Returns the current bucket state for a client without consuming a token.

**Response**
```json
{
  "clientId": "user1",
  "count": 3,
  "limit": 10,
  "avgRate": 7.0
}
```

---

## 🖥️ Dashboard

The dashboard lets you add any number of clients, send single requests or a 15-request burst, and watch token levels drain and refill live. A summary bar tracks total allowed/blocked requests across all tracked clients.

---

## 🗺️ Roadmap

- [ ] Per-endpoint rate limits (e.g. stricter limits on `/login`)
- [ ] Admin endpoint to reset or inspect all client buckets
- [ ] Deployment to a cloud provider (Render/Railway) with a public demo link
- [ ] Unit tests for the token bucket logic

---

## 📄 License

MIT — free to use and modify.
