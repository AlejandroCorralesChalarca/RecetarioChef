package com.example.chefapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chefapp.data.remote.FirebaseService
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

data class DashboardUiState(
    val totalPedidos: Int = 0,
    val completados: Int = 0, // En UI como "Listos"
    val enProceso: Int = 0,   // En UI como "Preparando"
    val ingresos: String = "$0",
    val featuredRecetas: List<Receta> = emptyList(),
    val chartData: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val chartLabels: List<String> = listOf("", "", "", "", "", "", ""),
    val trendText: String = "Sin actividad reciente",
    val isLoading: Boolean = true
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
            combine(
                firebaseService.getPedidos(),
                firebaseService.getRecetas()
            ) { pedidos, recetas ->
                // Mapeo de estados según la lógica de la app
                val total = pedidos.size
                val listos = pedidos.count { it.estado == "Listo" }
                val preparando = pedidos.count { it.estado == "Pendiente" || it.estado == "En Preparación" }
                val finalizados = pedidos.filter { it.estado == "Finalizado" }
                val sumaIngresos = finalizados.sumOf { it.total }
                
                // Carrusel: Obtener recetas más populares o las últimas 5
                val topRecetas = findTopRecipes(pedidos, recetas)
                val featured = if (topRecetas.isNotEmpty()) topRecetas.take(5) else recetas.take(5)

                val (chartData, chartLabels) = calculateChartData(pedidos)

                DashboardUiState(
                    totalPedidos = total,
                    completados = listos,
                    enProceso = preparando,
                    ingresos = formatIngresos(sumaIngresos),
                    featuredRecetas = featured,
                    chartData = chartData,
                    chartLabels = chartLabels,
                    trendText = calculateTrend(pedidos),
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
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
        
        // Iteramos los últimos 7 días terminando en hoy
        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            
            // Inicio del día
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            // Fin del día
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis
            
            // Contamos pedidos en este rango de tiempo
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
        return if (diff >= 0) {
            "Tendencia de pedidos en alza (↑$diff%)"
        } else {
            "Tendencia de pedidos a la baja (↓${abs(diff)}%)"
        }
    }

    private fun formatIngresos(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format(Locale.US, "$%.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format(Locale.US, "$%.0fK", amount / 1_000)
            else -> currencyFormat.format(amount)
        }
    }
}
