package com.example.chefapp.ui.recetas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chefapp.R
import com.example.chefapp.data.model.Receta
import java.text.NumberFormat
import java.util.Locale

class RecetasAdapter(
    private var recetas: List<Receta>,
    private val onRecetaClick: (Receta) -> Unit
) : RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txt_nombre_receta)
        val desc: TextView = view.findViewById(R.id.txt_desc_corta)
        val tiempo: TextView = view.findViewById(R.id.txt_tiempo_receta)
        val precio: TextView = view.findViewById(R.id.txt_precio_receta)
        val imagen: ImageView = view.findViewById(R.id.img_receta)
        val btnMenu: ImageButton = view.findViewById(R.id.btn_menu_receta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receta_card, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.nombre.text = receta.nombre
        holder.desc.text = receta.descripcion
        holder.tiempo.text = receta.tiempo
        
        // Formatear precio a Pesos Colombianos (COP)
        try {
            val precioLimpio = receta.precio.replace(".", "").toDouble()
            holder.precio.text = currencyFormat.format(precioLimpio)
        } catch (e: Exception) {
            holder.precio.text = "$ ${receta.precio}"
        }

        Glide.with(holder.itemView.context)
            .load(receta.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.bg_chip_selected)
            .error(R.drawable.bg_chip_selected)
            .into(holder.imagen)

        holder.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_receta_opciones, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_modify -> {
                        Toast.makeText(view.context, "Modificar ${receta.nombre}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_delete -> {
                        Toast.makeText(view.context, "Eliminar ${receta.nombre}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        holder.itemView.setOnClickListener {
            onRecetaClick(receta)
        }
    }

    override fun getItemCount() = recetas.size

    fun updateList(newList: List<Receta>) {
        recetas = newList
        notifyDataSetChanged()
    }
}