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
import com.example.chefapp.viewmodel.DialogType
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.viewmodel.InventarioViewModel
import com.example.chefapp.viewmodel.InventarioUiState
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.databinding.FragmentInventarioBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InventarioViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: InventarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupSearch()
        setupObservers()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(
            productos = emptyList(),
            onProductoClick = { producto ->
                Toast.makeText(context, "Stock: ${producto.cantidadActual} ${producto.unidad}", Toast.LENGTH_SHORT).show()
            },
            onModifyClick = { producto ->
                mainViewModel.showDialog(DialogType.NUEVO_PRODUCTO, producto)
            },
            onDeleteClick = { producto ->
                confirmarEliminacion(producto)
            }
        )
        binding.recyclerInventario.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInventario.adapter = adapter
    }

    private fun confirmarEliminacion(producto: Producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Deseas eliminar '${producto.nombre}' del inventario?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarProducto(producto)
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupSearch() {
        // Corrección del ID para que coincida con ViewBinding
        binding.editBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    actualizarUI(state)
                    actualizarChips(state.categorias)
                }
            }
        }
    }

    private fun actualizarChips(categorias: List<Categoria>) {
        val group = binding.chipGroupCategoriasInv
        val currentCategory = viewModel.uiState.value.currentCategory
        
        if (group.childCount == categorias.size + 1) return

        group.removeAllViews()
        
        val chipTodas = Chip(requireContext()).apply {
            text = "Todas"
            isCheckable = true
            isChecked = currentCategory == "Todas"
            setOnClickListener { viewModel.filtrarPorCategoria("Todas") }
        }
        group.addView(chipTodas)

        for (cat in categorias) {
            val chip = Chip(requireContext()).apply {
                text = cat.nombre
                isCheckable = true
                isChecked = currentCategory == cat.nombre
                setOnClickListener { viewModel.filtrarPorCategoria(cat.nombre) }
            }
            group.addView(chip)
        }
    }

    private fun actualizarUI(state: InventarioUiState) {
        adapter.updateList(state.productos)
        
        binding.tvResumenAlertas.text = state.resumenAlertas
        binding.cardAlertaStock.visibility = if (state.mostrarAlerta) View.VISIBLE else View.GONE

        binding.editBuscarProducto.isEnabled = !state.isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
