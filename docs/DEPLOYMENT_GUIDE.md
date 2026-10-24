# 🚀 RippleReady — Render & Vercel Production Deployment Guide

> **FNB App of the Year Hackathon 2026**  
> Complete blueprint for deploying the **Spring Boot Java Backend on Render** and the **React + Vite Frontend on Vercel**.

---

## 🏗️ Architecture Overview

```
                      +---------------------------------------+
                      |         Vercel (Frontend SPA)         |
                      |    https://rippleready.vercel.app     |
                      |  - React 18, Vite, Leaflet, Tailwind  |
                      |  - Environment: VITE_API_URL          |
                      +-------------------+-------------------+
                                          |
                                          | HTTPS / REST / CORS
                                          v
                      +---------------------------------------+
                      |         Render (Backend API)          |
                      | https://rippleready-backend.onrender  |
                      |  - Java 21 LTS / Spring Boot 3.3.5    |
                      |  - Embedded SQLite 3 (WAL Journaling) |
                      |  - Port: $PORT (Dynamic)              |
                      +---------------------------------------+
```

---

## Part 1: Deploy Backend to Render (Docker & SQLite)

### Option A: 1-Click Render Blueprint (Recommended)
RippleReady includes a pre-configured [`render.yaml`](../render.yaml) file:
1. Go to [dashboard.render.com](https://dashboard.render.com/) and log in with GitHub.
2. Click **New +** &rarr; **Blueprint**.
3. Select your repository `RippleReady`.
4. Render will auto-detect [`render.yaml`](../render.yaml), Dockerfile, and environment variables.
5. Click **Apply**.
6. When deployment finishes, copy your live backend URL (e.g., `https://rippleready-backend.onrender.com`).

---

### Option B: Manual Web Service Setup on Render
1. Go to [dashboard.render.com](https://dashboard.render.com/) &rarr; Click **New +** &rarr; **Web Service**.
2. Connect your GitHub repository: `moeketsi200/RippleReady`.
3. Configure the service settings:
   - **Name:** `rippleready-backend`
   - **Region:** Frankfurt (EU Central) or Oregon (US West)
   - **Branch:** `main`
   - **Root Directory:** `.` (or `backend`)
   - **Runtime:** `Docker`
   - **Dockerfile Path:** `./Dockerfile` (or `./backend/Dockerfile` if Root Directory is `backend`)
   - **Instance Type:** `Free`
4. Set the **Health Check Path:**
   - `/healthz`
5. Under **Environment Variables**, add:
   | Key | Value | Notes |
   | :--- | :--- | :--- |
   | `PORT` | `10000` | Render dynamically forwards traffic to this port |
   | `SQLITE_DB_PATH` | `data/rippleready.db` | Embedded SQLite persistent database |
6. Click **Create Web Service**.
7. Once deployed, verify in your browser:
   - Health: `https://<YOUR-RENDER-URL>.onrender.com/healthz`
   - Swagger Docs: `https://<YOUR-RENDER-URL>.onrender.com/swagger-ui.html`
   - SQLite Status: `https://<YOUR-RENDER-URL>.onrender.com/api/database/status`

---

## Part 2: Deploy Frontend to Vercel (React + Vite)

1. Go to [vercel.com](https://vercel.com/) and sign in with GitHub.
2. Click **Add New...** &rarr; **Project**.
3. Select your `RippleReady` repository and click **Import**.
4. Configure Project Settings:
   - **Framework Preset:** `Vite`
   - **Root Directory:** Click *Edit* and select `frontend` (or leave as root `.`, as both `vercel.json` files are pre-configured).
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
5. **Add Environment Variable:**
   - Expand the **Environment Variables** section.
   - **Key:** `VITE_API_URL`
   - **Value:** `https://<YOUR-RENDER-APP-NAME>.onrender.com` *(paste your live Render backend URL from Part 1, without a trailing slash)*
6. Click **Deploy**.
7. In ~45 seconds, your frontend is live on `https://<YOUR-APP>.vercel.app`!

---

## 🔒 Security & CORS Pre-Flight Guarantee

The Spring Boot backend includes [`CorsConfig.java`](../backend/src/main/java/com/rippleready/config/CorsConfig.java) which natively authorizes:
- All Vercel domains (`*.vercel.app`)
- Custom production domains
- All HTTP methods: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`, `HEAD`
- Pre-flight `OPTIONS` caching (3600s)

---

## 🧪 Post-Deployment Verification Checklist

1. **Verify Backend Health:**
   ```bash
   curl -s https://<YOUR-RENDER-APP>.onrender.com/healthz
   # Expects: {"version":"1.0.0","service":"RippleReady Backend","track":"FNB App of the Year - Topic 6: Climate Resilience & Essential Services","status":"UP"}
   ```
2. **Verify SQLite Database Status:**
   ```bash
   curl -s https://<YOUR-RENDER-APP>.onrender.com/api/database/status
   # Expects: {"status":"HEALTHY","engine":"SQLite 3","journalMode":"wal", ...}
   ```
3. **Verify Vercel Web App:**
   - Open `https://<YOUR-VERCEL-APP>.vercel.app`
   - Click **"Demo Scenario"** &rarr; Red 2.0 km ripple perimeter renders over George, Western Cape.
   - Click **"Impact Ledger"** &rarr; Inspect live SQLite WAL engine badge and SHA-256 cryptographic trail.
