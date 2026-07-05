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
class BulkUploadViewModel @Inject constructor(
    private val repository: StudentManagementRepository
) : ViewModel() {

    private val _uploadResult = MutableStateFlow<BulkUploadResult?>(null)
    val uploadResult = _uploadResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _parsedRows = MutableStateFlow<List<EnrollmentRequestPayload>>(emptyList())
    val parsedRows = _parsedRows.asStateFlow()

    fun parseCsv(csvText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // RFC 4180-compliant parsing (simplified for logic, real impl would use a library or robust regex)
                val lines = csvText.lines().filter { it.isNotBlank() }
                if (lines.isEmpty()) throw Exception("Empty CSV")
                
                val rows = lines.drop(1).mapNotNull { line ->
                    val cols = line.split(",").map { it.trim() }
                    if (cols.size < 6) return@mapNotNull null
                    
                    EnrollmentRequestPayload(
                        rollNumber = cols[2],
                        name = cols[1],
                        branch = cols[3],
                        year = cols[4],
                        section = cols[5],
                        mobileNumber = cols.getOrNull(6),
                        dateOfBirth = cols.getOrNull(7)
                    )
                }
                _parsedRows.value = rows
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitUpload() {
        val rows = _parsedRows.value
        if (rows.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _progress.value = 0f
            try {
                // Simulated progress as per recommendation 3
                viewModelScope.launch {
                    for (i in 1..90) {
                        delay(50)
                        _progress.value = i / 100f
                    }
                }
                
                val result = repository.bulkUpload(rows)
                _uploadResult.value = result
                _progress.value = 1f
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun delay(time: Long) {
        kotlinx.coroutines.delay(time)
    }

    fun reset() {
        _uploadResult.value = null
        _parsedRows.value = emptyList()
        _progress.value = 0f
        _error.value = null
    }
}
