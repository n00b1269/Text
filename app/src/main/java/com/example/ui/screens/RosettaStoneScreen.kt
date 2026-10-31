package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiMapping

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosettaStoneScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val mappings = remember {
        listOf(
            // UI
            ApiMapping("UI", "Column { ... }", "VStack(spacing: ...) { ... }", "100% Direct", "Vertical flex container."),
            ApiMapping("UI", "Row { ... }", "HStack(spacing: ...) { ... }", "100% Direct", "Horizontal flex container."),
            ApiMapping("UI", "Box { ... }", "ZStack { ... }", "100% Direct", "Overlapping depth container."),
            ApiMapping("UI", "LazyColumn(items)", "List(items) or ScrollView { LazyVStack }", "100% Direct", "Virtual scrolling list."),
            ApiMapping("UI", "Text(text = \"Hello\")", "Text(\"Hello\")", "100% Direct", "Standard typography text view."),
            ApiMapping("UI", "Button(onClick = { })", "Button(action: { })", "100% Direct", "Interactive action control."),
            ApiMapping("UI", "OutlinedTextField", "TextField(text: $...) with border", "100% Direct", "Single-line or multiline text input."),
            ApiMapping("UI", "Scaffold(topBar = ...)", "NavigationStack { ... .toolbar { ... } }", "100% Direct", "Screen structure with navigation bar."),
            ApiMapping("UI", "CircularProgressIndicator", "ProgressView()", "100% Direct", "Activity loading spinner."),
            ApiMapping("UI", "Icon(Icons.Default...)", "Image(systemName: \"...\")", "100% Direct", "SF Symbols icon system."),

            // State & Architecture
            ApiMapping("State", "ViewModel", "@Observable class ... (iOS 17+)", "100% Direct", "Swift Observation macro."),
            ApiMapping("State", "MutableStateFlow<T>", "var state: T = ...", "100% Direct", "Observed Swift properties."),
            ApiMapping("State", "remember { mutableStateOf() }", "@State private var ...", "100% Direct", "Local component state."),
            ApiMapping("State", "viewModelScope.launch { }", "Task { await ... }", "100% Direct", "Swift Structured Concurrency."),
            ApiMapping("State", "suspend fun fetch()", "func fetch() async throws", "100% Direct", "Native asynchronous functions."),

            // Data & Persistence
            ApiMapping("Data", "Room Database (@Entity, @Dao)", "SwiftData (@Model, @Query)", "High", "Apple native SwiftData ORM."),
            ApiMapping("Data", "SharedPreferences / DataStore", "@AppStorage(\"key\")", "100% Direct", "Property wrapper backed by UserDefaults."),
            ApiMapping("Data", "Retrofit interface + Moshi", "URLSession + Decodable Structs", "100% Direct", "Standard Swift HTTP networking."),

            // Hardware & System
            ApiMapping("Hardware", "android.permission.CAMERA", "NSCameraUsageDescription & AVFoundation", "100% Direct", "Apple Camera capture session."),
            ApiMapping("Hardware", "ACCESS_FINE_LOCATION", "NSLocationWhenInUse & CoreLocation", "100% Direct", "CLLManager location telemetry."),
            ApiMapping("Hardware", "RECORD_AUDIO", "NSMicrophoneUsageDescription & AVAudioRecorder", "100% Direct", "Apple audio engine input."),
            ApiMapping("Hardware", "Vibrator / VIBRATE", "UIImpactFeedbackGenerator / CoreHaptics", "100% Direct", "Taptic Engine haptic impulses.")
        )
    }

    val categories = remember {
        listOf("All", "UI", "State", "Data", "Hardware")
    }

    val filtered = remember(searchQuery, selectedCategory) {
        mappings.filter { item ->
            (selectedCategory == "All" || item.category == selectedCategory) &&
            (item.androidConcept.contains(searchQuery, ignoreCase = true) ||
             item.appleConcept.contains(searchQuery, ignoreCase = true) ||
             item.description.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rosetta Stone (Android ⇄ Apple)") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Architectural and syntax cross-reference guide between Google Android and Apple iOS frameworks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Filter
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Search Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter concepts (e.g. StateFlow, Column, Room)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            items(filtered) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.compatibility, fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Android", fontSize = 10.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                Text(item.androidConcept, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Apple iOS", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                Text(item.appleConcept, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
