package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)
}

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automations")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(automation: AutomationEntity)

    @Update
    suspend fun updateAutomation(automation: AutomationEntity)

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun deleteAutomation(id: String)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM connected_channel WHERE id = 'active_channel' LIMIT 1")
    fun getChannel(): Flow<ChannelEntity?>

    @Query("SELECT * FROM connected_channel WHERE id = 'active_channel' LIMIT 1")
    suspend fun getChannelSync(): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChannel(channel: ChannelEntity)

    @Query("DELETE FROM connected_channel WHERE id = 'active_channel'")
    suspend fun clearChannel()
}

@Dao
interface ScheduledJobDao {
    @Query("SELECT * FROM scheduled_jobs ORDER BY createdTimestamp DESC")
    fun getAllJobs(): Flow<List<ScheduledJobEntity>>

    @Query("SELECT * FROM scheduled_jobs WHERE id = :id")
    suspend fun getJobById(id: String): ScheduledJobEntity?

    @Query("SELECT * FROM scheduled_jobs WHERE deduplicationKey = :key AND status IN ('PENDING', 'RUNNING') LIMIT 1")
    suspend fun findActiveJobByDeduplicationKey(key: String): ScheduledJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ScheduledJobEntity)

    @Update
    suspend fun updateJob(job: ScheduledJobEntity)

    @Query("UPDATE scheduled_jobs SET status = 'EMERGENCY_STOPPED' WHERE status IN ('PENDING', 'RUNNING')")
    suspend fun emergencyStopAllJobs()

    @Query("UPDATE scheduled_jobs SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelJob(id: String)

    @Query("DELETE FROM scheduled_jobs WHERE id = :id")
    suspend fun deleteJob(id: String)
}

@Dao
interface AnalystReportDao {
    @Query("SELECT * FROM analyst_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<AnalystReportEntity>>

    @Query("SELECT * FROM analyst_reports ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReport(): AnalystReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: AnalystReportEntity)

    @Query("DELETE FROM analyst_reports WHERE id = :id")
    suspend fun deleteReport(id: String)
}

@Database(
    entities = [
        ProjectEntity::class,
        AutomationEntity::class,
        ChannelEntity::class,
        ScheduledJobEntity::class,
        AnalystReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun automationDao(): AutomationDao
    abstract fun channelDao(): ChannelDao
    abstract fun scheduledJobDao(): ScheduledJobDao
    abstract fun analystReportDao(): AnalystReportDao
}
