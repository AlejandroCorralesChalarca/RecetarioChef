package com.example.chefapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.databinding.FragmentDashboardBinding
import com.example.chefapp.viewmodel.DashboardViewModel
import com.example.chefapp.viewmodel.DashboardUiState
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
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        setupObservers()
        setupChart()
        setupFeaturedRecipe()

        return binding.root
    }

    private fun setupFeaturedRecipe() {

        binding.tvFeaturedTitle.text = "Pasta Carbonara Real"
        binding.tvFeaturedDesc.text = "Receta original romana con guanciale"
        
        try {
            val precio = 22000.0
            binding.tvFeaturedPrice.text = currencyFormat.format(precio)
        } catch (e: Exception) {
            binding.tvFeaturedPrice.text = "$ 22.000"
        }
        
        binding.tvFeaturedTime.text = "20 min"
        
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1612874742237-6526221588e3?q=80&w=800&auto=format&fit=crop")
            .centerCrop()
            .placeholder(R.drawable.bg_chip_selected)
            .into(binding.imgFeatured)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    actualizarUI(state)
                }
            }
        }
    }

    private fun actualizarUI(state: DashboardUiState) {
        binding.tvTotalPedidosCount.text = state.totalPedidos.toString()
        binding.tvCompletadosCount.text = state.completados.toString()
        binding.tvEnProcesoCount.text = state.enProceso.toString()
        
        // Formato para el diseño premium
        binding.tvIngresosCount.text = "$1.2M"
    }

    private fun setupChart() {
        val chart = binding.chartPedidos
        val entries = ArrayList<Entry>().apply {
            add(Entry(0f, 45f))
            add(Entry(1f, 52f))
            add(Entry(2f, 68f))
            add(Entry(3f, 55f))
            add(Entry(4f, 72f))
            add(Entry(5f, 64f))
            add(Entry(6f, 80f))
        }

        val dataSet = LineDataSet(entries, "Pedidos").apply {
            color = Color.parseColor("#F05A28")
            valueTextColor = Color.GRAY
            lineWidth = 3f
            setCircleColor(Color.parseColor("#F05A28"))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFF3E0")
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(arrayOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"))
            textColor = Color.GRAY
        }
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#E9ECEF")
            textColor = Color.GRAY
        }
        chart.animateY(1000)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
