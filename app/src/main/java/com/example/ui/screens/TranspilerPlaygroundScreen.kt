package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CodeViewer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranspilerPlaygroundScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val snippets = listOf(
        "Counter Button" to """
@Composable
fun CounterCard() {
    var count by remember { mutableStateOf(0) }
    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Clicks: ${'$'}count")
            Button(onClick = { count++ }) {
                Text("Increment")
            }
        }
    }
}
        """.trimIndent(),
        "LazyColumn List" to """
@Composable
fun ItemList(items: List<String>) {
    LazyColumn {
        items(items) { item ->
            Text(item, modifier = Modifier.padding(12.dp))
        }
    }
}
        """.trimIndent(),
        "ViewModel & Flow" to """
class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count
    fun increment() { _count.value++ }
}
        """.trimIndent(),
        "TextField Input" to """
@Composable
fun NameInput() {
    var name by remember { mutableStateOf("") }
    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Your Name") }
    )
}
        """.trimIndent()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transpiler Playground") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Paste or edit Kotlin / Compose code to see the generated SwiftUI translation in real time:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Snippet Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(snippets.size) { index ->
                        val (title, code) = snippets[index]
                        SuggestionChip(
                            onClick = { viewModel.setPlaygroundInput(code) },
                            label = { Text(title, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Kotlin Input Area
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kotlin / Jetpack Compose Input", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { viewModel.setPlaygroundInput("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }

                    OutlinedTextField(
                        value = uiState.playgroundKotlinInput,
                        onValueChange = { viewModel.setPlaygroundInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp)
                            .testTag("playground_input_field"),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Swift Output Area
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Generated Apple Swift / SwiftUI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    CodeViewer(
                        code = uiState.playgroundSwiftOutput.ifBlank { "// Enter Kotlin/Compose above to transpile" },
                        language = "swift",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
