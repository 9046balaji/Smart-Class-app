package com.vfstr.smartclass.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.local.db.*
import com.vfstr.smartclass.data.preferences.AppPreferencesRepository
import com.vfstr.smartclass.data.preferences.SecurePreferences
import com.vfstr.smartclass.data.repositories.AppRepository
import com.vfstr.smartclass.data.remote.api.*
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.ui.navigation.Navigation
import com.vfstr.smartclass.utils.geofence.LocationHelper
import com.vfstr.smartclass.utils.geofence.WifiHelper
import com.vfstr.smartclass.utils.DndManager
import com.vfstr.smartclass.utils.geofence.GeofenceUtils
import com.vfstr.smartclass.utils.network.ConnectivityMonitor
import com.vfstr.smartclass.utils.security.BiometricAuthManager
import com.vfstr.smartclass.utils.security.JwtUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import javax.crypto.Cipher
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppRepository,
    private val securePrefs: SecurePreferences,
    private val appPrefs: AppPreferencesRepository,
    private val biometricAuthManager: BiometricAuthManager,
    private val connectivityMonitor: ConnectivityMonitor,
    private val savedStateHandle: SavedStateHandle,
    private val wifiHelper: WifiHelper,
    private val locationHelper: LocationHelper,
    private val dndManager: DndManager
) : ViewModel() {

    // Auth State Flows
    val currentRole = MutableStateFlow<UserRole?>(null)
    val currentUserName = MutableStateFlow("")
    val authLoading = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)
    val isLoggedIn = MutableStateFlow(false)

    // Current navigation state
    val currentRoute = MutableStateFlow(savedStateHandle.get<String>("current_route") ?: Navigation.ROUTE_SPLASH)

    // Cache Stats Flow (Overview stats)
    val dashboardStats = MutableStateFlow<CachedStatsEntity?>(null)
    val statsLoading = MutableStateFlow(false)
    val statsError = MutableStateFlow<String?>(null)

    // Data lists Flow
    val students = MutableStateFlow<List<Student>>(emptyList())
    val studentsLoading = MutableStateFlow(false)
    val studentsError = MutableStateFlow<String?>(null)

    val attendanceEvents = MutableStateFlow<List<AttendanceEvent>>(emptyList())
    val eventsLoading = MutableStateFlow(false)

    val classSessions = MutableStateFlow<List<ClassSession>>(emptyList())
    val sessionsLoading = MutableStateFlow(false)

    val timetableSlots = MutableStateFlow<List<TimetableSlot>>(emptyList())
    val timetableLoading = MutableStateFlow(false)

    val odRequests = MutableStateFlow<List<ODRequest>>(emptyList())
    
    // Audit & Notification Logs (Rule 2)
    val auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val notificationLogs = MutableStateFlow<List<String>>(emptyList())
    val guardians = MutableStateFlow<List<List<String>>>(emptyList())
    
    // Scanner Overlay & coordinates states (RULE 20/21)
    val scanningGeoStatus = MutableStateFlow("LOADING") // LOADING, INSIDE, OUTSIDE
    val scanResultOverlay = MutableStateFlow<String?>(null) // SUCCESS: info, ERROR: text, or null
    val lastScannedStudent = MutableStateFlow<Student?>(null)

    // Excel import preview containers
    val timetablePreviewList = MutableStateFlow<List<TimetableSlot>>(emptyList())
    val studentPreviewList = MutableStateFlow<List<Student>>(emptyList())
    
    // Student Portal States (RULE 12 / 21)
    val studentProfile = MutableStateFlow<Student?>(null)
    val cgpaAnimated = MutableStateFlow(0.0)
    
    // Student Portal Expanded States
    val studentEligibility = MutableStateFlow<StudentEligibilityDto?>(null)
    val studentAttendanceReport = MutableStateFlow<List<StudentAttendanceRecordDto>>(emptyList())
    val attendanceFilterFrom = MutableStateFlow<String?>(null)
    val attendanceFilterTo = MutableStateFlow<String?>(null)
    val studentODRequests = MutableStateFlow<List<ODRequestDto>>(emptyList())
    val studentMOOCEnrollments = MutableStateFlow<List<MOOCEnrollmentDto>>(emptyList())
    val studentMarks = MutableStateFlow<List<StudentMarksDto>>(emptyList())
    val semesterResults = MutableStateFlow<List<SemesterResultDto>>(emptyList())
    val selectedMarksSemester = MutableStateFlow<String>("SEM-5")
    val studentBacklogs = MutableStateFlow<BacklogsSummaryDto?>(null)
    val studentMentor = MutableStateFlow<MentorDto?>(null)
    val isSubmittingOD = MutableStateFlow(false)
    val isEnrollingMOOC = MutableStateFlow(false)
    val isPasswordChanging = MutableStateFlow(false)
    val passwordChangeSuccess = MutableStateFlow<Boolean?>(null)
    val odSubmitSuccess = MutableStateFlow<Boolean?>(null)
    val moocEnrollSuccess = MutableStateFlow<Boolean?>(null)
    
    // Profile & Settings States
    val staffProfile = MutableStateFlow<User?>(null)
    val isDarkTheme = MutableStateFlow(true)
    val localCgpa = MutableStateFlow(0.0f)
    val localLastYearSubjects = MutableStateFlow("")
    val notifAttendance = MutableStateFlow(true)
    val notifDefaulter = MutableStateFlow(true)
    val notifCompliance = MutableStateFlow(true)
    val biometricLockEnabled = MutableStateFlow(false)
    val activeRoleContext = MutableStateFlow<UserRole?>(null)
    val isDataSaverEnabled = MutableStateFlow(false)
    val isDndAutomationEnabled = MutableStateFlow(false)
    val hasBiometricToken = MutableStateFlow(securePrefs.hasBiometricToken())
    val geoTier1 = MutableStateFlow(false)
    val geoTier2 = MutableStateFlow(false)
    val geoTier3 = MutableStateFlow(false)
    val geoMessage = MutableStateFlow("Initializing location...")
    val isMockLocation = MutableStateFlow(false)
    val syncQueueCount = MutableStateFlow(0)
    val localDbSize = MutableStateFlow(0L)

    // BLE Radar State
    val isBleRadarActive = MutableStateFlow(false)
    val isScanningForBeacon = MutableStateFlow(false)

    // Network State
    val isOnline = connectivityMonitor.isConnected.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var _prevLat: Double? = null
    private var _prevLon: Double? = null
    private var _prevTime: Long? = null

    private var locationTrackingJob: Job? = null

    fun startGlobalLocationTracking() {
        locationTrackingJob?.cancel()
        
        // Check permissions before starting to avoid SecurityException/ANR
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasFine && !hasCoarse) {
            geoMessage.value = "Location permission not granted. Verification disabled."
            return
        }

        locationTrackingJob = viewModelScope.launch {
            try {
                locationHelper.getLocationUpdates().collect { location ->
                    val currentTime = System.currentTimeMillis()
                    val bssid = wifiHelper.getCurrentBssid()
                    
                    // Heavy geofencing logic off-thread
                    val result = withContext(kotlinx.coroutines.Dispatchers.Default) {
                        GeofenceUtils.verifyLocation(
                            lat = location.latitude,
                            lon = location.longitude,
                            accuracy = location.accuracy,
                            bssid = bssid,
                            prevLat = _prevLat,
                            prevLon = _prevLon,
                            prevTime = _prevTime,
                            currentTime = currentTime,
                            isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                location.isMock
                            } else {
                                @Suppress("DEPRECATION")
                                location.isFromMockProvider
                            }
                        )
                    }
                    
                    geoTier1.value = result.tier1
                    geoTier2.value = result.tier2
                    geoTier3.value = result.tier3
                    geoMessage.value = result.message
                    isMockLocation.value = result.isMock
                    
                    if (result.success) {
                        _prevLat = location.latitude
                        _prevLon = location.longitude
                        _prevTime = currentTime
                    }
                }
            } catch (e: Exception) {
                geoMessage.value = "Location tracking paused: ${e.message}"
                // Retry after a delay if it's a security exception, 
                // assuming user might grant it later
                if (e is SecurityException) {
                    delay(30000)
                    startGlobalLocationTracking()
                }
            }
        }
    }

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadLocalSettings()
            checkPersistedAuth()
            // Removed startGlobalLocationTracking() from here to prevent crash on first run
        }

        // Persist current route for process death recovery
        viewModelScope.launch {
            currentRoute.collect { route ->
                savedStateHandle["current_route"] = route
            }
        }
        
        // Listen to live updates via repository events (Rule 13)
        viewModelScope.launch {
            repository.liveAttendanceEvents.collect { _ ->
                if (isLoggedIn.value) {
                    refreshDashboardData()
                }
            }
        }
    }

    private var lastRefreshTime = 0L
    private val REFRESH_THRESHOLD = 5000L // 5 seconds debounce

    private fun refreshDashboardData() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_THRESHOLD) return
        lastRefreshTime = now

        loadDashboardStats()
        loadAttendanceEvents()
    }

    private fun checkPersistedAuth() {
        val role = securePrefs.getUserRole()
        if (role != null) {
            currentRole.value = role
            currentUserName.value = securePrefs.getUserName()
            isLoggedIn.value = true
            currentRoute.value = if (role == UserRole.student) Navigation.ROUTE_STUDENT_OVERVIEW else Navigation.ROUTE_OVERVIEW
            refreshAllData()
            startGlobalLocationTracking()
        }
    }

    fun loginStaff(username: String, pin: String) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            try {
                val success = repository.loginStaff(username, pin)
                if (success) {
                    val role = securePrefs.getUserRole() ?: UserRole.viewer
                    currentUserName.value = securePrefs.getUserName()
                    currentRole.value = role
                    isLoggedIn.value = true
                    currentRoute.value = Navigation.ROUTE_OVERVIEW

                    refreshAllData()
                    startGlobalLocationTracking()
                    
                    // Secure token for biometric login if consent is present
                    if (securePrefs.hasBiometricConsent()) {
                        encryptTokenForBiometric(securePrefs.getStaffToken() ?: "")
                    }
                } else {
                    authError.value = "Incorrect credentials."
                    // Clear biometric if login fails? No, keep it.
                }
            } catch (e: Exception) {
                authError.value = e.message ?: "Authentication failed."
            } finally {
                authLoading.value = false
            }
        }
    }

    fun loginStudent(rollNo: String, dob: String) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            try {
                val success = repository.loginStudent(rollNo, dob)
                if (success) {
                    val role = UserRole.student
                    // Fetch profile detail before updating login state to avoid UI flashes/glitches
                    val sList = repository.getStudents(emptyMap())
                    val profile = sList.find { it.rollNo.uppercase() == rollNo.uppercase() }
                    
                    studentProfile.value = profile
                    cgpaAnimated.value = profile?.cgpa ?: 0.0
                    
                    currentUserName.value = securePrefs.getUserName()
                    currentRole.value = role
                    isLoggedIn.value = true
                    currentRoute.value = Navigation.ROUTE_STUDENT_OVERVIEW
                    
                    refreshAllData()
                    startGlobalLocationTracking()
                    
                    // Secure token for biometric login if consent is present
                    if (securePrefs.hasBiometricConsent()) {
                        encryptTokenForBiometric(securePrefs.getStudentToken() ?: "")
                    }
                } else {
                    authError.value = "Roll Number not found."
                }
            } catch (e: Exception) {
                authError.value = e.message ?: "Authentication failed."
            } finally {
                authLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Sequential, safe shutdown to prevent ANRs and crashes
            try {
                // 1. Telemetry record (Rule 4)
                repository.trackEvents(listOf(
                    com.vfstr.smartclass.data.remote.api.FrontendEventPayload(
                        event = "auth.logout",
                        properties = mapOf("user" to currentUserName.value),
                        timestamp = java.time.Instant.now().toString()
                    )
                ))
            } catch (e: Exception) {}

            // 2. Stop hardware services immediately
            try {
                stopBleRadar()
                stopBleScanner()
                locationTrackingJob?.cancel()
                locationTrackingJob = null
            } catch (e: Exception) {
                Log.e("MainVM", "Hardware stop failed: ${e.message}")
            }

            // 3. Purge tokens and user info
            securePrefs.logout()
            
            // 4. Drop current Room tables on background thread
            withContext(kotlinx.coroutines.Dispatchers.IO) {
                repository.clearAllData()
            }
            
            // 5. Atomic UI reset
            currentRole.value = null
            currentUserName.value = ""
            isLoggedIn.value = false
            studentProfile.value = null
            staffProfile.value = null
            cgpaAnimated.value = 0.0
            
            // 6. Final navigation redirect
            currentRoute.value = Navigation.ROUTE_LOGIN
        }
    }

    private fun loadLocalSettings() {
        isDarkTheme.value = securePrefs.getTheme() == "dark"
        localCgpa.value = securePrefs.getCgpa()
        localLastYearSubjects.value = securePrefs.getLastYearSubjects()
        notifAttendance.value = securePrefs.getNotificationSetting("attendance")
        notifDefaulter.value = securePrefs.getNotificationSetting("defaulter")
        notifCompliance.value = securePrefs.getNotificationSetting("compliance")
        biometricLockEnabled.value = securePrefs.isBiometricLockEnabled()
        activeRoleContext.value = securePrefs.getActiveRoleContext()
        isDataSaverEnabled.value = securePrefs.isDataSaverEnabled()
        isDndAutomationEnabled.value = securePrefs.isDndAutomationEnabled()
        updateStorageMetrics()
    }

    fun updateStorageMetrics() {
        viewModelScope.launch {
            syncQueueCount.value = repository.getSyncQueueCount()
            localDbSize.value = repository.getLocalDatabaseSize()
        }
    }

    fun updateTheme(dark: Boolean) {
        if (currentRole.value == UserRole.student) {
            isDarkTheme.value = dark
            securePrefs.saveTheme(if (dark) "dark" else "light")
        }
    }

    fun updateCgpa(cgpa: Float) {
        localCgpa.value = cgpa
        securePrefs.saveCgpa(cgpa)
    }

    fun updateLastYearSubjects(subjects: String) {
        localLastYearSubjects.value = subjects
        securePrefs.saveLastYearSubjects(subjects)
    }

    fun updateNotificationSetting(key: String, enabled: Boolean) {
        when(key) {
            "attendance" -> notifAttendance.value = enabled
            "defaulter" -> notifDefaulter.value = enabled
            "compliance" -> notifCompliance.value = enabled
        }
        securePrefs.saveNotificationSetting(key, enabled)
    }

    fun updateBiometricLock(enabled: Boolean) {
        securePrefs.saveBiometricLockEnabled(enabled)
        biometricLockEnabled.value = enabled
    }

    fun updateActiveRoleContext(role: UserRole?) {
        securePrefs.saveActiveRoleContext(role)
        activeRoleContext.value = role
        // Refresh data or navigation if needed
        refreshAllData()
    }

    fun updateDataSaver(enabled: Boolean) {
        securePrefs.saveDataSaverEnabled(enabled)
        isDataSaverEnabled.value = enabled
    }

    fun updateDndAutomation(enabled: Boolean) {
        securePrefs.saveDndAutomationEnabled(enabled)
        isDndAutomationEnabled.value = enabled
    }

    fun startBleScanner() {
        if (!com.vfstr.smartclass.utils.PermissionUtils.hasBleScanPermissions(context)) {
            android.util.Log.e("BLE", "Missing BLE scan permissions. Cannot start scanner.")
            return
        }
        val intent = Intent(context, com.vfstr.smartclass.data.remote.ble.BleScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        isScanningForBeacon.value = true
    }

    fun stopBleScanner() {
        val intent = Intent(context, com.vfstr.smartclass.data.remote.ble.BleScanService::class.java)
        context.stopService(intent)
        isScanningForBeacon.value = false
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                if (currentRole.value == UserRole.student) {
                    try {
                        val p = repository.getStudentProfile()
                        val sList = repository.getStudents(emptyMap())
                        val matched = sList.find { it.rollNo.uppercase() == p.username.uppercase() }
                        if (matched != null) {
                            studentProfile.value = matched.copy(
                                email = p.email ?: matched.email,
                                department = p.department ?: matched.department,
                                year = p.year?.toString() ?: matched.year,
                                section = p.section ?: matched.section
                            )
                        } else {
                            studentProfile.value = Student(
                                id = p.id,
                                studentId = p.student_id,
                                name = p.name ?: p.full_name ?: "",
                                rollNo = p.username,
                                department = p.department ?: "",
                                year = p.year?.toString() ?: "1",
                                section = p.section ?: "A",
                                email = p.email,
                                faceEnrolled = false,
                                biometricConsent = false
                            )
                        }
                    } catch (e: Exception) {
                        val sList = repository.getStudents(emptyMap())
                        val roll = securePrefs.getUserName()
                        studentProfile.value = sList.find { it.rollNo.uppercase() == roll.uppercase() }
                    }
                } else {
                    // Fetch staff profile
                    val profileDto = repository.getStaffProfile()
                    staffProfile.value = User(
                        id = profileDto.id,
                        username = profileDto.username,
                        displayName = profileDto.full_name ?: profileDto.name ?: "Staff",
                        role = currentRole.value ?: UserRole.viewer,
                        department = profileDto.department,
                        email = profileDto.email,
                        permissions = profileDto.permissions ?: emptyList(),
                        isActive = profileDto.is_active
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            appPrefs.setFirstLaunchCompleted()
            
            // Trigger asset initialization worker
            val workManager = androidx.work.WorkManager.getInstance(context)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.vfstr.smartclass.utils.initialization.AssetInitializationWorker>()
                .build()
            workManager.enqueue(workRequest)
            
            // Navigate based on role (default to Login)
            currentRoute.value = Navigation.ROUTE_LOGIN
        }
    }

    fun saveBiometricConsent(granted: Boolean) {
        viewModelScope.launch {
            securePrefs.saveBiometricConsent(granted)
            if (!granted) {
                securePrefs.clearBiometricToken()
                hasBiometricToken.value = false
            }
        }
    }

    private fun encryptTokenForBiometric(token: String) {
        if (token.isEmpty()) return
        Log.d("MainVM", "Encrypting token: ${token.take(10)}...")
        try {
            val cipher = biometricAuthManager.getInitializedCipher(Cipher.ENCRYPT_MODE)
            val ciphertext = cipher.doFinal(token.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
            securePrefs.saveEncryptedToken(ciphertext, cipher.iv)
            hasBiometricToken.value = true
            Log.d("MainVM", "Token encrypted and saved successfully")
        } catch (e: Exception) {
            Log.e("MainVM", "Encryption failed: ${e.message}", e)
        }
    }

    fun initiateBiometricLogin(activity: FragmentActivity) {
        val encryptedData = securePrefs.getEncryptedToken()
        if (encryptedData == null) {
            Log.e("MainVM", "No biometric token found in preferences")
            authError.value = "Biometric not set up. Please login once."
            return
        }
        val (ciphertext, iv) = encryptedData

        try {
            val cipher = biometricAuthManager.getInitializedCipher(Cipher.DECRYPT_MODE, iv)
            val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
            
            val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val decryptedCipher = result.cryptoObject?.cipher ?: return
                    val decryptedToken = String(decryptedCipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8)
                    
                    handleBiometricSuccess(decryptedToken)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    authError.value = "Biometric Error: $errString"
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Authorize access to SmartClass")
                .setNegativeButtonText("Use Password")
                .build()

            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Exception) {
            authError.value = "Initialization failed: ${e.message}"
        }
    }

    private fun handleBiometricSuccess(token: String) {
        viewModelScope.launch {
            authLoading.value = true
            try {
                val role = JwtUtils.getRoleFromToken(token)
                if (role == UserRole.student) {
                    securePrefs.saveStudentToken(token)
                } else {
                    securePrefs.saveStaffToken(token)
                }
                
                // Re-verify auth state
                checkPersistedAuth()
            } catch (e: Exception) {
                authError.value = "Session recovery failed."
            } finally {
                authLoading.value = false
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearAllData()
            refreshDashboardData()
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            if (currentRole.value == UserRole.student) {
                loadProfile()
                loadStudentEligibility(attendanceFilterFrom.value, attendanceFilterTo.value)
                loadStudentAttendanceReport(attendanceFilterFrom.value, attendanceFilterTo.value)
                loadStudentODRequests()
                loadStudentMOOCs()
                loadStudentMarks(selectedMarksSemester.value)
                loadSemesterResults()
                loadStudentBacklogs()
                loadStudentMentor()
            } else {
                loadDashboardStats()
                loadStudents()
                loadAttendanceEvents()
                loadSessions()
                loadTimetable()
                loadODRequests()
                loadAuditLogs()
                loadNotificationLogs()
            }
        }
    }

    fun loadAuditLogs() {
        // Mock data for Rule 2 compliance
        auditLogs.value = listOf(
            AuditLog("1", "2026-05-22T15:42:00", "user_1", "admin_rao", "UPDATE", "Attendance Record: 22L11A0504", "172.16.50.12", emptyMap()),
            AuditLog("2", "2026-05-22T14:15:00", "user_2", "superadmin", "CREATE", "New Student: 22L11A0512", "172.16.50.1", emptyMap()),
            AuditLog("3", "2026-05-22T11:30:00", "user_3", "faculty_vani", "LOGIN", "Staff Web Portal", "10.42.102.3", emptyMap()),
            AuditLog("4", "2026-05-21T09:12:00", "user_1", "admin_rao", "DELETE", "Old Session: sess_9281", "172.16.50.12", emptyMap())
        )
    }

    fun loadNotificationLogs() {
        notificationLogs.value = (1..10).map { "ID_$it" }
        guardians.value = listOf(
            listOf("22L11A0501", "Ramesh Babu", "9848012345", "Father"),
            listOf("22L11A0502", "Sita Devi", "9848054321", "Mother"),
            listOf("22L11A0503", "Kalyan Ram", "9848098765", "Guardian")
        )
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            statsLoading.value = true
            statsError.value = null
            try {
                val stats = repository.getStats()
                dashboardStats.value = stats
            } catch (e: Exception) {
                statsError.value = e.message ?: "Failed to load academic statistics."
            } finally {
                statsLoading.value = false
            }
        }
    }

    fun loadStudents(dept: String? = null, sec: String? = null) {
        viewModelScope.launch {
            studentsLoading.value = true
            studentsError.value = null
            try {
                val filters = mutableMapOf<String, String>()
                if (dept != null) filters["department"] = dept
                if (sec != null) filters["section"] = sec
                val list = repository.getStudents(filters)
                students.value = list
            } catch (e: Exception) {
                studentsError.value = e.message ?: "Failed to list students."
            } finally {
                studentsLoading.value = false
            }
        }
    }

    fun loadAttendanceEvents(dept: String? = null, sec: String? = null, status: String? = null) {
        viewModelScope.launch {
            eventsLoading.value = true
            try {
                val filters = mutableMapOf<String, String>()
                if (dept != null) filters["department"] = dept
                if (sec != null) filters["section"] = sec
                if (status != null) filters["status"] = status
                val list = repository.getAttendanceEvents(filters)
                attendanceEvents.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                eventsLoading.value = false
            }
        }
    }

    fun loadSessions(dept: String? = null, sec: String? = null) {
        viewModelScope.launch {
            sessionsLoading.value = true
            try {
                val filters = mutableMapOf<String, String>()
                if (dept != null) filters["department"] = dept
                if (sec != null) filters["section"] = sec
                val list = repository.getSessions(filters)
                classSessions.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                sessionsLoading.value = false
            }
        }
    }

    fun createSession(roomId: String, classId: String, subject: String?) {
        viewModelScope.launch {
            repository.createSession(CreateSessionPayload(roomId, classId, subject))
            loadSessions()
        }
    }

    fun startSessionDirect(id: String) {
        viewModelScope.launch {
            repository.startSession(id)
            if (isDndAutomationEnabled.value) {
                dndManager.enableDnd()
            }
            loadSessions()
            startBleRadar(id)
        }
    }

    fun endSessionDirect(id: String, backfill: Boolean = true) {
        viewModelScope.launch {
            repository.endSession(id, backfill)
            if (isDndAutomationEnabled.value) {
                dndManager.disableDnd()
            }
            loadSessions()
            stopBleRadar()
        }
    }

    // BLE Radar Actions
    fun startBleRadar(sessionId: String) {
        if (!com.vfstr.smartclass.utils.PermissionUtils.hasBleAdvertisePermissions(context)) {
            android.util.Log.e("BLE", "Missing BLE advertise permissions. Cannot start radar.")
            return
        }
        val intent = Intent(context, com.vfstr.smartclass.data.remote.ble.BleAdvertiserService::class.java).apply {
            putExtra("SESSION_ID", sessionId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        isBleRadarActive.value = true
    }

    fun stopBleRadar() {
        val intent = Intent(context, com.vfstr.smartclass.data.remote.ble.BleAdvertiserService::class.java)
        context.stopService(intent)
        isBleRadarActive.value = false
    }

    fun loadTimetable(dept: String? = null, sec: String? = null) {
        viewModelScope.launch {
            timetableLoading.value = true
            try {
                val filters = mutableMapOf<String, String>()
                if (dept != null) filters["department"] = dept
                if (sec != null) filters["section"] = sec
                val list = repository.getTimetable(filters)
                timetableSlots.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                timetableLoading.value = false
            }
        }
    }

    fun loadODRequests(dept: String? = null, sec: String? = null) {
        viewModelScope.launch {
            val filters = mutableMapOf<String, String>()
            if (dept != null) filters["department"] = dept
            if (sec != null) filters["section"] = sec
            odRequests.value = repository.getODRequests(filters)
        }
    }

    fun submitOverride(rollNo: String, sessionId: String, status: String, reason: String) {
        viewModelScope.launch {
            repository.submitOverride(rollNo, sessionId, status, reason)
            refreshAllData()
        }
    }

    fun loadStudentEligibility(fromDate: String? = null, toDate: String? = null) {
        viewModelScope.launch {
            try {
                val data = repository.getStudentEligibility(fromDate, toDate)
                studentEligibility.value = data
                if (cgpaAnimated.value == 0.0) {
                    cgpaAnimated.value = studentProfile.value?.cgpa ?: (data.overall_percentage / 10.0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentAttendanceReport(fromDate: String? = null, toDate: String? = null) {
        viewModelScope.launch {
            try {
                studentAttendanceReport.value = repository.getStudentAttendanceReport(fromDate, toDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentMarks(semester: String? = null) {
        viewModelScope.launch {
            try {
                studentMarks.value = repository.getStudentMarks(semester)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSemesterResults() {
        viewModelScope.launch {
            try {
                val results = repository.getStudentResults()
                semesterResults.value = results
                val latest = results.maxByOrNull { it.semester }
                if (latest != null) {
                    cgpaAnimated.value = latest.cgpa.toDouble()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentBacklogs() {
        viewModelScope.launch {
            try {
                studentBacklogs.value = repository.getStudentBacklogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentMentor() {
        viewModelScope.launch {
            try {
                studentMentor.value = repository.getStudentMentor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentODRequests() {
        viewModelScope.launch {
            try {
                studentODRequests.value = repository.getStudentODRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadStudentMOOCs() {
        viewModelScope.launch {
            try {
                studentMOOCEnrollments.value = repository.getStudentMOOCs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitStudentODRequest(type: String, name: String, dates: String, reason: String) {
        viewModelScope.launch {
            isSubmittingOD.value = true
            odSubmitSuccess.value = null
            try {
                val cleanDate = if (dates.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) dates else java.time.LocalDate.now().toString()
                val success = repository.submitStudentODRequest(
                    eventName = name,
                    eventDate = cleanDate,
                    duration = 1,
                    reason = reason
                )
                odSubmitSuccess.value = success
                if (success) {
                    loadStudentODRequests()
                }
            } catch (e: Exception) {
                odSubmitSuccess.value = false
            } finally {
                isSubmittingOD.value = false
            }
        }
    }

    fun enrollStudentMOOC(courseName: String, platform: String, courseCode: String?, credits: Int?, enrollmentDate: String?, semester: Int?, academicYear: String?) {
        viewModelScope.launch {
            isEnrollingMOOC.value = true
            moocEnrollSuccess.value = null
            try {
                val payload = StudentMOOCEnrollPayload(
                    course_name = courseName,
                    platform = platform,
                    course_code = courseCode,
                    credits = credits,
                    enrollment_date = enrollmentDate,
                    semester = semester,
                    academic_year = academicYear
                )
                val result = repository.enrollStudentMOOC(payload)
                if (result != null) {
                    moocEnrollSuccess.value = true
                    loadStudentMOOCs()
                } else {
                    moocEnrollSuccess.value = false
                }
            } catch (e: Exception) {
                moocEnrollSuccess.value = false
            } finally {
                isEnrollingMOOC.value = false
            }
        }
    }

    fun changeStudentPassword(current: String, new: String) {
        viewModelScope.launch {
            isPasswordChanging.value = true
            passwordChangeSuccess.value = null
            try {
                val success = repository.changeStudentPassword(current, new)
                passwordChangeSuccess.value = success
            } catch (e: Exception) {
                passwordChangeSuccess.value = false
            } finally {
                isPasswordChanging.value = false
            }
        }
    }

    fun approveLeaveODAction(id: String, comment: String) {
        viewModelScope.launch {
            // In a real app, this would call the repository to update the status
            // For now, we mock the success and refresh
            refreshAllData()
        }
    }

    fun confirmImportedStudents() {
        viewModelScope.launch {
            repository.registerImportedStudents(studentPreviewList.value)
            studentPreviewList.value = emptyList()
            loadStudents()
        }
    }

    fun confirmImportedSlots() {
        viewModelScope.launch {
            repository.registerImportedSlots(timetablePreviewList.value)
            timetablePreviewList.value = emptyList()
            loadTimetable()
        }
    }

    val hierarchyData = MutableStateFlow<String>("")
    val analyticsData = MutableStateFlow<String>("")

    fun loadLocalAssets() {
        viewModelScope.launch {
            hierarchyData.value = repository.getHierarchy()
            analyticsData.value = repository.getAcademicAnalytics()
        }
    }

    fun faceEnrollmentAction(rollNo: String) {
        currentRoute.value = "${Navigation.ROUTE_SCANNER}?mode=enroll&rollNo=$rollNo"
    }

    // Excel import error states
    val importError = MutableStateFlow<String?>(null)

    fun parseExcelTimetable(inputStream: InputStream) {
        viewModelScope.launch {
            importError.value = null
            try {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                val list = mutableListOf<TimetableSlot>()
                
                // Validate Header (Rule 17)
                val headerRow = sheet.getRow(0)
                if (headerRow == null || headerRow.physicalNumberOfCells < 4) {
                    importError.value = "Invalid Excel format. Expected at least 4 columns."
                    return@launch
                }

                for (rowNum in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowNum) ?: continue
                    val subjectCode = row.getCell(0)?.toString() ?: ""
                    val subjectName = row.getCell(1)?.toString() ?: ""
                    val room = row.getCell(2)?.toString() ?: ""
                    val section = row.getCell(3)?.toString() ?: ""
                    
                    if (subjectCode.isEmpty() || subjectName.isEmpty()) continue

                    list.add(
                        TimetableSlot(
                            id = "tmp_t_$rowNum",
                            dayOfWeek = "1",
                            period = 1,
                            startTime = "09:00",
                            endTime = "09:50",
                            subjectCode = subjectCode,
                            subjectName = subjectName,
                            sessionType = if (subjectCode.endsWith("P")) "P" else "L",
                            room = room,
                            facultyName = "TBD",
                            department = "TBD",
                            year = "1",
                            section = section
                        )
                    )
                }
                timetablePreviewList.value = list
                if (list.isEmpty()) importError.value = "No valid data rows found in Excel."
            } catch (e: Exception) {
                importError.value = "Excel parsing failed: ${e.message}"
            }
        }
    }

    fun parseExcelStudents(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                val list = mutableListOf<Student>()
                
                for (rowNum in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowNum) ?: continue
                    val name = row.getCell(0)?.stringCellValue ?: "Imported Student"
                    val roll = row.getCell(1)?.stringCellValue ?: "22L11A051${rowNum}"
                    val section = row.getCell(2)?.stringCellValue ?: "Sec A"
                    val dept = row.getCell(3)?.stringCellValue ?: "CSAI"
                    
                    list.add(
                        Student(
                            id = "",
                            rollNo = roll,
                            studentId = roll,
                            name = name,
                            section = section,
                            year = "1",
                            department = dept,
                            faceEnrolled = false,
                            biometricConsent = true,
                            cgpa = 0.0
                        )
                    )
                }
                studentPreviewList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBleRadar()
        stopBleScanner()
    }
}
