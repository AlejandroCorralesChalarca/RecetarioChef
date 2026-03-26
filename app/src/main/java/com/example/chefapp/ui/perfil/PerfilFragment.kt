package com.example.chefapp.ui.perfil

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
import com.example.chefapp.databinding.FragmentPerfilBinding
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PerfilViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        
        setupObservers()
        cargarImagenPerfil()
        
        return binding.root
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

    private fun actualizarUI(state: PerfilUiState) {

        binding.tvPerfilNombre.text = state.nombre
        binding.tvPerfilRol.text = state.rol
        binding.tvPerfilEmail.text = state.email

    }

    private fun cargarImagenPerfil() {
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1583394293214-28ded15ee548?q=80&w=400&auto=format&fit=crop")
            .centerCrop()
            .placeholder(R.drawable.bg_chip_selected)
            .into(binding.imgPerfilFoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}