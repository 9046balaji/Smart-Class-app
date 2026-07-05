package com.vfstr.smartclass.utils

import java.time.LocalDate

data class BranchInfo(val code: String, val name: String, val school: String)

val VFSTR_BRANCHES = listOf(
    BranchInfo("CS", "Computer Science", "CSE"),
    BranchInfo("AIANDML", "Artificial Intelligence and Machine Learning", "CSE"),
    BranchInfo("IT", "Information Technology", "CSE"),
    BranchInfo("ECE", "Electronics and Communication", "ECE"),
    BranchInfo("EEE", "Electrical and Electronics", "EEE"),
    BranchInfo("MECH", "Mechanical Engineering", "MECH"),
    BranchInfo("CIVIL", "Civil Engineering", "CIVIL"),
    BranchInfo("BIOTECH", "Biotechnology", "BIOTECH"),
    BranchInfo("CHEM", "Chemical Engineering", "CHEM"),
)

fun inferDepartment(branch: String): String {
    val normalized = branch.uppercase().replace(Regex("[^A-Z0-9]"), "")
    
    val directMatch = VFSTR_BRANCHES.find {
        (it.code.uppercase() == normalized) || (it.name.uppercase().replace(Regex("[^A-Z0-9]"), "") == normalized)
    }
    directMatch?.let { return it.school }
    
    return when {
        "BIO" in normalized                        -> "BIOTECH"
        "CHEM" in normalized                       -> "CHEM"
        "CIVIL" in normalized                      -> "CIVIL"
        "MECH" in normalized || normalized == "RA" -> "MECH"
        "ECE" in normalized                        -> "ECE"
        "EEE" in normalized || normalized == "EE"  -> "EEE"
        "CSE" in normalized || "CS" in normalized
            || "IT" in normalized                  -> "CSE"
        else                                       -> branch.uppercase()
    }
}

fun getYearLabel(admissionYearStr: String): String {
    val admissionYear = admissionYearStr.toIntOrNull() ?: return admissionYearStr
    val now = LocalDate.now()
    val academicYear = if (now.monthValue < 7) now.year - 1 else now.year
    return when (val yearNumber = academicYear - admissionYear + 1) {
        1 -> "1st Year"
        2 -> "2nd Year"
        3 -> "3rd Year"
        4 -> "4th Year"
        else -> "${yearNumber}th Year ($admissionYearStr)"
    }
}

fun toScannerSectionId(branch: String, batchYear: String, section: String): String {
    val normalized = branch.uppercase().replace(Regex("[^A-Z0-9]"), "")
    val branchAliases = mapOf("AIANDML" to "AIML")
    val canonicalBranch = branchAliases[normalized] ?: branch.uppercase()
    
    val batchNum = batchYear.toIntOrNull()
    val canonicalYear = if (batchNum != null) {
        val now = LocalDate.now()
        val academicYear = if (now.monthValue >= 7) now.year else now.year - 1
        val studyYear = (academicYear - batchNum + 1).coerceIn(1, 4)
        studyYear.toString()
    } else batchYear
    
    return "$canonicalBranch-$canonicalYear-$section"
}
