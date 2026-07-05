package com.vfstr.smartclass.data.local.db

import android.content.Context
import androidx.room.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String,
    val period: Int,
    val subjectCode: String,
    val subjectName: String,
    val sessionType: String,
    val room: String,
    val facultyName: String,
    val department: String,
    val year: String,
    val section: String
)

@Entity(tableName = "student_cache")
data class StudentEntity(
    @PrimaryKey val rollNo: String,
    val name: String,
    val section: String,
    val year: String,
    val department: String,
    val faceEnrolled: Boolean,
    val biometricConsent: Boolean,
    val cgpa: Double,
    val attendancePercentagesJson: String // Serialized Map<String, Double>
)

@Entity(tableName = "session_cache")
data class SessionEntity(
    @PrimaryKey val id: String,
    val subjectName: String,
    val subjectCode: String,
    val sessionType: String,
    val room: String,
    val section: String,
    val department: String,
    val year: String,
    val count: Int,
    val total: Int,
    val status: String,
    val startTime: String,
    val durationMinutes: Int
)

@Entity(tableName = "dashboard_stats")
data class CachedStatsEntity(
    @PrimaryKey val id: String = "last_stats",
    val presentCount: Int,
    val totalCount: Int,
    val activeSessions: Int,
    val eventsCount: Int,
    val onlineDevices: Int,
    val totalDevices: Int,
    val offlineAlerts: Int,
    val reviewWeek: Int,
    val todayPercentage: Float,
    val weeklyPercentage: Float,
    val monthlyPercentage: Float
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String, // e.g., "ATTENDANCE_MARK", "OVERRIDE"
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SmartClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(slots: List<TimetableSlotEntity>)

    @Query("SELECT * FROM timetable_slots")
    suspend fun getTimetable(): List<TimetableSlotEntity>

    @Query("DELETE FROM timetable_slots")
    suspend fun clearTimetable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("SELECT * FROM student_cache")
    suspend fun getStudents(): List<StudentEntity>

    @Query("SELECT * FROM student_cache WHERE rollNo = :rollNo")
    suspend fun getStudentByRoll(rollNo: String): StudentEntity?

    @Query("DELETE FROM student_cache")
    suspend fun clearStudents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Query("SELECT * FROM session_cache")
    suspend fun getSessions(): List<SessionEntity>

    @Query("DELETE FROM session_cache")
    suspend fun clearSessions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStats(stats: CachedStatsEntity)

    @Query("SELECT * FROM dashboard_stats WHERE id = 'last_stats'")
    suspend fun getStats(): CachedStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getSyncQueue(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: Int)

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getSyncQueueCount(): Int
}

@Database(
    entities = [
        TimetableSlotEntity::class,
        StudentEntity::class,
        SessionEntity::class,
        CachedStatsEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun dao(): SmartClassDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "smartclass_local_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

