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
class ManageStudentsViewModel @Inject constructor(
    private val repository: StudentManagementRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<StudentListItem>>(emptyList())
    val students = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20

    private val _filters = MutableStateFlow<Map<String, String>>(emptyMap())
    val filters = _filters.asStateFlow()

    fun loadStudents(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            _students.value = emptyList()
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getStudents(_filters.value, currentPage, pageSize)
                _students.value = if (reset) response.items else _students.value + response.items
                currentPage++
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFilters(newFilters: Map<String, String>) {
        _filters.value = newFilters
        loadStudents(reset = true)
    }

    fun deleteStudent(id: String) {
        viewModelScope.launch {
            try {
                repository.deactivateStudent(id)
                loadStudents(reset = true)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun reactivateStudent(id: String) {
        viewModelScope.launch {
            try {
                repository.reactivateStudent(id)
                loadStudents(reset = true)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
