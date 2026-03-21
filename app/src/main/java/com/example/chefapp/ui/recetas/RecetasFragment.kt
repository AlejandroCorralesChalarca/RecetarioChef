package com.example.chefapp.ui.recetas

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chefapp.R
import com.example.chefapp.databinding.FragmentRecetasBinding

class RecetasFragment : Fragment() {

    private var _binding: FragmentRecetasBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetasViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetasBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecetasViewModel::class.java]

        binding.editBuscarReceta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.recetasFiltradas.observe(viewLifecycleOwner) { recetas ->
            renderRecetas(recetas)
        }

        return binding.root
    }

    private fun renderRecetas(recetas: List<Receta>) {
        binding.contenedorRecetas.removeAllViews()

        if (recetas.isEmpty()) {
            val txtVacio = TextView(requireContext())
            txtVacio.text = "😕 No se encontraron recetas"
            txtVacio.textSize = 15f
            txtVacio.setTextColor(Color.parseColor("#5A5A5A"))
            txtVacio.setPadding(0, 40, 0, 0)
            binding.contenedorRecetas.addView(txtVacio)
            return
        }

        for (receta in recetas) {
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
            filaTop.gravity = android.view.Gravity.CENTER_VERTICAL

            val txtEmoji = TextView(requireContext())
            txtEmoji.text = receta.emoji
            txtEmoji.textSize = 32f
            val emojiParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            emojiParams.marginEnd = 24
            txtEmoji.layoutParams = emojiParams

            val infoLayout = LinearLayout(requireContext())
            infoLayout.orientation = LinearLayout.VERTICAL
            val infoParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            infoLayout.layoutParams = infoParams

            val txtNombre = TextView(requireContext())
            txtNombre.text = receta.nombre
            txtNombre.textSize = 16f
            txtNombre.setTextColor(Color.parseColor("#5A5A5A"))
            txtNombre.setTypeface(null, Typeface.BOLD)

            val txtInfo = TextView(requireContext())
            txtInfo.text = "⏱ ${receta.tiempo}  •  🍽 ${receta.porciones}"
            txtInfo.textSize = 13f
            txtInfo.setTextColor(Color.parseColor("#F08030"))

            infoLayout.addView(txtNombre)
            infoLayout.addView(txtInfo)

            val txtVer = TextView(requireContext())
            txtVer.text = "Ver"
            txtVer.textSize = 13f
            txtVer.setTextColor(Color.WHITE)
            txtVer.setPadding(28, 12, 28, 12)
            txtVer.background = resources.getDrawable(R.drawable.bg_chip_selected, null)

            filaTop.addView(txtEmoji)
            filaTop.addView(infoLayout)
            filaTop.addView(txtVer)
            contenido.addView(filaTop)

            val txtIngredientes = TextView(requireContext())
            txtIngredientes.text = "Ingredientes: ${receta.ingredientes}"
            txtIngredientes.textSize = 13f
            txtIngredientes.setTextColor(Color.parseColor("#5A5A5A"))
            val ingParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ingParams.topMargin = 16
            txtIngredientes.layoutParams = ingParams
            contenido.addView(txtIngredientes)

            card.addView(contenido)
            binding.contenedorRecetas.addView(card)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}