package com.example.chefapp.ui.pedidos

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.R
import com.example.chefapp.domain.model.Pedido
import com.example.chefapp.databinding.ItemPedidoCardBinding
import java.text.NumberFormat
import java.util.Locale

class PedidosAdapter(
    private var pedidos: List<Pedido>,
    private val onActionClick: (Pedido) -> Unit,
    private val onCardClick: (Pedido) -> Unit,
    private val onModifyClick: (Pedido) -> Unit,
    private val onDeleteClick: (Pedido) -> Unit
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

        binding.txtPedidoId.text = "Pedido #${pedido.numeroPedido.ifEmpty { "---" }}"
        binding.txtMesaPedido.text = pedido.mesa
        binding.txtTiempoPedido.text = pedido.tiempo
        binding.txtTotalPedido.text = currencyFormat.format(pedido.total)
        binding.txtEstadoBadge.text = pedido.estado

        when (pedido.estado) {
            "Pendiente" -> {
                holder.binding.root.setCardBackgroundColor(Color.WHITE)
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EAF6"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#3F51B5"))
                binding.btnAccionPedido.text = "Iniciar Preparación"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3F51B5"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            "En Preparación" -> {
                holder.binding.root.setCardBackgroundColor(Color.WHITE)
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#1976D2"))
                binding.btnAccionPedido.text = "Marcar como Listo"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            "Listo" -> {
                holder.binding.root.setCardBackgroundColor(Color.WHITE)
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                binding.txtEstadoBadge.setTextColor(Color.parseColor("#2E7D32"))
                binding.btnAccionPedido.text = "Marcar como Entregado"
                binding.btnAccionPedido.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#37474F"))
                binding.btnAccionPedido.visibility = View.VISIBLE
            }
            "Finalizado" -> {
                holder.binding.root.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                binding.txtEstadoBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
                binding.txtEstadoBadge.setTextColor(Color.GRAY)
                binding.btnAccionPedido.visibility = View.GONE
            }
            else -> {
                holder.binding.root.setCardBackgroundColor(Color.WHITE)
                binding.btnAccionPedido.visibility = View.GONE
            }
        }

        binding.itemsContainerPreview.removeAllViews()
        for (item in pedido.items.take(2)) {
            val itemView = LayoutInflater.from(binding.root.context).inflate(R.layout.item_ingrediente_detalle, binding.itemsContainerPreview, false)
            itemView.findViewById<TextView>(R.id.tv_ing_nombre).text = "${item.cantidad}  ${item.nombre}"
            itemView.findViewById<TextView>(R.id.tv_ing_cantidad).text = currencyFormat.format(item.precio)
            binding.itemsContainerPreview.addView(itemView)
        }

        binding.btnMenuPedido.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_receta_opciones, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_modify -> {
                        onModifyClick(pedido)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(pedido)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        binding.btnAccionPedido.setOnClickListener { onActionClick(pedido) }
        holder.itemView.setOnClickListener { onCardClick(pedido) }
    }

    override fun getItemCount() = pedidos.size

    fun updateList(newList: List<Pedido>) {
        pedidos = newList
        notifyDataSetChanged()
    }
}
