package com.example.aibomm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aibomm.data.TodoItem
import com.example.aibomm.viewmodel.TodoViewModel

@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "待办",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "${todos.count { !it.isDone }} 未完成",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(12.dp))

            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "没有待办，点右下角 + 新增",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(todos, key = { it.id }) { todo ->
                        TodoRow(
                            todo = todo,
                            onToggle = { viewModel.toggle(todo) },
                            onDelete = { viewModel.delete(todo) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title ->
                viewModel.add(title)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TodoRow(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (todo.isDone) Icons.Default.CheckCircle else Icons.Default.Circle,
                    contentDescription = "完成状态",
                    tint = if (todo.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            val textStyle = if (todo.isDone) {
                TextStyle(textDecoration = TextDecoration.LineThrough)
            } else {
                TextStyle.Default
            }

            Text(
                text = todo.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
                    .alpha(if (todo.isDone) 0.55f else 1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.merge(textStyle)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
private fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增待办") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("例如：晚上 8 点跑步") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(title.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
