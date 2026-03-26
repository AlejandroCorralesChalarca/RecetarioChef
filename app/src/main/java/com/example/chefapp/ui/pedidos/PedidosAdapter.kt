package com.example.chefapp.ui.pedidos

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.data.model.Pedido
import com.example.chefapp.databinding.ItemPedidoCardBinding
import java.text.NumberFormat
import java.util.Locale

class PedidosAdapter(
    private var pedidos: List<Pedido>,
    private val onActionClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class PedidoViewHolder(val binding: ItemPedidoCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        val binding = holder.binding

        binding.txtPedidoId.text = "Pedido #${pedido.id}"
        binding.txtMesaPedido.text = "📍 ${pedido.mesa}"
        binding.txtTiempoPedido.text = "🕒 ${pedido.tiempo}"
        binding.txtTotalPedido.text = currencyFormat.format(pedido.total)
        binding.txtEstadoBadge.text = pedido.estado

        // Color del Badge y Botón según estado
        when (pedido.estado) {
            "Pendiente" -> {
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EAF6"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#3F51B5"))
                binding.btnAccionPedido.text = "Iniciar Preparación"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            "En Preparación" -> {
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#1976D2"))
                binding.btnAccionPedido.text = "Marcar como Listo"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            "Listo" -> {
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#2E7D32"))
                binding.btnAccionPedido.text = "Marcar como Entregado"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#37474F"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            else -> {
                binding.btnAccionPedido.visibility = View.GONE
            }
        }

        // Renderizar Items
        binding.itemsContainer.removeAllViews()
        for (item in pedido.items) {
            val itemView = LayoutInflater.from(binding.root.context).inflate(R.layout.item_ingrediente_detalle, binding.itemsContainer, false)
            itemView.findViewById<TextView>(R.id.tv_ing_nombre).text = "${item.cantidad}  ${item.nombre}"
            itemView.findViewById<TextView>(R.id.tv_ing_cantidad).text = currencyFormat.format(item.precio)
            
            binding.itemsContainer.addView(itemView)
        }

        binding.btnAccionPedido.setOnClickListener { onActionClick(pedido) }
    }

    override fun getItemCount() = pedidos.size

    fun updateList(newList: List<Pedido>) {
        pedidos = newList
        notifyDataSetChanged()
    }
}