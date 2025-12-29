package com.example.aibomm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import android.content.Intent
import com.example.aibomm.QuickCaptureActivity
import com.example.aibomm.data.TodoItem
import com.example.aibomm.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    var currentTab by remember { mutableStateOf("Hub") }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }
    val filters = listOf("All", "AI Processed", "Action Items", "Voice", "Links")
    val context = LocalContext.current

    val filteredTodos = remember(todos, selectedFilter, searchText) {
        todos.filter { todo ->
            val matchesFilter = when (selectedFilter) {
                "AI Processed" -> todo.isAiProcessed
                "Action Items" -> todo.intent != null
                "Voice" -> todo.category == "audio"
                "Links" -> todo.category == "link"
                else -> true
            }
            val matchesSearch = if (searchText.isBlank()) true else {
                todo.title.contains(searchText, ignoreCase = true) ||
                todo.content.contains(searchText, ignoreCase = true) ||
                todo.tags.contains(searchText, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FE),
        bottomBar = {
            HubBottomNavigation(
                currentTab = currentTab,
                onTabClick = { currentTab = it },
                onAddClick = {
                    val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                        putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_TEXT)
                    }
                    context.startActivity(intent)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentTab == "Hub") {
                HubMainContent(
                    searchText = searchText,
                    onSearchChange = { searchText = it },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    onTabChange = { currentTab = it },
                    filters = filters,
                    filteredTodos = filteredTodos,
                    viewModel = viewModel
                )
            } else if (currentTab == "Insights") {
                InsightsContent(todos = todos, viewModel = viewModel)
            } else if (currentTab == "Settings") {
                SettingsScreen()
            } else if (currentTab == "Profile") {
                ProfileScreen(todos = todos)
            } else {
                // Other tabs placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("$currentTab coming soon", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun HubMainContent(
    searchText: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onTabChange: (String) -> Unit,
    filters: List<String>,
    filteredTodos: List<TodoItem>,
    viewModel: TodoViewModel
) {
    val context = LocalContext.current
    
    // Top Bar: Flash Capsule Title & Profile
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color(0xFF007AFF),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Flash Capsule",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDAB9)) // Peach profile placeholder
                .clickable { onTabChange("Profile") } // Change to Profile tab
        )
    }

    // Search Bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchText.isEmpty()) {
                    Text(
                        text = "Find notes about design...",
                        color = Color.LightGray,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
                BasicTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    cursorBrush = SolidColor(Color(0xFF007AFF)),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            IconButton(onClick = {
                val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                    putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_VOICE)
                }
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.Gray)
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Filter Chips
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF007AFF),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color.Gray
                ),
                border = null,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = if (filter == "AI Processed") {
                    { Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp)) }
                } else if (filter == "Action Items") {
                    { Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Todo List
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(filteredTodos, key = { it.id }) { todo ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        viewModel.delete(todo)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color = when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.EndToStart -> Color(0xFFFFEBEE)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFE57373)
                            )
                        }
                    }
                },
                enableDismissFromStartToEnd = false
            ) {
                HubNoteCard(
                    todo = todo,
                    onClick = {
                        val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                            putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_TEXT)
                            putExtra(QuickCaptureActivity.EXTRA_PREFILL, todo.content)
                            putExtra(QuickCaptureActivity.EXTRA_ID, todo.id)
                            putExtra(QuickCaptureActivity.EXTRA_VIEW_MODE, true)
                        }
                        context.startActivity(intent)
                    },
                    onActionClick = {
                        if (todo.intent == "calendar") {
                            Toast.makeText(context, "Added to Calendar: ${todo.title}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onAiOrganizeClick = {
                        val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                            putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_TEXT)
                            putExtra(QuickCaptureActivity.EXTRA_PREFILL, todo.content)
                            putExtra(QuickCaptureActivity.EXTRA_ID, todo.id)
                            putExtra(QuickCaptureActivity.EXTRA_AUTO_AI, true)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        if (filteredTodos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("No notes found", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsContent(todos: List<TodoItem>, viewModel: TodoViewModel) {
    val context = LocalContext.current
    val keywords = remember(todos) {
        // 每个笔记只贡献一个最相关的灵感（优先取标签的第一项，没有则取标题或内容中最长的一个词）
        val noteInspirations = todos.mapNotNull { todo ->
            val tagList = todo.tags.split(",").filter { it.isNotBlank() }
            if (tagList.isNotEmpty()) {
                tagList.first()
            } else {
                val words = (todo.title.split(" ", "，", "。", "、") + 
                             todo.content.split(" ", "，", "。", "、"))
                            .filter { it.length >= 2 && it.isNotBlank() }
                words.maxByOrNull { it.length }
            }
        }
        
        noteInspirations
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(20)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "灵感洞察",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "AI 自动整理的灵感关键词",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(keywords) { (word, count) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            // 跳转到搜索结果
                            val relatedNote = todos.firstOrNull { it.tags.contains(word) || it.title.contains(word) || it.content.contains(word) }
                            if (relatedNote != null) {
                                val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                                    putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_TEXT)
                                    putExtra(QuickCaptureActivity.EXTRA_PREFILL, relatedNote.content)
                                    putExtra(QuickCaptureActivity.EXTRA_ID, relatedNote.id)
                                    putExtra(QuickCaptureActivity.EXTRA_VIEW_MODE, true)
                                }
                                context.startActivity(intent)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#",
                                    color = Color(0xFF007AFF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = word,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        
                        Surface(
                            color = Color(0xFFF8F9FE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$count 处提到",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            if (keywords.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        Text("暂无灵感关键词", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun HubNoteCard(
    todo: TodoItem,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    onAiOrganizeClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Category Icon
                    val (icon, color) = when (todo.category) {
                        "audio" -> Icons.Default.GraphicEq to Color(0xFFFFEBEE)
                        "link" -> Icons.Default.Link to Color(0xFFE3F2FD)
                        "task" -> Icons.Default.TaskAlt to Color(0xFFE8F5E9)
                        else -> Icons.Default.AutoAwesome to Color(0xFFF3E5F5)
                    }
                    val tint = when (todo.category) {
                        "audio" -> Color(0xFFE57373)
                        "link" -> Color(0xFF64B5F6)
                        "task" -> Color(0xFF81C784)
                        else -> Color(0xFFBA68C8)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (todo.isAiProcessed) todo.summary ?: todo.content else todo.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Event Action Card
                        if (todo.intent == "calendar") {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFF0F7FF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onActionClick)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "添加到日历",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "识别到日程信息",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF007AFF)
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Organize Button (Sparkles)
                if (!todo.isAiProcessed) {
                    IconButton(
                        onClick = onAiOrganizeClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF0F4FF), Color(0xFFE8F1FF))
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Organize",
                            tint = Color(0xFF007AFF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Tags
            if (todo.isAiProcessed || todo.tags.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (todo.isAiProcessed) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "AI 总结",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    todo.tags.split(",").filter { it.isNotBlank() }.take(3).forEach { tag ->
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(todos: List<TodoItem>) {
    val aiProcessedCount = todos.count { it.isAiProcessed }
    val actionCount = todos.count { it.intent != null }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "个人中心",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(24.dp))
        
        // Profile Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFDAB9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("AI 探索者", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("aibomm_user@example.com", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(modifier = Modifier.weight(1f), title = "笔记总数", value = "${todos.size}", icon = Icons.Default.Note, color = Color(0xFFE3F2FD))
            StatCard(modifier = Modifier.weight(1f), title = "AI 处理", value = "$aiProcessedCount", icon = Icons.Default.AutoAwesome, color = Color(0xFFF3E5F5))
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(modifier = Modifier.weight(1f), title = "行动项", value = "$actionCount", icon = Icons.Default.Bolt, color = Color(0xFFFFF3E0))
            StatCard(modifier = Modifier.weight(1f), title = "灵感度", value = "98%", icon = Icons.Default.Favorite, color = Color(0xFFFFEBEE))
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(24.dp))
        
        SettingGroup("通用设置") {
            SettingItem("深色模式", Icons.Default.DarkMode, hasSwitch = true)
            SettingItem("通知管理", Icons.Default.Notifications)
            SettingItem("语言设置", Icons.Default.Language, value = "简体中文")
        }
        
        Spacer(Modifier.height(24.dp))
        
        SettingGroup("AI 配置") {
            SettingItem("模型选择", Icons.Default.ModelTraining, value = "Gemini 2.0 Flash")
            SettingItem("自动摘要", Icons.Default.Summarize, hasSwitch = true, initialSwitchState = true)
            SettingItem("意图识别", Icons.Default.Psychology, hasSwitch = true, initialSwitchState = true)
        }
        
        Spacer(Modifier.height(24.dp))
        
        SettingGroup("关于") {
            SettingItem("版本信息", Icons.Default.Info, value = "v1.0.2")
            SettingItem("用户协议", Icons.Default.Description)
            SettingItem("隐私政策", Icons.Default.Security)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(48.dp))
        
        // Footer/Closing Statement
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FlashOn,
                null,
                tint = Color(0xFF007AFF).copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "闪念胶囊",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                text = "不仅是你的笔记，更是你的 Android 智能助理。",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String, 
    icon: ImageVector, 
    value: String? = null, 
    hasSwitch: Boolean = false,
    initialSwitchState: Boolean = false
) {
    var checked by remember { mutableStateOf(initialSwitchState) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (hasSwitch) checked = !checked }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        
        if (hasSwitch) {
            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF007AFF))
            )
        } else if (value != null) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun HubBottomNavigation(
    currentTab: String,
    onTabClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        val items = listOf(
            Triple("Hub", Icons.Default.Home, "Hub"),
            Triple("Insights", Icons.Default.AutoAwesome, "Insights"),
            Triple("Add", Icons.Default.Add, "Add"),
            Triple("Profile", Icons.Default.Person, "Profile"),
            Triple("Settings", Icons.Default.Settings, "Settings")
        )

        items.forEach { (label, icon, tabId) ->
            if (tabId == "Add") {
                // FAB-like button in the middle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onAddClick
                            ),
                        shape = CircleShape,
                        color = Color(0xFF007AFF),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                NavigationBarItem(
                    selected = currentTab == tabId,
                    onClick = { 
                        onTabClick(tabId)
                    },
                    icon = { Icon(icon, contentDescription = label) },
                    label = { 
                        Text(
                            text = if (label == "Insights") "洞察" else label, 
                            fontSize = 10.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
