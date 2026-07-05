package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class FirebaseAuthResult {
    data class Success(val email: String, val localId: String, val idToken: String) : FirebaseAuthResult()
    data class Error(val message: String) : FirebaseAuthResult()
}

class CemvaAuthManager private constructor(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("cemva_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val _webApiKey = MutableStateFlow(sharedPrefs.getString("firebase_web_api_key", "") ?: "")
    val webApiKey: StateFlow<String> = _webApiKey

    private val _isAuthenticated = MutableStateFlow(sharedPrefs.getString("firebase_id_token", null) != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage

    companion object {
        @Volatile
        private var INSTANCE: CemvaAuthManager? = null

        fun getInstance(context: Context): CemvaAuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CemvaAuthManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun updateWebApiKey(key: String) {
        _webApiKey.value = key
        sharedPrefs.edit().putString("firebase_web_api_key", key).apply()
        _authStatusMessage.value = "Updated Firebase Web API Key"
    }

    suspend fun signUp(email: String, password: String): FirebaseAuthResult = withContext(Dispatchers.IO) {
        val key = _webApiKey.value.trim()
        if (key.isEmpty()) {
            return@withContext FirebaseAuthResult.Error("Firebase Auth Web API Key is not configured. Falling back to local secure auth.")
        }

        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$key"
        val requestBodyJson = """
            {
                "email": "$email",
                "password": "$password",
                "returnSecureToken": true
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val adapter = moshi.adapter(FirebaseSignUpResponse::class.java)
                    val result = adapter.fromJson(bodyStr)
                    if (result != null) {
                        sharedPrefs.edit()
                            .putString("firebase_id_token", result.idToken)
                            .putString("firebase_local_id", result.localId)
                            .putString("firebase_email", result.email)
                            .apply()
                        _isAuthenticated.value = true
                        _authStatusMessage.value = "Registered successfully in Firebase Auth!"
                        FirebaseAuthResult.Success(result.email, result.localId, result.idToken)
                    } else {
                        FirebaseAuthResult.Error("Moshi parsing failed for Firebase response.")
                    }
                } else {
                    val errorMsg = parseFirebaseError(bodyStr)
                    FirebaseAuthResult.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e("CemvaAuthManager", "Firebase Sign Up Request failed", e)
            FirebaseAuthResult.Error("Network Error: ${e.message ?: "Could not connect to Firebase"}")
        }
    }

    suspend fun signIn(email: String, password: String): FirebaseAuthResult = withContext(Dispatchers.IO) {
        val key = _webApiKey.value.trim()
        if (key.isEmpty()) {
            return@withContext FirebaseAuthResult.Error("Firebase Auth Web API Key is empty. Falling back to local secure auth.")
        }

        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$key"
        val requestBodyJson = """
            {
                "email": "$email",
                "password": "$password",
                "returnSecureToken": true
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val adapter = moshi.adapter(FirebaseSignInResponse::class.java)
                    val result = adapter.fromJson(bodyStr)
                    if (result != null) {
                        sharedPrefs.edit()
                            .putString("firebase_id_token", result.idToken)
                            .putString("firebase_local_id", result.localId)
                            .putString("firebase_email", result.email)
                            .apply()
                        _isAuthenticated.value = true
                        _authStatusMessage.value = "Successfully authenticated against Firebase!"
                        FirebaseAuthResult.Success(result.email, result.localId, result.idToken)
                    } else {
                        FirebaseAuthResult.Error("Moshi parsing failed for Firebase response.")
                    }
                } else {
                    val errorMsg = parseFirebaseError(bodyStr)
                    FirebaseAuthResult.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e("CemvaAuthManager", "Firebase Sign In Request failed", e)
            FirebaseAuthResult.Error("Network Error: ${e.message ?: "Could not connect to Firebase"}")
        }
    }

    fun logout() {
        sharedPrefs.edit()
            .remove("firebase_id_token")
            .remove("firebase_local_id")
            .remove("firebase_email")
            .apply()
        _isAuthenticated.value = false
        _authStatusMessage.value = "Logged out from Firebase Authentication"
    }

    private fun parseFirebaseError(jsonStr: String): String {
        return try {
            val adapter = moshi.adapter(FirebaseErrorWrapper::class.java)
            val wrapper = adapter.fromJson(jsonStr)
            wrapper?.error?.message ?: "Firebase Authentication Error"
        } catch (e: Exception) {
            "Firebase Authentication Failed"
        }
    }
}

// Data Classes for Moshi JSON parsing
data class FirebaseSignUpResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String
)

data class FirebaseSignInResponse(
    val idToken: String,
    val email: String,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String,
    val registered: Boolean?
)

data class FirebaseErrorWrapper(
    val error: FirebaseErrorDetail?
)

data class FirebaseErrorDetail(
    val code: Int?,
    val message: String?,
    val errors: List<Map<String, Any>>?
)
