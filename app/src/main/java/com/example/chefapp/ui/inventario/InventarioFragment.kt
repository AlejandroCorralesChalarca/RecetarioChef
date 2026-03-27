package com.example.chefapp.ui.inventario

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chefapp.databinding.FragmentInventarioBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventarioViewModel by viewModels()
    private lateinit var adapter: InventarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupObservers()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = InventarioAdapter(emptyList()) { producto ->
            Toast.makeText(context, "Detalle de: ${producto.nombre}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerInventario.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInventario.adapter = adapter
    }

    private fun setupSearch() {
        binding.editBuscarProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupCategoriasInv.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                viewModel.filtrarPorCategoria(chip.text.toString())
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