package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String, // e.g., "CEMVA-2026-012"
    val name: String,
    val status: String,       // "Active", "Inactive", "Applicant"
    val position: String,     // "EMT", "Medic", "First Responder", "Rescue Officer", "Volunteer"
    val department: String,   // "Emergency Response", "Training", "Logistics", "Operations", "Medical"
    val email: String,
    val phone: String,
    val qrCodeToken: String,  // Token for Digital ID QR scanning
    val joinedDate: String,
    val isApproved: Boolean = true
)

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,              // "Deployment", "Event Standby", "Duty Roster"
    val date: String,
    val location: String,
    val details: String,
    val assignedPersonnel: String, // Comma-separated names or IDs
    val status: String             // "Scheduled", "Active", "Completed"
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporterName: String,
    val type: String,              // "Incident Report", "Patient Care Report"
    val date: String,
    val location: String,
    val patientName: String = "",
    val patientAgeGender: String = "",
    val chiefComplaint: String = "",
    val vitals: String = "",       // e.g., "BP: 120/80, HR: 72, RR: 16"
    val interventions: String = "",
    val narrative: String
)

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val instructor: String,
    val location: String,
    val status: String,            // "Completed", "Upcoming"
    val materialsUrl: String,      // Google Drive link
    val category: String           // "BLS", "ALS", "MFR", "ICS", "Ambulance Ops"
)

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,          // "Medical Kits", "Radios", "AED", "Stretchers", "Other"
    val quantity: Int,
    val status: String,            // "Available", "Checked Out", "Maintenance"
    val lastCheckedOutBy: String = "",
    val checkoutDate: String = ""
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val author: String,
    val priority: String           // "High", "Normal"
)

@Entity(tableName = "attendance")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: String,
    val memberName: String,
    val date: String,
    val time: String,
    val type: String,              // "Check-In", "Check-Out"
    val method: String,            // "QR Code", "Manual"
    val activity: String           // "Duty", "Training", "Deployment"
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val role: String,              // "GUEST", "MEMBER", "ADMIN"
    val memberId: String,          // Matches MemberEntity.id
    val isApproved: Boolean = false
)

