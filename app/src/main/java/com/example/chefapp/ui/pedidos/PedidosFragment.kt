package com.example.chefapp.ui.pedidos

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chefapp.R
import com.example.chefapp.databinding.FragmentPedidosBinding

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PedidosViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PedidosViewModel::class.java]

        viewModel.pedidos.observe(viewLifecycleOwner) { pedidos ->
            renderPedidos(pedidos)
        }

        return binding.root
    }

    private fun renderPedidos(pedidos: List<Pedido>) {
        binding.contenedorPedidos.removeAllViews()

        for (pedido in pedidos) {
            val card = CardView(requireContext())
            card.radius = 32f
            card.cardElevation = 8f
            card.setCardBackgroundColor(Color.WHITE)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.bottomMargin = 32
            card.layoutParams = cardParams

            val contenido = LinearLayout(requireContext())
            contenido.orientation = LinearLayout.VERTICAL
            contenido.setPadding(40, 40, 40, 40)

            val filaTop = LinearLayout(requireContext())
            filaTop.orientation = LinearLayout.HORIZONTAL

            val txtMesa = TextView(requireContext())
            txtMesa.text = pedido.mesa
            txtMesa.textSize = 16f
            txtMesa.setTextColor(Color.parseColor("#5A5A5A"))
            txtMesa.setTypeface(null, Typeface.BOLD)
            val mesaParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            txtMesa.layoutParams = mesaParams

            val txtEstado = TextView(requireContext())
            txtEstado.text = pedido.estado
            txtEstado.textSize = 12f
            txtEstado.setTextColor(Color.WHITE)
            txtEstado.setPadding(20, 8, 20, 8)
            if (pedido.estado == "Listo") {
                txtEstado.background = resources.getDrawable(R.drawable.bg_estado_listo, null)
            } else {
                txtEstado.background = resources.getDrawable(R.drawable.bg_estado_cocina, null)
            }

            filaTop.addView(txtMesa)
            filaTop.addView(txtEstado)
            contenido.addView(filaTop)

            for (item in pedido.items) {
                val txtItem = TextView(requireContext())
                txtItem.text = "• $item"
                txtItem.textSize = 14f
                txtItem.setTextColor(Color.parseColor("#5A5A5A"))
                val itemParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                itemParams.topMargin = 8
                txtItem.layoutParams = itemParams
                contenido.addView(txtItem)
            }

            val txtTiempo = TextView(requireContext())
            txtTiempo.text = "⏱ ${pedido.tiempo}"
            txtTiempo.textSize = 12f
            txtTiempo.setTextColor(Color.parseColor("#F08030"))
            val tiempoParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            tiempoParams.topMargin = 8
            txtTiempo.layoutParams = tiempoParams
            contenido.addView(txtTiempo)

            if (pedido.estado != "Listo") {
                val btnListo = Button(requireContext())
                btnListo.text = "✔ Marcar como listo"
                btnListo.textSize = 13f
                btnListo.setTextColor(Color.WHITE)
                btnListo.background = resources.getDrawable(R.drawable.bg_chip_selected, null)
                val btnParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                btnParams.topMargin = 16
                btnListo.layoutParams = btnParams
                val pedidoId = pedido.id
                btnListo.setOnClickListener {
                    viewModel.marcarComoListo(pedidoId)
                }
                contenido.addView(btnListo)
            }

            card.addView(contenido)
            binding.contenedorPedidos.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}