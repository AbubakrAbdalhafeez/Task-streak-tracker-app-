package com.abubakr.taskstreak.data.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not fully configured or missing google-services.json: ${e.message}")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = auth != null

    fun currentUserFlow(): Flow<AuthUserState?> = callbackFlow {
        val instance = auth
        if (instance == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user?.toAuthUserState())
        }

        instance.addAuthStateListener(listener)
        trySend(instance.currentUser?.toAuthUserState())

        awaitClose {
            instance.removeAuthStateListener(listener)
        }
    }

    fun getCurrentUser(): AuthUserState? {
        return auth?.currentUser?.toAuthUserState()
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        val instance = auth ?: return AuthResult.Error("Firebase is not initialized.")
        return try {
            val result = instance.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.toAuthUserState(), "Signed in as ${user.email ?: user.displayName ?: "User"}")
            } else {
                AuthResult.Error("Failed to retrieve user.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail failed", e)
            AuthResult.Error(e.localizedMessage ?: "Sign in failed.")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): AuthResult {
        val instance = auth ?: return AuthResult.Error("Firebase is not initialized.")
        return try {
            val result = instance.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                if (!displayName.isNullOrBlank()) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.trim())
                            .build()
                        user.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to update display name", e)
                    }
                }
                AuthResult.Success(user.toAuthUserState(), "Account created successfully!")
            } else {
                AuthResult.Error("Failed to create account.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail failed", e)
            AuthResult.Error(e.localizedMessage ?: "Sign up failed.")
        }
    }

    suspend fun signInWithGoogleAccount(account: GoogleSignInAccount): AuthResult {
        val instance = auth ?: return AuthResult.Error("Firebase is not initialized.")
        val idToken = account.idToken
        return if (idToken != null) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = instance.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    AuthResult.Success(user.toAuthUserState(), "Firebase connected with Google (${user.email})")
                } else {
                    AuthResult.Error("Firebase user is null after Google sign in.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "signInWithCredential failed", e)
                AuthResult.Error(e.localizedMessage ?: "Google sign-in to Firebase failed.")
            }
        } else {
            // No idToken provided by GoogleSignInAccount (e.g. only Drive scope was requested)
            // Fall back gracefully to current Firebase state or mock connection
            val current = getCurrentUser()
            if (current != null) {
                AuthResult.Success(current, "Authenticated with Google account: ${account.email}")
            } else {
                AuthResult.Error("Google ID token unavailable. Configure Web Client ID in Google Cloud.")
            }
        }
    }

    suspend fun signInAnonymously(): AuthResult {
        val instance = auth ?: return AuthResult.Error("Firebase is not initialized.")
        return try {
            val result = instance.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.toAuthUserState(), "Signed in anonymously")
            } else {
                AuthResult.Error("Failed to create anonymous session.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInAnonymously failed", e)
            AuthResult.Error(e.localizedMessage ?: "Anonymous sign-in failed.")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        val instance = auth ?: return AuthResult.Error("Firebase is not initialized.")
        return try {
            instance.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(
                AuthUserState("dummy", email, null, null),
                "Password reset email sent to $email"
            )
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail failed", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to send reset email.")
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "signOut failed", e)
        }
    }

    private fun FirebaseUser.toAuthUserState(): AuthUserState {
        return AuthUserState(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous
        )
    }
}
