package com.example.chefapp.ui.pedidos

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.example.chefapp.R
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.databinding.FragmentPedidosBinding
import com.example.chefapp.viewmodel.DialogType
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.viewmodel.PedidosViewModel
import com.example.chefapp.viewmodel.PedidosUiState
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PedidosViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: PedidosAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = PedidosAdapter(
            pedidos = emptyList(),
            onActionClick = { pedido -> viewModel.cambiarEstadoPedido(pedido) },
            onCardClick = { pedido -> viewModel.seleccionarPedido(pedido) },
            onModifyClick = { pedido -> 
                mainViewModel.showDialog(DialogType.NUEVO_PEDIDO, pedido)
            },
            onDeleteClick = { pedido ->
                confirmarEliminacion(pedido)
            }
        )
        binding.recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPedidos.adapter = adapter
    }

    private fun confirmarEliminacion(pedido: Pedido) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Pedido")
            .setMessage("¿Estás seguro de que deseas eliminar el pedido #${pedido.numeroPedido}?")
            .setPositiveButton("Eliminar") { _, _ ->
                mainViewModel.eliminarPedido(pedido.docId)
                Toast.makeText(requireContext(), "Pedido eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupSearch() {
        binding.editBuscarPedido.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupEstadosPedidos.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                viewModel.filtrarPorEstado(chip.text.toString())
            }
        }
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

    private fun actualizarUI(state: PedidosUiState) {
        val transition = MaterialFadeThrough().apply {
            duration = 400
        }
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
        adapter.updateList(state.pedidos)
        if (state.selectedPedido != null) {
            mostrarDetallePedidoUI(state.selectedPedido)
        } else {
            binding.detallePedidoContainer.visibility = View.GONE
            binding.layoutListaPedidos.visibility = View.VISIBLE
            binding.detallePedidoContainer.removeAllViews()
        }
    }

    private fun mostrarDetallePedidoUI(pedido: Pedido) {
        binding.layoutListaPedidos.visibility = View.GONE
        binding.detallePedidoContainer.visibility = View.VISIBLE
        if (binding.detallePedidoContainer.childCount == 0) {
            val detailView = layoutInflater.inflate(R.layout.fragment_detalle_pedido, binding.detallePedidoContainer, false)
            detailView.findViewById<TextView>(R.id.txt_id_detalle_pedido).text = "Pedido #${pedido.numeroPedido.ifEmpty { "---" }}"
            detailView.findViewById<TextView>(R.id.txt_mesa_detalle_pedido).text = pedido.mesa
            detailView.findViewById<TextView>(R.id.txt_total_detalle_pedido).text = currencyFormat.format(pedido.total)
            detailView.findViewById<TextView>(R.id.txt_tiempo_detalle_pedido).text = pedido.tiempo.ifEmpty { "N/A" }
            val badge = detailView.findViewById<TextView>(R.id.txt_estado_detalle_pedido)
            badge.text = pedido.estado
            val btnAccion = detailView.findViewById<android.widget.Button>(R.id.btn_accion_detalle_pedido)
            when (pedido.estado) {
                "Pendiente" -> {
                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EAF6"))
                    badge.setTextColor(Color.parseColor("#3F51B5"))
                    btnAccion.text = "Iniciar Preparación"
                    btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
                    btnAccion.visibility = View.VISIBLE
                }
                "En Preparación" -> {
                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                    badge.setTextColor(Color.parseColor("#1976D2"))
                    btnAccion.text = "Marcar como Listo"
                    btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    btnAccion.visibility = View.VISIBLE
                }
                "Listo" -> {
                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                    badge.setTextColor(Color.parseColor("#2E7D32"))
                    btnAccion.text = "Marcar como Entregado"
                    btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#37474F"))
                    btnAccion.visibility = View.VISIBLE
                }
                "Finalizado" -> {
                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
                    badge.setTextColor(Color.GRAY)
                    btnAccion.visibility = View.GONE
                }
                else -> btnAccion.visibility = View.GONE
            }
            btnAccion.setOnClickListener {
                viewModel.cambiarEstadoPedido(pedido)
            }
            detailView.findViewById<View>(R.id.btn_volver_pedido).setOnClickListener {
                val exitTransition = MaterialFadeThrough().apply { duration = 300 }
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup, exitTransition)
                viewModel.seleccionarPedido(null)
            }
            val containerItems = detailView.findViewById<LinearLayout>(R.id.container_items_detalle_pedido)
            for (item in pedido.items) {
                val row = layoutInflater.inflate(R.layout.item_pedido_detalle_row, containerItems, false)
                row.findViewById<TextView>(R.id.tv_item_cantidad).text = "${item.cantidad}x"
                row.findViewById<TextView>(R.id.tv_item_nombre).text = item.nombre
                row.findViewById<TextView>(R.id.tv_item_precio).text = currencyFormat.format(item.precio * item.cantidad)
                row.findViewById<TextView>(R.id.tv_item_tiempo).text = if (item.tiempo.isNotEmpty()) item.tiempo else "---"
                row.setOnClickListener {
                    verRecetaDelPlato(item.nombre)
                }
                containerItems.addView(row)
            }
            binding.detallePedidoContainer.addView(detailView)
        }
    }

    private fun verRecetaDelPlato(nombrePlato: String) {
        val receta = mainViewModel.uiState.value.listaRecetas.find { it.nombre == nombrePlato }
        if (receta != null) {
            mainViewModel.setRecetaSeleccionada(receta)
            findNavController().navigate(R.id.navigation_recetas)
        } else {
            Toast.makeText(requireContext(), "Receta no encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
