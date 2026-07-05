package com.vfstr.smartclass.ui.screens.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.repositories.StudentManagementRepository
import com.vfstr.smartclass.domain.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrollmentAdminViewModel @Inject constructor(
    private val repository: StudentManagementRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<DashboardStats?>(null)
    val stats = _stats.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<StudentListItem>>(emptyList())
    val pendingRequests = _pendingRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _stats.value = repository.getEnrollmentStats()
                _pendingRequests.value = repository.getEnrollmentRequests(1, 10, "PENDING").items
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveRequest(id: String) {
        viewModelScope.launch {
            try {
                repository.approveRequest(id)
                loadDashboard() // Refresh
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rejectRequest(id: String, comment: String) {
        viewModelScope.launch {
            try {
                repository.rejectRequest(id, comment)
                loadDashboard()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun requestRevision(id: String, comment: String) {
        viewModelScope.launch {
            try {
                repository.requestRevision(id, comment)
                loadDashboard()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
