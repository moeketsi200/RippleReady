# 🎥 RippleReady — Hackathon Video Demo Guide & Script
> **FNB App of the Year Hackathon 2026**  
> **Topic 6:** Climate Resilience & Essential Services  
> **Video Target Duration:** 2 Minutes 45 Seconds (Max 3:00)  
> **Resolution:** 1080p (1920x1080) @ 60 FPS  
> **Recommended Tool:** macOS Screen Recording (`Cmd + Shift + 5`) or Loom / OBS Studio

---

## 🎬 Video Recording Checklist (Pre-Flight)

1. **Browser Setup:**
   - Open Chrome or Safari at `http://localhost:3000` (or `http://localhost:8080`).
   - Hide the Bookmarks bar (`Cmd + Shift + B`).
   - Set zoom to 100% (`Cmd + 0`).
   - Keep a second tab open with `http://localhost:3000/slides.html` (for the opening slide).
2. **Audio Setup:**
   - Use a wired headset or external USB microphone (avoid laptop built-in mic if possible).
   - Quiet room, zero background echo.
3. **Terminal Setup (Optional for 10-second tech showcase):**
   - Have a terminal window ready to run `mvn test` to showcase the **64 automated tests passing in 3.5s**.

---

## ⏱️ Video Timeline at a Glance

| Time | Scene | On-Screen Visual Action | Key Message |
| :---: | :--- | :--- | :--- |
| **0:00 – 0:25** | **The Hook & Problem** | Slide 1 & 2 on `slides.html` | Disasters cascade; daycares & clinics run out of water in 2h. |
| **0:25 – 0:50** | **The Solution** | Switch to `http://localhost:3000` | RippleReady intercepts disruptions before they become crises. |
| **0:50 – 1:40** | **Live Interactive Demo** | Demo Scenario &rarr; Crèche &rarr; Pledge Aid &rarr; Handshake Confetti | The complete mutual aid sourcing & resolution cycle. |
| **1:40 – 2:15** | **FNB Rails & Ledger** | Open "Impact Ledger" dialog | R150 Instant Pay fuel payout & SHA-256 cryptographic trail. |
| **2:15 – 2:40** | **Engineering Rigor** | Terminal `mvn test` / Swagger UI | Enterprise Spring Boot 3.3.5, 64 tests, sub-3ms math. |
| **2:40 – 3:00** | **Closing & Vision** | Operations Map (all nodes green) | Scale to national resilience across the FNB App. |

---

## 🎙️ Scene-by-Scene Script & Voiceover

---

### **Scene 1: The Hook & The Cascading Problem (0:00 – 0:25)**
- **Visual:** Open on `http://localhost:3000/slides.html` showing Slide 1 (*Title*), then press `→` to Slide 2 (*The Problem*).
- **Speaker Voiceover (Energetic, Urgent):**
  > *"Judges, when an essential water main ruptures on York Street in George, municipal ticketing systems log a single line item: 'Broken Pipe — Ticket Logged'.*  
  > *But in reality, disasters never happen in isolation. Within two hours, 40 toddlers at a nearby crèche run out of drinking water, frail elderly patients face medical sanitation risks, and township bakeries lose their daily turnover.*  
  > *Municipal repair tankers take 8 to 18 hours to arrive. These facilities run dry in under 120 minutes."*

---

### **Scene 2: The Solution (0:25 – 0:50)**
- **Visual:** Switch browser tab to `http://localhost:3000` (Operations Map).
- **Speaker Voiceover (Confident, Solution-Oriented):**
  > *"Meet **RippleReady**—an autonomous disaster cascade interception and mutual aid engine built for the FNB App of the Year Hackathon.*  
  > *Instead of passive ticket logging, RippleReady models downstream interdependencies and mobilizes neighborhood-level capacity—like private boreholes, church JoJo tanks, and mobile volunteer bowsers—in minutes, before small disruptions trigger community catastrophes."*

---

### **Scene 3: Live Interactive Demo (0:50 – 1:40)**
- **Action 1 (0:50 – 1:05): Trigger Outage**
  - Click **"Demo Scenario"** in the top navigation bar.
  - *Visual:* Watch the **2.0 km red ripple perimeter** render over York Street, George.
  - **Speaker Voiceover:**
    > *"Let’s trigger an active disruption in Ward 18, George. Instantly, our deterministic spatial engine evaluates a 2.0 kilometer ripple zone in under 3 milliseconds, sorting downstream facilities by buffer runway into Life & Care, Food Security, and Commercial Continuity."*

- **Action 2 (1:05 – 1:18): Focus On Threatened Facility**
  - Click on the card **"Little Stars Crèche"** in the right panel.
  - *Visual:* The map smoothly flies directly to the crèche at street level with a glowing highlight ring.
  - **Speaker Voiceover:**
    > *"Little Stars Crèche only has 2 hours of water remaining for 40 toddlers. With one click, the map flies directly to their building."*

- **Action 3 (1:18 – 1:30): Mutual Aid Capacity Match**
  - Click **"Mobilize Mutual Aid"** on the crèche card.
  - In the modal, show the matched resource: **St. Mark's Church Reservoir (5,000L JoJo, 0.22 km away)**.
  - Click **"Lock Capacity & Generate Token"**.
  - **Speaker Voiceover:**
    > *"RippleReady automatically matches the nearest available surplus—in this case, St. Mark's Church JoJo tank just 220 meters away. We lock 500 liters atomically and generate a secure 6-character delivery token."*

- **Action 4 (1:30 – 1:40): Handshake & Confetti Celebration**
  - Click **"Confirm Handshake Token"** on the crèche card.
  - Enter the verification token and click **"Verify & Release FNB Instant Pay"**.
  - *Visual:* **Confetti fires on screen**, and the crèche node transitions to **GREEN (RESOLVED)**.
  - **Speaker Voiceover:**
    > *"When volunteer transport arrives, the school signs off with the token. Instantly, the node transitions to green—problem solved in 18 minutes without waiting for municipal tankers."*

---

### **Scene 4: FNB Financial Rails & Cryptographic Ledger (1:40 – 2:15)**
- **Visual:** Click **"Impact Ledger"** in the top navigation bar to open the modal.
- **Speaker Voiceover (Authoritative, Commercial):**
  > *"Here is where First National Bank’s commercial and social pillars natively integrate:*  
  > *First, **Merchant Turnover Protection:** By prioritizing water continuity to local township bakeries and spaza shops, we safeguard daily Speedpoint POS turnover, preventing SME default risk.*  
  > *Second, **Automated Instant Pay Rails:** The moment delivery is verified, an automated R150 volunteer fuel stipend is disbursed via simulated FNB Instant Pay rails directly to the driver's mobile.*  
  > *And third, **100% Cryptographic Traceability:** Every state change—from burst report to payment—is chained into an immutable SHA-256 audit ledger, giving disaster relief funds and corporate ESG donors fraud-proof verification."*

---

### **Scene 5: Technical Architecture & Test Rigor (2:15 – 2:40)**
- **Visual:** Close the modal and either show the terminal running `mvn test` OR highlight the Swagger OpenAPI docs (`/swagger-ui.html`).
- **Speaker Voiceover (Technical, Rigorous):**
  > *"Under the hood, RippleReady is engineered to FNB enterprise standards. Built with Java 25 LTS and Spring Boot 3.3.5, backed by **64 automated tests covering 100% of acceptance criteria** across all 5 Hackathon Epics.*  
  > *Our spatial math is completely deterministic—zero LLM hallucination in safety-critical disaster routing—with a lightweight, bounded memory footprint running at under 384 megabytes."*

---

### **Scene 6: Call to Action & Conclusion (2:40 – 3:00)**
- **Visual:** Back to the full operations map. Click **"Fit Zone"** in the top-left to zoom out to the full 2.0 km perimeter showing all resolved green pins and active JoJo reserves.
- **Speaker Voiceover (Inspirational, Closing):**
  > *"RippleReady shifts disaster response from passive ticket-logging to proactive cascade interception.*  
  > *From Ward 18 George to every municipality across South Africa, this is how we build climate-resilient essential services directly within the FNB ecosystem.*  
  > *RippleReady: Solve the problem before it spreads. Thank you!"*

---

## 💡 Pro Tips for a Winning Hackathon Video

1. **Energy & Pacing:** Keep your voice upbeat and confident. Don't rush, but keep transitions snappy.
2. **Cursor Smoothness:** Move your mouse deliberately; avoid erratic cursor shaking.
3. **Sound Effects:** The built-in confetti celebration in RippleReady provides a natural visual punch—pause for 1 second right after it fires so the judges appreciate the resolution moment!
4. **Export Format:** Export as MP4 (H.264), 1080p, AAC audio at 192kbps.
