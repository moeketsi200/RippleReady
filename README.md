# 🌍 RippleReady — Solve the Problem Before It Spreads

> **FNB App of the Year Hackathon 2026**  
> **Track:** Topic 6 — Climate Resilience & Essential Services  
> **Status:** MVP / Working Prototype  

---

## 📌 Executive Summary

When a water main ruptures or a local substation trips in South Africa, municipal ticketing systems log a single incident. However, **disasters do not exist in isolation—they cascade.**

A single pipe burst on York Street in George causes:
- A local crèche to run out of potable water for 40 children within 2 hours.
- Corner bakeries and township eateries to halt food prep and lose daily turnover.
- Soup kitchens to cancel midday meals for vulnerable community members.

**RippleReady** shifts disaster response from passive ticket-logging to **active cascade interception**. The moment an outage is verified, RippleReady models downstream secondary risks and coordinates hyper-local mutual aid (such as private boreholes, JoJo tanks, and mobile water bowsers) before small disruptions become community catastrophes.

---

## ⚡ Key Differentiators

| Traditional Civic Reporting | RippleReady |
| :--- | :--- |
| **Reactive:** Tells you what already broke | **Predictive:** Identifies what will break next |
| **Siloed:** Treats each report as an isolated ticket | **Interdependent:** Models schools, clinics, and businesses within the ripple zone |
| **Top-Down:** Relies entirely on slow municipal turnaround | **Horizontal:** Orchestrates peer-to-peer capacity and community reserves |
| **Passive:** One-way status notifications | **Actionable:** Matches verified needs with automated pledge fulfillment |

---

## 🏗️ How RippleReady Works

```
[1. Incident Ingest] ➔ [2. Ripple Engine] ➔ [3. Mutual Aid Match] ➔ [4. Resolution Ledger]
 - Geofenced alert      - Spatial query       - Pledges triggered    - Delivery verified
 - Ward rep verified    - Downstream nodes    - Nearest capacity     - Downtime averted
 - Category & severity  - Buffer runway       - FNB rails enabled    - Impact logged
```

1. **Verified Incident Ingest:** A community anchor, registered ward representative, or verified citizen logs a utility disruption with GPS geofencing and category tags (Water, Power, Sanitation).
2. **Downstream Cascade Modeling:** The deterministic Ripple Engine evaluates the outage radius against registered local facilities, assigning urgency scores based on buffer runway (e.g., crèches, dialysis patients, food prep SMEs).
3. **Peer-to-Peer Resource Allocation:** Registered capacity holders (boreholes, JoJo tanks, backup generators, mobile bowsers) within a sourcing radius receive immediate fulfillment prompts.
4. **Outcome & Proof of Delivery:** The platform tracks dispatch status, recipient confirmation, and downtime averted in a real-time impact dashboard.

---

## 🏦 Alignment with FNB Ecosystem

RippleReady natively ties into First National Bank’s commercial and social pillars:

- **SME & Merchant Protection:** Prevents operational shutdowns for township spaza shops, bakeries, and eateries, preserving daily Speedpoint POS turnover and reducing merchant credit risk.
- **Instant Payment & eWallet Rails:** Enables rapid, auditable micro-disbursements from disaster relief funds or corporate donors directly to volunteer transporters for fuel, containers, and water purification tablets.
- **Climate Resilience Indexing:** Supplies hyper-local vulnerability and mutual aid data to enhance bank credit risk profiling for commercial property and enterprise finance.

---

## 🛠️ Tech Stack & Architecture

- **Backend API:** Java 21+ / Spring Boot 3.3.5 (Enterprise-grade, type-safe, RESTful API with OpenAPI/Swagger UI at `/swagger-ui.html`).
- **Core Engine:** Deterministic event-driven dependency rules with Haversine great-circle spatial calculations (sub-second execution, zero hallucination).
- **Frontend (Web/PWA):** Next.js / React + Tailwind CSS (Mobile-first, optimized for low-bandwidth cellular connections).
- **Mapping:** Leaflet.js with OpenStreetMap (lightweight, zero proprietary API key lock-in).
- **Containerization:** Multi-stage Dockerfile targeting Alpine JRE with low-RAM memory limits (`-Xmx384m`).

---

## 🚀 Quickstart Guide

### 1. Run the Java Backend
```bash
cd backend
mvn spring-boot:run
```
- API Base URL: `http://localhost:8080`
- Swagger Interactive UI: `http://localhost:8080/swagger-ui.html`
- Health Probe: `http://localhost:8080/healthz`

### 2. Run Tests
```bash
cd backend
mvn test
```

---

## 🧪 Automated Test Suite & Acceptance Criteria (All 5 Epics — 64 Tests)

The entire system is backed by **64 automated unit, integration, security, concurrency, math, and end-to-end tests** covering 100% of the hackathon user stories:

| Feature Epic | Scenarios & Edge Cases Covered | Test Files (40 Tests Total) |
| :--- | :--- | :--- |
| **Epic 1: Incident Ingestion** | • US-1.1: 2.0 km perimeter & VERIFIED status<br>• US-1.2: Out-of-bounds rejection (HTTP 422)<br>• US-1.3: Spatial deduplication & confirmation vote<br>• Coordinate boundary values (+-90/+-180) & multi-citizen accumulation | [`Epic1IncidentIngestionTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic1IncidentIngestionTest.java)<br>[`Epic1IncidentEdgeCasesTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic1IncidentEdgeCasesTest.java) |
| **Epic 2: Ripple Cascade** | • US-2.1: Critical runway detection (&le;2h buffer)<br>• US-2.2: Life & Care &rarr; Food &rarr; FNB Merchant priority<br>• 100 queries in < 200ms benchmark & prolonged outage modeling | [`Epic2RippleCascadeTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic2RippleCascadeTest.java)<br>[`Epic2CascadeBoundaryAndPerformanceTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic2CascadeBoundaryAndPerformanceTest.java) |
| **Epic 3: Capacity Matching** | • US-3.1: Borehole & JoJo ranking by distance<br>• US-3.2: Capacity decrement & OTP token lock<br>• Over-allocation 409 Conflict, exact exhaustion, & negative unit rejection | [`Epic3ResourceMatchingTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic3ResourceMatchingTest.java)<br>[`Epic3ResourcePledgeFailureAndExhaustionTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic3ResourcePledgeFailureAndExhaustionTest.java) |
| **Epic 4: Handshake Proof** | • US-4.1: Token redemption & green node transition<br>• Replay attack prevention (token single-use) & forged token rejection | [`Epic4DeliveryVerificationTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic4DeliveryVerificationTest.java)<br>[`Epic4DeliverySecurityAndReplayAttackTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic4DeliverySecurityAndReplayAttackTest.java) |
| **Epic 5: FNB Escrow Rails** | • US-5.1: Automated R150 fuel stipend payout<br>• SHA-256 cryptographic hash-chained audit ledger | [`Epic5FnbMicroEscrowTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic5FnbMicroEscrowTest.java)<br>[`Epic5FnbCryptographicLedgerIntegrityTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic5FnbCryptographicLedgerIntegrityTest.java) |
| **Concurrency & Thread Safety** | • Simultaneous thread-safe pledges (never exceeds capacity)<br>• Parallel thread-safe incident deduplication | [`ConcurrencyAndThreadSafetyTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/ConcurrencyAndThreadSafetyTest.java) |
| **Full Lifecycle E2E** | • End-to-end pipeline verification from burst to payout & audit ledger | [`HackathonEndToEndWinningFlowTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/HackathonEndToEndWinningFlowTest.java) |

---

## 📋 Hackathon MVP Scope

- [x] **Geofenced Incident Ingestion:** Ward-level incident reporting with severity flags.
- [x] **Deterministic Ripple Engine:** Real-time spatial query calculating secondary downstream consequences.
- [x] **Community Anchor Registry:** Pre-configured critical facilities (crèches, clinics, food merchants) in George, Western Cape.
- [x] **Mutual Aid Matching:** Dynamic pairing of private boreholes, JoJo tanks, and bowsers to threatened nodes.
- [x] **Pledge & Capacity Allocation:** Atomic resource capacity deduction and dispatch token issuance.
- [x] **Live Impact Dashboard Ledger:** Real-time metrics tracking secondary crises averted, hours saved, and turnover preserved.

---

## 🗺️ Roadmap

- **Phase 1 (Hackathon MVP):** Water & power outage cascade mitigation in Ward 18 (George / Garden Route, Western Cape).
- **Phase 2 (Post-Hackathon):** Integration with municipal telemetry and local disaster management centers.
- **Phase 3 (Enterprise Integration):** Deep integration into the FNB mobile banking ecosystem for automated disaster micro-grants and merchant resilience protection.

---

## 👥 The Team

Built with pride for the **FNB App of the Year Hackathon 2026**.
