package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: String, // YYYY-MM-DD HH:mm
    val priority: String = "Normal", // Low, Normal, High
    val isCompleted: Boolean = false,
    val category: String = "Duty" // Duty, Training, Personal, Admin
)
