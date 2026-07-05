package com.vfstr.smartclass.ui.screens.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.local.enrollment.EnrollmentDataSource
import com.vfstr.smartclass.data.remote.api.SessionDto
import com.vfstr.smartclass.data.repositories.AttendanceRepository
import com.vfstr.smartclass.domain.models.*
import com.vfstr.smartclass.utils.inferDepartment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val enrollmentDataSource: EnrollmentDataSource,
    private val attendanceRepository: AttendanceRepository,
    private val okHttpClient: OkHttpClient,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _hierarchy = MutableStateFlow<StudentHierarchy>(emptyMap())
    val hierarchy: StateFlow<StudentHierarchy> = _hierarchy.asStateFlow()

    private val _selectedYear = MutableStateFlow(savedStateHandle.get<String>("sel_year") ?: "")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _selectedDepartment = MutableStateFlow(savedStateHandle.get<String>("sel_dept") ?: "")
    val selectedDepartment: StateFlow<String> = _selectedDepartment.asStateFlow()

    private val _selectedBranch = MutableStateFlow(savedStateHandle.get<String>("sel_branch") ?: "")
    val selectedBranch: StateFlow<String> = _selectedBranch.asStateFlow()

    private val _selectedSection = MutableStateFlow(savedStateHandle.get<String>("sel_section") ?: "")
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()

    private val _searchQuery = MutableStateFlow(savedStateHandle.get<String>("search") ?: "")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selection = MutableStateFlow<Map<String, Boolean>>(savedStateHandle.get<Map<String, Boolean>>("selection") ?: emptyMap())
    val selection: StateFlow<Map<String, Boolean>> = _selection.asStateFlow()

    private val _sessions = MutableStateFlow<List<SessionDto>>(emptyList())
    val sessions: StateFlow<List<SessionDto>> = _sessions.asStateFlow()

    private val _selectedSessionId = MutableStateFlow("")
    val selectedSessionId: StateFlow<String> = _selectedSessionId.asStateFlow()

    private val _manualSessionId = MutableStateFlow("")
    val manualSessionId: StateFlow<String> = _manualSessionId.asStateFlow()

    private val _isSubmitting = MutableStateFlow(value = false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val years = _hierarchy.map { it.keys.sortedDescending() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments = combine(_hierarchy, _selectedYear) { h, year ->
        h[year]?.keys?.map { inferDepartment(it) }?.toSortedSet()?.toList() ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val branches = combine(_hierarchy, _selectedYear, _selectedDepartment) { h, year, dept ->
        h[year]?.keys?.filter { inferDepartment(it) == dept }?.sorted() ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sections = combine(_hierarchy, _selectedYear, _selectedBranch) { h, year, branch ->
        h[year]?.get(branch)?.keys?.sorted() ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students = combine(_hierarchy, _selectedYear, _selectedBranch, _selectedSection) { h, year, branch, section ->
        h[year]?.get(branch)?.get(section)?.sortedBy { it.roll_number } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents = combine(students, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.student_id.contains(query, ignoreCase = true) ||
                    it.roll_number.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val hierarchyData = enrollmentDataSource.getHierarchy()
            _hierarchy.value = hierarchyData
        }

        // Persist selections for process death recovery
        viewModelScope.launch { _selectedYear.collect { savedStateHandle["sel_year"] = it } }
        viewModelScope.launch { _selectedDepartment.collect { savedStateHandle["sel_dept"] = it } }
        viewModelScope.launch { _selectedBranch.collect { savedStateHandle["sel_branch"] = it } }
        viewModelScope.launch { _selectedSection.collect { savedStateHandle["sel_section"] = it } }
        viewModelScope.launch { _searchQuery.collect { savedStateHandle["search"] = it } }
        viewModelScope.launch { _selection.collect { savedStateHandle["selection"] = it } }
        
        // Auto-select defaults
        viewModelScope.launch {
            years.collectLatest { list ->
                if (list.isNotEmpty() && _selectedYear.value.isEmpty()) {
                    _selectedYear.value = list.first()
                }
            }
        }

        viewModelScope.launch {
            departments.collectLatest { list ->
                if (list.isNotEmpty() && _selectedDepartment.value.isEmpty()) {
                    _selectedDepartment.value = list.first()
                }
            }
        }

        viewModelScope.launch {
            branches.collectLatest { list ->
                if (list.isNotEmpty() && _selectedBranch.value.isEmpty()) {
                    _selectedBranch.value = list.first()
                }
            }
        }

        viewModelScope.launch {
            sections.collectLatest { list ->
                if (list.isNotEmpty() && _selectedSection.value.isEmpty()) {
                    _selectedSection.value = list.first()
                }
            }
        }

        // Preserve selection when students change
        viewModelScope.launch {
            students.collectLatest { list ->
                val current = _selection.value
                _selection.value = list.associate { it.student_id to (current[it.student_id] ?: true) }
            }
        }

        // Fetch sessions
        viewModelScope.launch {
            combine(_selectedYear, _selectedSection, _selectedBranch) { year, sec, branch ->
                Triple(year, sec, branch)
            }.collectLatest { (year, sec, branch) ->
                fetchSessions()
            }
        }
    }

    private fun fetchSessions() {
        viewModelScope.launch {
            try {
                _sessions.value = attendanceRepository.getSessions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
        _selectedDepartment.value = ""
        _selectedBranch.value = ""
        _selectedSection.value = ""
    }

    fun selectDepartment(dept: String) {
        _selectedDepartment.value = dept
        _selectedBranch.value = ""
        _selectedSection.value = ""
    }

    fun selectBranch(branch: String) {
        _selectedBranch.value = branch
        _selectedSection.value = ""
    }

    fun selectSection(section: String) {
        _selectedSection.value = section
    }

    fun toggleStudent(studentId: String) {
        val current = _selection.value.toMutableMap()
        current[studentId] = !(current[studentId] ?: true)
        _selection.value = current
    }

    fun markAll(present: Boolean) {
        _selection.value = students.value.associate { it.student_id to present }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun setManualSessionId(sessionId: String) {
        _manualSessionId.value = sessionId
    }

    fun submitAttendance(onSuccess: (MarkBulkResponse) -> Unit, onError: (String) -> Unit) {
        val sessionId = _manualSessionId.value.trim().ifEmpty { _selectedSessionId.value }
        if (sessionId.isEmpty()) {
            onError("Select a session or enter a session ID first.")
            return
        }
        
        val presentIds = _selection.value.filter { it.value }.keys.toList()
        if (presentIds.isEmpty()) {
            onError("At least one student must be marked present.")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val res = attendanceRepository.markBulk(MarkBulkRequest(sessionId, presentIds))
                onSuccess(res)
            } catch (e: Exception) {
                onError("Unable to submit attendance. Verify session ID and try again.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private var webSocket: WebSocket? = null

    private fun connectWebSocket(sessionId: String) {
        webSocket?.cancel()
        if (sessionId.isEmpty()) return

        val request = Request.Builder()
            .url("wss://smartclass-api.vfstr.ac.in/ws/attendance?session_id=$sessionId")
            .build()
            
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.optString("type") == "attendance_marked") {
                        val studentId = json.optString("student_id")
                        if (studentId.isNotEmpty()) {
                            val current = _selection.value.toMutableMap()
                            current[studentId] = true
                            _selection.value = current
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.cancel()
    }
}
