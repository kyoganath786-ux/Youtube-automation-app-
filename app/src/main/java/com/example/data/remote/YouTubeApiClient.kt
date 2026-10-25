package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AnalyticsData
import com.example.data.model.ChannelProfile
import com.example.data.model.ConnectionStatus
import com.example.data.model.TopVideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object YouTubeApiClient {

    private const val TAG = "YouTubeApiClient"
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if YouTube API Key or credentials are configured
     */
    fun isConfigured(): Boolean {
        val key = BuildConfig.YOUTUBE_API_KEY
        return !key.isNullOrBlank() && key != "MY_YOUTUBE_API_KEY"
    }

    /**
     * Fetches real channel telemetry from official YouTube Data API v3
     */
    suspend fun fetchChannelProfile(
        channelIdentifier: String,
        authToken: String? = null
    ): ChannelProfile = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val hasKey = isConfigured()
        val hasAuth = !authToken.isNullOrBlank()

        if (!hasKey && !hasAuth) {
            return@withContext ChannelProfile(
                status = ConnectionStatus.NOT_CONNECTED,
                errorMessage = "YouTube Data API credentials not configured. Please connect your authorized account."
            )
        }

        try {
            val url = if (channelIdentifier.startsWith("@")) {
                "$BASE_URL/channels?part=snippet,statistics&forHandle=$channelIdentifier&key=$apiKey"
            } else if (hasAuth) {
                "$BASE_URL/channels?part=snippet,statistics&mine=true"
            } else {
                "$BASE_URL/channels?part=snippet,statistics&id=$channelIdentifier&key=$apiKey"
            }

            val requestBuilder = Request.Builder().url(url)
            if (hasAuth) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "YouTube API response error: $bodyString")
                return@withContext ChannelProfile(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Failed to retrieve channel: HTTP ${response.code}"
                )
            }

            val json = JSONObject(bodyString)
            val items = json.optJSONArray("items")
            if (items == null || items.length() == 0) {
                return@withContext ChannelProfile(
                    status = ConnectionStatus.ERROR,
                    errorMessage = "Channel '$channelIdentifier' was not found on YouTube."
                )
            }

            val channelObj = items.getJSONObject(0)
            val id = channelObj.optString("id")
            val snippet = channelObj.optJSONObject("snippet")
            val statistics = channelObj.optJSONObject("statistics")

            val title = snippet?.optString("title") ?: "YouTube Creator"
            val handle = snippet?.optString("customUrl") ?: "@$title"
            val avatarUrl = snippet?.optJSONObject("thumbnails")?.optJSONObject("medium")?.optString("url")

            val subscriberCount = statistics?.optString("subscriberCount")?.toLongOrNull() ?: 0L
            val viewCount = statistics?.optString("viewCount")?.toLongOrNull() ?: 0L
            val videoCount = statistics?.optString("videoCount")?.toIntOrNull() ?: 0

            ChannelProfile(
                status = ConnectionStatus.CONNECTED,
                channelId = id,
                channelName = title,
                handle = handle,
                avatarUrl = avatarUrl,
                subscribers = subscriberCount,
                totalVideos = videoCount,
                totalViews = viewCount,
                lastRefreshedTimestamp = System.currentTimeMillis(),
                isCached = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception querying YouTube Data API", e)
            ChannelProfile(
                status = ConnectionStatus.ERROR,
                errorMessage = e.message ?: "Network error connecting to YouTube API"
            )
        }
    }

    /**
     * Uploads actual video file to YouTube via official Resumable Upload protocol
     */
    suspend fun uploadVideo(
        title: String,
        description: String,
        tags: List<String>,
        privacy: String,
        videoFile: File,
        authToken: String?,
        onProgress: (Int) -> Unit
    ): UploadResponse = withContext(Dispatchers.IO) {
        if (authToken.isNullOrBlank()) {
            return@withContext UploadResponse(
                success = false,
                error = "Authorization required. Please connect your verified YouTube channel."
            )
        }

        if (!videoFile.exists() || videoFile.length() == 0L) {
            return@withContext UploadResponse(
                success = false,
                error = "Rendered video file does not exist on disk."
            )
        }

        try {
            // Step 1: Initiate Resumable Upload session
            val metadata = JSONObject().apply {
                put("snippet", JSONObject().apply {
                    put("title", title)
                    put("description", description)
                    put("tags", org.json.JSONArray(tags))
                    put("categoryId", "28") // Science & Technology
                })
                put("status", JSONObject().apply {
                    put("privacyStatus", privacy.lowercase())
                    put("selfDeclaredMadeForKids", false)
                })
            }

            val initUrl = "https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=snippet,status"
            val initRequest = Request.Builder()
                .url(initUrl)
                .header("Authorization", "Bearer $authToken")
                .header("X-Upload-Content-Type", "video/mp4")
                .header("X-Upload-Content-Length", videoFile.length().toString())
                .post(metadata.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val initResponse = client.newCall(initRequest).execute()
            val uploadUri = initResponse.header("Location")

            if (uploadUri.isNullOrBlank()) {
                return@withContext UploadResponse(
                    success = false,
                    error = "Failed to initiate resumable upload session: HTTP ${initResponse.code}"
                )
            }

            // Step 2: Upload Binary Bytes with progress tracking
            onProgress(25)
            val uploadRequest = Request.Builder()
                .url(uploadUri)
                .put(videoFile.asRequestBody("video/mp4".toMediaType()))
                .build()

            onProgress(65)
            val uploadResult = client.newCall(uploadRequest).execute()
            onProgress(95)

            val body = uploadResult.body?.string() ?: ""
            if (!uploadResult.isSuccessful) {
                return@withContext UploadResponse(
                    success = false,
                    error = "YouTube upload rejected: $body"
                )
            }

            val resultJson = JSONObject(body)
            val videoId = resultJson.optString("id")
            onProgress(100)

            UploadResponse(
                success = true,
                videoId = videoId,
                watchUrl = "https://youtu.be/$videoId"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed with exception", e)
            UploadResponse(success = false, error = e.message ?: "Upload network failure")
        }
    }
}

data class UploadResponse(
    val success: Boolean,
    val videoId: String? = null,
    val watchUrl: String? = null,
    val error: String? = null
)
