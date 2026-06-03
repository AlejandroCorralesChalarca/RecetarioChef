package com.example.chefapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.databinding.FragmentDashboardBinding
import com.example.chefapp.viewmodel.DashboardViewModel
import com.example.chefapp.viewmodel.DashboardUiState
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.domain.model.Receta
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    private var currentRecipeIndex = 0
    private val carouselHandler = Handler(Looper.getMainLooper())
    private val carouselRunnable = object : Runnable {
        override fun run() {
            val recipes = viewModel.uiState.value.featuredRecetas
            if (recipes.isNotEmpty()) {
                currentRecipeIndex = (currentRecipeIndex + 1) % recipes.size
                updateFeaturedRecipe(recipes[currentRecipeIndex])
            }
            carouselHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        setupListeners()
        setupObservers()
        
        return binding.root
    }

    private fun setupListeners() {
        // Al hacer clic en el carrusel, ir al detalle de la receta
        binding.carouselCard.setOnClickListener {
            val recipes = viewModel.uiState.value.featuredRecetas
            if (recipes.isNotEmpty()) {
                val currentReceta = recipes[currentRecipeIndex % recipes.size]
                mainViewModel.setRecetaSeleccionada(currentReceta)
                findNavController().navigate(R.id.navigation_recetas)
            }
        }

        // Navegación rápida desde las estadísticas
        binding.cardStatsContainer.setOnClickListener {
            findNavController().navigate(R.id.navigation_pedidos)
        }
    }

    override fun onResume() {
        super.onResume()
        carouselHandler.postDelayed(carouselRunnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        carouselHandler.removeCallbacks(carouselRunnable)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (!state.isLoading) {
                        actualizarUI(state)
                    }
                }
            }
        }
    }

    private fun actualizarUI(state: DashboardUiState) {
        binding.tvTotalPedidosCount.text = state.totalPedidos.toString()
        binding.tvCompletadosCount.text = state.completados.toString()
        binding.tvEnProcesoCount.text = state.enProceso.toString()
        binding.tvIngresosCount.text = state.ingresos
        
        binding.tvPromedioSemanal.text = state.trendText

        if (state.featuredRecetas.isNotEmpty()) {
            updateFeaturedRecipe(state.featuredRecetas[currentRecipeIndex % state.featuredRecetas.size])
        } else {
            binding.tvFeaturedTitle.text = "¡Crea tu primera receta!"
            binding.tvFeaturedDesc.text = "Tus platos destacados aparecerán aquí"
            binding.imgFeatured.setImageResource(R.drawable.bg_gradient_card)
        }

        updateChart(state.chartData, state.chartLabels)
    }

    private fun updateFeaturedRecipe(receta: Receta) {
        binding.tvFeaturedTitle.text = receta.nombre
        binding.tvFeaturedDesc.text = receta.descripcion
        binding.tvFeaturedTime.text = receta.tiempo
        
        try {
            val precioVal = receta.precio.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
            binding.tvFeaturedPrice.text = if (precioVal > 0) currencyFormat.format(precioVal) else "$ ${receta.precio}"
        } catch (e: Exception) {
            binding.tvFeaturedPrice.text = "$ ${receta.precio}"
        }

        Glide.with(this)
            .load(receta.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_gradient_card)
            .error(R.drawable.bg_gradient_card)
            .into(binding.imgFeatured)
    }

    private fun updateChart(chartData: List<Float>, labels: List<String>) {
        val entries = ArrayList<Entry>()
        chartData.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, "Pedidos").apply {
            color = Color.parseColor("#F05A28")
            setCircleColor(Color.parseColor("#F05A28"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFF3E0")
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = Color.GRAY
        }

        binding.chartPedidos.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textColor = Color.GRAY
            }
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E9ECEF")
                textColor = Color.GRAY
                axisMinimum = 0f
                granularity = 1f // Solo números enteros para cantidad de pedidos
            }
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
