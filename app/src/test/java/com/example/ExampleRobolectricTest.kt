package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SampleAndroidApps
import com.example.data.service.CodeTranspilerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DroidToXcode", appName)
    }

    @Test
    fun `transpile sample app generates xcode project`() {
        val sampleApp = SampleAndroidApps.sampleApps.first()
        val engine = CodeTranspilerEngine()
        val xcodeProject = engine.transpileToXcodeProject(sampleApp)

        assertTrue(xcodeProject.files.isNotEmpty())
        assertTrue(xcodeProject.files.any { it.filename == "ContentView.swift" })
        assertTrue(xcodeProject.files.any { it.filename == "Info.plist" })
        assertTrue(xcodeProject.files.any { it.filename == "Package.swift" })
        assertTrue(xcodeProject.compatibilityScore > 80)
    }
}
