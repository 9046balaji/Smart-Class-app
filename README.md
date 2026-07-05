# SmartClass Android Application

Welcome to the SmartClass Android Application repository! This app is a comprehensive mobile client for the SmartClass ecosystem, designed to provide a rich, interactive, and role-based mobile experience for students, staff, faculty, and administrators. 

Developed with a modern technology stack and adopting the latest Android architecture guidelines, this application handles everything from real-time attendance tracking and geolocation constraints to complex role-based routing and offline-capable data layers.

## 🚀 Key Features

This application encompasses a massive suite of features spanning 26+ distinct screens and flows:

- **🔐 Authentication & Role-Based Access**
  - Secure JWT-based authentication flow with `EncryptedSharedPreferences`.
  - Role-guards for navigating students, staff, and admins to their respective, isolated portals.
  - Token refresh handling via OkHttp Interceptor.
  - Biometric authentication integration.

- **📍 Advanced Geolocation & BLE Constraints**
  - Multi-tier geofencing logic (Campus -> Building -> Classroom).
  - Haversine formula-based distance validation with minimal acceptable GPS accuracy constraints.
  - Bluetooth Low Energy (BLE) peripheral scanning and advertising for localized validations.

- **📊 Comprehensive Dashboards (Overview)**
  - Real-time updates pushed via WebSockets to keep dashboard feeds fresh.
  - Beautiful charts and metric summaries with introductory shimmer loading effects and interactive animations.

- **👩‍🏫 Attendance & Sessions Management**
  - View real-time ongoing sessions and submit attendance records.
  - Scrollable data tables utilizing `LazyRow` and `LazyColumn` for large datasets (e.g., student rosters) with sticky headers.

- **📷 CameraX Scanner**
  - Built-in QR/Barcode scanner leveraging Android `CameraX`.
  - Animated scanning overlay tied to geospatial checks before approval.

- **📅 Timetable Management**
  - Advanced timetable visualization.
  - Excel `.xlsx` workbook parsing directly on-device using Apache POI for bulk uploads and schedule overrides.

- **📋 Approvals, Leaves & On-Duty (OD)**
  - Granular `ApprovalTimeline` for hierarchical multi-step request handling.
  - Interactive UI for staff to request and approve leaves and ODs.

- **🎓 Dedicated Student Portal**
  - Fully isolated navigation graph specifically designed for students, ensuring they do not see staff-centric features.
  - Analytics, compliance tracking, and personalized dashboards.

- **🛠️ Utilities & Auditing**
  - Hierarchy management.
  - Device compliance checks and registration.
  - Detailed audit logs.
  - MOOC (Massive Open Online Course) integrations.

## 🛠️ Technology Stack & Architecture

Built with modern Android Development (MAD) practices to ensure a highly maintainable, scalable, and premium application.

### Core Architecture
- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose (Declarative UI)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) + Clean Architecture (UI -> Domain -> Data layers)
- **Dependency Injection**: Hilt (Dagger)
- **Navigation**: Compose Navigation with distinct `NavGraph` separation (Staff vs. Student).

### Libraries & Frameworks
- **Networking**: Retrofit2 & OkHttp (with WebSockets & Logging Interceptors)
- **Asynchrony**: Kotlin Coroutines & Flow (`StateFlow` for state management)
- **Local Database**: Room persistence library
- **Preferences**: DataStore & EncryptedSharedPreferences
- **Permissions**: Accompanist Permissions
- **Camera**: AndroidX CameraX (camera2, lifecycle, view)
- **Animations**: Lottie for complex vector animations, Konfetti for celebration micro-interactions.
- **Image Loading**: Coil
- **Excel Parsing**: Apache POI
- **Testing**: JUnit4, Espresso, Robolectric, and Roborazzi (snapshot testing).

### UI/UX Design System
- Dark mode default (`#0A0F1E` backgrounds) invoking a sleek, glassmorphism aesthetic.
- Unified `Theme.kt` containing rigid definitions of typography, color schemes, spacing, and shapes.
- Heavy utilization of micro-animations (`animateFloatAsState`, `spring` physics) and Shimmer layouts for loading states rather than simple progress indicators.

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Ladybug (or latest stable)
- JDK 11 (Configured in Gradle)
- Android SDK (minSdk 26, targetSdk 35)

### Steps to Run

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd smartclass
   ```

2. **Environment Variables:**
   - Copy `.env.example` to `.env` in the root directory.
   - Fill in your API Base URLs and configuration keys. The app utilizes the Secrets Gradle Plugin to safely inject these at build time.
   ```bash
   cp .env.example .env
   ```

3. **Open in Android Studio:**
   - Launch Android Studio and open the project directory.
   - Allow Gradle to sync the project dependencies.

4. **Build & Run:**
   - Select a physical device or an emulator running API 26 (Android 8.0) or higher.
   - Click the "Run" button or execute:
   ```bash
   ./gradlew installDebug
   ```

## 🧪 Testing

The repository relies on a mix of unit tests and UI tests:
- **Unit Testing**: Run core domain logic tests (e.g., Geofence validation, JWT parsing, Business UseCases) with:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **UI / Snapshot Testing**: The project employs Roborazzi for validating Compose UI layouts.

## 🛡️ Best Practices & Conventions Used

- **Network Resilience**: Strict error handling ensuring all network failures (Timeout, 5xx, 401, Offline) are caught and handled gracefully in the UI layer (Error Cards, automatic token refresh).
- **Hardwired Access Control**: `RoleGuard` logic to protect unauthorized routes immediately at the navigation graph level.
- **Accessibility (a11y)**: Focus on touch target sizes, `semantics` mappings, and `contentDescription` for a TalkBack-friendly interface.
