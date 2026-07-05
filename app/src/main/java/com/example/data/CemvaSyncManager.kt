package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncLog(
    val timestamp: String,
    val message: String,
    val isError: Boolean = false
)

class CemvaSyncManager private constructor(context: Context) {
    private val database = CemvaDatabase.getDatabase(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val sharedPrefs = context.getSharedPreferences("cemva_prefs", Context.MODE_PRIVATE)

    // Configuration
    var firebaseDbUrl = MutableStateFlow(sharedPrefs.getString("firebase_db_url", "https://cemva-build-default-rtdb.firebaseio.com/") ?: "https://cemva-build-default-rtdb.firebaseio.com/")
    var isAutoSyncEnabled = MutableStateFlow(sharedPrefs.getBoolean("is_auto_sync_enabled", true))

    fun updateFirebaseUrl(url: String) {
        firebaseDbUrl.value = url
        sharedPrefs.edit().putString("firebase_db_url", url).apply()
    }

    fun toggleAutoSync(enabled: Boolean) {
        isAutoSyncEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_auto_sync_enabled", enabled).apply()
    }

    // Status flows
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime

    private val _syncLogs = MutableStateFlow<List<SyncLog>>(emptyList())
    val syncLogs: StateFlow<List<SyncLog>> = _syncLogs

    companion object {
        @Volatile
        private var INSTANCE: CemvaSyncManager? = null

        fun getInstance(context: Context): CemvaSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CemvaSyncManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private fun addLog(message: String, isError: Boolean = false) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = SyncLog(timeStr, message, isError)
        _syncLogs.value = listOf(newLog) + _syncLogs.value.take(49) // Keep last 50 logs
    }

    suspend fun performFullSync(): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.SYNCING
        addLog("Starting full cloud sync...")

        var baseUrl = firebaseDbUrl.value.trim()
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            _syncStatus.value = SyncStatus.ERROR
            addLog("Invalid URL scheme. Must start with https://", true)
            return@withContext false
        }

        try {
            // 1. Sync Announcements (PULL & PUSH)
            addLog("Syncing Announcements...")
            val announcements = database.announcementDao().getAllAnnouncements().first()
            val announcementsAdapter = moshi.adapter<List<AnnouncementEntity>>(
                Types.newParameterizedType(List::class.java, AnnouncementEntity::class.java)
            )
            val annJson = announcementsAdapter.toJson(announcements)
            val annRequest = Request.Builder()
                .url("${baseUrl}announcements.json")
                .put(annJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(annRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync announcements: ${response.code}")
            }

            // 2. Sync Members (PUSH)
            addLog("Syncing Members roster...")
            val members = database.memberDao().getAllMembers().first()
            val membersAdapter = moshi.adapter<List<MemberEntity>>(
                Types.newParameterizedType(List::class.java, MemberEntity::class.java)
            )
            val membersJson = membersAdapter.toJson(members)
            val membersRequest = Request.Builder()
                .url("${baseUrl}members.json")
                .put(membersJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(membersRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync members: ${response.code}")
            }

            // 3. Sync Operations (PUSH)
            addLog("Syncing Operations and Duty rosters...")
            val operations = database.operationDao().getAllOperations().first()
            val opsAdapter = moshi.adapter<List<OperationEntity>>(
                Types.newParameterizedType(List::class.java, OperationEntity::class.java)
            )
            val opsJson = opsAdapter.toJson(operations)
            val opsRequest = Request.Builder()
                .url("${baseUrl}operations.json")
                .put(opsJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(opsRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync operations: ${response.code}")
            }

            // 4. Sync Equipment (PUSH)
            addLog("Syncing Equipment inventory...")
            val equipment = database.equipmentDao().getAllEquipment().first()
            val eqAdapter = moshi.adapter<List<EquipmentEntity>>(
                Types.newParameterizedType(List::class.java, EquipmentEntity::class.java)
            )
            val eqJson = eqAdapter.toJson(equipment)
            val eqRequest = Request.Builder()
                .url("${baseUrl}equipment.json")
                .put(eqJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(eqRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync equipment: ${response.code}")
            }

            // 5. Sync Attendance Logs (PUSH)
            addLog("Syncing Attendance logs...")
            val attendance = database.attendanceDao().getAllAttendance().first()
            val attendanceAdapter = moshi.adapter<List<AttendanceRecordEntity>>(
                Types.newParameterizedType(List::class.java, AttendanceRecordEntity::class.java)
            )
            val attJson = attendanceAdapter.toJson(attendance)
            val attRequest = Request.Builder()
                .url("${baseUrl}attendance.json")
                .put(attJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(attRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync attendance: ${response.code}")
            }

            // 6. Sync Patient Reports (PUSH)
            addLog("Syncing PCR / Incident reports...")
            val reports = database.reportDao().getAllReports().first()
            val reportsAdapter = moshi.adapter<List<ReportEntity>>(
                Types.newParameterizedType(List::class.java, ReportEntity::class.java)
            )
            val repJson = reportsAdapter.toJson(reports)
            val repRequest = Request.Builder()
                .url("${baseUrl}reports.json")
                .put(repJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(repRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync reports: ${response.code}")
            }

            // 7. Sync Trainings (PUSH)
            addLog("Syncing Trainings database...")
            val trainings = database.trainingDao().getAllTrainings().first()
            val trainingsAdapter = moshi.adapter<List<TrainingEntity>>(
                Types.newParameterizedType(List::class.java, TrainingEntity::class.java)
            )
            val trainJson = trainingsAdapter.toJson(trainings)
            val trainRequest = Request.Builder()
                .url("${baseUrl}trainings.json")
                .put(trainJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(trainRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync trainings: ${response.code}")
            }

            // 8. Sync User Accounts (PUSH)
            addLog("Syncing Secure User Accounts...")
            val accounts = database.userAccountDao().getAllUserAccounts().first()
            val accountsAdapter = moshi.adapter<List<UserAccountEntity>>(
                Types.newParameterizedType(List::class.java, UserAccountEntity::class.java)
            )
            val accJson = accountsAdapter.toJson(accounts)
            val accRequest = Request.Builder()
                .url("${baseUrl}user_accounts.json")
                .put(accJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(accRequest).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to sync user accounts: ${response.code}")
            }

            // Success! Update UI
            _syncStatus.value = SyncStatus.SUCCESS
            val nowTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            _lastSyncTime.value = nowTime
            addLog("Sync completed successfully!")
            true
        } catch (e: Exception) {
            Log.e("CemvaSyncManager", "Sync failed", e)
            _syncStatus.value = SyncStatus.ERROR
            addLog("Sync failed: ${e.message ?: "Unknown Connection Error"}", true)
            false
        }
    }
}
