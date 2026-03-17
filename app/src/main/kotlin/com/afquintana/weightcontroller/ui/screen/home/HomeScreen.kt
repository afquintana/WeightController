package com.afquintana.weightcontroller.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afquintana.weightcontroller.R
import com.afquintana.weightcontroller.data.model.WeightEntry
import com.afquintana.weightcontroller.viewmodel.HomeUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    state: HomeUiState,
    onWeightInputChange: (String) -> Unit,
    onAddWeight: () -> Unit,
    onDeleteWeight: (String) -> Unit,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) { state.errorMessage?.let { snackbarHostState.showSnackbar(it) } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding().navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Hola, ${state.userName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Controla tu evolución de forma simple")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estatura: ${pretty(state.heightCm)} cm")
                    Text("Peso ideal: ${pretty(state.idealWeightKg)} kg")
                    Text("Último IMC: ${state.lastBmi?.let { pretty(it) } ?: "--"}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Nuevo pesaje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.newWeightInput,
                            onValueChange = onWeightInputChange,
                            label = { Text("Peso actual (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = onAddWeight, enabled = !state.isSaving) {
                            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Añadir")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Histórico de pesajes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            if (state.weights.isEmpty()) {
                ElevatedCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.empty_weights), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Todavía no has registrado pesajes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Añade tu primer peso para empezar a calcular tu IMC.")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.weights, key = { it.id }) { item ->
                        ElevatedCard {
                            Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${pretty(item.weightKg)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AssistChip(onClick = {}, label = { Text("IMC ${pretty(item.bmi)}") })
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(formatDate(item.createdAt), style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onDeleteWeight(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

private fun pretty(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.2f", value)

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
