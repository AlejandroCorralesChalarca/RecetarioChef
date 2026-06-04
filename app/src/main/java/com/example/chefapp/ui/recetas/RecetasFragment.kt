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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.databinding.FragmentRecetasBinding
import com.example.chefapp.viewmodel.DialogType
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.viewmodel.RecetasUiState
import com.example.chefapp.viewmodel.RecetasViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RecetasFragment : Fragment() {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecetasViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: RecetasAdapter
    
    private var isExternalNavigation = false
    private var lastCategoriesNames: List<String>? = null

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetasBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearch()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = RecetasAdapter(
            recetas = emptyList(),
            onRecetaClick = { receta ->
                if (!viewModel.uiState.value.isLoading) {
                    isExternalNavigation = false
                    viewModel.seleccionarReceta(receta)
                }
            },
            onModifyClick = { receta ->
                mainViewModel.showDialog(DialogType.NUEVA_RECETA, receta)
            },
            onDeleteClick = { receta ->
                confirmarEliminacionReceta(receta)
            }
        )
        binding.recyclerRecetas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecetas.adapter = adapter
    }

    private fun handleBackNavigation() {
        val transition = MaterialFadeThrough().apply { duration = 300 }
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
        
        viewModel.seleccionarReceta(null)
        
        if (isExternalNavigation) {
            isExternalNavigation = false
            if (!findNavController().popBackStack()) {
                findNavController().navigate(R.id.navigation_dashboard)
            }
        }
    }

    private fun confirmarEliminacionReceta(receta: Receta) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Receta")
            .setMessage("¿Estás seguro de que deseas eliminar la receta '${receta.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarReceta(receta)
                Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

    private fun setupObservers() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiState.collect { state ->
                // RESET de vistas: Ocultamos todo antes de mostrar el estado actual
                binding.progressBar.visibility = View.GONE
                binding.recyclerRecetas.visibility = View.GONE
                binding.layoutVacio.visibility = View.GONE // Asegúrate de tener este ID en tu XML

                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.recyclerRecetas.visibility = View.VISIBLE
                        adapter.updateList(state.data)
                    }
                    is UiState.Empty -> {
                        binding.layoutVacio.visibility = View.VISIBLE
                        // Puedes cambiar el texto del layout vacío dinámicamente
                        // binding.txtVacio.text = "No se encontraron recetas"
                    }
                    is UiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is UiState.NoConnection -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Sin Conexión")
                            .setMessage("No puedes ver las recetas sin internet. Revisa tu conexión.")
                            .setPositiveButton("Reintentar") { _, _ -> viewModel.cargarDatos() }
                            .show()
                    }
                    is UiState.SessionExpired -> {
                        Toast.makeText(requireContext(), "Tu sesión ha expirado", Toast.LENGTH_SHORT).show()
                        navegarALogin()
                    }
                }   
            }
        }
    }
    
        // Observador para categorías (se mantiene independiente)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categorias.collect { actualizarChips(it) }
        }
    }

    private fun navegarALogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun actualizarChips(categorias: List<Categoria>) {
        val currentNames = categorias.map { it.nombre }.sorted()
        if (lastCategoriesNames == currentNames) {
            return
        }
        lastCategoriesNames = currentNames

        val group = binding.chipGroupCategorias
        val currentCategory = viewModel.uiState.value.currentCategory
        
        group.removeAllViews()
        
        val chipTodas = Chip(requireContext()).apply {
            text = "Todas"
            isCheckable = true
            isChecked = currentCategory == "Todas"
            setOnClickListener { 
                viewModel.filtrarPorCategoria("Todas")
            }
        }
        group.addView(chipTodas)

        for (cat in categorias) {
            val chip = Chip(requireContext()).apply {
                text = cat.nombre
                isCheckable = true
                isChecked = currentCategory == cat.nombre
                setOnClickListener { 
                    viewModel.filtrarPorCategoria(cat.nombre)
                }
            }
            group.addView(chip)
        }
    }

    private fun actualizarUI(state: RecetasUiState) {
        val transition = MaterialFadeThrough().apply {
            duration = 400
        }
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)

        adapter.updateList(state.recetas)
        
        state.error?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        if (state.selectedReceta != null) {
            mostrarDetalleRecetaUI(state.selectedReceta)
        } else {
            binding.detalleRecetaContainer.visibility = View.GONE
            binding.layoutListaRecetas.visibility = View.VISIBLE
            binding.detalleRecetaContainer.removeAllViews()
        }
    }

    private fun mostrarDetalleRecetaUI(receta: Receta) {
        binding.layoutListaRecetas.visibility = View.GONE
        binding.detalleRecetaContainer.visibility = View.VISIBLE
        binding.detalleRecetaContainer.removeAllViews()
        
        val detailView = layoutInflater.inflate(R.layout.fragment_detalle_receta, binding.detalleRecetaContainer, false)
        
        detailView.findViewById<TextView>(R.id.txt_categoria_detalle).text = receta.categoria
        detailView.findViewById<TextView>(R.id.txt_nombre_detalle).text = receta.nombre
        detailView.findViewById<TextView>(R.id.txt_desc_detalle).text = receta.descripcion
        detailView.findViewById<TextView>(R.id.txt_tiempo_info).text = receta.tiempo
        
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        try {
            val precioVal = receta.precio.replace("[^\\d]".toRegex(), "").toDouble()
            detailView.findViewById<TextView>(R.id.txt_precio_info).text = currencyFormat.format(precioVal)
        } catch (e: Exception) {
            detailView.findViewById<TextView>(R.id.txt_precio_info).text = "$ ${receta.precio}"
        }
        
        val imgDetalle = detailView.findViewById<ImageView>(R.id.img_detalle)
        Glide.with(this).load(receta.imageUrl).centerCrop().placeholder(R.drawable.ic_chef_hat).into(imgDetalle)

        val btnVolver = detailView.findViewById<TextView>(R.id.btn_volver)
        if (isExternalNavigation) {
            btnVolver.text = "← Volver"
        }

        btnVolver.setOnClickListener {
            handleBackNavigation()
        }

        val containerIngredientes = detailView.findViewById<LinearLayout>(R.id.container_ingredientes_lista)
        for (ingrediente in receta.ingredientes) {
            val row = layoutInflater.inflate(R.layout.item_ingrediente_detalle, containerIngredientes, false)
            row.findViewById<TextView>(R.id.tv_ing_nombre).text = ingrediente.keys.firstOrNull() ?: ""
            row.findViewById<TextView>(R.id.tv_ing_cantidad).text = ingrediente.values.firstOrNull() ?: ""
            containerIngredientes.addView(row)
        }

        val containerPasos = detailView.findViewById<LinearLayout>(R.id.container_pasos_lista)
        for ((index, paso) in receta.pasos.withIndex()) {
            val row = layoutInflater.inflate(R.layout.item_paso_preparacion, containerPasos, false)
            row.findViewById<TextView>(R.id.tv_paso_numero).text = (index + 1).toString()
            row.findViewById<TextView>(R.id.tv_paso_descripcion).text = paso
            containerPasos.addView(row)
        }

        binding.detalleRecetaContainer.addView(detailView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
