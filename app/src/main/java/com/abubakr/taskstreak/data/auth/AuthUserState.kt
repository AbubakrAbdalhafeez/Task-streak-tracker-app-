package com.abubakr.taskstreak.data.auth

data class AuthUserState(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false
)

sealed class AuthResult {
    data class Success(val user: AuthUserState, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
