package com.example.data.model

import android.graphics.drawable.Drawable

enum class XcodeFileType {
    SWIFT,
    PLIST,
    PACKAGE_SWIFT,
    STRINGS,
    MARKDOWN,
    JSON
}

data class XcodeFile(
    val path: String,
    val filename: String,
    val content: String,
    val fileType: XcodeFileType
)

data class ApiMapping(
    val category: String,
    val androidConcept: String,
    val appleConcept: String,
    val compatibility: String, // "100% Direct", "High", "Partial", "Manual Review"
    val description: String
)

data class AndroidAppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val apkPath: String = "",
    val apkSizeMb: Double = 0.0,
    val isSystemApp: Boolean = false,
    val targetSdk: Int = 34,
    val minSdk: Int = 24,
    val permissions: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val isSample: Boolean = false,
    val sampleCategory: String = "",
    val sourceCodePreview: String = ""
)

data class AppleStoreResult(
    val trackName: String,
    val bundleId: String,
    val artistName: String,
    val artworkUrl: String,
    val averageUserRating: Double,
    val userRatingCount: Int,
    val description: String,
    val price: Double = 0.0,
    val formattedPrice: String = "Free",
    val primaryGenreName: String,
    val trackViewUrl: String,
    val screenshotUrls: List<String> = emptyList(),
    val minimumOsVersion: String = "16.0"
)

data class XcodeProject(
    val appName: String,
    val bundleId: String,
    val version: String = "1.0.0",
    val files: List<XcodeFile>,
    val compatibilityScore: Int, // e.g. 95%
    val detectedApis: List<ApiMapping>,
    val migrationWarnings: List<String>,
    val targetIosVersion: String = "17.0+",
    val swiftVersion: String = "6.0",
    val generatedTimestamp: Long = System.currentTimeMillis()
)
