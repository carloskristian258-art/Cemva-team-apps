package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CemvaRepository(
    private val memberDao: MemberDao,
    private val operationDao: OperationDao,
    private val reportDao: ReportDao,
    private val trainingDao: TrainingDao,
    private val equipmentDao: EquipmentDao,
    private val announcementDao: AnnouncementDao,
    private val attendanceDao: AttendanceDao,
    private val userAccountDao: UserAccountDao,
    private val statusLogDao: StatusLogDao,
    private val emergencyReportDao: EmergencyReportDao,
    private val emergencyAlertDao: EmergencyAlertDao,
    private val reminderDao: ReminderDao
) {
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()
    val allOperations: Flow<List<OperationEntity>> = operationDao.getAllOperations()
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()
    val allTrainings: Flow<List<TrainingEntity>> = trainingDao.getAllTrainings()
    val allEquipment: Flow<List<EquipmentEntity>> = equipmentDao.getAllEquipment()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()
    val allAttendance: Flow<List<AttendanceRecordEntity>> = attendanceDao.getAllAttendance()
    val allUserAccounts: Flow<List<UserAccountEntity>> = userAccountDao.getAllUserAccounts()
    val allStatusLogs: Flow<List<StatusLogEntity>> = statusLogDao.getAllStatusLogs()
    val allEmergencyReports: Flow<List<EmergencyReportEntity>> = emergencyReportDao.getAllEmergencyReports()
    val allEmergencyAlerts: Flow<List<EmergencyAlertEntity>> = emergencyAlertDao.getAllEmergencyAlerts()
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    fun getLogsByMemberId(memberId: String): Flow<List<StatusLogEntity>> = statusLogDao.getLogsByMemberId(memberId)

    suspend fun insertStatusLog(log: StatusLogEntity) = statusLogDao.insertStatusLog(log)

    suspend fun insertEmergencyReport(report: EmergencyReportEntity) = emergencyReportDao.insertEmergencyReport(report)
    suspend fun updateEmergencyReport(report: EmergencyReportEntity) = emergencyReportDao.updateEmergencyReport(report)
    suspend fun deleteEmergencyReport(report: EmergencyReportEntity) = emergencyReportDao.deleteEmergencyReport(report)

    suspend fun insertEmergencyAlert(alert: EmergencyAlertEntity) = emergencyAlertDao.insertEmergencyAlert(alert)
    suspend fun deleteEmergencyAlert(alert: EmergencyAlertEntity) = emergencyAlertDao.deleteEmergencyAlert(alert)

    suspend fun insertReminder(reminder: ReminderEntity) = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun clearCompletedReminders() = reminderDao.clearCompletedReminders()

    suspend fun getMemberById(id: String): MemberEntity? = memberDao.getMemberById(id)
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity? = userAccountDao.getUserAccountByEmail(email)

    suspend fun insertUserAccount(user: UserAccountEntity) = userAccountDao.insertUserAccount(user)
    suspend fun updateUserAccount(user: UserAccountEntity) = userAccountDao.updateUserAccount(user)
    suspend fun deleteUserAccount(user: UserAccountEntity) = userAccountDao.deleteUserAccount(user)

    suspend fun insertMember(member: MemberEntity) = memberDao.insertMember(member)
    suspend fun updateMember(member: MemberEntity) = memberDao.updateMember(member)
    suspend fun deleteMember(member: MemberEntity) = memberDao.deleteMember(member)
    suspend fun deleteMemberById(id: String) = memberDao.deleteMemberById(id)

    suspend fun insertOperation(operation: OperationEntity) = operationDao.insertOperation(operation)
    suspend fun updateOperation(operation: OperationEntity) = operationDao.updateOperation(operation)
    suspend fun deleteOperation(operation: OperationEntity) = operationDao.deleteOperation(operation)

    suspend fun insertReport(report: ReportEntity) = reportDao.insertReport(report)
    suspend fun deleteReport(report: ReportEntity) = reportDao.deleteReport(report)

    suspend fun insertTraining(training: TrainingEntity) = trainingDao.insertTraining(training)
    suspend fun updateTraining(training: TrainingEntity) = trainingDao.updateTraining(training)
    suspend fun deleteTraining(training: TrainingEntity) = trainingDao.deleteTraining(training)

    suspend fun insertEquipment(equipment: EquipmentEntity) = equipmentDao.insertEquipment(equipment)
    suspend fun updateEquipment(equipment: EquipmentEntity) = equipmentDao.updateEquipment(equipment)
    suspend fun deleteEquipment(equipment: EquipmentEntity) = equipmentDao.deleteEquipment(equipment)

    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = announcementDao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity) = announcementDao.deleteAnnouncement(announcement)

    suspend fun insertAttendance(attendance: AttendanceRecordEntity) = attendanceDao.insertAttendance(attendance)
    suspend fun deleteAttendanceById(id: Int) = attendanceDao.deleteAttendanceById(id)

    suspend fun populateInitialData() {
        // Only populate if announcements or members are empty
        val currentAnnouncements = allAnnouncements.first()
        if (currentAnnouncements.isEmpty()) {
            // 1. Prepopulate Announcements
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    title = "HEAVY RAIN ADVISORY: Cavite Province Alert",
                    content = "La Niña enhanced monsoon is bringing continuous rain. All EMS teams are on standard yellow standby. Ready your personal go-bags and emergency gears. Log details on duty rosters if activated.",
                    date = "2026-07-05",
                    author = "Rescue Officer Carlos Kristian",
                    priority = "High"
                )
            )
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Monthly General Assembly and Skill Refresher",
                    content = "Our quarterly mass assembly will be held at Imus Sports Complex from 0800H to 1700H. Topics include Advanced Airway Management and Incident Command refreshers. Attendance is required for active EMT credentials.",
                    date = "2026-07-02",
                    author = "Training Director",
                    priority = "Normal"
                )
            )
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Google Workspace Form Synchronized",
                    content = "CEMVA Volunteer forms, application rosters, and SOP checklists are now officially synced with Google Drive & Google Sheets! Keep documents loaded offline.",
                    date = "2026-06-28",
                    author = "System Administrator",
                    priority = "Normal"
                )
            )

            // 2. Prepopulate Members
            val members = listOf(
                MemberEntity(
                    id = "CEMVA-2026-001",
                    name = "Carlos Kristian",
                    status = "Active",
                    position = "Rescue Officer",
                    department = "Operations",
                    email = "carloskristian258@gmail.com",
                    phone = "0917-555-0101",
                    qrCodeToken = "M-CEMVA-CK01",
                    joinedDate = "2023-01-15"
                ),
                MemberEntity(
                    id = "CEMVA-2026-002",
                    name = "Maria Santos",
                    status = "Active",
                    position = "EMT",
                    department = "Emergency Response",
                    email = "maria.santos@cemva.org",
                    phone = "0918-123-4567",
                    qrCodeToken = "M-CEMVA-MS02",
                    joinedDate = "2024-03-20"
                ),
                MemberEntity(
                    id = "CEMVA-2026-003",
                    name = "Juan Dela Cruz",
                    status = "Active",
                    position = "Medic",
                    department = "Medical",
                    email = "juan.delacruz@cemva.org",
                    phone = "0920-987-6543",
                    qrCodeToken = "M-CEMVA-JD03",
                    joinedDate = "2024-05-10"
                ),
                MemberEntity(
                    id = "CEMVA-2026-004",
                    name = "Dave Almeda",
                    status = "Active",
                    position = "First Responder",
                    department = "Logistics",
                    email = "dave.almeda@cemva.org",
                    phone = "0905-234-5678",
                    qrCodeToken = "M-CEMVA-DA04",
                    joinedDate = "2025-02-11"
                ),
                MemberEntity(
                    id = "CEMVA-2026-005",
                    name = "Sarah Garcia",
                    status = "Inactive",
                    position = "Volunteer",
                    department = "Training",
                    email = "sarah.garcia@cemva.org",
                    phone = "0916-444-5555",
                    qrCodeToken = "M-CEMVA-SG05",
                    joinedDate = "2025-06-01"
                )
            )
            for (m in members) {
                memberDao.insertMember(m)
            }

            // 3. Prepopulate Operations & Duty Rosters
            operationDao.insertOperation(
                OperationEntity(
                    title = "Cavite Day-Shift Duty Base HQ",
                    type = "Duty Roster",
                    date = "2026-07-05",
                    location = "CEMVA Provincial HQ",
                    details = "Day-shift routine standby. Ambulance response teams: EMT Maria Santos, Medic Juan Dela Cruz.",
                    assignedPersonnel = "Maria Santos, Juan Dela Cruz",
                    status = "Active"
                )
            )
            operationDao.insertOperation(
                OperationEntity(
                    title = "Tagaytay Eco-Marathon Standby",
                    type = "Event Standby",
                    date = "2026-07-12",
                    location = "Tagaytay Picnic Grove",
                    details = "Provide high-altitude ambulance staging and medic coverage for 500+ runners.",
                    assignedPersonnel = "Carlos Kristian, Dave Almeda",
                    status = "Scheduled"
                )
            )
            operationDao.insertOperation(
                OperationEntity(
                    title = "Bacoor Flood Extraction Deployment",
                    type = "Deployment",
                    date = "2026-06-18",
                    location = "Brgy. Habay, Bacoor City",
                    details = "Assisted in evacuation of 12 senior citizens during heavy tropical storm flooding.",
                    assignedPersonnel = "Carlos Kristian, Maria Santos, Dave Almeda",
                    status = "Completed"
                )
            )

            // 4. Prepopulate Trainings
            trainingDao.insertTraining(
                TrainingEntity(
                    title = "AHA CPR and Basic Life Support",
                    date = "2026-07-19",
                    instructor = "Dr. Roberto Reyes, MD",
                    location = "CEMVA Central Training Room",
                    status = "Upcoming",
                    materialsUrl = "https://docs.google.com/presentation/d/placeholder-cpr-guide",
                    category = "BLS"
                )
            )
            trainingDao.insertTraining(
                TrainingEntity(
                    title = "Pre-Hospital Trauma Life Support (PHTLS)",
                    date = "2026-08-02",
                    instructor = "Rescue Officer Carlos Kristian",
                    location = "Cavite Sports Complex Arena",
                    status = "Upcoming",
                    materialsUrl = "https://docs.google.com/document/d/placeholder-trauma-ops",
                    category = "MFR"
                )
            )
            trainingDao.insertTraining(
                TrainingEntity(
                    title = "Mass Casualty Incident Command System",
                    date = "2026-06-10",
                    instructor = "Operations Chief Alcantara",
                    location = "Bacoor Fire Station",
                    status = "Completed",
                    materialsUrl = "https://docs.google.com/document/d/placeholder-command-ics",
                    category = "ICS"
                )
            )

            // 5. Prepopulate Equipment Inventory
            equipmentDao.insertEquipment(
                EquipmentEntity(
                    name = "M3 Trauma responder pack A",
                    category = "Medical Kits",
                    quantity = 6,
                    status = "Available"
                )
            )
            equipmentDao.insertEquipment(
                EquipmentEntity(
                    name = "Motorola VHF Rugged VHF Radio GP328",
                    category = "Radios",
                    quantity = 15,
                    status = "Available"
                )
            )
            equipmentDao.insertEquipment(
                EquipmentEntity(
                    name = "Philips HeartStart FRx AED Unit",
                    category = "AED",
                    quantity = 3,
                    status = "Available"
                )
            )
            equipmentDao.insertEquipment(
                EquipmentEntity(
                    name = "Ferno Spine Board with straps",
                    category = "Stretchers",
                    quantity = 5,
                    status = "Available"
                )
            )

            // 6. Prepopulate Reports
            reportDao.insertReport(
                ReportEntity(
                    reporterName = "Maria Santos, EMT",
                    type = "Patient Care Report",
                    date = "2026-07-04",
                    location = "Aguinaldo Highway, Imus",
                    patientName = "Jane Doe",
                    patientAgeGender = "28 / Female",
                    chiefComplaint = "Laceration on right leg due to motorcycle skid",
                    vitals = "BP: 110/70, HR: 88, RR: 18, SpO2: 99%",
                    interventions = "Direct pressure, sterile dressing applied, wound irrigated with saline, splinted, transported via Bravo Ambulance.",
                    narrative = "Patient skidded while driving a motorcycle. Sustained clean laceration, fully conscious, oriented. Refused full spinal immobilization."
                )
            )

            // 7. Prepopulate Attendance Logs
            attendanceDao.insertAttendance(
                AttendanceRecordEntity(
                    memberId = "CEMVA-2026-001",
                    memberName = "Carlos Kristian",
                    date = "2026-07-05",
                    time = "08:00 AM",
                    type = "Check-In",
                    method = "QR Code",
                    activity = "Duty"
                )
            )

            // 8. Prepopulate User Accounts for Login & Sync
            userAccountDao.insertUserAccount(
                UserAccountEntity(
                    email = "carloskristian258@gmail.com",
                    passwordHash = "admin123", // Simple plain-text or hash for this application
                    name = "Carlos Kristian",
                    phone = "0917-555-0101",
                    role = "ADMIN",
                    memberId = "CEMVA-2026-001",
                    isApproved = true
                )
            )
            userAccountDao.insertUserAccount(
                UserAccountEntity(
                    email = "maria.santos@cemva.org",
                    passwordHash = "maria123",
                    name = "Maria Santos",
                    phone = "0918-123-4567",
                    role = "MEMBER",
                    memberId = "CEMVA-2026-002",
                    isApproved = true
                )
            )
            userAccountDao.insertUserAccount(
                UserAccountEntity(
                    email = "juan.delacruz@cemva.org",
                    passwordHash = "juan123",
                    name = "Juan Dela Cruz",
                    phone = "0920-987-6543",
                    role = "MEMBER",
                    memberId = "CEMVA-2026-003",
                    isApproved = true
                )
            )

            // 9. Prepopulate Reminders
            reminderDao.insertReminder(
                ReminderEntity(
                    title = "Equipment Maintenance",
                    description = "Monthly check of all medical kits and radio batteries.",
                    dateTime = "2026-07-10 09:00",
                    priority = "High",
                    category = "Admin"
                )
            )
            reminderDao.insertReminder(
                ReminderEntity(
                    title = "Skill Audit Submission",
                    description = "Submit your updated skills checklist for Q3 validation.",
                    dateTime = "2026-07-15 17:00",
                    priority = "Normal",
                    category = "Training"
                )
            )
        }
    }
}
