package com.afquintana.weightcontroller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.weightcontroller.data.auth.AuthRepository
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.WeightEntry
import com.afquintana.weightcontroller.data.weight.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val heightCm: Double = 0.0,
    val idealWeightKg: Double = 0.0,
    val newWeightInput: String = "",
    val lastBmi: Double? = null,
    val weights: List<WeightEntry> = emptyList(),
    val isSaving: Boolean = false,
    val isProfileLoaded: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeWeights()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            runCatching { authRepository.getCurrentProfile() }
                .onSuccess { profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(
                            userName = profile.name,
                            heightCm = profile.heightCm,
                            idealWeightKg = profile.idealWeightKg,
                            isProfileLoaded = true,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isProfileLoaded = true,
                            errorMessage = "No se pudo cargar tu perfil. Cierra sesión y vuelve a entrar."
                        )
                    }
                }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(
                        isProfileLoaded = true,
                        errorMessage = error.message ?: "No se pudo cargar tu perfil."
                    )
                }
        }
    }

    private fun observeWeights() {
        viewModelScope.launch {
            weightRepository.observeWeights().collect { items ->
                _uiState.value = _uiState.value.copy(
                    weights = items,
                    lastBmi = items.lastOrNull()?.bmi
                )
            }
        }
    }

    fun onWeightInputChange(value: String) {
        _uiState.value = _uiState.value.copy(
            newWeightInput = value,
            errorMessage = null
        )
    }

    fun addWeight() {
        val state = _uiState.value

        if (!state.isProfileLoaded) {
            _uiState.value = state.copy(
                errorMessage = "Tu perfil todavía se está cargando. Inténtalo de nuevo en un momento."
            )
            return
        }

        val weight = state.newWeightInput.toDoubleOrNull()
        if (weight == null) {
            _uiState.value = state.copy(errorMessage = "Introduce un peso válido.")
            return
        }

        if (state.heightCm <= 0.0) {
            _uiState.value = state.copy(
                errorMessage = "No se ha podido cargar una estatura válida para tu perfil. Cierra sesión y vuelve a entrar."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            runCatching { weightRepository.addWeight(weight, state.heightCm) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        newWeightInput = ""
                    )
                }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "No se pudo guardar el pesaje."
                    )
                }
        }
    }

    fun deleteWeight(weightId: String) {
        viewModelScope.launch {
            runCatching { weightRepository.deleteWeight(weightId) }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo eliminar el pesaje."
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { authRepository.logout() }
                .onFailure { error -> crashReporter.record(error) }
        }
    }
}
