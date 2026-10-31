package com.example.data.model

object SampleAndroidApps {

    val sampleApps: List<AndroidAppInfo> = listOf(
        AndroidAppInfo(
            appName = "TaskPulse",
            packageName = "com.google.samples.taskpulse",
            versionName = "2.4.0",
            versionCode = 240,
            apkPath = "/system/app/TaskPulse.apk",
            apkSizeMb = 14.8,
            isSystemApp = false,
            targetSdk = 35,
            minSdk = 26,
            permissions = listOf(
                "android.permission.INTERNET",
                "android.permission.VIBRATE",
                "android.permission.POST_NOTIFICATIONS"
            ),
            activities = listOf(
                "com.google.samples.taskpulse.MainActivity",
                "com.google.samples.taskpulse.TaskDetailActivity"
            ),
            isSample = true,
            sampleCategory = "Productivity & StateFlow",
            sourceCodePreview = """
package com.google.samples.taskpulse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val priority: String
)

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskItem>>(listOf(
        TaskItem("1", "Audit Jetpack Compose UI", true, "High"),
        TaskItem("2", "Sync offline database", false, "Medium"),
        TaskItem("3", "Generate Xcode Swift project", false, "High")
    ))
    val tasks: StateFlow<List<TaskItem>> = _tasks

    fun toggleTask(id: String) {
        viewModelScope.launch {
            _tasks.value = _tasks.value.map {
                if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
            }
        }
    }
}

@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("TaskPulse Tasks") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            Text(task.priority, style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}
            """.trimIndent()
        ),
        AndroidAppInfo(
            appName = "SkyCast Weather",
            packageName = "io.openweather.skycast",
            versionName = "1.8.2",
            versionCode = 182,
            apkPath = "/data/app/SkyCast.apk",
            apkSizeMb = 18.5,
            isSystemApp = false,
            targetSdk = 35,
            minSdk = 24,
            permissions = listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"
            ),
            activities = listOf("io.openweather.skycast.WeatherMainActivity"),
            isSample = true,
            sampleCategory = "Weather & REST API",
            sourceCodePreview = """
package io.openweather.skycast

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WeatherReport(
    val city: String,
    val temperatureC: Double,
    val condition: String,
    val humidity: Int
)

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        WeatherReport("San Francisco", 19.5, "Partly Cloudy", 62)
    )
    val state: StateFlow<WeatherReport> = _state

    fun refreshWeather() {
        viewModelScope.launch {
            // Retrofit fetch call simulated
            _state.value = _state.value.copy(temperatureC = 20.1)
        }
    }
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weather by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(weather.city, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text("${'$'}{weather.temperatureC}°C", style = MaterialTheme.typography.displayMedium)
        Text(weather.condition, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.refreshWeather() }) {
            Text("Update Live Forecast")
        }
    }
}
            """.trimIndent()
        ),
        AndroidAppInfo(
            appName = "QuickNotes Pro",
            packageName = "net.android.quicknotes",
            versionName = "3.1.0",
            versionCode = 310,
            apkPath = "/data/app/QuickNotes.apk",
            apkSizeMb = 9.2,
            isSystemApp = false,
            targetSdk = 34,
            minSdk = 26,
            permissions = listOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.VIBRATE"
            ),
            activities = listOf("net.android.quicknotes.EditorActivity"),
            isSample = true,
            sampleCategory = "Room DB & Local Storage",
            sourceCodePreview = """
package net.android.quicknotes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAt: Long
)

@Composable
fun NoteEditorScreen(note: NoteEntity, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Note Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Write content...") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onSave(title, content) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Note")
        }
    }
}
            """.trimIndent()
        ),
        AndroidAppInfo(
            appName = "VoiceMemo Studio",
            packageName = "org.sounddroid.audiomemo",
            versionName = "1.0.4",
            versionCode = 104,
            apkPath = "/data/app/AudioMemo.apk",
            apkSizeMb = 12.1,
            isSystemApp = false,
            targetSdk = 35,
            minSdk = 26,
            permissions = listOf(
                "android.permission.RECORD_AUDIO",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.WAKE_LOCK"
            ),
            activities = listOf("org.sounddroid.audiomemo.RecordActivity"),
            isSample = true,
            sampleCategory = "Audio & Hardware Sensors",
            sourceCodePreview = """
package org.sounddroid.audiomemo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AudioRecorderView(isRecording: Boolean, onToggleRecord: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isRecording) "Recording Audio..." else "Ready to Record",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onToggleRecord,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isRecording) "Stop Recording" else "Start Microphone")
        }
    }
}
            """.trimIndent()
        )
    )
}
