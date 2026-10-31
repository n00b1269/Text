package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.XcodeFile
import com.example.data.model.XcodeFileType
import com.example.data.model.XcodeProject
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.components.CodeViewer
import com.example.ui.components.SimulatedIphonePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XcodeStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val project = uiState.xcodeProject ?: return
    val context = LocalContext.current

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    BackHandler {
        viewModel.navigateTo(AppScreen.STORE_CHECK)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${project.appName}.xcodeproj", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${project.compatibilityScore}% Compatible", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(project.bundleId, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.STORE_CHECK) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.exportZipArchive { shareIntent ->
                                shareLauncher.launch(Intent.createChooser(shareIntent, "Save or Share Xcode Project"))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("export_zip_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export .ZIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = uiState.activeStudioTab.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.activeStudioTab == StudioTab.CODE_VIEWER,
                    onClick = { viewModel.setStudioTab(StudioTab.CODE_VIEWER) },
                    text = { Text("Code Explorer") },
                    icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tab_code_explorer")
                )
                Tab(
                    selected = uiState.activeStudioTab == StudioTab.SIDE_BY_SIDE,
                    onClick = { viewModel.setStudioTab(StudioTab.SIDE_BY_SIDE) },
                    text = { Text("Android vs Swift") },
                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tab_side_by_side")
                )
                Tab(
                    selected = uiState.activeStudioTab == StudioTab.LIVE_PREVIEW,
                    onClick = { viewModel.setStudioTab(StudioTab.LIVE_PREVIEW) },
                    text = { Text("iPhone Live Preview") },
                    icon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tab_live_preview")
                )
                Tab(
                    selected = uiState.activeStudioTab == StudioTab.COMPATIBILITY_REPORT,
                    onClick = { viewModel.setStudioTab(StudioTab.COMPATIBILITY_REPORT) },
                    text = { Text("Audit & Migration") },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tab_audit_report")
                )
            }

            // Tab Content
            when (uiState.activeStudioTab) {
                StudioTab.CODE_VIEWER -> CodeViewerTab(project, uiState.selectedFile) { viewModel.selectFile(it) }
                StudioTab.SIDE_BY_SIDE -> SideBySideTab(uiState.selectedApp?.sourceCodePreview ?: "", uiState.selectedFile?.content ?: "")
                StudioTab.LIVE_PREVIEW -> LivePreviewTab(project)
                StudioTab.COMPATIBILITY_REPORT -> CompatibilityReportTab(project)
            }
        }
    }
}

@Composable
private fun CodeViewerTab(
    project: XcodeProject,
    selectedFile: XcodeFile?,
    onSelectFile: (XcodeFile) -> Unit
) {
    val activeFile = selectedFile ?: project.files.firstOrNull()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        // Horizontal file tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            items(project.files) { file ->
                val isSelected = file == activeFile
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectFile(file) },
                    label = { Text(file.filename, fontSize = 12.sp) },
                    leadingIcon = {
                        val icon = when (file.fileType) {
                            XcodeFileType.SWIFT -> Icons.Default.Description
                            XcodeFileType.PLIST -> Icons.Default.Settings
                            XcodeFileType.PACKAGE_SWIFT -> Icons.Default.Build
                            XcodeFileType.STRINGS -> Icons.Default.Translate
                            XcodeFileType.MARKDOWN -> Icons.Default.HelpOutline
                            XcodeFileType.JSON -> Icons.Default.DataObject
                        }
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        if (activeFile != null) {
            // Path indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                Text(
                    text = "${project.appName}/${activeFile.path}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            CodeViewer(
                code = activeFile.content,
                language = when (activeFile.fileType) {
                    XcodeFileType.SWIFT, XcodeFileType.PACKAGE_SWIFT -> "swift"
                    XcodeFileType.PLIST -> "xml"
                    XcodeFileType.STRINGS -> "strings"
                    XcodeFileType.MARKDOWN -> "markdown"
                    XcodeFileType.JSON -> "json"
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SideBySideTab(androidCode: String, swiftCode: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Side-by-side view compares Android Kotlin / Jetpack Compose with the ported Apple Swift / SwiftUI code.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text("Original Android (Kotlin / Compose)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            CodeViewer(
                code = if (androidCode.isNotBlank()) androidCode else "// Android source code extracted from APK",
                language = "kotlin"
            )
        }

        item {
            Text("Ported Apple (Swift / SwiftUI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            CodeViewer(
                code = swiftCode,
                language = "swift"
            )
        }
    }
}

@Composable
private fun LivePreviewTab(project: XcodeProject) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "Interact with the ported SwiftUI interface inside the simulated iPhone 16 frame below!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            SimulatedIphonePreview(project = project)
        }
    }
}

@Composable
private fun CompatibilityReportTab(project: XcodeProject) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${project.compatibilityScore}%",
                            color = Color(0xFF10B981),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Xcode Compatibility Rating", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Direct 1:1 translation for Jetpack Compose, ViewModels, and system entitlements.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Detected API Mappings
        item {
            Text("Detected API Translations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(project.detectedApis) { api ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(api.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFECFDF5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(api.compatibility, fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(api.androidConcept, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(api.appleConcept, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0284C7), modifier = Modifier.weight(1f))
                    }

                    Text(api.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Warnings / Checklist
        if (project.migrationWarnings.isNotEmpty()) {
            item {
                Text("Migration Notices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(project.migrationWarnings) { warning ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Text(warning, fontSize = 12.sp, color = Color(0xFF92400E))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
