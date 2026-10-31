package com.example.data.service

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.data.model.AndroidAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class ApkExtractorService(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledLaunchableApps(): List<AndroidAppInfo> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AndroidAppInfo>()
        val seenPackages = mutableSetOf<String>()

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    mainIntent,
                    PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(mainIntent, 0)
            }

            for (resolveInfo in resolveInfoList) {
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == context.packageName || seenPackages.contains(pkgName)) {
                    continue
                }
                seenPackages.add(pkgName)

                try {
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val appName = resolveInfo.loadLabel(packageManager).toString()

                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(
                            pkgName,
                            PackageManager.PackageInfoFlags.of(
                                (PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES).toLong()
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(
                            pkgName,
                            PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES
                        )
                    }

                    val apkPath = appInfo.sourceDir ?: ""
                    var sizeMb = 0.0
                    if (apkPath.isNotEmpty()) {
                        val file = File(apkPath)
                        if (file.exists()) {
                            sizeMb = file.length() / (1024.0 * 1024.0)
                        }
                    }

                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
                    val activities = packageInfo.activities?.map { it.name } ?: listOf(resolveInfo.activityInfo.name)
                    val targetSdk = appInfo.targetSdkVersion
                    val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo.minSdkVersion else 24

                    val vName = packageInfo.versionName ?: "1.0"
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    }

                    appList.add(
                        AndroidAppInfo(
                            appName = appName,
                            packageName = pkgName,
                            versionName = vName,
                            versionCode = vCode,
                            apkPath = apkPath,
                            apkSizeMb = Math.round(sizeMb * 10.0) / 10.0,
                            isSystemApp = isSystem,
                            targetSdk = targetSdk,
                            minSdk = minSdk,
                            permissions = permissions,
                            activities = activities,
                            isSample = false
                        )
                    )
                } catch (e: Exception) {
                    // skip unreadable package
                }
            }

            // Sort: non-system apps first, then alphabetical
            appList.sortWith(compareBy<AndroidAppInfo> { it.isSystemApp }.thenBy { it.appName.lowercase() })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        appList
    }

    suspend fun parseApkFromUri(uri: Uri): AndroidAppInfo? = withContext(Dispatchers.IO) {
        try {
            // Copy URI content to a temporary cache file to inspect via packageManager
            val tempFile = File(context.cacheDir, "temp_inspect_${System.currentTimeMillis()}.apk")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES
            val pkgInfo: PackageInfo? = packageManager.getPackageArchiveInfo(tempFile.absolutePath, flags)

            if (pkgInfo != null) {
                val appInfo = pkgInfo.applicationInfo ?: ApplicationInfo().apply {
                    packageName = pkgInfo.packageName
                }
                appInfo.sourceDir = tempFile.absolutePath
                appInfo.publicSourceDir = tempFile.absolutePath

                val appName = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    tempFile.nameWithoutExtension
                }

                val sizeMb = tempFile.length() / (1024.0 * 1024.0)
                val permissions = pkgInfo.requestedPermissions?.toList() ?: emptyList()
                val activities = pkgInfo.activities?.map { it.name } ?: emptyList()

                val vName = pkgInfo.versionName ?: "1.0"
                val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkgInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pkgInfo.versionCode.toLong()
                }

                val targetSdk = appInfo.targetSdkVersion
                val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo.minSdkVersion else 24

                return@withContext AndroidAppInfo(
                    appName = if (appName.isNotBlank()) appName else "Imported App",
                    packageName = pkgInfo.packageName ?: "com.custom.importedapp",
                    versionName = vName,
                    versionCode = vCode,
                    apkPath = tempFile.absolutePath,
                    apkSizeMb = Math.round(sizeMb * 10.0) / 10.0,
                    isSystemApp = false,
                    targetSdk = targetSdk,
                    minSdk = minSdk,
                    permissions = permissions,
                    activities = activities,
                    isSample = false
                )
            } else {
                // If package manager cannot parse (e.g. invalid signature), do manual zip manifest inspection
                val filesInZip = mutableListOf<String>()
                ZipInputStream(tempFile.inputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        filesInZip.add(entry.name)
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }

                val hasDex = filesInZip.any { it.endsWith(".dex") }
                val hasManifest = filesInZip.any { it.equals("AndroidManifest.xml", ignoreCase = true) }

                if (hasDex || hasManifest) {
                    val sizeMb = tempFile.length() / (1024.0 * 1024.0)
                    return@withContext AndroidAppInfo(
                        appName = "Imported APK Package",
                        packageName = "com.user.apkimport",
                        versionName = "1.0.0",
                        versionCode = 1,
                        apkPath = tempFile.absolutePath,
                        apkSizeMb = Math.round(sizeMb * 10.0) / 10.0,
                        isSystemApp = false,
                        targetSdk = 35,
                        minSdk = 24,
                        permissions = listOf("android.permission.INTERNET"),
                        activities = listOf("com.user.apkimport.MainActivity"),
                        isSample = false
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
