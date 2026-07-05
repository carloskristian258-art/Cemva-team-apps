package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: String)
}

@Dao
interface OperationDao {
    @Query("SELECT * FROM operations ORDER BY date DESC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)
}

@Dao
interface TrainingDao {
    @Query("SELECT * FROM trainings ORDER BY date DESC")
    fun getAllTrainings(): Flow<List<TrainingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(training: TrainingEntity)

    @Update
    suspend fun updateTraining(training: TrainingEntity)

    @Delete
    suspend fun deleteTraining(training: TrainingEntity)
}

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY name ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity)

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, time DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecordEntity)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Int)
}
