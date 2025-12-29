package com.example.aibomm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
        containerColor = Color(0xFFF8F9FE), // Light bluish background like Image 5
        bottomBar = {
            HubBottomNavigation(onAddClick = {
                val intent = Intent(context, QuickCaptureActivity::class.java).apply {
                    putExtra(QuickCaptureActivity.EXTRA_MODE, QuickCaptureActivity.MODE_TEXT)
                }
                context.startActivity(intent)
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        .clickable { Toast.makeText(context, "Profile coming soon", Toast.LENGTH_SHORT).show() }
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
                            onValueChange = { searchText = it },
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                            cursorBrush = SolidColor(Color(0xFF007AFF)),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
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
                        onClick = { selectedFilter = filter },
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
                                }
                                context.startActivity(intent)
                            },
                            onActionClick = {
                                if (todo.intent == "calendar") {
                                    Toast.makeText(context, "Added to Calendar: ${todo.title}", Toast.LENGTH_LONG).show()
                                }
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
    }
}

@Composable
fun HubNoteCard(
    todo: TodoItem,
    onClick: () -> Unit,
    onActionClick: () -> Unit
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
                        
                        // Event Action Card (Reference Image 1, 2)
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
                                            null,
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Add to Calendar",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF007AFF),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = Color(0xFF007AFF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = "2h ago", // Mock time
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }

            if (todo.isAiProcessed || todo.tags.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo.isAiProcessed) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "AI Summary",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    todo.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HubBottomNavigation(onAddClick: () -> Unit) {
    val context = LocalContext.current
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        val items = listOf(
            Triple("Hub", Icons.Default.Home, true),
            Triple("Calendar", Icons.Default.CalendarToday, false),
            Triple("Add", Icons.Default.Add, false),
            Triple("Projects", Icons.Default.Folder, false),
            Triple("Settings", Icons.Default.Settings, false)
        )

        items.forEach { (label, icon, selected) ->
            if (label == "Add") {
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
                    selected = selected,
                    onClick = { 
                        if (label != "Hub") {
                            Toast.makeText(context, "$label coming soon", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label, fontSize = 10.sp) },
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
