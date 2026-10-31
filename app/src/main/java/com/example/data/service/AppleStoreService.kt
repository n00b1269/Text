package com.example.data.service

import com.example.data.model.AppleStoreResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AppleStoreService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun searchAppleStore(appName: String, packageName: String? = null): List<AppleStoreResult> =
        withContext(Dispatchers.IO) {
            try {
                // Search primarily by app name, or fallback to clean keyword
                val cleanTerm = cleanAppName(appName)
                val encoded = URLEncoder.encode(cleanTerm, "UTF-8")
                val url = "https://itunes.apple.com/search?term=$encoded&entity=software&limit=5"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 DroidToXcode/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext emptyList()

                val json = JSONObject(body)
                val resultCount = json.optInt("resultCount", 0)
                if (resultCount == 0) return@withContext emptyList()

                val resultsArray = json.getJSONArray("results")
                val list = mutableListOf<AppleStoreResult>()

                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val trackName = item.optString("trackName", "")
                    val bundleId = item.optString("bundleId", "")
                    val artistName = item.optString("artistName", "")
                    val artworkUrl = item.optString("artworkUrl100", item.optString("artworkUrl60", ""))
                    val rating = item.optDouble("averageUserRating", 0.0)
                    val ratingCount = item.optInt("userRatingCount", 0)
                    val desc = item.optString("description", "")
                    val price = item.optDouble("price", 0.0)
                    val formattedPrice = item.optString("formattedPrice", if (price == 0.0) "Free" else "${'$'}$price")
                    val genre = item.optString("primaryGenreName", "Utilities")
                    val trackViewUrl = item.optString("trackViewUrl", "")
                    val minOs = item.optString("minimumOsVersion", "16.0")

                    val screenshots = mutableListOf<String>()
                    val screenshotsArray = item.optJSONArray("screenshotUrls")
                    if (screenshotsArray != null) {
                        for (s in 0 until minOf(4, screenshotsArray.length())) {
                            screenshots.add(screenshotsArray.getString(s))
                        }
                    }

                    list.add(
                        AppleStoreResult(
                            trackName = trackName,
                            bundleId = bundleId,
                            artistName = artistName,
                            artworkUrl = artworkUrl,
                            averageUserRating = rating,
                            userRatingCount = ratingCount,
                            description = desc,
                            price = price,
                            formattedPrice = formattedPrice,
                            primaryGenreName = genre,
                            trackViewUrl = trackViewUrl,
                            screenshotUrls = screenshots,
                            minimumOsVersion = minOs
                        )
                    )
                }

                return@withContext list
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }

    private fun cleanAppName(name: String): String {
        return name
            .replace(" - ", " ")
            .replace("Pro", "")
            .replace("Free", "")
            .replace(Regex("[^a-zA-Z0-9 ]"), "")
            .trim()
    }
}
