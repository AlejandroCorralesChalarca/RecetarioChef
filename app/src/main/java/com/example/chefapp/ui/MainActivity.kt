package com.example.chefapp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.databinding.ActivityMainBinding
import com.example.chefapp.databinding.DialogNuevaRecetaBinding
import com.example.chefapp.databinding.DialogNuevoProductoBinding
import com.example.chefapp.databinding.DialogNuevoPedidoBinding
import com.example.chefapp.databinding.DialogGestionCategoriasBinding
import com.example.chefapp.domain.model.ItemPedido
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.domain.model.Producto
import com.example.chefapp.domain.model.Receta
import com.example.chefapp.viewmodel.MainViewModel
import com.example.chefapp.viewmodel.DialogType
import com.example.chefapp.ui.categorias.CategoriasAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var currentDialog: AlertDialog? = null
    private var selectedImageUri: Uri? = null
    private var recetaDialogBinding: DialogNuevaRecetaBinding? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            recetaDialogBinding?.ivPlaceholder?.let { iv ->
                Glide.with(this).load(it).into(iv)
            }
        }
    }

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
        binding.fabAdd.setOnClickListener { viewModel.toggleFabMenu() }
        binding.layoutFabMenu.setOnClickListener { viewModel.hideFabMenu() }
        binding.btnNuevaReceta.setOnClickListener { viewModel.showDialog(DialogType.NUEVA_RECETA) }
        binding.btnNuevoProducto.setOnClickListener { viewModel.showDialog(DialogType.NUEVO_PRODUCTO) }
        binding.btnNuevoPedido.setOnClickListener { viewModel.showDialog(DialogType.NUEVO_PEDIDO) }
        binding.btnGestionCategorias.setOnClickListener { viewModel.showDialog(DialogType.GESTION_CATEGORIAS) }
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

    private fun actualizarUI(state: com.example.chefapp.viewmodel.MainUiState) {
        binding.layoutFabMenu.visibility = if (state.isFabMenuVisible) View.VISIBLE else View.GONE
        binding.fabAdd.animate().rotation(if (state.isFabMenuVisible) 45f else 0f).setDuration(200).start()
        if (state.activeDialog != null && (currentDialog == null || !currentDialog!!.isShowing)) {
            when(state.activeDialog) {
                DialogType.NUEVO_PEDIDO -> mostrarDialogoNuevoPedido(state.pedidoAEditar)
                DialogType.NUEVA_RECETA -> mostrarDialogoNuevaReceta(state.recetaAEditar)
                DialogType.NUEVO_PRODUCTO -> mostrarDialogoNuevoProducto(state.productoAEditar)
                DialogType.GESTION_CATEGORIAS -> mostrarDialogoGestionCategorias()
            }
        } else if (state.activeDialog == null) {
            currentDialog?.dismiss()
            currentDialog = null
        }
    }

    private fun mostrarDialogoNuevoPedido(pedido: Pedido?) {
        val dialogBinding = DialogNuevoPedidoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(dialogBinding.root).create()
        currentDialog = dialog
        val itemsDelPedido = mutableListOf<ItemPedido>()
        pedido?.let { 
            dialogBinding.etMesaPed.setText(it.mesa.replace("Mesa ", ""))
            itemsDelPedido.addAll(it.items)
        }
        val recetas = viewModel.uiState.value.listaRecetas
        dialogBinding.spinnerPlatosPed.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recetas.map { it.nombre })
        actualizarListaItemsPedido(dialogBinding, itemsDelPedido)
        dialogBinding.btnAddPlatoPed.setOnClickListener {
            val nombre = dialogBinding.spinnerPlatosPed.selectedItem?.toString() ?: return@setOnClickListener
            val cant = dialogBinding.etCantidadPlatoPed.text.toString().toIntOrNull() ?: 1
            val receta = recetas.find { it.nombre == nombre } ?: return@setOnClickListener
            val precio = receta.precio.replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
            itemsDelPedido.add(ItemPedido(cant, nombre, precio, receta.tiempo))
            actualizarListaItemsPedido(dialogBinding, itemsDelPedido)
            dialogBinding.etCantidadPlatoPed.setText("")
        }
        dialogBinding.btnSavePed.setOnClickListener {
            val mesa = dialogBinding.etMesaPed.text.toString()
            if (mesa.isNotEmpty() && itemsDelPedido.isNotEmpty()) {
                val total = itemsDelPedido.sumOf { it.cantidad * it.precio }
                viewModel.guardarPedido(Pedido(
                    docId = pedido?.docId ?: "",
                    mesa = "Mesa $mesa",
                    items = ArrayList(itemsDelPedido),
                    total = total,
                    estado = pedido?.estado ?: "Pendiente"
                ))
            } else {
                Toast.makeText(this, "Mesa y platos requeridos", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBinding.btnCancelPed.setOnClickListener { viewModel.dismissDialog() }
        dialogBinding.btnClosePed.setOnClickListener { viewModel.dismissDialog() }
        configurarVentanaDialogo(dialog)
        dialog.show()
    }

    private fun actualizarListaItemsPedido(binding: DialogNuevoPedidoBinding, items: MutableList<ItemPedido>) {
        binding.containerItemsPed.removeAllViews()
        items.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dpToPx(12), dpToPx(8), dpToPx(8), dpToPx(8))
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_edittext_rounded)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, dpToPx(8))
                layoutParams = params
            }
            val tvInfo = TextView(this).apply {
                text = "${item.cantidad}x ${item.nombre}"
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
            }
            val btnEdit = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                background = null
                imageTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.azul_accion))
                setOnClickListener {
                    binding.etCantidadPlatoPed.setText(item.cantidad.toString())
                    val pos = (binding.spinnerPlatosPed.adapter as ArrayAdapter<String>).getPosition(item.nombre)
                    binding.spinnerPlatosPed.setSelection(pos)
                    items.removeAt(index)
                    actualizarListaItemsPedido(binding, items)
                }
            }
            val btnDelete = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                imageTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.primary_orange))
                setOnClickListener {
                    items.removeAt(index)
                    actualizarListaItemsPedido(binding, items)
                }
            }
            row.addView(tvInfo)
            row.addView(btnEdit)
            row.addView(btnDelete)
            binding.containerItemsPed.addView(row)
        }
        val total = items.sumOf { it.cantidad * it.precio }
        binding.txtTotalPed.text = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(total)
    }

    private fun mostrarDialogoNuevaReceta(receta: Receta?) {
        val dialogBinding = DialogNuevaRecetaBinding.inflate(layoutInflater)
        recetaDialogBinding = dialogBinding
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(dialogBinding.root).create()
        currentDialog = dialog
        selectedImageUri = null
        val ingredientes = mutableListOf<MutableMap<String, String>>()
        val pasos = mutableListOf<String>()
        if (receta != null) {
            dialogBinding.tvDialogTitle.text = "Editar Receta"
            dialogBinding.etNombreReceta.setText(receta.nombre)
            dialogBinding.etDescripcionReceta.setText(receta.descripcion)
            dialogBinding.etPrecioReceta.setText(receta.precio.replace("[^\\d]".toRegex(), ""))
            dialogBinding.etTiempoReceta.setText(receta.tiempo.replace("[^\\d]".toRegex(), ""))
            receta.ingredientes.forEach { ing -> ingredientes.add(ing.toMutableMap()) }
            pasos.addAll(receta.pasos)
            Glide.with(this).load(receta.imageUrl).placeholder(R.drawable.ic_chef_hat).into(dialogBinding.ivPlaceholder)
        } else {
            ingredientes.add(mutableMapOf("" to ""))
            pasos.add("")
        }
        val listaCats = viewModel.uiState.value.listaCategorias.filter { it.tipo == "RECETA" }.map { it.nombre }
        val adapterCats = ArrayAdapter(this, android.R.layout.simple_spinner_item, if (listaCats.isEmpty()) listOf("Sin categoría") else listaCats)
        dialogBinding.spinnerCategoriaReceta.adapter = adapterCats
        receta?.let {
            val pos = adapterCats.getPosition(it.categoria)
            if (pos >= 0) dialogBinding.spinnerCategoriaReceta.setSelection(pos)
        }
        actualizarListaIngredientesUI(ingredientes)
        actualizarListaPasosUI(pasos)
        dialogBinding.btnSelectPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        dialogBinding.btnAddIngrediente.setOnClickListener {
            ingredientes.add(mutableMapOf("" to ""))
            actualizarListaIngredientesUI(ingredientes)
        }
        dialogBinding.btnAddPaso.setOnClickListener {
            pasos.add("")
            actualizarListaPasosUI(pasos)
        }
        dialogBinding.btnSave.setOnClickListener {
            val nombre = dialogBinding.etNombreReceta.text.toString()
            val desc = dialogBinding.etDescripcionReceta.text.toString()
            val precio = dialogBinding.etPrecioReceta.text.toString()
            val tiempo = dialogBinding.etTiempoReceta.text.toString()
            val cat = dialogBinding.spinnerCategoriaReceta.selectedItem?.toString() ?: ""
            if (nombre.isNotEmpty()) {
                viewModel.guardarReceta(
                    docId = receta?.docId ?: "",
                    nombre = nombre,
                    descripcion = desc,
                    tiempo = "$tiempo min",
                    precio = "$ $precio",
                    categoria = if (cat == "Sin categoría") "" else cat,
                    ingredientes = ingredientes.filter { it.keys.firstOrNull()?.isNotBlank() == true },
                    pasos = pasos.filter { it.isNotBlank() },
                    imageUri = selectedImageUri
                )
            } else {
                Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBinding.btnCancel.setOnClickListener { viewModel.dismissDialog() }
        dialogBinding.btnClose.setOnClickListener { viewModel.dismissDialog() }
        configurarVentanaDialogo(dialog)
        dialog.show()
    }

    private fun actualizarListaIngredientesUI(ingredientes: MutableList<MutableMap<String, String>>) {
        val container = recetaDialogBinding?.containerIngredientes ?: return
        container.removeAllViews()
        val nombresProductos = viewModel.uiState.value.listaProductos.map { it.nombre }
        val adapterSugerencias = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresProductos)
        ingredientes.forEachIndexed { index, map ->
            val editRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 0, 0, dpToPx(8))
            }
            val name = AutoCompleteTextView(this).apply {
                hint = "Ingrediente"
                setText(map.keys.firstOrNull() ?: "")
                setAdapter(adapterSugerencias)
                threshold = 1
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(48), 1f)
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_edittext_rounded)
                setPadding(dpToPx(12), 0, dpToPx(12), 0)
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val currentVal = map.values.firstOrNull() ?: ""
                        map.clear()
                        map[s.toString()] = currentVal
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            val quant = EditText(this).apply {
                hint = "Cant"
                setText(map.values.firstOrNull() ?: "")
                layoutParams = LinearLayout.LayoutParams(dpToPx(80), dpToPx(48))
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_edittext_rounded)
                setPadding(dpToPx(12), 0, dpToPx(12), 0)
                val params = layoutParams as LinearLayout.LayoutParams
                params.setMargins(dpToPx(8), 0, dpToPx(8), 0)
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val currentKey = map.keys.firstOrNull() ?: ""
                        map.clear()
                        map[currentKey] = s.toString()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            val btnDel = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                imageTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnClickListener {
                    ingredientes.removeAt(index)
                    actualizarListaIngredientesUI(ingredientes)
                }
            }
            editRow.addView(name)
            editRow.addView(quant)
            editRow.addView(btnDel)
            container.addView(editRow)
        }
    }

    private fun actualizarListaPasosUI(pasos: MutableList<String>) {
        val container = recetaDialogBinding?.containerPasos ?: return
        container.removeAllViews()
        pasos.forEachIndexed { index, paso ->
            val editRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 0, 0, dpToPx(8))
            }
            val etPaso = EditText(this).apply {
                hint = "Paso ${index + 1}"
                setText(paso)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                minHeight = dpToPx(48)
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_edittext_rounded)
                setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) { if (index < pasos.size) pasos[index] = s.toString() }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            val btnDel = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                imageTintList = android.content.res.ColorStateList.valueOf(Color.RED)
                setOnClickListener {
                    if (index < pasos.size) {
                        pasos.removeAt(index)
                        actualizarListaPasosUI(pasos)
                    }
                }
            }
            editRow.addView(etPaso)
            editRow.addView(btnDel)
            container.addView(editRow)
        }
    }

    private fun mostrarDialogoNuevoProducto(producto: Producto?) {
        val dialogBinding = DialogNuevoProductoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(dialogBinding.root).create()
        currentDialog = dialog
        val listaCats = viewModel.uiState.value.listaCategorias.filter { it.tipo == "INVENTARIO" }.map { it.nombre }
        val adapterCats = ArrayAdapter(this, android.R.layout.simple_spinner_item, if (listaCats.isEmpty()) listOf("General") else listaCats)
        dialogBinding.spinnerCategoriaProd.adapter = adapterCats
        val unidades = listOf("kg", "g", "L", "ml", "und", "paquete", "bulto")
        dialogBinding.spinnerUnidadProd.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unidades)
        producto?.let {
            dialogBinding.tvDialogTitleProd.text = "Editar Producto"
            dialogBinding.etNombreProd.setText(it.nombre)
            dialogBinding.etCantidadProd.setText(it.cantidadActual.toString())
            dialogBinding.etMinimoProd.setText(it.stockMinimo.toString())
            val posCat = adapterCats.getPosition(it.categoria)
            if (posCat >= 0) dialogBinding.spinnerCategoriaProd.setSelection(posCat)
            val posUni = (dialogBinding.spinnerUnidadProd.adapter as ArrayAdapter<String>).getPosition(it.unidad)
            if (posUni >= 0) dialogBinding.spinnerUnidadProd.setSelection(posUni)
        }
        dialogBinding.btnSaveProd.setOnClickListener {
            val nombre = dialogBinding.etNombreProd.text.toString()
            val cat = dialogBinding.spinnerCategoriaProd.selectedItem?.toString() ?: ""
            val cant = dialogBinding.etCantidadProd.text.toString().toFloatOrNull() ?: 0f
            val min = dialogBinding.etMinimoProd.text.toString().toFloatOrNull() ?: 0f
            val unidad = dialogBinding.spinnerUnidadProd.selectedItem?.toString() ?: ""
            if (nombre.isNotEmpty()) {
                viewModel.guardarProducto(producto?.docId ?: "", nombre, cat, cant, min, unidad)
            } else {
                Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show()
            }
        }
        dialogBinding.btnCancelProd.setOnClickListener { viewModel.dismissDialog() }
        dialogBinding.btnCloseProd.setOnClickListener { viewModel.dismissDialog() }
        configurarVentanaDialogo(dialog)
        dialog.show()
    }

    private fun mostrarDialogoGestionCategorias() {
        val dialogBinding = DialogGestionCategoriasBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(dialogBinding.root).create()
        currentDialog = dialog
        val tipos = listOf("RECETA", "INVENTARIO")
        dialogBinding.spinnerTipoCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        val adapter = CategoriasAdapter { cat -> viewModel.eliminarCategoria(cat.docId) }
        dialogBinding.recyclerCategorias.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerCategorias.adapter = adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.listaCategorias)
                }
            }
        }
        dialogBinding.btnAddCat.setOnClickListener {
            val nombre = dialogBinding.etNombreCat.text.toString()
            val tipo = dialogBinding.spinnerTipoCat.selectedItem?.toString() ?: ""
            if (nombre.isNotEmpty()) {
                viewModel.guardarCategoria(nombre, tipo)
                dialogBinding.etNombreCat.setText("")
            }
        }
        dialogBinding.btnCloseCat.setOnClickListener { viewModel.dismissDialog() }
        configurarVentanaDialogo(dialog)
        dialog.show()
    }

    private fun configurarVentanaDialogo(dialog: AlertDialog) {
        dialog.setOnShowListener {
            dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.95).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
