package com.afquintana.weightcontroller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.afquintana.weightcontroller.data.auth.AuthRepository
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.WeightEntry
import com.afquintana.weightcontroller.data.weight.WeightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val heightCm: Double = 0.0,
    val idealWeightKg: Double = 0.0,
    val newWeightInput: String = "",
    val lastBmi: Double? = null,
    val weights: List<WeightEntry> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
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
                        _uiState.value = _uiState.value.copy(userName = profile.name, heightCm = profile.heightCm, idealWeightKg = profile.idealWeightKg)
                    }
                }
                .onFailure { crashReporter.record(it) }
        }
    }

    private fun observeWeights() {
        viewModelScope.launch {
            weightRepository.observeWeights().collect { items ->
                _uiState.value = _uiState.value.copy(weights = items, lastBmi = items.lastOrNull()?.bmi)
            }
        }
    }

    fun onWeightInputChange(value: String) { _uiState.value = _uiState.value.copy(newWeightInput = value, errorMessage = null) }

    fun addWeight() {
        val state = _uiState.value
        val weight = state.newWeightInput.toDoubleOrNull()
        if (weight == null) {
            _uiState.value = state.copy(errorMessage = "Introduce un peso válido.")
            return
        }
        if (state.heightCm <= 0.0) {
            _uiState.value = state.copy(errorMessage = "No hay estatura válida en el perfil.")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            runCatching { weightRepository.addWeight(weight, state.heightCm) }
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, newWeightInput = "") }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message ?: "No se pudo guardar el pesaje.")
                }
        }
    }

    fun deleteWeight(weightId: String) {
        viewModelScope.launch {
            runCatching { weightRepository.deleteWeight(weightId) }
                .onFailure { error ->
                    crashReporter.record(error)
                    _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "No se pudo eliminar el pesaje.")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { authRepository.logout() }.onFailure { crashReporter.record(it) }
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository,
    private val crashReporter: CrashReporter
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(authRepository, weightRepository, crashReporter) as T
}
