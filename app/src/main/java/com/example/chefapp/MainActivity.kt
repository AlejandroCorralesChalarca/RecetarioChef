package com.example.chefapp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.chefapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var currentDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupListeners()
        setupObservers()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navView.setupWithNavController(navController)
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            viewModel.toggleFabMenu()
        }

        binding.layoutFabMenu.setOnClickListener { 
            viewModel.hideFabMenu() 
        }

        binding.btnNuevaReceta.setOnClickListener {
            viewModel.showDialog(DialogType.NUEVA_RECETA)
        }

        binding.btnNuevoProducto.setOnClickListener {
            viewModel.showDialog(DialogType.NUEVO_PRODUCTO)
        }

        binding.btnNuevoPedido.setOnClickListener {
            viewModel.showDialog(DialogType.NUEVO_PEDIDO)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    actualizarUI(state)
                }
            }
        }
    }

    private fun actualizarUI(state: MainUiState) {
        if (state.isFabMenuVisible) {
            mostrarMenuFab()
        } else {
            ocultarMenuFab()
        }

        val enabled = !state.isActionInProgress
        binding.btnNuevaReceta.isEnabled = enabled
        binding.btnNuevoProducto.isEnabled = enabled
        binding.btnNuevoPedido.isEnabled = enabled
        binding.fabAdd.isEnabled = enabled

        // Manejar Diálogos (Persistencia en rotación)
        if (state.activeDialog != null) {
            if (currentDialog == null || !currentDialog!!.isShowing) {
                when(state.activeDialog) {
                    DialogType.NUEVA_RECETA -> mostrarDialogoNuevaReceta()
                    DialogType.NUEVO_PRODUCTO -> mostrarDialogoNuevoProducto()
                    DialogType.NUEVO_PEDIDO -> mostrarDialogoNuevoPedido()
                }
            }
        } else {
            currentDialog?.dismiss()
            currentDialog = null
        }
    }

    private fun mostrarMenuFab() {
        binding.layoutFabMenu.visibility = View.VISIBLE
        binding.fabAdd.animate().rotation(45f).setDuration(200).start()
    }

    private fun ocultarMenuFab() {
        binding.layoutFabMenu.visibility = View.GONE
        binding.fabAdd.animate().rotation(0f).setDuration(200).start()
    }

    private fun configurarVentanaDialogo(dialog: AlertDialog) {
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener { 
            if (viewModel.uiState.value.activeDialog != null) {
                viewModel.dismissDialog()
            }
        }
    }

    private fun mostrarDialogoNuevaReceta() {
        val view = layoutInflater.inflate(R.layout.dialog_nueva_receta, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(view).create()
        currentDialog = dialog
        dialog.show()
        configurarVentanaDialogo(dialog)
        
        view.findViewById<TextView>(R.id.btn_close).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            viewModel.setActionInProgress(true)
            binding.root.postDelayed({
                viewModel.setActionInProgress(false)
                Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }, 1000)
        }
    }

    private fun mostrarDialogoNuevoProducto() {
        val view = layoutInflater.inflate(R.layout.dialog_nuevo_producto, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(view).create()
        currentDialog = dialog
        dialog.show()
        configurarVentanaDialogo(dialog)
        
        view.findViewById<TextView>(R.id.btn_close_prod).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_cancel_prod).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_save_prod).setOnClickListener {
            viewModel.setActionInProgress(true)
            binding.root.postDelayed({
                viewModel.setActionInProgress(false)
                Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }, 1000)
        }
    }

    private fun mostrarDialogoNuevoPedido() {
        val view = layoutInflater.inflate(R.layout.dialog_nuevo_pedido, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(view).create()
        currentDialog = dialog
        dialog.show()
        configurarVentanaDialogo(dialog)
        
        view.findViewById<TextView>(R.id.btn_close_ped).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_cancel_ped).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_save_ped).setOnClickListener {
            viewModel.setActionInProgress(true)
            binding.root.postDelayed({
                viewModel.setActionInProgress(false)
                Toast.makeText(this, "Pedido creado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }, 1000)
        }
    }
}
