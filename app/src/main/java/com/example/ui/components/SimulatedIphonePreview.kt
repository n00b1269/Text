package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.XcodeProject

@Composable
fun SimulatedIphonePreview(
    project: XcodeProject,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // iPhone chassis frame
        Card(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
                .border(6.dp, Color(0xFF27272A), RoundedCornerShape(44.dp)),
            shape = RoundedCornerShape(44.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic Island & iOS Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "9:41",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Dynamic Island pill
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                }

                Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                // SwiftUI App Screen Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .background(Color(0xFFF1F5F9))
                ) {
                    when {
                        project.appName.contains("Task", ignoreCase = true) -> {
                            TaskAppLiveView(project.appName)
                        }
                        project.appName.contains("Weather", ignoreCase = true) || project.appName.contains("Sky", ignoreCase = true) -> {
                            WeatherAppLiveView(project.appName)
                        }
                        project.appName.contains("Note", ignoreCase = true) -> {
                            NoteAppLiveView(project.appName)
                        }
                        project.appName.contains("Audio", ignoreCase = true) || project.appName.contains("Voice", ignoreCase = true) -> {
                            AudioAppLiveView(project.appName)
                        }
                        else -> {
                            GenericAppLiveView(project.appName)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // iOS Home Indicator bar
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF71717A))
                )
            }
        }
    }
}

@Composable
private fun TaskAppLiveView(appName: String) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                Triple("1", "Audit Jetpack Compose UI", true),
                Triple("2", "Sync offline database", false),
                Triple("3", "Generate Xcode Swift project", false)
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // iOS Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(appName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            IconButton(
                onClick = {
                    val nextId = "${tasks.size + 1}"
                    tasks = tasks + Triple(nextId, "New Apple Task #$nextId", false)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFF007AFF))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tasks.forEach { (id, title, done) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            color = if (done) Color.Gray else Color.Black,
                            textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                tasks = tasks.map { if (it.first == id) it.copy(third = !it.third) else it }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (done) Color(0xFF34C759) else Color(0xFF8E8E93)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherAppLiveView(appName: String) {
    var temp by remember { mutableStateOf(19.5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2FE))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("San Francisco", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("Partly Cloudy", fontSize = 14.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "${String.format("%.1f", temp)}°C",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF0284C7)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Humidity", fontSize = 11.sp, color = Color.Gray)
                    Text("62%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wind", fontSize = 11.sp, color = Color.Gray)
                    Text("14 km/h", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UV Index", fontSize = 11.sp, color = Color.Gray)
                    Text("Moderate", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Button(
            onClick = { temp += ((-5..5).random() / 10.0) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Refresh Live Forecast", fontSize = 13.sp)
        }
    }
}

@Composable
private fun NoteAppLiveView(appName: String) {
    var title by remember { mutableStateOf("Meeting Notes") }
    var content by remember { mutableStateOf("Ported to SwiftData and SwiftUI.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("QuickNotes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(8.dp)
        )
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Save to SwiftData")
        }
    }
}

@Composable
private fun AudioAppLiveView(appName: String) {
    var isRecording by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(appName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(if (isRecording) Color(0xFFFF3B30) else Color(0xFF007AFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = if (isRecording) "Recording Audio..." else "Ready to Record",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Button(
            onClick = { isRecording = !isRecording },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color(0xFFFF3B30) else Color(0xFF007AFF)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Text(if (isRecording) "Stop Recording" else "Start Microphone")
        }
    }
}

@Composable
private fun GenericAppLiveView(appName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(appName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(
            "Transpiled SwiftUI Interface with native iOS styling and NavigationStack.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Swift 6.0 & iOS 17+", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Text("Ready to run on Apple Simulator", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
