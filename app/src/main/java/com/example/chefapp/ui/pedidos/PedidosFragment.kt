package com.example.chefapp.ui.pedidos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chefapp.databinding.FragmentPedidosBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PedidosViewModel by viewModels()
    private lateinit var adapter: PedidosAdapter

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
        adapter = PedidosAdapter(emptyList()) { pedido ->
            viewModel.cambiarEstadoPedido(pedido)
        }
        binding.recyclerPedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPedidos.adapter = adapter
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
        adapter.updateList(state.pedidos)
        binding.editBuscarPedido.isEnabled = !state.isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}