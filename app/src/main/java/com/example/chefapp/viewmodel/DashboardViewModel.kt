package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.ui.UiState
import com.example.chefapp.crash.CrashReporter
import com.google.firebase.FirebaseNetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

data class DashboardUiState(
    val mainState: UiState<Boolean> = UiState.Loading,
    val totalPedidos: Int = 0,
    val completados: Int = 0,
    val enProceso: Int = 0,
    val ingresos: String = "$0",
    val featuredRecetas: List<Receta> = emptyList(),
    val chartData: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val chartLabels: List<String> = listOf("", "", "", "", "", "", ""),
    val trendText: String = "Sin actividad reciente"
)

class DashboardViewModel : ViewModel() {

    private val firebaseService = FirebaseService()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            try {
                combine(
                    firebaseService.getPedidos(),
                    firebaseService.getRecetas()
                ) { pedidos, recetas ->
                    if (pedidos.isEmpty() && recetas.isEmpty()) {
                        return@combine DashboardUiState(mainState = UiState.Empty)
                    }

                    val total = pedidos.size
                    val listos = pedidos.count { it.estado == "Listo" }
                    val preparando = pedidos.count { it.estado == "Pendiente" || it.estado == "En Preparación" }
                    val finalizados = pedidos.filter { it.estado == "Finalizado" }
                    val sumaIngresos = finalizados.sumOf { it.total }
                    
                    val topRecetas = findTopRecipes(pedidos, recetas)
                    val featured = if (topRecetas.isNotEmpty()) topRecetas.take(5) else recetas.take(5)

                    val (chartData, chartLabels) = calculateChartData(pedidos)

                    DashboardUiState(
                        mainState = UiState.Success(true),
                        totalPedidos = total,
                        completados = listos,
                        enProceso = preparando,
                        ingresos = formatIngresos(sumaIngresos),
                        featuredRecetas = featured,
                        chartData = chartData,
                        chartLabels = chartLabels,
                        trendText = calculateTrend(pedidos)
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                CrashReporter.logError(e, "Error en Dashboard")
                val errorState = if (e is FirebaseNetworkException) UiState.NoConnection else UiState.Error(e.message ?: "Error")
                _uiState.update { it.copy(mainState = errorState) }
            }
        }
    }

    private fun findTopRecipes(pedidos: List<Pedido>, recetas: List<Receta>): List<Receta> {
        val counts = mutableMapOf<String, Int>()
        pedidos.forEach { pedido ->
            pedido.items.forEach { item ->
                counts[item.nombre] = (counts[item.nombre] ?: 0) + item.cantidad
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .mapNotNull { entry -> recetas.find { it.nombre == entry.key } }
    }

    private fun calculateChartData(pedidos: List<Pedido>): Pair<List<Float>, List<String>> {
        val data = mutableListOf<Float>()
        val labels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("EEE", Locale("es", "CO"))
        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
            val startOfDay = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59)
            val endOfDay = calendar.timeInMillis
            val count = pedidos.count { it.timestamp in startOfDay..endOfDay }
            data.add(count.toFloat())
            labels.add(dateFormat.format(calendar.time).replaceFirstChar { it.uppercase() })
        }
        return Pair(data, labels)
    }

    private fun calculateTrend(pedidos: List<Pedido>): String {
        if (pedidos.size < 2) return "Comienza a registrar pedidos"
        val currentTotal = pedidos.take(5).sumOf { it.total }
        val previousTotal = pedidos.drop(5).take(5).sumOf { it.total }
        if (previousTotal == 0.0) return "Incremento de actividad detectado"
        val diff = ((currentTotal - previousTotal) / previousTotal * 100).toInt()
        return if (diff >= 0) "Tendencia de pedidos en alza (↑$diff%)" else "Tendencia de pedidos a la baja (↓${abs(diff)}%)"
    }

    private fun formatIngresos(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format(Locale.US, "$%.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format(Locale.US, "$%.0fK", amount / 1_000)
            else -> currencyFormat.format(amount)
        }
    }
}