package com.example.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.XcodeProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class XcodeZipExporter(private val context: Context) {

    suspend fun exportProjectToZip(project: XcodeProject): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "xcode_exports").apply { mkdirs() }
        val zipFile = File(exportDir, "${project.appName}_XcodeProject.zip")

        if (zipFile.exists()) {
            zipFile.delete()
        }

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val rootFolder = "${project.appName}/"

            for (file in project.files) {
                val entryPath = rootFolder + file.path
                val entry = ZipEntry(entryPath)
                zos.putNextEntry(entry)
                zos.write(file.content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }

        zipFile
    }

    fun createShareIntent(zipFile: File, appName: String): Intent {
        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
        } catch (e: Exception) {
            // Fallback for direct uri if fileprovider not configured
            Uri.fromFile(zipFile)
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$appName Xcode Project")
            putExtra(Intent.EXTRA_TEXT, "Generated Apple Xcode Swift project for $appName translated from Android.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
