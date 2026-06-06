package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskTrackerScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val weekDays by viewModel.weekDays.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val progress by viewModel.dayProgress.collectAsStateWithLifecycle()
    val stats by viewModel.dayStats.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val categories = listOf("All", "Work", "Personal", "Health", "Study", "Finance")

    // Personalize greeting from user email metadata dynamically
    val userNameGreeting = remember {
        val email = "premsah99999@gmail.com"
        val namePart = email.substringBefore("@")
        val clean = namePart.replace(Regex("[0-9]"), "").replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
        if (clean.length > 2) "Hello, $clean" else "Hello, Special Guest"
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground),
        containerColor = BentoBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                // Top row: App branding header matching M3 structure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatHeaderDateLabel(selectedDate),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMediumLabel,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = userNameGreeting,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnBackground,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .drawBehind {
                                    drawCircle(
                                        color = BentoBorderColor,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Toggle Search Input",
                                tint = BentoPurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // M3 Styled Avatar Circle representation from Bento Mock
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BentoPrimaryCardBg)
                                .drawBehind {
                                    drawCircle(
                                        color = BentoPrimaryCardAccent,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile icon",
                                tint = BentoPrimaryCardOnText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Search expanded block
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .testTag("task_search_input"),
                        placeholder = { Text("Filter daily tasks...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = BentoPurpleAccent,
                            unfocusedBorderColor = BentoBorderColor
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BentoPurpleAccent
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search query",
                                        tint = BentoMediumLabel
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }
            }
        },
        bottomBar = {
            // High fidelity replication of Bento Bottom Navigation matching HTML mockup
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = BentoSurfaceGray,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BentoBorderColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Tasks (Active)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = {}).padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoPrimaryCardBg)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Tasks Home active",
                                tint = BentoPrimaryCardOnText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tasks",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryCardOnText
                        )
                    }

                    // Item 2: Timeline (Secondary)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = {}).padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Timeline Mode inactive",
                            tint = BentoMediumLabel,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Timeline",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMediumLabel
                        )
                    }

                    // Floating Centered Custom FAB Button as in the bottom navigation schema
                    Box(
                        modifier = Modifier
                            .offset(y = (-14).dp)
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoPinkCardOnText)
                            .clickable { showAddDialog = true }
                            .testTag("add_task_fab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Open add task dialog",
                            tint = BentoPinkCardBg,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Item 3: Stats
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = {}).padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = "Stats Mode inactive",
                            tint = BentoMediumLabel,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stats",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMediumLabel
                        )
                    }

                    // Item 4: Settings
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(onClick = {}).padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings Mode inactive",
                            tint = BentoMediumLabel,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Settings",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMediumLabel
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Horizontal calendar day row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weekDays) { day ->
                        val isSelected = day.isSelected
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(78.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) BentoPurpleAccent
                                    else Color.White
                                )
                                .clickable { viewModel.selectDate(day.dateString) }
                                .drawBehind {
                                    if (!isSelected) {
                                        drawRoundRect(
                                            color = BentoBorderColor,
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                        )
                                    }
                                }
                                .testTag("date_chip_${day.dateString}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayName.uppercase(Locale.getDefault()),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f)
                                    else BentoMediumLabel.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day.dayNumber,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else BentoOnBackground
                                )
                            }
                        }
                    }
                }
            }

            // High Fidelity Featured Bento Card (RowSpan=2, ColSpan=2 in Bento mockup)
            item {
                // Find today's prioritized uncompleted task
                val featuredTask = remember(tasks) {
                    tasks.firstOrNull { !it.isCompleted && it.priority == "High" }
                        ?: tasks.firstOrNull { !it.isCompleted }
                        ?: tasks.firstOrNull { it.isCompleted }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(BentoPrimaryCardBg)
                        .padding(22.dp)
                        .testTag("featured_bento_box")
                ) {
                    // Decorative gradient blur background circle matching Bento HTML
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(BentoPrimaryCardAccent.copy(alpha = 0.45f))
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // High-Priority active badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoPrimaryCardAccent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Primary Target Focus",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryCardOnText,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = featuredTask?.title ?: "No target scheduled",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPrimaryCardOnText,
                            lineHeight = 28.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = BentoMediumLabel,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (featuredTask != null) {
                                    val timeLabel = if (featuredTask.time.isNotEmpty()) " at ${featuredTask.time}" else ""
                                    "${featuredTask.category} Portfolio$timeLabel"
                                } else "Pick the plus button to compile targets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = BentoMediumLabel
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Bottom line with team avatars cluster and complete target controller
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar placeholders matching Bento draft reports mock
                            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(BentoPrimaryCardAccent)
                                        .drawBehind { drawCircle(color = Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())) }
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(BentoPinkCardBg)
                                        .drawBehind { drawCircle(color = Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())) }
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(BentoPurpleAccent.copy(alpha = 0.4f))
                                        .drawBehind { drawCircle(color = Color.White, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryCardOnText)
                                }
                            }

                            if (featuredTask != null) {
                                // Toggle complete play button action
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BentoPrimaryCardOnText)
                                        .clickable { viewModel.toggleTaskCompletion(featuredTask) }
                                        .testTag("featured_task_toggle"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (featuredTask.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                                        contentDescription = "Complete featured task",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Side-by-Side dual metric Bento Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bento Cell A: Progress stats block
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BentoSurfaceGray)
                            .padding(16.dp)
                            .testTag("progress_card"),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = "Task Done Symbol",
                                    tint = BentoPurpleAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Trend increase label
                            val trendIncr = if (progress > 0) "+${(progress * 100).toInt()}%" else "0%"
                            Text(
                                text = trendIncr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPurpleAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Column {
                            Text(
                                text = "${stats.first}/${stats.second}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoOnBackground
                            )
                            Text(
                                text = "Tasks Finished",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = BentoMediumLabel
                            )
                        }
                    }

                    // Bento Cell B: Room Consecutive Streak Day widget
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BentoPinkCardBg)
                            .padding(16.dp)
                            .testTag("streak_card"),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Active Streak lightning icon",
                                    tint = BentoPinkCardOnText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = if (streakDays > 0) "FIRE" else "STANDBY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoPinkCardOnText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Column {
                            Text(
                                text = "$streakDays Days",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoPinkCardOnText
                            )
                            Text(
                                text = "Active Streak",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = BentoPinkCardOnText.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Divider / Hub Title Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Targets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnBackground
                    )
                    Text(
                        text = "View Day (${tasks.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPurpleAccent,
                        modifier = Modifier.clickable {  }
                    )
                }
            }

            // Category tag selection strip
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_chip_$category"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPurpleAccent,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = BentoMediumLabel
                            ),
                            border = if (!isSelected) {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = BentoBorderColor
                                )
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Schedule tasks inside the bento white layout container
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .drawBehind {
                                drawRoundRect(
                                    color = BentoBorderColor,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyListPlaceholder(
                            searchActive = searchQuery.isNotEmpty() || selectedCategory != "All"
                        )
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    BentoTaskRowItem(
                        task = task,
                        onToggleCheck = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, time, category, priority ->
                viewModel.addTask(title, time, category, priority)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BentoTaskRowItem(
    task: Task,
    onToggleCheck: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFF9F7FA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, if (task.isCompleted) BentoBorderColor.copy(alpha = 0.4f) else BentoBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox target
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) BentoPurpleAccent
                        else Color.Transparent
                    )
                    .clickable { onToggleCheck() }
                    .drawBehind {
                        if (!task.isCompleted) {
                            drawRoundRect(
                                color = BentoPurpleAccent,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                    .testTag("task_check_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Task completed indicator",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) BentoMediumLabel.copy(alpha = 0.5f) else BentoOnBackground,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPurpleAccent
                    )
                    if (task.time.isNotEmpty()) {
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = BentoMediumLabel.copy(alpha = 0.5f)
                        )
                        Text(
                            text = task.time,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoMediumLabel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Priority pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(getPriorityTint(task.priority).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = task.priority,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = getPriorityTint(task.priority)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("task_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete target task",
                        tint = BentoMediumLabel.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder(searchActive: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TaskAlt,
            contentDescription = null,
            tint = BentoPurpleAccent.copy(alpha = 0.3f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (searchActive) "No custom matches" else "All tasks structured!",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BentoOnBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (searchActive) "Try resetting filter fields or searching other descriptions."
            else "Agenda for this day is clear of tasks. Fire up consecutive accomplishments with the plus FAB.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = BentoMediumLabel,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, time: String, category: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedPriority by remember { mutableStateOf("Medium") }

    val categories = listOf("Work", "Personal", "Health", "Study", "Finance")
    val priorities = listOf("Low", "Medium", "High")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, BentoBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "New Target Task",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleAccent,
                        unfocusedBorderColor = BentoBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time allocation (e.g. 10:00 AM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = BentoPurpleAccent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Optional") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleAccent,
                        unfocusedBorderColor = BentoBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Category Portfolio",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoMediumLabel.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedCategory == cat) BentoPurpleAccent
                                    else BentoSurfaceGray
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("dialog_category_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == cat) Color.White
                                else BentoOnBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Priority Level",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoMediumLabel.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { prio ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedPriority == prio) getPriorityTint(prio)
                                    else BentoSurfaceGray
                                )
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 10.dp)
                                .testTag("dialog_priority_$prio"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPriority == prio) Color.White
                                else BentoOnBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_button")
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            color = BentoMediumLabel
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { 
                            if (title.isNotBlank()) {
                                onSave(title, time, selectedCategory, selectedPriority)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPurpleAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text("Add Target", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Visual layout helper formatting labels
fun formatHeaderDateLabel(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateValue = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        formatter.format(dateValue).uppercase(Locale.getDefault())
    } catch (e: Exception) {
        dateStr.uppercase(Locale.getDefault())
    }
}

fun getPriorityTint(prio: String): Color {
    return when (prio.lowercase(Locale.getDefault())) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
}
