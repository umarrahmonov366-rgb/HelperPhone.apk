package com.example.helperphone

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CyberDarkBackground = Color(0xFF0A0E17)
val CyberCardBg = Color(0xFF121824)
val CyberNeonCyan = Color(0xFF00F0FF)
val CyberNeonGreen = Color(0xFF00FF66)
val CyberTextWhite = Color(0xFFE0F7FA)
val CyberTextDim = Color(0xFF8A9BAE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = CyberDarkBackground,
                    surface = CyberCardBg,
                    primary = CyberNeonCyan,
                    onPrimary = Color.Black,
                    onBackground = CyberTextWhite
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberDarkBackground
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    var selectedProblem by remember { mutableStateOf<Problem?>(null) }
    var currentAnswer by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf(0) }

    val historyLog = remember { mutableStateListOf<String>() }
    val haptic = LocalHapticFeedback.current

    val filteredProblems = remember(searchQuery, currentTab) {
        val baseList = when (currentTab) {
            1 -> ProblemRepository.problems.filter { it.isFavorite }
            else -> ProblemRepository.problems
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "[SYSTEM_HELPER :: 2.0]",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = CyberNeonCyan
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberCardBg
                )
            )
        },
        containerColor = CyberDarkBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                currentAnswer != null -> {
                    SolutionScreen(
                        solution = currentAnswer!!,
                        selectedProblemId = selectedProblem?.id ?: 1,
                        onBack = {
                            currentAnswer = null
                            selectedProblem = null
                        }
                    )
                }
                selectedProblem != null -> {
                    QuestionScreen(
                        problem = selectedProblem!!,
                        onAnswerSelected = { isYes ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val answerText = if (isYes) {
                                selectedProblem!!.question.yesSolution
                            } else {
                                selectedProblem!!.question.noSolution
                            }
                            currentAnswer = answerText
                            val logEntry = "ID: 0${selectedProblem!!.id} | ${selectedProblem!!.title}"
                            if (!historyLog.contains(logEntry)) {
                                historyLog.add(0, logEntry)
                            }
                        },
                        onBack = { selectedProblem = null }
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = currentTab,
                            containerColor = CyberCardBg,
                            contentColor = CyberNeonCyan
                        ) {
                            Tab(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                                text = { Text("[ МОДУЛИ ]", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                                text = { Text("[ ⭐ ИЗБРАННОЕ ]", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                                text = { Text("[ 📋 ЖУРНАЛ ]", fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                            )
                        }

                        if (currentTab == 2) {
                            HistoryLogScreen(historyLog = historyLog)
                        } else {
                            ProblemListScreen(
                                problems = filteredProblems,
                                searchQuery = searchQuery,
                                onSearchQueryChanged = { searchQuery = it },
                                onProblemClick = { clickedProblem ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedProblem = clickedProblem
                                },
                                onFavoriteToggle = { problem ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    problem.isFavorite = !problem.isFavorite
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProblemListScreen(
    problems: List<Problem>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onProblemClick: (Problem) -> Unit,
    onFavoriteToggle: (Problem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = {
                Text(
                    "> Поиск модуля проблемы...",
                    fontFamily = FontFamily.Monospace,
                    color = CyberTextDim
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CyberNeonCyan
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = CyberNeonCyan
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberNeonCyan,
                unfocusedBorderColor = CyberNeonCyan.copy(alpha = 0.4f),
                focusedTextColor = CyberTextWhite,
                unfocusedTextColor = CyberTextWhite
            )
        )

        if (problems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[МОДУЛИ НЕ ОБНАРУЖЕНЫ]",
                    fontFamily = FontFamily.Monospace,
                    color = CyberNeonCyan
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(problems, key = { it.id }) { problem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProblemClick(problem) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CyberCardBg
                        ),
                        border = BorderStroke(1.dp, CyberNeonCyan.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SYS_ID::0${problem.id}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyberNeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { onFavoriteToggle(problem) }) {
                                    Text(
                                        text = if (problem.isFavorite) "⭐" else "☆",
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = problem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = problem.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberTextDim
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryLogScreen(historyLog: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📋 БОРТОВОЙ ЖУРНАЛ ДИАГНОСТИКИ",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = CyberNeonCyan,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (historyLog.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ЖУРНАЛ ПУСТ. НЕТ ЗАПИСЕЙ]",
                    fontFamily = FontFamily.Monospace,
                    color = CyberTextDim,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(historyLog) { log ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, CyberNeonCyan.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "> $log",
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextWhite,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionScreen(problem: Problem, onAnswerSelected: (Boolean) -> Unit, onBack: () -> Unit) {
    Crossfade(targetState = problem, animationSpec = tween(300), label = "QuestionAnim") { currentProblem ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CyberCardBg
                ),
                border = BorderStroke(1.5.dp, CyberNeonCyan)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ДИАГНОСТИКА // ${currentProblem.title}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = CyberNeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentProblem.question.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp,
                        color = CyberTextWhite,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onAnswerSelected(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberNeonCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text("[ ДА ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                OutlinedButton(
                    onClick = { onAnswerSelected(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, CyberNeonCyan),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyberNeonCyan
                    )
                ) {
                    Text("[ НЕТ ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("< ОТМЕНА ДИАГНОСТИКИ", fontFamily = FontFamily.Monospace, color = CyberTextDim)
            }
        }
    }
}

@Composable
fun SolutionScreen(solution: String, selectedProblemId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Crossfade(targetState = solution, animationSpec = tween(300), label = "SolutionAnim") { currentSolution ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "⚡ АЛГОРИТМ РЕШЕНИЯ:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberNeonGreen,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CyberCardBg
                    ),
                    border = BorderStroke(1.5.dp, CyberNeonGreen)
                ) {
                    Text(
                        text = currentSolution,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = CyberTextWhite,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val buttonTitle = when (selectedProblemId) {
                    1, 12 -> "⚡ ОТКРЫТЬ НАСТРОЙКИ ЭКРАНА"
                    2, 14 -> "⚡ НАСТРОЙКИ ПИТАНИЯ / БАТАРЕИ"
                    3 -> "⚡ ОЧИСТКА ПАМЯТИ / ПРИЛОЖЕНИЯ"
                    5, 8 -> "⚡ ОТКРЫТЬ НАСТРОЙКИ WI-FI"
                    6 -> "⚡ НАСТРОЙКИ ЗВУКА И ГРОМКОСТИ"
                    10 -> "⚡ НАСТРОЙКИ КАМЕРЫ"
                    11 -> "⚡ НАСТРОЙКИ BLUETOOTH"
                    15 -> "⚡ НАСТРОЙКИ ЭКРАНА / ПОВОРОТА"
                    20 -> "⚡ НАСТРОЙКИ USB / ПЕРЕДАЧИ"
                    else -> "⚡ ОТКРЫТЬ ОБЩИЕ НАСТРОЙКИ"
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val intent = when (selectedProblemId) {
                            5, 8 -> Intent(Settings.ACTION_WIFI_SETTINGS)
                            11 -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            6 -> Intent(Settings.ACTION_SOUND_SETTINGS)
                            1, 12, 15 -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
                            2, 14 -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                            10 -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            else -> Intent(Settings.ACTION_SETTINGS)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberNeonCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text(buttonTitle, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberNeonGreen,
                    contentColor = Color.Black
                )
            ) {
                Text("[ ВЫПОЛНЕНО / В НАЧАЛО ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
