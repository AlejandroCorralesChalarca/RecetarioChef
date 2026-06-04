package com.example.chefapp.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.databinding.FragmentPerfilBinding
import com.example.chefapp.ui.LoginActivity
import com.example.chefapp.ui.UiState
import com.example.chefapp.viewmodel.PerfilViewModel
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
        setupListeners()
        setupObservers()
        cargarImagenPerfil()
        return binding.root
    }

    private fun setupListeners() {
        binding.btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Manejo de Estados de la Guía (Punto 7)
                    binding.progressBar.visibility = if (state.info is UiState.Loading) View.VISIBLE else View.GONE
                    
                    when (state.info) {
                        is UiState.Success -> {
                            binding.tvPerfilNombre.text = state.nombre
                            binding.tvPerfilRol.text = state.rol
                            binding.tvPerfilEmail.text = state.email
                            binding.tvPerfilTelefono.text = if (state.telefono.isEmpty()) "No registrado" else state.telefono
                            binding.tvPerfilRestaurante.text = if (state.restaurante.isEmpty()) "No registrado" else state.restaurante
                            binding.tvPerfilDireccion.text = if (state.direccion.isEmpty()) "No registrado" else state.direccion
                        }
                        is UiState.NoConnection -> {
                            Toast.makeText(context, "Sin conexión a internet", Toast.LENGTH_SHORT).show()
                        }
                        is UiState.SessionExpired -> {
                            navegarALogin()
                        }
                        is UiState.Error -> {
                            Toast.makeText(context, (state.info as UiState.Error).message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }

                    if (state.isLoggedOut) {
                        navegarALogin()
                    }
                }
            }
        }
    }

    private fun navegarALogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
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