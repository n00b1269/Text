package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AndroidAppInfo
import com.example.data.model.AppleStoreResult
import com.example.data.model.SampleAndroidApps
import com.example.data.model.XcodeFile
import com.example.data.model.XcodeProject
import com.example.data.service.ApkExtractorService
import com.example.data.service.AppleStoreService
import com.example.data.service.CodeTranspilerEngine
import com.example.data.service.XcodeZipExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    APP_SELECTION,
    STORE_CHECK,
    XCODE_STUDIO,
    PLAYGROUND,
    ROSETTA_STONE
}

enum class StudioTab {
    CODE_VIEWER,
    SIDE_BY_SIDE,
    LIVE_PREVIEW,
    COMPATIBILITY_REPORT
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.APP_SELECTION,
    val installedApps: List<AndroidAppInfo> = emptyList(),
    val sampleApps: List<AndroidAppInfo> = SampleAndroidApps.sampleApps,
    val isLoadingApps: Boolean = false,
    val searchQuery: String = "",
    val selectedApp: AndroidAppInfo? = null,
    val isCheckingStore: Boolean = false,
    val appleStoreResults: List<AppleStoreResult> = emptyList(),
    val storeCheckCompleted: Boolean = false,
    val hasAppleEquivalent: Boolean = false,
    val isConverting: Boolean = false,
    val xcodeProject: XcodeProject? = null,
    val activeStudioTab: StudioTab = StudioTab.CODE_VIEWER,
    val selectedFile: XcodeFile? = null,
    val exportedZipFile: File? = null,
    val statusMessage: String? = null,
    val playgroundKotlinInput: String = DEFAULT_PLAYGROUND_KOTLIN,
    val playgroundSwiftOutput: String = "",
    val isTranspilingPlayground: Boolean = false
)

private val DEFAULT_PLAYGROUND_KOTLIN = """
@Composable
fun CounterCard() {
    var count by remember { mutableStateOf(0) }

    Card(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Clicks: ${'$'}count", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { count++ }) {
                Text("Increment")
            }
        }
    }
}
""".trimIndent()

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apkService = ApkExtractorService(application)
    private val storeService = AppleStoreService()
    private val transpiler = CodeTranspilerEngine()
    private val zipExporter = XcodeZipExporter(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()
        transpilePlayground()
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun setStudioTab(tab: StudioTab) {
        _uiState.value = _uiState.value.copy(activeStudioTab = tab)
    }

    fun selectFile(file: XcodeFile) {
        _uiState.value = _uiState.value.copy(selectedFile = file)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setPlaygroundInput(input: String) {
        _uiState.value = _uiState.value.copy(playgroundKotlinInput = input)
        transpilePlayground()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingApps = true)
            val apps = apkService.getInstalledLaunchableApps()
            _uiState.value = _uiState.value.copy(
                installedApps = apps,
                isLoadingApps = false
            )
        }
    }

    fun selectApp(app: AndroidAppInfo) {
        _uiState.value = _uiState.value.copy(
            selectedApp = app,
            currentScreen = AppScreen.STORE_CHECK,
            isCheckingStore = true,
            storeCheckCompleted = false,
            appleStoreResults = emptyList(),
            hasAppleEquivalent = false
        )

        // Check Apple App Store for an iOS version
        viewModelScope.launch {
            val results = storeService.searchAppleStore(app.appName, app.packageName)
            val hasExactOrHighMatch = results.any {
                it.trackName.contains(app.appName, ignoreCase = true) ||
                app.appName.contains(it.trackName, ignoreCase = true)
            }

            _uiState.value = _uiState.value.copy(
                isCheckingStore = false,
                storeCheckCompleted = true,
                appleStoreResults = results,
                hasAppleEquivalent = hasExactOrHighMatch
            )
        }
    }

    fun onApkSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingApps = true)
            val parsedApp = apkService.parseApkFromUri(uri)
            _uiState.value = _uiState.value.copy(isLoadingApps = false)

            if (parsedApp != null) {
                selectApp(parsedApp)
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Could not parse APK header. Please verify the file."
                )
            }
        }
    }

    fun convertSelectedAppToXcode() {
        val app = _uiState.value.selectedApp ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConverting = true)
            val project = transpiler.transpileToXcodeProject(app)
            val defaultFile = project.files.find { it.filename == "ContentView.swift" } ?: project.files.firstOrNull()

            _uiState.value = _uiState.value.copy(
                isConverting = false,
                xcodeProject = project,
                selectedFile = defaultFile,
                currentScreen = AppScreen.XCODE_STUDIO,
                activeStudioTab = StudioTab.CODE_VIEWER
            )
        }
    }

    fun exportZipArchive(onReadyToShare: (Intent) -> Unit) {
        val project = _uiState.value.xcodeProject ?: return
        viewModelScope.launch {
            val file = zipExporter.exportProjectToZip(project)
            _uiState.value = _uiState.value.copy(
                exportedZipFile = file,
                statusMessage = "Xcode project ZIP archive exported (${file.length() / 1024} KB)!"
            )
            val shareIntent = zipExporter.createShareIntent(file, project.appName)
            onReadyToShare(shareIntent)
        }
    }

    private fun transpilePlayground() {
        val input = _uiState.value.playgroundKotlinInput
        viewModelScope.launch {
            val dummyApp = AndroidAppInfo(
                appName = "PlaygroundSnippet",
                packageName = "com.sample.snippet",
                versionName = "1.0",
                versionCode = 1,
                sourceCodePreview = input
            )
            val project = transpiler.transpileToXcodeProject(dummyApp, input)
            val swift = project.files.find { it.filename == "ContentView.swift" }?.content ?: ""
            _uiState.value = _uiState.value.copy(playgroundSwiftOutput = swift)
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
