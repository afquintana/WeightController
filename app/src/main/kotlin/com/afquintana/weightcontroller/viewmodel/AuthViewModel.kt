package com.afquintana.weightcontroller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.afquintana.weightcontroller.data.auth.AuthRepository
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.RegisterInput
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val heightCm: String = "",
    val idealWeightKg: String = "",
    val sex: String = "",
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthUser().collect { user ->
                _uiState.value = _uiState.value.copy(isLoggedIn = user != null)
            }
        }
    }

    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, errorMessage = null) }
    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value, errorMessage = null) }
    fun onHeightChange(value: String) { _uiState.value = _uiState.value.copy(heightCm = value, errorMessage = null) }
    fun onIdealWeightChange(value: String) { _uiState.value = _uiState.value.copy(idealWeightKg = value, errorMessage = null) }
    fun onSexChange(value: String) { _uiState.value = _uiState.value.copy(sex = value, errorMessage = null) }
    fun toggleMode() { _uiState.value = _uiState.value.copy(isLoginMode = !_uiState.value.isLoginMode, errorMessage = null) }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email y contraseña son obligatorios.")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            runCatching { authRepository.login(state.email.trim(), state.password) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "No se pudo iniciar sesión.")
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank() || state.heightCm.isBlank() || state.idealWeightKg.isBlank() || state.sex.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Completa todos los campos.")
            return
        }
        val heightCm = state.heightCm.toDoubleOrNull()
        val idealWeightKg = state.idealWeightKg.toDoubleOrNull()
        if (heightCm == null || idealWeightKg == null) {
            _uiState.value = state.copy(errorMessage = "Estatura y peso ideal deben ser numéricos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            runCatching {
                authRepository.register(RegisterInput(state.name.trim(), state.email.trim(), state.password, heightCm, idealWeightKg, state.sex))
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { error ->
                crashReporter.record(error)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "No se pudo registrar el usuario.")
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(authRepository, crashReporter) as T
}
