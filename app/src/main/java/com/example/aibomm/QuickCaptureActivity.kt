package com.example.aibomm

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import android.provider.CalendarContract
import android.provider.AlarmClock
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aibomm.ui.theme.AIBommTheme
import com.example.aibomm.viewmodel.QuickCaptureViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import dev.jeziellago.compose.markdowntext.MarkdownText

class QuickCaptureActivity : ComponentActivity() {
    private var mode: String by mutableStateOf(MODE_TEXT)
    private var prefill: String by mutableStateOf("")
    private var todoId: Long by mutableStateOf(-1L)
    private var isViewMode: Boolean by mutableStateOf(false)
    private var autoAi: Boolean by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateFromIntent(intent)
        setContent {
            AIBommTheme {
                val viewModel: QuickCaptureViewModel = viewModel()
                if (isViewMode) {
                    NoteViewScreen(
                        initialText = prefill,
                        onClose = { finish() },
                        onEdit = { isViewMode = false }
                    )
                } else {
                    QuickCaptureScreen(
                        mode = mode,
                        initialText = prefill,
                        autoAi = autoAi,
                        onClose = { finish() },
                        onSave = { text, summary, tags, category, isAi, intent, payload ->
                            if (todoId == -1L) {
                                viewModel.add(
                                    title = if (isAi) text.take(20) else text,
                                    content = text,
                                    summary = summary,
                                    tags = tags,
                                    category = category,
                                    isAiProcessed = isAi,
                                    intent = intent,
                                    intentPayload = payload
                                )
                            } else {
                                viewModel.update(
                                    id = todoId,
                                    title = if (isAi) text.take(20) else text,
                                    content = text,
                                    summary = summary,
                                    tags = tags,
                                    category = category,
                                    isAiProcessed = isAi,
                                    intent = intent,
                                    intentPayload = payload
                                )
                            }
                            finish()
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateFromIntent(intent)
    }

    private fun updateFromIntent(intent: Intent?) {
        mode = intent?.getStringExtra(EXTRA_MODE) ?: MODE_TEXT
        prefill = intent?.getStringExtra(EXTRA_PREFILL).orEmpty()
        todoId = intent?.getLongExtra(EXTRA_ID, -1L) ?: -1L
        isViewMode = intent?.getBooleanExtra(EXTRA_VIEW_MODE, false) ?: false
        autoAi = intent?.getBooleanExtra(EXTRA_AUTO_AI, false) ?: false
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_PREFILL = "prefill"
        const val EXTRA_ID = "todo_id"
        const val EXTRA_VIEW_MODE = "view_mode"
        const val EXTRA_AUTO_AI = "auto_ai"
        const val MODE_TEXT = "text"
        const val MODE_VOICE = "voice"
        const val MODE_IMAGE = "image"
    }
}

@Composable
fun NoteViewScreen(
    initialText: String,
    onClose: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("查看笔记", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "编辑")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            MarkdownText(
                markdown = initialText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun QuickCaptureScreen(
    mode: String,
    initialText: String,
    autoAi: Boolean,
    onClose: () -> Unit,
    onSave: (String, String?, String, String, Boolean, String?, String?) -> Unit,
    viewModel: QuickCaptureViewModel
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var originalText by remember { mutableStateOf<String?>(null) }
    var aiAnimationTrigger by remember { mutableStateOf(0) } // 专用触发器
    var aiSummary by remember { mutableStateOf<String?>(null) }
    var aiTags by remember { mutableStateOf("") }
    var aiCategory by remember { mutableStateOf("text") }
    var aiIntent by remember { mutableStateOf<String?>(null) }
    var aiPayload by remember { mutableStateOf<String?>(null) }
    var isAiProcessed by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var tempTag by remember { mutableStateOf("") }

    // Shimmer effect animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isVisible by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }
    
    val progress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isExiting) {
            tween(durationMillis = 180, easing = FastOutLinearInEasing)
        } else {
            tween(durationMillis = 220, easing = LinearOutSlowInEasing)
        },
        label = "quickCaptureProgress"
    )

    fun requestSave() {
        if (text.isBlank() || isExiting) return
        isExiting = true
        isVisible = false
        keyboardController?.hide()
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
            delay(190)
            onSave(text, aiSummary, aiTags, aiCategory, isAiProcessed, aiIntent, aiPayload)
        }
    }

    fun handleMagicAction(intentType: String?, payload: String?) {
        if (intentType == null || isExiting) return
        
        scope.launch {
            try {
                when (intentType) {
                    "alarm" -> {
                        val minutes = payload?.filter { it.isDigit() }?.toIntOrNull() ?: 20
                        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_MESSAGE, text.take(20))
                            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        }
                        context.startActivity(intent)
                        snackbarHostState.showSnackbar("已为您设置 $minutes 分钟后的闹钟")
                    }
                    "calendar" -> {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, text.take(20))
                            putExtra(CalendarContract.Events.DESCRIPTION, text)
                        }
                        context.startActivity(intent)
                        snackbarHostState.showSnackbar("正在打开日历添加日程...")
                    }
                    "message" -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:")
                            putExtra("sms_body", text)
                        }
                        context.startActivity(intent)
                        snackbarHostState.showSnackbar("正在打开短信...")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                snackbarHostState.showSnackbar("调用失败: 找不到对应的应用程序")
            }
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spoken = list?.firstOrNull().orEmpty()
        if (spoken.isNotBlank()) {
            text = if (text.isBlank()) spoken else "$text $spoken"
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Image selected: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
            aiCategory = "image"
            if (text.isBlank()) text = "Image: ${uri.lastPathSegment}"
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        voiceLauncher.launch(intent)
    }

    LaunchedEffect(mode) {
        if (mode == QuickCaptureActivity.MODE_VOICE) {
            startVoiceInput()
        } else if (mode == QuickCaptureActivity.MODE_IMAGE) {
            imageLauncher.launch("image/*")
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
        focusRequester.requestFocus()
        keyboardController?.show()
        
        // Auto-AI if requested
        if (autoAi && text.isNotBlank()) {
            isProcessing = true
            viewModel.processWithAi(text) { title, content, tags, category, intent, payload ->
                originalText = text
                text = content
                aiSummary = title
                aiTags = tags
                aiCategory = category
                aiIntent = intent
                aiPayload = payload
                isAiProcessed = true
                isProcessing = false
                aiAnimationTrigger++
                
                if (intent != null) {
                    handleMagicAction(intent, payload)
                }
            }
        }
    }

    if (showTagDialog) {
        Dialog(onDismissRequest = { showTagDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Add Tags", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = tempTag,
                        onValueChange = { tempTag = it },
                        placeholder = { Text("e.g. work, design") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F4FF),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTagDialog = false }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            if (tempTag.isNotBlank()) {
                                aiTags = if (aiTags.isBlank()) tempTag else "$aiTags,$tempTag"
                            }
                            tempTag = ""
                            showTagDialog = false
                        }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = progress
                val startScale = 0.92f
                val endScale = 1f
                val scale = startScale + (endScale - startScale) * p
                alpha = p
                scaleX = scale
                scaleY = scale
                translationY = (1f - p) * 48.dp.toPx()
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
            },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close, 
                            contentDescription = "关闭",
                            tint = Color.Gray
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { requestSave() },
                        enabled = text.isNotBlank() && !isExiting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 16.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFE8F1FF), // Light blue background like Image 4
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8F1FF), Color(0xFFF8F9FE))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(80.dp))
                
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 100.dp),
                            shape = RoundedCornerShape(32.dp),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            // Shimmer overlay when processing
                            if (isProcessing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    Color(0xFFE8F1FF).copy(alpha = 0.5f),
                                                    Color.White.copy(alpha = 0.3f)
                                                ),
                                                start = androidx.compose.ui.geometry.Offset(shimmerTranslateAnim - 500f, 0f),
                                                end = androidx.compose.ui.geometry.Offset(shimmerTranslateAnim, 500f)
                                            )
                                        )
                                )
                            }
                            
                            // 使用 aiAnimationTrigger 作为 Key，只有 AI 整理后才会触发 Crossfade 动画
                            Crossfade(targetState = aiAnimationTrigger, label = "textCrossfade") { _ ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    TextField(
                                        value = text, // 始终绑定到最新的 text
                                        onValueChange = { text = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        placeholder = { 
                                            Text(
                                                "What's on your mind?", 
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    color = Color.LightGray,
                                                    fontSize = 22.sp
                                                )
                                            ) 
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = Color(0xFF007AFF)
                                        ),
                                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                                            fontSize = 22.sp,
                                            lineHeight = 32.sp
                                        )
                                    )
                                    
                                    if (aiTags.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            aiTags.split(",").forEach { tag ->
                                                SuggestionChip(
                                                    onClick = { /* Remove tag logic if needed */ },
                                                    label = { Text("#$tag") },
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${text.length} CHARS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.LightGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
            }

            // Bottom Toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { startVoiceInput() }) {
                            Icon(Icons.Default.Mic, null, tint = Color.Gray)
                        }
                        IconButton(onClick = { imageLauncher.launch("image/*") }) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                        }
                        IconButton(onClick = { showTagDialog = true }) {
                            Icon(Icons.Default.Tag, null, tint = Color.Gray)
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        // AI Organize Button
                        if (originalText != null) {
                            TextButton(
                                onClick = { 
                                    text = originalText!!
                                    aiAnimationTrigger++ // 撤销时也触发动画
                                    originalText = null
                                    isAiProcessed = false
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("撤销", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Button(
                            onClick = { 
                                if (isProcessing) return@Button
                                originalText = text
                                isProcessing = true
                                viewModel.processWithAi(text) { title, summary, tags, category, intent, payload ->
                                    isProcessing = false
                                    if (summary.isNotBlank()) {
                                        // 直接将整理后的总结应用到主文本框
                                        text = summary 
                                        aiAnimationTrigger++ // 触发动画
                                        aiSummary = summary
                                        aiTags = tags
                                        aiCategory = category
                                        aiIntent = intent
                                        aiPayload = payload
                                        isAiProcessed = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        
                                        // 使用统一的 Magic Action 处理逻辑
                                        handleMagicAction(intent, payload)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("AI 整理失败")
                                        }
                                    }
                                }
                            },
                            enabled = text.isNotBlank() && !isProcessing,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF007AFF)
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFF0F4FF), Color(0xFFE8F1FF))
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF007AFF)
                                )
                            } else {
                                Icon(
                                    Icons.Default.AutoAwesome, 
                                    null, 
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF007AFF)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("AI 整理", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                            }
                        }
                    }
                }
            }
        }
    }
}
