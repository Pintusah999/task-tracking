package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "daily_tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // format YYYY-MM-DD
    val time: String = "", // format e.g., "10:30 AM" or empty
    val category: String = "Personal",
    val priority: String = "Medium", // "Low", "Medium", "High"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
