package com.vfstr.smartclass.utils.initialization

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vfstr.smartclass.data.local.db.LocalDatabase
import com.vfstr.smartclass.data.local.db.StudentEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@HiltWorker
class AssetInitializationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: LocalDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val assetManager = applicationContext.assets
            val csvFiles = listOf("enrollment_2023.csv", "enrollment_2024.csv", "enrollment_2025.csv")
            
            val students = mutableListOf<StudentEntity>()
            
            csvFiles.forEach { fileName ->
                assetManager.open(fileName).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // Skip header
                        reader.readLine()
                        
                        var line: String? = reader.readLine()
                        while (line != null) {
                            val parts = line.split(",")
                            if (parts.size >= 5) {
                                // Assuming CSV structure: rollNo, name, section, year, department
                                students.add(
                                    StudentEntity(
                                        rollNo = parts[0].trim(),
                                        name = parts[1].trim(),
                                        section = parts[2].trim(),
                                        year = parts[3].trim(),
                                        department = parts[4].trim(),
                                        faceEnrolled = false,
                                        biometricConsent = false,
                                        cgpa = 0.0,
                                        attendancePercentagesJson = "{}"
                                    )
                                )
                            }
                            line = reader.readLine()
                        }
                    }
                }
            }
            
            if (students.isNotEmpty()) {
                database.dao().insertStudents(students)
                Log.d("AssetInitWorker", "Successfully initialized ${students.size} students from CSV assets.")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("AssetInitWorker", "Error initializing assets", e)
            Result.retry()
        }
    }
}
