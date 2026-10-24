# ⚙️ RippleReady — Spring Boot Backend API

> **FNB App of the Year Hackathon 2026**  
> **Track:** Topic 6 — Climate Resilience & Essential Services  
> **Framework:** Java 21+ / Spring Boot 3.3.5 / Maven  
> **Status:** Production-Ready MVP (All 5 Epics & 64 Comprehensive Tests Passing)

---

## 📌 Complete Architecture & Test Matrix (64 Tests)

The backend provides the deterministic spatial matching, cascade prediction, and cryptographic audit engine for municipal outage mutual aid.

| Epic | Feature Scope & Acceptance Criteria | Status | Test Suites (64 Tests Total) |
| :--- | :--- | :---: | :--- |
| **Epic 1** | **Verified Incident Ingestion & Perimeter**<br>• US-1.1: 2.0 km perimeter & VERIFIED status<br>• US-1.2: Strict geographic bounds validation (HTTP 422)<br>• US-1.3: Spatial deduplication within 350m & confidence voting<br>• *Edge Cases*: Boundary lat/lon (+-90/+-180), multi-citizen voting | ✅ 100% | [`Epic1IncidentIngestionTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic1IncidentIngestionTest.java)<br>[`Epic1IncidentEdgeCasesTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic1IncidentEdgeCasesTest.java) |
| **Epic 2** | **Downstream Ripple Cascade Engine**<br>• US-2.1: Critical facility runway detection (&le; 2h = CRITICAL)<br>• US-2.2: Tiered prioritization: Life & Care &rarr; Food Security &rarr; FNB Merchants<br>• *Benchmarks*: 100 queries in < 200ms, prolonged vs brief outage modeling | ✅ 100% | [`Epic2RippleCascadeTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic2RippleCascadeTest.java)<br>[`Epic2CascadeBoundaryAndPerformanceTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic2CascadeBoundaryAndPerformanceTest.java) |
| **Epic 3** | **Hyper-Local Resource Matching & Capacity Sourcing**<br>• US-3.1: Borehole & JoJo matching ranked ascending by distance<br>• US-3.2: Pledging lock, capacity decrement, & OTP generation<br>• *Edge Cases*: Zero-capacity exhaustion, over-allocation 409 Conflict, validation | ✅ 100% | [`Epic3ResourceMatchingTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic3ResourceMatchingTest.java)<br>[`Epic3ResourcePledgeFailureAndExhaustionTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic3ResourcePledgeFailureAndExhaustionTest.java) |
| **Epic 4** | **Delivery Verification & Audit Ledger**<br>• US-4.1: Handshake confirmation via token, green node transition, & ledger update<br>• *Security*: Replay attack prevention (token single-use), forged token rejection | ✅ 100% | [`Epic4DeliveryVerificationTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic4DeliveryVerificationTest.java)<br>[`Epic4DeliverySecurityAndReplayAttackTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic4DeliverySecurityAndReplayAttackTest.java) |
| **Epic 5** | **FNB Ecosystem & Financial Micro-Escrows**<br>• US-5.1: Automated R150 fuel stipend payout & extended mileage scaling via FNB API<br>• *Audit*: SHA-256 cryptographic hash-chained audit ledger | ✅ 100% | [`Epic5FnbMicroEscrowTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic5FnbMicroEscrowTest.java)<br>[`Epic5FnbCryptographicLedgerIntegrityTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/Epic5FnbCryptographicLedgerIntegrityTest.java) |
| **FNB Commercial** | **Speedpoint & SME Merchant Continuity**<br>• Daily turnover preservation (Mama K's Spaza & York St Laundry)<br>• Merchant survival index and runway tracking | ✅ 100% | [`FnbMerchantResilienceTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/FnbMerchantResilienceTest.java) |
| **Geospatial Math** | **Haversine Geodesic Math & Antipodal Precision**<br>• Coordinate symmetry, 0-distance identity, and 1-degree meridian check | ✅ 100% | [`AdvancedGeospatialMathTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/AdvancedGeospatialMathTest.java) |
| **Multi-Ward** | **Multi-Epicenter Disaster Management**<br>• Concurrent isolation between Ward 18 (George) & Ward 24 (Pacaltsdorp)<br>• Cross-ward mobile bowser dispatching | ✅ 100% | [`MultiWardIncidentEscalationTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/MultiWardIncidentEscalationTest.java) |
| **Controllers & APIs** | **Complete REST API WebMvc Coverage**<br>• FacilityController, ResourceController, Audit & Impact Controllers | ✅ 100% | [`FacilityControllerTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/FacilityControllerTest.java)<br>[`ResourceControllerTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/ResourceControllerTest.java)<br>[`AuditAndImpactControllersTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/AuditAndImpactControllersTest.java) |
| **Concurrency** | **Thread-Safety & Race Condition Resilience**<br>• Parallel thread-safe pledges (never exceeds capacity)<br>• Parallel thread-safe incident deduplication | ✅ 100% | [`ConcurrencyAndThreadSafetyTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/ConcurrencyAndThreadSafetyTest.java) |
| **E2E Demo** | **Full Hackathon Lifecycle Simulation**<br>• Complete automated sequence from pipe burst to FNB payment & ledger | ✅ 100% | [`HackathonEndToEndWinningFlowTest.java`](file:///Users/katlego/work/RippleReady/backend/src/test/java/com/rippleready/HackathonEndToEndWinningFlowTest.java) |

---

## 🚀 Quickstart

### Run All 64 Tests
```bash
mvn test
```

### Run Service Locally
```bash
mvn spring-boot:run
```
- **Interactive Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Health Probe:** `http://localhost:8080/healthz`
- **Tamper-Evident Audit Ledger:** `http://localhost:8080/api/audit/ledger`
