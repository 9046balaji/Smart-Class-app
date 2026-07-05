You must implement **ALL 27 screens** listed in Part 1. Do not decide any screen is "too minor" or "can be combined." Every screen from the web app has a mobile equivalent and must be built with full fidelity. If you run into context limits, continue in the next response — never silently drop a screen.

---

### RULE 2 — NEVER USE PLACEHOLDER UI

Do not write `// TODO: implement this`, `Text("Coming soon")`, placeholder cards, or empty Composables. Every screen must be fully implemented with:
- Real data binding to ViewModel StateFlow
- Real API calls via Repository pattern
- Real loading states (shimmer skeletons)
- Real error states (error card + retry button)
- Real empty states (illustrated empty state with message)

---

### RULE 3 — ANIMATIONS ARE MANDATORY, NOT OPTIONAL

Do not skip animations to "save time." Every animation listed in the PREMIUM ANIMATIONS CHECKLIST must be present. If a Composable exists without its specified animation, it is incomplete. Animations must use:
- `animateFloatAsState` / `animateIntAsState` for value transitions
- `AnimatedVisibility` for show/hide
- `spring()` physics for natural motion
- `Crossfade` for tab content transitions
- Lottie for complex loading animations
- Custom Canvas drawing for particle effects

---

### RULE 4 — ROLE-BASED ACCESS IS HARDWIRED

Never show a screen to a role that should not see it. The `ROLE_PAGES` mapping from the web app must be replicated exactly in the Android `RoleGuard` utility. Navigation items must be filtered per role before rendering. Route guards must redirect unauthorized attempts to an "Unauthorized" screen.

---

### RULE 5 — API CONTRACT IS FIXED — DO NOT INVENT ENDPOINTS

All API endpoints are defined in Part 1. Do not invent new endpoints. Do not change endpoint paths. Do not change request/response shapes. If an endpoint is not in the list, do not call it. Every Retrofit interface must match the exact paths defined.

---

### RULE 6 — JWT AUTH FLOW IS NON-NEGOTIABLE

- Token must be stored in `EncryptedSharedPreferences` backed by Android Keystore
- Every API request must include `Authorization: Bearer {token}` header via OkHttp `Interceptor`
- On 401 response: auto-refresh using `/auth/refresh` in an `Authenticator`
- On refresh failure: emit a global logout event → navigate to Login screen
- Student token and staff token must be stored separately (different keys)
- Token expiry check must happen on app resume (not just on 401)

---

### RULE 7 — GEOFENCING MUST MATCH WEB LOGIC EXACTLY

The Android geofencing must replicate the exact 3-tier logic from `geofence.ts`:
- Tier 1: Point-in-polygon test against VFSTR campus polygon vertices (copy coordinates exactly)
- Tier 2: Distance to building centroids (copy building definitions exactly)
- Tier 3: Classroom GPS radius + optional WiFi BSSID cross-check
- MAX_ACCEPTABLE_GPS_ACCURACY_M = 30 meters — reject lower-accuracy positions
- Use Haversine formula for distance calculations (same as web)

---

### RULE 8 — DESIGN SYSTEM MUST BE CONSISTENT ACROSS ALL SCREENS

Define a single `Theme.kt` file and a `DesignSystem.kt` object at project start. Every Composable must draw colors, typography, spacing, and corner radii ONLY from these definitions. No hardcoded hex values in individual Composables. Use the exact colors defined in Part 1.

---

### RULE 9 — ARCHITECTURE LAYERS MUST BE RESPECTED

Strictly follow:
```
UI Layer (Composables, ViewModels)
  ↓
Domain Layer (UseCases, Models)
  ↓
Data Layer (Repositories, Remote Data Sources, Local Data Sources)
```
- ViewModels must NOT call Retrofit directly
- Composables must NOT call ViewModels' suspend functions directly — use events
- Repositories must NOT contain UI logic
- UseCases must contain single-responsibility business logic

---

### RULE 10 — ERROR HANDLING IS MANDATORY ON EVERY NETWORK CALL

Every API call must handle:
- **Network timeout** → "No connection. Tap to retry." error card
- **Server 5xx** → "Server error. Please try again." error card
- **Auth 401** → trigger auto-refresh or logout flow
- **Validation 422/400** → parse `{ detail: string }` and show specific error
- **Not found 404** → appropriate empty state (not a crash)
- **No network** → show cached data if available + offline banner

Never let an unhandled exception propagate to the UI as a crash.

---

### RULE 11 — LOADING STATES MUST USE SHIMMER SKELETONS

When any data is loading:
- Show shimmer skeleton that matches the shape of the content
- Use `Brush.linearGradient` with animated offset for shimmer effect
- Never show a blank white/black screen during loading
- Never show just a CircularProgressIndicator in the center of a full screen

---

### RULE 12 — STUDENT PORTAL IS A COMPLETELY SEPARATE NAVIGATION GRAPH

The Student Portal is not part of the staff nav graph. It must be a separate `NavGraph` with its own bottom navigation bar. Students routed to `/student/portal` must NEVER see sidebar items, staff navigation, or any admin/faculty screen. Implement this as a distinct navigation subtree in the NavHost.

---

### RULE 13 — REAL-TIME UPDATES MUST WORK

The Overview screen and Sessions screen must receive live updates via WebSocket:
- Implement `OkHttp WebSocket` in a long-running `CoroutineScope`
- Parse incoming JSON attendance events
- Update `StateFlow` in ViewModel
- Activity feed must auto-update without manual refresh

---

### RULE 14 — CAMERA/SCANNER MUST USE CAMERAX

- Use `CameraX` library with `ImageCapture` use case
- Camera preview in a `Box` composable using `AndroidView` wrapping `PreviewView`
- Geolocation must be checked BEFORE allowing any scan
- Scan result overlay must appear as an animated card overlay on top of camera preview
- Handle all camera permissions gracefully (request flow with rationale)

---

### RULE 15 — DATA TABLES MUST SUPPORT HORIZONTAL SCROLL WITH STICKY COLUMNS

For Summary, Attendance, Audit, and Notification tables:
- Use a `LazyRow` inside a `LazyColumn` or custom `Canvas`-drawn table
- First 1–2 columns (Name, Roll No) must remain sticky while user scrolls horizontally
- Header row must remain sticky while user scrolls vertically
- Each cell must be uniformly sized per column

---

### RULE 16 — CHARTS MUST ANIMATE ON FIRST APPEARANCE

All charts (bar, area, radar, scatter, donut) must:
- Animate from zero on first composition
- Use `LaunchedEffect` to trigger animation after data loads
- Use easing: `FastOutSlowInEasing` for bars, `LinearEasing` for continuous lines
- Show a shimmer skeleton while chart data is loading

---

### RULE 17 — TIMETABLE UPLOAD MUST PARSE EXCEL

- Use `Apache POI` for Excel file parsing on Android
- Must parse the exact same structure that the web's `timetableWorkbookLoader.ts` handles
- Preview parsed rows in a scrollable table BEFORE allowing confirm/upload
- Validate required columns; show specific error for missing columns

---

### RULE 18 — APPROVAL TIMELINE MUST BE A REUSABLE COMPOSABLE

The `ApprovalTimeline` component (used in Leave & OD) must be a standalone reusable Composable:
- Takes a list of `ApprovalStep` objects
- Renders vertical stepper with: step title, role label, actor, timestamp, comment
- Step states: `COMPLETE` (green fill), `PENDING` (grey hollow), `REJECTED` (red fill)
- Must work for both OD requests and Condonation requests

---

### RULE 19 — ALL DATE/TIME FORMATTING MUST BE CONSISTENT

- All timestamps from API are ISO-8601 strings — parse with `java.time` (API 26+) or `ThreeTenABP` for lower APIs
- Display format: `dd MMM yyyy, hh:mm a` (example: 22 May 2026, 10:30 AM)
- Date-only: `dd MMM yyyy`
- Time-only: `hh:mm a`
- Relative time (activity feed): "2 min ago", "just now" — use a utility function

---

### RULE 20 — MINIMUM API LEVEL IS 26 (Android 8.0)

- `minSdk = 26`
- `targetSdk = 35`
- `compileSdk = 35`
- Do not use deprecated APIs
- Use `java.time` package (available from API 26) — no `java.util.Date`
- EncryptedSharedPreferences from `androidx.security.crypto` — available from API 23

---

### RULE 21 — ACCESSIBILITY MUST NOT BE IGNORED

Every interactive Composable must have:
- `contentDescription` on icons
- `semantics` block where needed
- Minimum touch target size: 48dp × 48dp
- Color contrast ratio: 4.5:1 minimum for text
- Support `TalkBack` — test with screen reader

---

### RULE 22 — DEPENDENCY INJECTION VIA HILT ONLY

- All ViewModels injected with `@HiltViewModel`
- All Repositories injected at constructor
- All Retrofit services injected as singletons
- Do NOT instantiate dependencies manually (`= SomeRepository()` in ViewModel is forbidden)
- Application class must be annotated `@HiltAndroidApp`

---

### RULE 23 — DARK MODE IS THE DEFAULT AND ONLY THEME

The Android app uses the **dark theme** (`#0A0F1E` background) as default — the inverse of the web app's light mode. The web app was forced light for desktop; the mobile app should embrace the dark glassmorphism aesthetic. Implement a `DarkColorScheme` only (no light variant unless specifically requested). The Student Portal has an optional light/dark toggle which must work.

---

### RULE 24 — PACKAGE STRUCTURE MUST BE ORGANIZED

```
com.vfstr.smartclass/
├── di/                    # Hilt modules
├── data/
│   ├── remote/
│   │   ├── api/           # Retrofit interfaces
│   │   ├── dto/           # Response DTOs
│   │   └── interceptors/  # Auth, logging
│   ├── local/
│   │   ├── db/            # Room database, DAOs, entities
│   │   └── preferences/   # DataStore, EncryptedSharedPreferences
│   └── repositories/      # Repository implementations
├── domain/
│   ├── models/            # Domain models (NOT DTOs)
│   ├── repositories/      # Repository interfaces
│   └── usecases/          # One class per use case
├── ui/
│   ├── theme/             # Colors, Typography, Theme.kt
│   ├── components/        # Reusable Composables
│   ├── navigation/        # NavGraph, Routes
│   └── screens/
│       ├── auth/
│       ├── overview/
│       ├── attendance/
│       ├── sessions/
│       ├── override/
│       ├── students/
│       ├── hierarchy/
│       ├── timetable/
│       ├── summary/
│       ├── devices/
│       ├── compliance/
│       ├── leaveod/
│       ├── notifications/
│       ├── analytics/
│       ├── studentanalytics/
│       ├── studentreports/
│       ├── mooc/
│       ├── scanner/
│       ├── studentportal/
│       ├── users/
│       └── audit/
└── utils/
    ├── geofence/
    ├── jwt/
    ├── formatters/
    └── extensions/
```

---

### RULE 25 — TEST COVERAGE IS REQUIRED FOR CORE LOGIC

The following must have unit tests:
- `GeofenceUtils` (polygon test, Haversine formula)
- `JwtUtils` (decode role from token)
- `AttendanceRepository` (mock Retrofit, verify mapping)
- `AuthViewModel` (login success, failure, token storage)
- `ComplianceUseCase` (eligibility calculation)
- All formatters (date, percentage, attendance)

---

### FINAL CHECKLIST BEFORE SUBMITTING ANY SCREEN

Before marking any screen as done, verify:
- [ ] Loading state: shimmer skeleton shown
- [ ] Error state: error card with retry
- [ ] Empty state: illustrated message
- [ ] All animations present and working
- [ ] Role guard applied correctly
- [ ] API called through Repository (not direct)
- [ ] No hardcoded colors outside Theme.kt
- [ ] All interactive elements have contentDescription
- [ ] No TODO comments in submitted code
- [ ] Navigation transitions are animated

---

*Document generated from full source analysis of SmartClass Dashboard v2.1.0 (frontend.zip)*
*University: VFSTR, Vadlamudi, Guntur – 522213, Andhra Pradesh*
*Generated: May 2026*
