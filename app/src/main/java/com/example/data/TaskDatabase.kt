package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class TaskDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.taskDao())
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDao) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val todayStr = sdf.format(calendar.time)

            // Seed high-quality default daily tasks to guide user's starting journey
            taskDao.insertTask(
                Task(
                    title = "Morning focus workout & hydration",
                    date = todayStr,
                    time = "07:30 AM",
                    category = "Health",
                    priority = "High",
                    isCompleted = true
                )
            )
            taskDao.insertTask(
                Task(
                    title = "Review daily schedule & project milestones",
                    date = todayStr,
                    time = "09:00 AM",
                    category = "Work",
                    priority = "High",
                    isCompleted = false
                )
            )
            taskDao.insertTask(
                Task(
                    title = "Pick up grocery list & healthy meal ingredients",
                    date = todayStr,
                    time = "02:15 PM",
                    category = "Personal",
                    priority = "Low",
                    isCompleted = false
                )
            )
            taskDao.insertTask(
                Task(
                    title = "Deep dive into Kotlin Coroutines & Room",
                    date = todayStr,
                    time = "05:00 PM",
                    category = "Study",
                    priority = "Medium",
                    isCompleted = false
                )
            )
            taskDao.insertTask(
                Task(
                    title = "Plan schedule for tomorrow & power-down",
                    date = todayStr,
                    time = "09:30 PM",
                    category = "Personal",
                    priority = "Medium",
                    isCompleted = false
                )
            )
        }
    }
}
