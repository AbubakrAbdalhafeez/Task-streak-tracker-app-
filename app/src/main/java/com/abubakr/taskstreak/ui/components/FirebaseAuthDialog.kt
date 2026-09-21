package com.abubakr.taskstreak.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.abubakr.taskstreak.R

enum class AuthDialogMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseAuthDialog(
    initialMode: AuthDialogMode = AuthDialogMode.SIGN_IN,
    onDismiss: () -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onSignUpWithEmail: (email: String, pass: String, displayName: String) -> Unit,
    onSendPasswordReset: (email: String) -> Unit,
    onSignInAnonymously: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var mode by remember { mutableStateOf(initialMode) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("firebase_auth_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (mode) {
                                AuthDialogMode.SIGN_IN -> Icons.Default.Lock
                                AuthDialogMode.SIGN_UP -> Icons.Default.PersonAdd
                                AuthDialogMode.FORGOT_PASSWORD -> Icons.Default.MarkEmailRead
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = when (mode) {
                        AuthDialogMode.SIGN_IN -> stringResource(R.string.firebase_sign_in)
                        AuthDialogMode.SIGN_UP -> stringResource(R.string.firebase_sign_up)
                        AuthDialogMode.FORGOT_PASSWORD -> stringResource(R.string.firebase_forgot_password)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (mode) {
                        AuthDialogMode.SIGN_IN -> "Sign in to securely sync your streak data across devices."
                        AuthDialogMode.SIGN_UP -> "Create an account to keep your habits and streaks backed up safely."
                        AuthDialogMode.FORGOT_PASSWORD -> "Enter your email address to receive password reset instructions."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Display Name (only in Sign Up)
                if (mode == AuthDialogMode.SIGN_UP) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text(stringResource(R.string.firebase_display_name_label)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_display_name_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.firebase_email_label)) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = if (mode == AuthDialogMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (mode == AuthDialogMode.FORGOT_PASSWORD && email.isNotBlank()) {
                                onSendPasswordReset(email)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Field (Sign In & Sign Up)
                if (mode != AuthDialogMode.FORGOT_PASSWORD) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.firebase_password_label)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    if (mode == AuthDialogMode.SIGN_IN) {
                                        onSignInWithEmail(email, password)
                                    } else {
                                        onSignUpWithEmail(email, password, displayName)
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Forgot Password link (in Sign In mode)
                if (mode == AuthDialogMode.SIGN_IN) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { mode = AuthDialogMode.FORGOT_PASSWORD },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.firebase_forgot_password),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Primary Action Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when (mode) {
                            AuthDialogMode.SIGN_IN -> onSignInWithEmail(email, password)
                            AuthDialogMode.SIGN_UP -> onSignUpWithEmail(email, password, displayName)
                            AuthDialogMode.FORGOT_PASSWORD -> onSendPasswordReset(email)
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && (mode == AuthDialogMode.FORGOT_PASSWORD || password.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_primary_action_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = when (mode) {
                                AuthDialogMode.SIGN_IN -> stringResource(R.string.firebase_sign_in)
                                AuthDialogMode.SIGN_UP -> stringResource(R.string.firebase_sign_up)
                                AuthDialogMode.FORGOT_PASSWORD -> stringResource(R.string.firebase_reset_password)
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle between Sign In / Sign Up
                when (mode) {
                    AuthDialogMode.SIGN_IN -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Don't have an account?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { mode = AuthDialogMode.SIGN_UP }) {
                                Text(
                                    text = stringResource(R.string.firebase_sign_up),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Guest / Anonymous Sign In Option
                        OutlinedButton(
                            onClick = onSignInAnonymously,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("auth_anonymous_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.firebase_anonymous))
                        }
                    }
                    AuthDialogMode.SIGN_UP -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { mode = AuthDialogMode.SIGN_IN }) {
                                Text(
                                    text = stringResource(R.string.firebase_sign_in),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    AuthDialogMode.FORGOT_PASSWORD -> {
                        TextButton(onClick = { mode = AuthDialogMode.SIGN_IN }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back to Sign In")
                        }
                    }
                }
            }
        }
    }
}
