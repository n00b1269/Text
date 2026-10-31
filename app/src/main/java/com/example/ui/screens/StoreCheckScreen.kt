package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppleStoreResult
import com.example.ui.AppScreen
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCheckScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val app = uiState.selectedApp ?: return
    val context = LocalContext.current

    BackHandler {
        viewModel.navigateTo(AppScreen.APP_SELECTION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Store Equivalence Check") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.APP_SELECTION) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Inspected APK Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3DDC84).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color(0xFF16A34A))
                            }

                            Column {
                                Text(app.appName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Version", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("${app.versionName} (${app.versionCode})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("APK File Size", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("${app.apkSizeMb} MB", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Target SDK", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("API ${app.targetSdk}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Permissions", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("${app.permissions.size}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Real-time Store Check Status
            if (uiState.isCheckingStore) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(44.dp))
                            Text(
                                text = "Querying official Apple App Store...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Checking if an iOS equivalent of \"${app.appName}\" is published on Apple App Store.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else if (uiState.storeCheckCompleted) {
                if (uiState.appleStoreResults.isNotEmpty()) {
                    // Equivalent Found
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A))))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                                    Text(
                                        "Official Apple iOS App Found!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Text(
                                    "An official iOS version is already published on the Apple App Store for this app.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }

                    // Show Apple Store listing cards
                    items(uiState.appleStoreResults.size) { index ->
                        val result = uiState.appleStoreResults[index]
                        AppleStoreItemCard(
                            result = result,
                            onOpenStore = {
                                if (result.trackViewUrl.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.trackViewUrl))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }

                    // Option to convert anyway
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Want to Port & Modify Custom Source Code?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "You can still convert this Android APK into a full Xcode Swift & SwiftUI project to examine the architecture, customize features, or build a custom iOS app.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = { viewModel.convertSelectedAppToXcode() },
                                    modifier = Modifier.fillMaxWidth().testTag("convert_anyway_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Convert APK to Xcode Project Anyway")
                                }
                            }
                        }
                    }
                } else {
                    // NO Equivalent Found - Exclusive Android App!
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF0F172A),
                                                Color(0xFF1E293B),
                                                Color(0xFF312E81)
                                            )
                                        )
                                    )
                                    .padding(22.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }
                                        Text(
                                            "Android-Exclusive App!",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "No official Apple iOS equivalent was found in the App Store for \"${app.appName}\".",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = "This app is ready to be converted into an Apple iOS app! Our translation engine will generate the Xcode project, SwiftUI views, ViewModels, and Info.plist.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    Button(
                                        onClick = { viewModel.convertSelectedAppToXcode() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .testTag("convert_exclusive_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Convert to Apple Xcode Project Now", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppleStoreItemCard(
    result: AppleStoreResult,
    onOpenStore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // App artwork from iTunes
                AsyncImage(
                    model = result.artworkUrl,
                    contentDescription = result.trackName,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(result.trackName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.artistName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text(
                            "${String.format("%.1f", result.averageUserRating)} (${result.userRatingCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(result.primaryGenreName, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Button(
                    onClick = onOpenStore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(result.formattedPrice, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Description preview
            if (result.description.isNotBlank()) {
                Text(
                    text = result.description.take(160) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            // Screenshots carousel if available
            if (result.screenshotUrls.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    result.screenshotUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Screenshot",
                            modifier = Modifier
                                .height(120.dp)
                                .width(68.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
