function Commit-Path {
    param([string]$Path, [string]$Message)
    
    # Use Invoke-Expression to handle multiple paths if provided
    if ($Path -eq ".") {
        git add .
    } else {
        $paths = $Path -split " "
        foreach ($p in $paths) {
            if (Test-Path $p) {
                git add $p
            }
        }
    }

    # Check if there are staged changes
    $staged = git diff --cached --name-only
    if ($staged) {
        git commit -m $Message
    }
}

Commit-Path "build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat gradle/" "chore: initialize project with root gradle files and configurations"
Commit-Path ".gitignore .env.example AGENTS.md metadata.json task.artifact.md" "chore: setup gitignore, environment configs, and documentation"
Commit-Path "app/build.gradle.kts app/.gitignore app/proguard-rules.pro" "build: configure app-level gradle dependencies and plugins"
Commit-Path "app/src/main/AndroidManifest.xml app/src/main/res/ app/src/main/java/com/vfstr/smartclass/SmartClassApp.kt" "feat: initial android app setup with SmartClassApp and Manifest"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/theme/" "feat: setup basic UI theme and design system tokens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/components/" "feat: implement reusable UI components"
Commit-Path "app/src/main/java/com/vfstr/smartclass/di/" "feat: add dependency injection modules (Hilt setup)"
Commit-Path "app/src/main/java/com/vfstr/smartclass/utils/" "feat: implement base utilities (date formatters, extensions)"
Commit-Path "app/src/main/java/com/vfstr/smartclass/domain/" "feat: setup domain layer base models and use-cases"
Commit-Path "app/src/main/java/com/vfstr/smartclass/data/" "feat: implement data layer core configurations (Retrofit, Room)"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/navigation/" "feat: setup navigation graph base architecture"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/MainViewModel.kt" "feat: implement core ui MainViewModel"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/splash/" "feat: add splash screen feature"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/auth/" "feat: implement authentication flow and roles guard"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/overview/" "feat: implement overview dashboard screen"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/attendance/" "feat: implement attendance management screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/sessions/" "feat: add ongoing sessions monitoring screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/timetable/ app/src/main/java/com/vfstr/smartclass/ui/screens/override/" "feat: add student timetable and override capabilities"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/students/" "feat: implement student list and directory screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/hierarchy/" "feat: add organizational hierarchy views"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/summary/" "feat: implement data summary reports screen"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/compliance/ app/src/main/java/com/vfstr/smartclass/ui/screens/devices/" "feat: implement device compliance checks"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/leaveod/" "feat: add leave and OD (On Duty) management flow"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/notifications/" "feat: implement notification center UI"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/analytics/" "feat: add analytics and reporting screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/studentanalytics/" "feat: implement student-specific analytics"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/studentreports/" "feat: implement comprehensive student reports"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/mooc/" "feat: add MOOC progress tracking screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/scanner/" "feat: implement CameraX-based QR/Barcode scanner"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/studentportal/" "feat: add dedicated student portal navigation"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/users/" "feat: implement user management interface"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/audit/" "feat: implement system audit logs screen"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/profile/" "feat: add user profile and settings screen"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/staff/" "feat: implement staff management features"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/enrollment/" "feat: implement enrollment tracking screens"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/onboarding/" "feat: implement onboarding flow for new users"
Commit-Path "app/src/main/java/com/vfstr/smartclass/ui/screens/ScreenUtils.kt" "feat: setup basic screen utilities"
Commit-Path "app/src/main/java/com/vfstr/smartclass/MainActivity.kt" "chore: wire up main activity and final navigation integration"
Commit-Path "README.md" "docs: update README with massive features breakdown"
Commit-Path "." "chore: finalize initial project setup with all remaining assets"

Write-Host "All commits created successfully!"
