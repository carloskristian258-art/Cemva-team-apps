package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemberEntity::class,
        OperationEntity::class,
        ReportEntity::class,
        TrainingEntity::class,
        EquipmentEntity::class,
        AnnouncementEntity::class,
        AttendanceRecordEntity::class,
        UserAccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CemvaDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun operationDao(): OperationDao
    abstract fun reportDao(): ReportDao
    abstract fun trainingDao(): TrainingDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: CemvaDatabase? = null

        fun getDatabase(context: Context): CemvaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CemvaDatabase::class.java,
                    "cemva_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
