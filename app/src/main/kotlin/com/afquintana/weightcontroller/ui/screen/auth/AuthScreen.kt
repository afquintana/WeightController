package com.afquintana.weightcontroller.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.afquintana.weightcontroller.R
import com.afquintana.weightcontroller.viewmodel.AuthUiState

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onIdealWeightChange: (String) -> Unit,
    onSexChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it) } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.auth_header),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Weight Controller", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (state.isLoginMode) "Accede a tu cuenta y registra tus pesajes" else "Crea tu cuenta y guarda tu evolución en la nube",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(20.dp))
            AuthForm(
                padding = PaddingValues(0.dp),
                state = state,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onNameChange = onNameChange,
                onHeightChange = onHeightChange,
                onIdealWeightChange = onIdealWeightChange,
                onSexChange = onSexChange,
                onToggleMode = onToggleMode,
                onLogin = onLogin,
                onRegister = onRegister
            )
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthForm(
    padding: PaddingValues,
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onIdealWeightChange: (String) -> Unit,
    onSexChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sexOptions = listOf("Hombre", "Mujer", "Otro")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(padding)) {
        if (!state.isLoginMode) {
            OutlinedTextField(value = state.name, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true)
        }
        OutlinedTextField(value = state.email, onValueChange = onEmailChange, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        OutlinedTextField(value = state.password, onValueChange = onPasswordChange, modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)

        if (!state.isLoginMode) {
            OutlinedTextField(value = state.heightCm, onValueChange = onHeightChange, modifier = Modifier.fillMaxWidth(), label = { Text("Estatura (cm)") }, singleLine = true)
            OutlinedTextField(value = state.idealWeightKg, onValueChange = onIdealWeightChange, modifier = Modifier.fillMaxWidth(), label = { Text("Peso ideal (kg)") }, singleLine = true)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = state.sex,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Sexo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    sexOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            onSexChange(option)
                            expanded = false
                        })
                    }
                }
            }
        }

        Button(
            onClick = { if (state.isLoginMode) onLogin() else onRegister() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (state.isLoginMode) "Entrar" else "Registrarme")
        }

        TextButton(onClick = onToggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (state.isLoginMode) "No tengo cuenta. Quiero registrarme" else "Ya tengo cuenta. Quiero iniciar sesión")
        }
    }
}
