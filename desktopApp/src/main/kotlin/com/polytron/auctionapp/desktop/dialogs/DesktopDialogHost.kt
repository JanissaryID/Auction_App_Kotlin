package com.polytron.auctionapp.desktop.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import com.polytron.auctionapp.desktop.navigation.DesktopDialog
import com.polytron.auctionapp.presentation.auth.AuthViewModel

@Composable
fun DesktopDialogHost(
    dialog: DesktopDialog?,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    when (dialog) {
        DesktopDialog.Login -> LoginDialog(
            authViewModel = authViewModel,
            onDismiss = onDismiss
        )

        DesktopDialog.Profile -> ProfileDialog(
            authViewModel = authViewModel,
            onDismiss = onDismiss
        )

        null -> Unit
        else -> BasicDialog(
            title = "Dialog",
            onDismiss = onDismiss
        ) {
            Text(
                text = "Aksi ini akan tersedia pada fase layar fitur.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LoginDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    BasicDialog(
        title = "Login",
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = authViewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = authViewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let { message ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
            Spacer(Modifier.height(1.dp).weight(1f))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    strokeWidth = 2.dp
                )
            }
            Button(
                onClick = {
                    errorMessage = null
                    authViewModel.login(
                        onSuccess = onDismiss,
                        onError = { message -> errorMessage = message }
                    )
                },
                enabled = !isLoading
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
private fun ProfileDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    BasicDialog(
        title = "Profil",
        onDismiss = onDismiss
    ) {
        Text(
            text = userName?.takeIf { it.isNotBlank() } ?: "Pengguna",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = email.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Tutup")
            }
            if (isLoggedIn) {
                Spacer(Modifier.height(1.dp).weight(1f))
                Button(
                    onClick = {
                        authViewModel.logout()
                        onDismiss()
                    }
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun BasicDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DialogWindow(onCloseRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(min = 420.dp, max = 520.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(18.dp))
                content()
            }
        }
    }
}
