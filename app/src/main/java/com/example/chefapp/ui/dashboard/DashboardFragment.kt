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
        binding.tvFeaturedTitle.text = "Pollo al Limón"
        binding.tvFeaturedDesc.text = "Pechuga de pollo jugosa con salsa de limón y hierbas"
        
        // Formatear precio destacado
        try {
            val precio = 15500.0
            binding.tvFeaturedPrice.text = currencyFormat.format(precio)
        } catch (e: Exception) {
            binding.tvFeaturedPrice.text = "$ 15.500"
        }
        
        binding.tvFeaturedCategory.text = "Aves"
        
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?q=80&w=800&auto=format&fit=crop")
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
        
        // Formatear ingresos a Pesos Colombianos
        try {
            val ingresos = state.ingresos.replace(".", "").toDouble()
            binding.tvIngresosCount.text = currencyFormat.format(ingresos)
        } catch (e: Exception) {
            binding.tvIngresosCount.text = "$ ${state.ingresos}"
        }
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
            color = Color.parseColor("#FF9800")
            valueTextColor = Color.GRAY
            lineWidth = 3f
            setCircleColor(Color.parseColor("#FF9800"))
            circleRadius = 5f
            setDrawFilled(true)
            fillColor = Color.parseColor("#FFE0B2")
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
            gridColor = Color.parseColor("#E0E0E0")
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