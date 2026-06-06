package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TaskDatabase.getDatabase(application, viewModelScope)
    private val repository = TaskRepository(database.taskDao())

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Reactive task query focused on selected date
    private val baseTasks = _selectedDate.flatMapLatest { date ->
        repository.getTasksForDate(date)
    }

    // Dynamic completed-tasks streak calculation (Chronologically contiguous days)
    val streakDays: StateFlow<Int> = repository.allTasks.map { allTasksList ->
        if (allTasksList.isEmpty()) return@map 0
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val completedDatesList = allTasksList
            .filter { it.isCompleted }
            .map { it.date }
            .toSet()
            .mapNotNull { 
                try { format.parse(it) } catch(e: Exception) { null } 
            }
            .sortedDescending() // newest first
            
        if (completedDatesList.isEmpty()) return@map 0
        
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        
        fun isSameDay(d1: Date, d2: Date): Boolean {
            val c1 = Calendar.getInstance().apply { time = d1 }
            val c2 = Calendar.getInstance().apply { time = d2 }
            return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                   c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
        }
        
        val hasCompletedRecent = completedDatesList.any { isSameDay(it, today) || isSameDay(it, yesterday) }
        if (!hasCompletedRecent) return@map 0
        
        var streak = 0
        var currentCheckDate = if (completedDatesList.any { isSameDay(it, today) }) today else yesterday
        
        while (true) {
            val hasCompletedCurrentDay = completedDatesList.any { isSameDay(it, currentCheckDate) }
            if (hasCompletedCurrentDay) {
                streak++
                val tempCal = Calendar.getInstance().apply { time = currentCheckDate }
                tempCal.add(Calendar.DAY_OF_YEAR, -1)
                currentCheckDate = tempCal.time
            } else {
                break
            }
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Combined filtered task stream for real-time searching and filtering
    val tasks: StateFlow<List<Task>> = combine(
        baseTasks,
        _selectedCategory,
        _searchQuery
    ) { rawTasks, category, query ->
        rawTasks.filter { task ->
            val matchCategory = category == "All" || task.category.equals(category, ignoreCase = true)
            val matchQuery = query.isEmpty() || task.title.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Completion fraction for today's visual meter
    val dayProgress: StateFlow<Float> = baseTasks.map { list ->
        if (list.isEmpty()) 0f
        else {
            val completed = list.count { it.isCompleted }
            completed.toFloat() / list.size.toFloat()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Complete / Total states
    val dayStats: StateFlow<Pair<Int, Int>> = baseTasks.map { list ->
        val completed = list.count { it.isCompleted }
        Pair(completed, list.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    // Interactive week horizontal list
    private val _weekDays = MutableStateFlow<List<DateItem>>(emptyList())
    val weekDays: StateFlow<List<DateItem>> = _weekDays.asStateFlow()

    init {
        generateCurrentWeek()
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
        _weekDays.update { days ->
            days.map { it.copy(isSelected = it.dateString == dateString) }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(title: String, time: String, category: String, priority: String) {
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                date = _selectedDate.value,
                time = time.trim(),
                category = category,
                priority = priority,
                isCompleted = false
            )
            repository.insert(newTask)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    private fun generateCurrentWeek() {
        val formats = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())

        val calendar = Calendar.getInstance()
        // Align calendar to the start of the week (Monday)
        val todayStr = getTodayDateString()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val days = mutableListOf<DateItem>()
        for (i in 0 until 7) {
            val dStr = formats.format(calendar.time)
            days.add(
                DateItem(
                    dayName = dayNameFormat.format(calendar.time),
                    dayNumber = dayNumberFormat.format(calendar.time),
                    dateString = dStr,
                    isSelected = dStr == todayStr
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        _weekDays.value = days
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }
}

data class DateItem(
    val dayName: String,
    val dayNumber: String,
    val dateString: String,
    val isSelected: Boolean
)
