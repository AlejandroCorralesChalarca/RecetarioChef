package com.example.chefapp.ui.inventario

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chefapp.databinding.FragmentInventarioBinding
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.ui.UiState
import com.example.chefapp.viewmodel.DialogType
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.viewmodel.InventarioViewModel
import com.example.chefapp.viewmodel.InventarioUiStateFull
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventarioViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: InventarioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearch()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(
            productos = emptyList(),
            onProductoClick = { prod -> Toast.makeText(context, "${prod.nombre}: ${prod.cantidadActual}", Toast.LENGTH_SHORT).show() },
            onModifyClick = { prod -> mainViewModel.showDialog(DialogType.NUEVO_PRODUCTO, prod) },
            onDeleteClick = { prod -> confirmarEliminacion(prod) }
        )
        binding.recyclerInventario.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInventario.adapter = adapter
    }

    private fun confirmarEliminacion(producto: Producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Deseas eliminar '${producto.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarProducto(producto) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun setupSearch() {
        binding.editBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { viewModel.buscar(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Punto 7: Reacción visual
                    binding.progressBar.visibility = if (state.mainState is UiState.Loading) View.VISIBLE else View.GONE
                    
                    when (state.mainState) {
                        is UiState.Success -> {
                            adapter.updateList(state.productos)
                            binding.recyclerInventario.visibility = View.VISIBLE
                        }
                        is UiState.Empty -> {
                            binding.recyclerInventario.visibility = View.GONE
                            Toast.makeText(context, "No hay productos", Toast.LENGTH_SHORT).show()
                        }
                        is UiState.NoConnection -> {
                            Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                    binding.tvResumenAlertas.text = state.resumenAlertas
                    binding.cardAlertaStock.visibility = if (state.mostrarAlerta) View.VISIBLE else View.GONE
                    actualizarChips(state.categorias)
                }
            }
        }
    }

    private fun actualizarChips(categorias: List<Categoria>) {
        val group = binding.chipGroupCategoriasInv
        if (group.childCount == categorias.size + 1) return
        group.removeAllViews()
        val chipTodas = Chip(requireContext()).apply {
            text = "Todas"; isCheckable = true; isChecked = viewModel.uiState.value.currentCategory == "Todas"
            setOnClickListener { viewModel.filtrarPorCategoria("Todas") }
        }
        group.addView(chipTodas)
        categorias.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat.nombre; isCheckable = true; isChecked = viewModel.uiState.value.currentCategory == cat.nombre
                setOnClickListener { viewModel.filtrarPorCategoria(cat.nombre) }
            }
            group.addView(chip)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}