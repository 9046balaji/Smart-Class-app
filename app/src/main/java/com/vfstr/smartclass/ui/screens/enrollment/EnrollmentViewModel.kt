package com.vfstr.smartclass.ui.screens.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vfstr.smartclass.data.repositories.EnrollmentRepository
import com.vfstr.smartclass.domain.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val repository: EnrollmentRepository
) : ViewModel() {

    private val _requestState = MutableStateFlow<EnrollmentRequest?>(null)
    val requestState = _requestState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _notifications = MutableStateFlow<List<EnrollmentNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private var pollingJob: Job? = null

    fun loadRequest(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _requestState.value = repository.getEnrollmentRequest(id)
                startPolling(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitEnrollment(payload: EnrollmentRequestPayload, photos: Map<PhotoType, File>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val front = photos[PhotoType.FRONT] ?: throw Exception("Front photo missing")
                val left = photos[PhotoType.LEFT] ?: throw Exception("Left photo missing")
                val right = photos[PhotoType.RIGHT] ?: throw Exception("Right photo missing")
                
                val result = repository.enrollStudent(payload, front, left, right)
                _requestState.value = result
                startPolling(result.id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Starts a 60-second polling loop for application status and notifications.
     * Rule S2: Real-time updates via periodic polling.
     * Note: This uses viewModelScope, so it is automatically cancelled when the user
     * navigates away and the ViewModel is cleared. This is the intended lifecycle behavior.
     */
    private fun startPolling(id: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(60000) // 60s polling (Rule S2)
                try {
                    _requestState.value = repository.getEnrollmentRequest(id)
                    _notifications.value = repository.getNotifications(id)
                } catch (e: Exception) {
                    // Silent failure for polling
                }
            }
        }
    }

    fun markNotificationsRead() {
        val requestId = _requestState.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.markNotificationsRead(requestId)
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
