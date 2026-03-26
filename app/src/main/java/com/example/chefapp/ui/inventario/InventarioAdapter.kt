package com.example.chefapp.ui.inventario

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.data.model.Producto
import com.example.chefapp.databinding.ItemProductoCardBinding

class InventarioAdapter(
    private var productos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(val binding: ItemProductoCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        val binding = holder.binding

        binding.tvProductoNombre.text = producto.nombre
        binding.tvProductoCategoria.text = producto.categoria
        binding.tvCantidadActual.text = "${producto.cantidadActual} ${producto.unidad}"
        binding.tvStockMinimo.text = "${producto.stockMinimo} ${producto.unidad}"
        binding.tvEstadoBadge.text = producto.estado


        val color = when (producto.estado) {
            "Óptimo" -> Color.parseColor("#2E7D32")
            "Medio" -> Color.parseColor("#EF6C00")
            else -> Color.parseColor("#C62828")
        }
        
        val bgColor = when (producto.estado) {
            "Óptimo" -> Color.parseColor("#E8F5E9")
            "Medio" -> Color.parseColor("#FFF3E0")
            else -> Color.parseColor("#FFEBEE")
        }

        binding.tvEstadoBadge.setTextColor(color)
        binding.tvEstadoBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
        binding.progressStock.progressTintList = ColorStateList.valueOf(color)
        

        val progreso = (producto.cantidadActual / (producto.stockMinimo * 2) * 100).toInt().coerceAtMost(100)
        binding.progressStock.progress = progreso

        binding.btnMenuProducto.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_receta_opciones, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_modify -> {
                        Toast.makeText(view.context, "Modificar ${producto.nombre}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_delete -> {
                        Toast.makeText(view.context, "Eliminar ${producto.nombre}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        holder.itemView.setOnClickListener { onProductoClick(producto) }
    }

    override fun getItemCount() = productos.size

    fun updateList(newList: List<Producto>) {
        productos = newList
        notifyDataSetChanged()
    }
}