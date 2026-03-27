package com.example.chefapp.ui.recetas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.data.model.Receta
import com.example.chefapp.databinding.FragmentRecetasBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class RecetasFragment : Fragment() {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecetasViewModel by viewModels()
    private lateinit var adapter: RecetasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetasBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupObservers()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = RecetasAdapter(emptyList()) { receta ->
            mostrarDetalleReceta(receta)
        }
        binding.recyclerRecetas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecetas.adapter = adapter
    }

    private fun setupSearch() {
        binding.editBuscarReceta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupCategorias.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
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

    private fun actualizarUI(state: RecetasUiState) {
        adapter.updateList(state.recetas)
        binding.editBuscarReceta.isEnabled = !state.isLoading
    }

    private fun mostrarDetalleReceta(receta: Receta) {
        binding.layoutListaRecetas.visibility = View.GONE
        binding.detalleRecetaContainer.visibility = View.VISIBLE

        val detailView = layoutInflater.inflate(R.layout.fragment_detalle_receta, binding.detalleRecetaContainer, false)
        
        detailView.findViewById<TextView>(R.id.txt_categoria_detalle).text = receta.categoria
        detailView.findViewById<TextView>(R.id.txt_nombre_detalle).text = receta.nombre
        detailView.findViewById<TextView>(R.id.txt_desc_detalle).text = receta.descripcion
        detailView.findViewById<TextView>(R.id.txt_tiempo_info).text = receta.tiempo
        detailView.findViewById<TextView>(R.id.txt_precio_info).text = "$ ${receta.precio}"
        detailView.findViewById<TextView>(R.id.txt_ingredientes_info).text = receta.ingredientesCount.toString()

        val imgDetalle = detailView.findViewById<ImageView>(R.id.img_detalle)
        Glide.with(this)
            .load(receta.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_chip_selected)
            .into(imgDetalle)

        detailView.findViewById<TextView>(R.id.btn_volver).setOnClickListener {
            binding.detalleRecetaContainer.visibility = View.GONE
            binding.layoutListaRecetas.visibility = View.VISIBLE
        }

        val containerIngredientes = detailView.findViewById<LinearLayout>(R.id.container_ingredientes_lista)
        containerIngredientes.removeAllViews()
        for (ingrediente in receta.ingredientes) {
            val row = layoutInflater.inflate(R.layout.item_ingrediente_detalle, containerIngredientes, false)
            row.findViewById<TextView>(R.id.tv_ing_nombre).text = ingrediente.first
            row.findViewById<TextView>(R.id.tv_ing_cantidad).text = ingrediente.second
            containerIngredientes.addView(row)
        }

        val containerPasos = detailView.findViewById<LinearLayout>(R.id.container_pasos_lista)
        containerPasos.removeAllViews()
        for ((index, paso) in receta.pasos.withIndex()) {
            val row = layoutInflater.inflate(R.layout.item_paso_preparacion, containerPasos, false)
            row.findViewById<TextView>(R.id.tv_paso_numero).text = (index + 1).toString()
            row.findViewById<TextView>(R.id.tv_paso_descripcion).text = paso
            containerPasos.addView(row)
        }

        binding.detalleRecetaContainer.removeAllViews()
        binding.detalleRecetaContainer.addView(detailView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}