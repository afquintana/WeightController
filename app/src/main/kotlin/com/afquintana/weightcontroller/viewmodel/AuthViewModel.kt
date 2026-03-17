package com.afquintana.weightcontroller.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.weightcontroller.R
import com.afquintana.weightcontroller.data.auth.AuthRepository
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.RegisterInput
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val errorMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    @ApplicationContext private val context: Context,
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

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onHeightChange(value: String) {
        _uiState.value = _uiState.value.copy(heightCm = value, errorMessage = null)
    }

    fun onIdealWeightChange(value: String) {
        _uiState.value = _uiState.value.copy(idealWeightKg = value, errorMessage = null)
    }

    fun onSexChange(value: String) {
        _uiState.value = _uiState.value.copy(sex = value, errorMessage = null)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null,
        )
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = context.getString(R.string.error_email_password_required),
            )
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: context.getString(R.string.error_login_failed),
                    )
                }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (
            state.name.isBlank() ||
            state.email.isBlank() ||
            state.password.isBlank() ||
            state.heightCm.isBlank() ||
            state.idealWeightKg.isBlank() ||
            state.sex.isBlank()
        ) {
            _uiState.value = state.copy(errorMessage = context.getString(R.string.error_complete_all_fields))
            return
        }

        val heightCm = state.heightCm.toDoubleOrNull()
        val idealWeightKg = state.idealWeightKg.toDoubleOrNull()
        if (heightCm == null || idealWeightKg == null) {
            _uiState.value = state.copy(
                errorMessage = context.getString(R.string.error_enter_valid_profile_values),
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            runCatching {
                authRepository.register(
                    RegisterInput(
                        email = state.email.trim(),
                        password = state.password,
                        name = state.name.trim(),
                        heightCm = heightCm,
                        idealWeightKg = idealWeightKg,
                        sex = state.sex,
                    ),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { error ->
                crashReporter.record(error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                        ?: context.getString(R.string.error_register_failed),
                )
            }
        }
    }
}
