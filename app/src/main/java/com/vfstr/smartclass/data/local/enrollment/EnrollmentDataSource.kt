package com.vfstr.smartclass.data.local.enrollment

import android.content.Context
import com.vfstr.smartclass.domain.models.LocalEnrollmentStudent
import com.vfstr.smartclass.domain.models.StudentHierarchy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollmentDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cache: StudentHierarchy by lazy { buildHierarchy() }

    fun getHierarchy(): StudentHierarchy = cache

    private fun buildHierarchy(): StudentHierarchy {
        val files = listOf(
            "B. Tech.. Details - 2023 - 27.csv" to "2023",
            "B. Tech.. Details - 2024-28.csv" to "2024",
            "B. Tech.. Details - 2025-29.csv" to "2025"
        )
        val allStudents = files.flatMap { (fileName, year) ->
            try {
                context.assets.open(fileName)
                    .bufferedReader()
                    .use { parseEnrollmentCsv(it.readText(), year) }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
        return allStudents.groupBy { it.year }
            .mapValues { (_, yearStudents) ->
                yearStudents.groupBy { it.branch.uppercase() }
                    .mapValues { (_, branchStudents) ->
                        branchStudents.groupBy { it.section.trim() }
                    }
            }
    }

    private fun parseEnrollmentCsv(csvText: String, year: String): List<LocalEnrollmentStudent> {
        val allLines = csvText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (allLines.isEmpty()) return emptyList()

        // Find header row starting with "Sl.No"
        val headerIdx = allLines.indexOfFirst { it.startsWith("Sl.No") }
        if (headerIdx == -1) return emptyList()

        val headerLine = allLines[headerIdx]
        val header = headerLine.split(",").map { it.trim() }

        val rollIdx = header.indexOf("Registerno")
        val nameIdx = header.indexOf("Name")
        val branchIdx = header.indexOf("Branch")
        val sectionIdx = header.indexOf("Section")
        val dobIdx = header.indexOf("Date of Birth")

        return allLines.drop(headerIdx + 1).mapNotNull { line ->
            // Use a simple regex to split by comma while respecting quotes for addresses
            val cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim().removeSurrounding("\"") }
            
            if (cols.size <= rollIdx || cols.size <= nameIdx) return@mapNotNull null
            
            val rollNo = cols.getOrElse(rollIdx) { "" }
            if (rollNo.isEmpty()) return@mapNotNull null

            LocalEnrollmentStudent(
                student_id = rollNo,
                name = cols.getOrElse(nameIdx) { "" },
                roll_number = rollNo,
                branch = cols.getOrElse(branchIdx) { "" },
                year = year,
                section = cols.getOrElse(sectionIdx) { "" },
                dob = cols.getOrElse(dobIdx) { "" },
                photo_path = "photos/$year/$rollNo.jpg"
            )
        }
    }
}
