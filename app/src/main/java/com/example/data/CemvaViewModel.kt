package com.example.data

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UserRole {
    GUEST,     // Applicant level: view-only basic, can apply
    MEMBER,    // Member level: view profiles, files, check-in
    ADMIN      // Administrator level: edit database, manage roster, approve applicants
}

data class GoogleIntegrationUrls(
    val googleFormUrl: String = "https://docs.google.com/forms/d/e/1FAIpQLSfD_uP2KxY-F_Wl5Yg_placeholder/viewform",
    val googleSheetsUrl: String = "https://docs.google.com/spreadsheets/d/1_placeholder_cemva_db/edit",
    val googleDriveUrl: String = "https://drive.google.com/drive/folders/placeholder_cemva_storage",
    val googleCalendarUrl: String = "https://calendar.google.com/calendar/u/0/embed?src=placeholder_cemva_calendar",
    val googleMapsUrl: String = "https://maps.app.goo.gl/placeholder_cemva_location",
    val gmailAddress: String = "carloskristian258@gmail.com",
    val googleSitesUrl: String = "https://sites.google.com/view/cemva-volunteer-site"
)

class CemvaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CemvaDatabase.getDatabase(application)
    private val repository = CemvaRepository(
        memberDao = database.memberDao(),
        operationDao = database.operationDao(),
        reportDao = database.reportDao(),
        trainingDao = database.trainingDao(),
        equipmentDao = database.equipmentDao(),
        announcementDao = database.announcementDao(),
        attendanceDao = database.attendanceDao()
    )

    // Current navigation screen
    var currentScreen = mutableStateOf("home")
    
    // Auth & Role State
    var currentUserEmail = mutableStateOf("carloskristian258@gmail.com")
    var currentUserRole = mutableStateOf(UserRole.ADMIN)
    var currentUserId = mutableStateOf("CEMVA-2026-001") // Carlos Kristian by default
    
    // Google Integration URLs
    var googleUrls = mutableStateOf(GoogleIntegrationUrls())

    // Live Database Flows
    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operations: StateFlow<List<OperationEntity>> = repository.allOperations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trainings: StateFlow<List<TrainingEntity>> = repository.allTrainings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equipment: StateFlow<List<EquipmentEntity>> = repository.allEquipment
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceRecordEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Query State
    var memberSearchQuery = mutableStateOf("")
    var equipmentSearchQuery = mutableStateOf("")

    // Selected member for Digital ID / Profile view
    var selectedMemberId = mutableStateOf("CEMVA-2026-001")

    // QR Code Verification result state
    var qrVerificationResult = mutableStateOf<MemberEntity?>(null)

    // User's training checklist state (saved dynamically in VM for the current user session)
    val userSkillsChecklist = mutableStateOf(
        mapOf(
            "Cardiopulmonary Resuscitation (CPR)" to true,
            "Automated External Defibrillator (AED)" to true,
            "Spine Board and Patient Immobilization" to true,
            "VHF Radio Communications" to true,
            "Splinting and Bandaging" to false,
            "Intravenous Access & Therapy" to false,
            "Incident Command System (ICS) basics" to true
        )
    )

    init {
        viewModelScope.launch {
            repository.populateInitialData()
        }
    }

    // Google Sign-In Simulation
    fun signInAs(email: String, role: UserRole, memberId: String) {
        currentUserEmail.value = email
        currentUserRole.value = role
        currentUserId.value = memberId
        selectedMemberId.value = memberId
    }

    fun logout() {
        currentUserEmail.value = "guest@cemva.org"
        currentUserRole.value = UserRole.GUEST
        currentUserId.value = ""
    }

    // Member Operations
    fun addMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.insertMember(member)
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun approveMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member.copy(isApproved = true, status = "Active"))
        }
    }

    // Operation Operations
    fun addOperation(operation: OperationEntity) {
        viewModelScope.launch {
            repository.insertOperation(operation)
        }
    }

    fun updateOperation(operation: OperationEntity) {
        viewModelScope.launch {
            repository.updateOperation(operation)
        }
    }

    fun deleteOperation(operation: OperationEntity) {
        viewModelScope.launch {
            repository.deleteOperation(operation)
        }
    }

    // Report Operations
    fun addReport(report: ReportEntity) {
        viewModelScope.launch {
            repository.insertReport(report)
        }
    }

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }

    // Training Operations
    fun addTraining(training: TrainingEntity) {
        viewModelScope.launch {
            repository.insertTraining(training)
        }
    }

    fun updateTraining(training: TrainingEntity) {
        viewModelScope.launch {
            repository.updateTraining(training)
        }
    }

    fun deleteTraining(training: TrainingEntity) {
        viewModelScope.launch {
            repository.deleteTraining(training)
        }
    }

    // Equipment Operations
    fun addEquipment(eq: EquipmentEntity) {
        viewModelScope.launch {
            repository.insertEquipment(eq)
        }
    }

    fun updateEquipment(eq: EquipmentEntity) {
        viewModelScope.launch {
            repository.updateEquipment(eq)
        }
    }

    fun deleteEquipment(eq: EquipmentEntity) {
        viewModelScope.launch {
            repository.deleteEquipment(eq)
        }
    }

    // Checkout/Checkin Equipment
    fun checkOutEquipment(eq: EquipmentEntity, checkedOutBy: String) {
        viewModelScope.launch {
            if (eq.quantity > 0) {
                val updated = eq.copy(
                    quantity = eq.quantity - 1,
                    status = if (eq.quantity - 1 == 0) "Checked Out" else eq.status,
                    lastCheckedOutBy = checkedOutBy,
                    checkoutDate = "2026-07-05 10:00 AM"
                )
                repository.updateEquipment(updated)
            }
        }
    }

    fun checkInEquipment(eq: EquipmentEntity) {
        viewModelScope.launch {
            val updated = eq.copy(
                quantity = eq.quantity + 1,
                status = "Available",
                lastCheckedOutBy = "",
                checkoutDate = ""
            )
            repository.updateEquipment(updated)
        }
    }

    // Announcement Operations
    fun addAnnouncement(ann: AnnouncementEntity) {
        viewModelScope.launch {
            repository.insertAnnouncement(ann)
        }
    }

    fun deleteAnnouncement(ann: AnnouncementEntity) {
        viewModelScope.launch {
            repository.deleteAnnouncement(ann)
        }
    }

    // Attendance Operations
    fun recordAttendance(memberId: String, memberName: String, type: String, activity: String, method: String = "QR Code") {
        viewModelScope.launch {
            val currentDateTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val date = currentDateTime.substringBefore(" ")
            val time = currentDateTime.substringAfter(" ")
            repository.insertAttendance(
                AttendanceRecordEntity(
                    memberId = memberId,
                    memberName = memberName,
                    date = date,
                    time = time,
                    type = type,
                    method = method,
                    activity = activity
                )
            )
        }
    }

    // QR Verification logic
    fun verifyQrToken(token: String) {
        viewModelScope.launch {
            val matched = members.value.find { it.qrCodeToken == token }
            qrVerificationResult.value = matched
        }
    }

    fun clearQrVerification() {
        qrVerificationResult.value = null
    }

    // Toggle skill checklist
    fun toggleUserSkill(skillName: String) {
        val current = userSkillsChecklist.value.toMutableMap()
        current[skillName] = !(current[skillName] ?: false)
        userSkillsChecklist.value = current
    }

    // Update URLs
    fun updateGoogleUrls(urls: GoogleIntegrationUrls) {
        googleUrls.value = urls
    }
}
