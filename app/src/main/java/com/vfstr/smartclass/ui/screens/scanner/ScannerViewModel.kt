package com.vfstr.smartclass.ui.screens.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.remote.api.*
import com.vfstr.smartclass.domain.models.AttendanceScanResult
import com.vfstr.smartclass.data.repositories.AppRepository
import com.vfstr.smartclass.utils.geofence.GeofenceUtils
import com.vfstr.smartclass.utils.geofence.LocationHelper
import com.vfstr.smartclass.utils.geofence.WifiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GeoStatus { IDLE, CHECKING, ALLOWED, OUT_OF_RANGE, DENIED, UNAVAILABLE }

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val repository: AppRepository,
    private val api: RetrofitApi,
    private val locationHelper: LocationHelper,
    private val wifiHelper: WifiHelper,
    private val securePrefs: com.vfstr.smartclass.data.preferences.SecurePreferences,
    private val dndManager: com.vfstr.smartclass.utils.DndManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _geoStatus = MutableStateFlow(GeoStatus.IDLE)
    val geoStatus: StateFlow<GeoStatus> = _geoStatus

    private val _geoDistance = MutableStateFlow<Double?>(null)
    val geoDistance: StateFlow<Double?> = _geoDistance

    private val _lastLat = MutableStateFlow(0.0)
    private val _lastLon = MutableStateFlow(0.0)
    private val _prevLat = MutableStateFlow<Double?>(null)
    private val _prevLon = MutableStateFlow<Double?>(null)
    private val _prevTime = MutableStateFlow<Long?>(null)

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning

    private val _scanResult = MutableStateFlow<AttendanceScanResult?>(savedStateHandle.get<AttendanceScanResult>("scan_result"))
    val scanResult: StateFlow<AttendanceScanResult?> = _scanResult

    private val _error = MutableStateFlow<String?>(savedStateHandle.get<String>("scan_error"))
    val error: StateFlow<String?> = _error

    private val _uploadingImage = MutableStateFlow(false)
    val uploadingImage: StateFlow<Boolean> = _uploadingImage

    private val _uploadProgress = MutableStateFlow("")
    val uploadProgress: StateFlow<String> = _uploadProgress

    private val _sectionStudents = MutableStateFlow<List<StudentDto>>(emptyList())
    val sectionStudents: StateFlow<List<StudentDto>> = _sectionStudents

    private val _pendingManualStudentId = MutableStateFlow<String?>(savedStateHandle.get<String>("pending_student"))
    val pendingManualStudentId: StateFlow<String?> = _pendingManualStudentId

    private val _sessionId = MutableStateFlow<String?>(savedStateHandle.get<String>("session_id"))
    val sessionId: StateFlow<String?> = _sessionId

    private val _activeSessionSubject = MutableStateFlow("")
    val activeSessionSubject: StateFlow<String> = _activeSessionSubject

    private var currentSectionId: String? = null

    init {
        viewModelScope.launch {
            _scanResult.collect { savedStateHandle["scan_result"] = it }
        }
        viewModelScope.launch {
            _error.collect { savedStateHandle["scan_error"] = it }
        }
        viewModelScope.launch {
            _pendingManualStudentId.collect { savedStateHandle["pending_student"] = it }
        }
        viewModelScope.launch {
            _sessionId.collect { savedStateHandle["session_id"] = it }
        }
    }

    fun setSectionId(id: String) {
        currentSectionId = id
        loadSectionData()
        fetchActiveSession()
        
        // Rule: Start DND when entering scanner if enabled
        if (securePrefs.isDndAutomationEnabled()) {
            dndManager.enableDnd()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Rule: Stop DND when exiting scanner if enabled
        if (securePrefs.isDndAutomationEnabled()) {
            dndManager.disableDnd()
        }
    }

    private fun loadSectionData() {
        viewModelScope.launch {
            try {
                val students = api.getSectionStudents(currentSectionId!!)
                _sectionStudents.value = students
            } catch (e: Exception) {
                _error.value = "Failed to load section students"
            }
        }
    }

    private fun fetchActiveSession() {
        viewModelScope.launch {
            try {
                val res = api.getActiveSectionSession(currentSectionId!!)
                if (res.active) {
                    _sessionId.value = res.session_id
                    _activeSessionSubject.value = res.subject ?: "Unknown"
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun startLocationTracking() {
        viewModelScope.launch {
            _geoStatus.value = GeoStatus.CHECKING
            try {
                locationHelper.getLocationUpdates().collectLatest { location ->
                    val currentTime = System.currentTimeMillis()
                    val bssid = wifiHelper.getCurrentBssid()
                    
                    val result = GeofenceUtils.verifyLocation(
                        lat = location.latitude, 
                        lon = location.longitude, 
                        accuracy = location.accuracy,
                        bssid = bssid,
                        prevLat = _prevLat.value,
                        prevLon = _prevLon.value,
                        prevTime = _prevTime.value,
                        currentTime = currentTime
                    )
                    
                    _lastLat.value = location.latitude
                    _lastLon.value = location.longitude
                    
                    if (result.success) {
                        _geoStatus.value = GeoStatus.ALLOWED
                        _geoDistance.value = 0.0
                        
                        // Update previous state only on successful valid points
                        _prevLat.value = location.latitude
                        _prevLon.value = location.longitude
                        _prevTime.value = currentTime
                    } else {
                        _geoStatus.value = if (result.code == "SPOOFING_DETECTED") GeoStatus.DENIED else GeoStatus.OUT_OF_RANGE
                        _geoDistance.value = GeofenceUtils.distanceToNearestCampusAnchor(
                            location.latitude, 
                            location.longitude
                        )
                        if (result.code == "SPOOFING_DETECTED") {
                            _error.value = "Security Alert: ${result.message}"
                        }
                    }
                }
            } catch (e: Exception) {
                _geoStatus.value = GeoStatus.UNAVAILABLE
            }
        }
    }

    fun handleCapture(base64Image: String) {
        if (_geoStatus.value != GeoStatus.ALLOWED) return
        
        viewModelScope.launch {
            _scanning.value = true
            _scanResult.value = null
            _error.value = null
            try {
                val res = api.recognizeFace(
                    currentSectionId!!, 
                    RecognizeRequest(
                        base64Image, 
                        currentSectionId!!, 
                        _sessionId.value, 
                        _lastLat.value, 
                        _lastLon.value
                    )
                )
                _scanResult.value = res
            } catch (e: Exception) {
                _error.value = "Face recognition failed"
            } finally {
                _scanning.value = false
            }
        }
    }

    fun handleImageUpload(base64Image: String) {
        _uploadingImage.value = true
        handleCapture(base64Image)
        _uploadingImage.value = false
    }

    fun handleManualMark(studentId: String) {
        viewModelScope.launch {
            _scanning.value = true
            try {
                val res = api.markManual(currentSectionId!!, ManualMarkRequest(studentId, _sessionId.value))
                _scanResult.value = res
            } catch (e: Exception) {
                _error.value = "Manual marking failed"
            } finally {
                _scanning.value = false
                _pendingManualStudentId.value = null
            }
        }
    }

    fun setPendingStudent(id: String) {
        _pendingManualStudentId.value = id
    }

    fun syncIndex() {
        viewModelScope.launch {
            try {
                api.syncIndex(currentSectionId!!)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
