# SmartClass — Security Audit Prompt, Threat Model, and Production Hardening Plan

**How to use this document:** Part 1 is a copy-paste-ready prompt engineered to get a thorough, methodical bug/vulnerability sweep when you run it against your **actual repo** (Claude Code, or this chat with your files uploaded — I don't have your source in this conversation, only the two feature-spec documents you've pasted). Part 2 is the deep spec-level threat model I can do *right now* without the code — every attack path I can reason about from the module descriptions you've given me across both rounds. Part 3 turns both into a single production-hardening program: one main task, ordered subtasks, each with an explicit "done" definition so nothing gets marked complete on a guess.

---

## Part 1 — The Security Audit Prompt

Paste this into Claude Code (or any session with real read access to your FastAPI backend + Kotlin app) exactly as-is, or adapt the `<repo_context>` block to point at your actual paths.

```xml
<role>
You are a senior application security engineer performing a paid third-party penetration-test-style code audit on a production-bound student portal (FastAPI backend + Jetpack Compose Android app). Treat this with the same rigor as a pre-launch security sign-off — assume a motivated attacker will read every line you're reading, and assume this code will hold real students' marks, attendance, fee payments, and PII.
</role>

<repo_context>
Backend: FastAPI, endpoints under /student/* (attendance, marks, results, backlogs, hall-ticket, fees, mooc, od-requests, mentor, change-password, fcm-token).
Frontend: Kotlin + Jetpack Compose Android app, screens: OverviewScreen, AttendanceScreen, MarksScreen, ResultsScreen, ProfileScreen, HallTicketDialog, FeePaymentDialog, ScreenStudentOverview (announcements), ODScreen, CertificatesScreen.
Auth: JWT-based student sessions. Attendance is captured via a foreground BLE scanner sending an "encrypted token" to /student/attendance/check-in.
Fee payment currently uses a simulated gateway that writes balance updates directly — this is explicitly a pre-production stub and needs to be treated as the highest-risk surface.
</repo_context>

<task>
Audit the codebase in the following order. Do not skip ahead — each pass builds context the next pass needs.

<pass_1_recon>
1. List every route/endpoint under /student/* with its HTTP method, auth dependency, and request/response schema.
2. List every Android screen and the endpoints it calls.
3. Identify every place a client-supplied identifier (student_id, subject_code, semester_id, request_id, transaction_id) is used in a database query or file path.
4. Identify every place the app trusts a client-supplied value for something that should be server-computed (amounts, grades, statuses, eligibility flags, timestamps).
Output this as a table before moving to pass 2.
</pass_1_recon>

<pass_2_authn_authz>
For every endpoint from pass 1, answer explicitly:
- Does it verify the JWT signature AND expiry AND that the subject claim matches the resource being accessed (IDOR check)?
- Could student A ever read or write student B's data by changing a path/query parameter or request body field, holding only their own valid token?
- Are there any endpoints reachable without authentication that shouldn't be (e.g. debug routes, health checks that leak data, an unauthenticated hall-ticket PDF endpoint reachable if the URL/token is guessed or leaked)?
- Is there a role check anywhere that only exists in the Android UI (button hidden) but not enforced server-side? Flag every case where a hidden button is the only thing preventing an action.
</pass_2_authn_authz>

<pass_3_business_logic>
Business logic is where this app's real risk lives — a clean auth layer doesn't stop these:
- Attendance: can the /student/attendance/check-in endpoint be called with a replayed or forged token? Is there a time window / one-check-in-per-session-per-device constraint? Is there a "MANUAL OVERRIDE" check-in method — if so, is it role-gated to faculty/admin server-side, not just a value the client can send?
- Marks: can a student write to any marks-related endpoint, even indirectly (e.g. an enrollment or self-assessment endpoint that also happens to touch the marks table)? Is there a locked_at / finalized flag enforced server-side, not just in the UI?
- Hall ticket: is the attendance-eligibility AND fee-clearance gate enforced in the endpoint handler itself (403 on ineligible), not just hidden in the dialog UI? Can the PDF be fetched directly by URL/ID without re-checking eligibility at fetch time (not just at generation time)?
- Fee payment: walk the full lifecycle — amount entry, gateway call, callback/webhook, balance update, receipt generation. At every step, ask "what happens if this request is sent twice?", "what happens if the client sends a negative or fabricated amount?", "what happens if the webhook is spoofed by a third party who knows the endpoint URL?". Flag the simulated gateway explicitly as unsafe for any real transaction and confirm there's a feature flag or environment check preventing it from running against a production fee balance.
- OD/Leave: can a student set their own request status to APPROVED via a crafted request, bypassing whatever approval workflow exists? Does "Smart Session Resolution" trust any client-supplied date/session data that should instead be looked up server-side from the timetable?
- MOOC credits: is there any upper bound / validation on credits or platform values submitted at enrollment, preventing a student from fabricating enough credits to appear to meet the R22 requirement?
</pass_3_business_logic>

<pass_4_mobile_specific>
- Where is the JWT stored on-device? Flag plain SharedPreferences; require EncryptedSharedPreferences or Android Keystore-backed storage.
- Is there certificate pinning on the API client? If not, flag as vulnerable to MITM via a malicious CA/proxy on a compromised network (relevant for a campus WiFi environment).
- Are there exported Activities/Services/BroadcastReceivers in the manifest that don't need to be exported? Specifically check the BLE foreground service and any deep-link handlers.
- Is cleartext HTTP traffic disabled (`android:usesCleartextTraffic="false"`, network security config present)?
- Is there root/emulator detection, and if not, is that an accepted risk given the BLE-attendance anti-spoofing goal (a rooted device is the most likely vector for GPS/BLE spoofing tools)?
- Are API keys, backend URLs, or signing secrets hardcoded in the APK? Decompile-check (or reason from source) for anything that should be server-side-only.
</pass_4_mobile_specific>

<pass_5_infra_and_data>
- SQL/NoSQL injection: any raw string-formatted queries instead of parameterized queries/ORM calls?
- Rate limiting: is there any on login, password change, hall-ticket generation, payment endpoints? Absence here enables brute force and abuse.
- Logging: does any log line contain a password, full JWT, card/UPI details, or full marks/results payloads? PII in logs is itself a breach surface.
- Dependency check: list backend (requirements.txt/pyproject) and Android (build.gradle) dependencies with known CVEs at their pinned versions.
- CORS configuration: is it wide open (`*`) anywhere it shouldn't be, especially on cookie/JWT-bearing endpoints?
- Secrets management: are DB credentials, JWT signing keys, and (eventual) payment gateway keys in environment variables / a secrets manager, or committed to the repo?
</pass_5_infra_and_data>

<output_format>
For every finding, give:
1. Severity (Critical/High/Medium/Low, using OWASP risk-rating style: likelihood x impact)
2. Exact file + line reference
3. One-sentence exploit scenario ("an attacker could...")
4. The minimal fix (code diff or precise description)
Group findings by pass. After all 5 passes, do a second full read-through of only the Critical and High findings to confirm each is real and not a false positive — state explicitly "confirmed on re-read" or "downgraded after re-read, here's why" for each.
End with a prioritized fix order.
</output_format>
</task>
```

**Why this prompt is structured this way:** it forces sequential passes (recon → authn/authz → business logic → mobile → infra) so nothing gets skipped by jumping straight to "look for bugs" — business logic flaws (the OD self-approval case, the fee race condition) are exactly the class of bug generic "find vulnerabilities" prompts miss, because they're not pattern-matchable the way SQLi or hardcoded secrets are. The explicit re-read/confirm step at the end is there because a single pass tends to over-report speculative findings; asking for a second, skeptical pass on just the high-severity items is what separates a real audit from a keyword-matched list.

---

## Part 2 — Deep Threat Model (spec-level, done now, no code access)

This is everything I can reason about from the two feature documents you've given me across both rounds. Treat every item below as **"verify in code"** rather than **"confirmed bug"** — I'm reasoning from feature descriptions, not reading FastAPI route handlers.

### 2.1 Authentication & Session
| Threat | Why it matters here | What "safe" looks like |
|---|---|---|
| JWT never expires / long TTL | Biometric app-lock is cosmetic if the underlying token outlives it — a stolen/extracted token works indefinitely | Short-lived access token (15–30 min) + refresh token flow, refresh revoked on password change |
| No re-validation after biometric unlock | The lock guards UI, not the API | Re-check token validity/expiry on every resume, not just on cold start |
| Password change endpoint lacks rate limiting | Brute-force of current-password field | Rate limit + account lockout after N failed attempts |
| Password reset (if it exists) via predictable token | Account takeover | Cryptographically random, single-use, time-boxed reset tokens |

### 2.2 BLE Attendance Check-in — your highest-value, highest-risk feature
This is the feature that makes your app worth using over the official apps, so it's also the one worth attacking hardest before shipping:
- **Beacon spoofing**: anyone with a BLE broadcaster app can advertise the same UUID/major/minor as the Vignan classroom beacon from anywhere. If the server trusts "device saw beacon X" alone, remote/proxy attendance is trivial. Mitigation: rotate a server-issued nonce into the beacon's advertised payload on a short cycle, so a static clone doesn't work.
- **Replay attack**: if the "encrypted token" sent to `/student/attendance/check-in` isn't bound to a single use, a captured token (from network traffic, e.g. on shared campus WiFi) can be replayed. Mitigation: single-use nonce, checked and invalidated server-side atomically.
- **Relay/proxy attack**: a phone physically in the room relays the BLE signal (via a live network connection) to a phone elsewhere. This defeats even a rotating nonce, because the relayed data is real-time-valid. Mitigation is genuinely hard (this is the same class of problem as passive-keyless-entry relay attacks on cars) — realistic mitigation is a secondary signal: require the check-in also match expected WiFi BSSID (campus AP) or a loose GPS geofence as a second factor, not to prove presence alone but to raise the cost of a relay attack.
- **Manual override abuse**: flagged in the v2 review — confirm this is faculty-role-gated server-side.

### 2.3 IDOR (Insecure Direct Object Reference) — across every module
Every one of these ten modules pulls data by some ID (student_id, subject_code, semester_id, request_id, transaction_id, course enrollment id). The single most common real-world vulnerability class in student-portal-style apps is: **student A's valid token can view/edit student B's record by changing the ID in the request.** This needs to be checked exhaustively, endpoint by endpoint:
- `/student/marks` — can I fetch another student's marks by ID?
- `/student/results` — same
- `/student/attendance` — same
- `/student/od-requests` — can I view or, worse, approve/reject someone else's OD request?
- `/student/mooc` — can I edit another student's enrollment?
- `/student/hall-ticket` — can I fetch another student's hall ticket PDF?
- `/student/fees` — can I view another student's dues, or worse, submit a payment against another student's account?
The fix is uniform: every handler must derive the "whose data is this" identity from the **JWT subject claim**, never from a client-supplied ID, or must explicitly verify the two match before proceeding.

### 2.4 Payment Flow — currently the single biggest production risk
Confirmed already as a simulated gateway. Beyond "replace with a real gateway," the specific things that go wrong in real fee-payment systems:
- **Idempotency**: a network blip causes the client to retry a payment POST; without an idempotency key, this can double-charge or double-record a receipt.
- **Webhook trust**: once a real gateway (Razorpay/PayU/CCAvenue) is wired in, the payment-confirmed webhook must be signature-verified using the gateway's shared secret — otherwise anyone who knows your webhook URL can POST a fake "payment succeeded" event and zero out their fee dues for free. This is the single most common real-world fee-portal exploit in Indian ed-tech/university systems.
- **Race condition on balance updates**: two near-simultaneous requests reading balance, then writing balance - X, can both succeed and double-deduct or corrupt the ledger. Needs a DB-level transaction with row locking (`SELECT ... FOR UPDATE`) or an atomic decrement, not read-then-write application logic.
- **Receipt ID generation**: must be server-side, post-confirmation, not generated client-side or pre-confirmation (flagged in v2 review — repeating here because it's this important).

### 2.5 Hall Ticket
- Double-gate (attendance + fees) must be enforced at the **fetch** endpoint, not only at a separate "generate" step — otherwise a student who becomes ineligible after a hall ticket was once generated (e.g. attendance drops after a late-recorded absence, or a fee due appears) could still re-download a stale ticket.
- PDF generation: if using any template engine that accepts student-controlled input (name fields, subject names) into a server-side rendering pipeline, check for template injection (SSTI) — low likelihood here since these are typically DB-sourced fixed fields, but worth a quick check if any of it is free-text.
- Share Slip: confirm it shares an actual PDF/image attachment via `Intent.ACTION_SEND`, not a text template with roll number/DOB pasted as plain text — that's a PII-leak vector into whatever app receives the share (WhatsApp groups, etc. sometimes get forwarded).

### 2.6 Marks & Results
- Confirm no endpoint lets a student write internal-marks or grade data, even indirectly through an endpoint meant for something else (e.g. a shared "update subject enrollment" endpoint that has an unrelated marks field left mutable).
- `locked_at` must be enforced server-side on every write path that could touch marks, including any bulk/batch import path faculty might use — don't just gate the one obvious student-facing endpoint.

### 2.7 Announcements Board
- If announcement content is rendered as HTML/Markdown from a server field, and if that field can ever be populated from anything other than a trusted admin panel, check for stored XSS (unlikely in Compose since it's not a WebView, but worth confirming there's no WebView-based rendering anywhere in this screen).

### 2.8 Infrastructure-level (applies across everything above)
- TLS everywhere (no cleartext), HSTS on the backend if it's also served to any web client.
- Rate limiting on all write endpoints, not just login.
- Structured audit logging (who did what, when) — especially for OD approvals, marks changes, and fee-balance changes — this is what you'll actually need if a dispute ever comes up ("I paid, the system says I didn't").
- Automated dependency scanning (`pip-audit`/`safety` for the FastAPI backend, Gradle dependency check for Android) as a CI step, not a one-time manual check.

---

## Part 3 — Production Hardening Program: Main Task + Subtasks

### MAIN TASK
**"SmartClass Security Hardening & Production Readiness"** — bring every module from prototype-grade to production-safe, with server-side enforcement of every business rule, a real (not simulated) payment path, and a verified audit trail. Definition of done: every subtask below is checked off **and** re-verified in a second pass (per the Part 1 prompt's own methodology — don't mark a subtask done off a single read).

Work top-to-bottom; phases 1–3 are blocking (don't start feature work again until they're done), phases 4+ can interleave with normal development.

---

#### Phase 1 — Auth & Session Hardening
- [ ] 1.1 Add/verify JWT expiry (≤30 min access token) + refresh token flow
- [ ] 1.2 Invalidate all refresh tokens on password change
- [ ] 1.3 Re-validate token on app resume from background, not just cold start, so biometric lock isn't cosmetic
- [ ] 1.4 Rate-limit `/student/change-password` (current-password brute force) and login
- [ ] 1.5 Confirm password reset (if present) uses single-use, time-boxed, cryptographically random tokens

#### Phase 2 — BLE Attendance Anti-Spoofing
- [ ] 2.1 Confirm/implement single-use nonce per check-in token, invalidated atomically server-side
- [ ] 2.2 Add short-cycle rotation to the beacon-derived token so a static clone stops working
- [ ] 2.3 Add a secondary weak signal (campus WiFi BSSID match, or loose geofence) to raise relay-attack cost
- [ ] 2.4 Confirm `MANUAL OVERRIDE` check-in path is role-checked server-side against faculty/admin claim, not just hidden in student UI
- [ ] 2.5 Add per-session-per-device check-in rate limit (one successful check-in per scheduled class period)

#### Phase 3 — Authorization / IDOR Sweep (every endpoint, no exceptions)
- [ ] 3.1 `/student/attendance*` — identity derived from JWT subject, never from request param
- [ ] 3.2 `/student/marks*` — same, plus confirm no write path exists for students
- [ ] 3.3 `/student/results*` — same
- [ ] 3.4 `/student/od-requests*` — same, plus confirm students cannot self-approve
- [ ] 3.5 `/student/mooc*` — same, plus validate platform/credits against an allow-list, not free text
- [ ] 3.6 `/student/hall-ticket*` — same, plus re-check eligibility at fetch time, not just at generation time
- [ ] 3.7 `/student/fees*` — same, plus confirm no student can write another student's balance
- [ ] 3.8 `/student/mentor`, `/student/fcm-token` — same pattern check even on "low risk" endpoints

#### Phase 4 — Payment Security (before any real money moves)
- [ ] 4.1 Replace simulated gateway with real integration (Razorpay/PayU/CCAvenue) behind a feature flag until fully verified
- [ ] 4.2 Verify gateway webhook signatures using the shared secret; reject unsigned/invalid callbacks
- [ ] 4.3 Add idempotency keys on payment-initiation requests
- [ ] 4.4 Move balance updates to a DB transaction with row-level locking (`SELECT ... FOR UPDATE`) or atomic decrement
- [ ] 4.5 Generate receipt IDs server-side, only after gateway confirmation, never client-side or pre-confirmation
- [ ] 4.6 Add minimum/allowed partial-payment amount validation matching university policy

#### Phase 5 — Business Logic Gating (server-enforced everywhere)
- [ ] 5.1 Hall ticket: attendance + fee gate enforced in the handler, re-checked on every fetch
- [ ] 5.2 Marks: `locked_at` enforced on every write path (including any faculty batch-import path)
- [ ] 5.3 Marks: explicit "not yet graded" state, distinct from zero, for ungraded components
- [ ] 5.4 OD: explicit handling for a requested date with no scheduled session (reject/no-op/record — pick one, make it explicit)
- [ ] 5.5 Results: explicit handling for Absent/Malpractice grade states if the regulation includes them, not forced into nearest letter grade

#### Phase 6 — Mobile App Hardening
- [ ] 6.1 Move JWT storage to EncryptedSharedPreferences or Keystore-backed storage
- [ ] 6.2 Add certificate pinning on the API client
- [ ] 6.3 Audit manifest for unnecessarily exported components (BLE service, any deep links)
- [ ] 6.4 Disable cleartext traffic (`usesCleartextTraffic="false"`, network security config)
- [ ] 6.5 Decide and implement root/emulator detection stance (recommended given BLE anti-spoofing goals)
- [ ] 6.6 Confirm no hardcoded secrets/API keys in the APK; enable ProGuard/R8 obfuscation for release builds
- [ ] 6.7 Confirm Hall Ticket "Share Slip" shares a document attachment, not a plain-text PII template

#### Phase 7 — Infrastructure & Data
- [ ] 7.1 Confirm all queries are parameterized/ORM-based, no string-formatted SQL
- [ ] 7.2 Add rate limiting on all write endpoints, not just login
- [ ] 7.3 Audit logs for PII/secrets leakage (passwords, full JWTs, card/UPI data, full marks payloads) — scrub
- [ ] 7.4 Run dependency vulnerability scan (backend + Android), fix or pin around known CVEs
- [ ] 7.5 Lock down CORS to actual known origins, not wildcard, especially on JWT-bearing endpoints
- [ ] 7.6 Move all secrets (DB creds, JWT signing key, gateway keys) to environment variables/secrets manager, confirm none committed to repo history

#### Phase 8 — Verification (do not skip)
- [ ] 8.1 Run the Part 1 audit prompt against the real codebase end-to-end
- [ ] 8.2 Second, skeptical pass on every Critical/High finding — confirm or downgrade explicitly
- [ ] 8.3 Manual exploit attempt on the 3 highest-risk items (BLE replay, IDOR on fees, payment webhook spoof) in a staging environment
- [ ] 8.4 Load/race-condition test on the payment balance-update path specifically

#### Phase 9 — Sign-off
- [ ] 9.1 Document every fix with before/after, so there's a record for any future audit or compliance review
- [ ] 9.2 Re-confirm each Phase 1–7 item against the actual merged code, not the plan — a checked box here should mean "verified in the diff," not "was on the list"

---

**Work order recommendation:** Phases 1–3 first (they're foundational — payment and mobile hardening don't matter much if auth/IDOR is broken underneath). Phase 4 next since it's the highest real-world financial risk. Phases 5–7 can run in parallel with normal feature work. Phase 8 is mandatory before calling anything "production ready," and Phase 9 is what actually makes it defensible if anyone ever asks "how do you know this is secure."
