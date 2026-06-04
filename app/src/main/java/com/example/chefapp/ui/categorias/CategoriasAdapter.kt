package com.example.chefapp.ui.categorias

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chefapp.domain.model.Categoria
import com.example.chefapp.databinding.ItemCategoriaBinding

class CategoriasAdapter(
    private val onDelete: (Categoria) -> Unit
) : ListAdapter<Categoria, CategoriasAdapter.ViewHolder>(CategoriaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvCatInfo.text = "[${item.tipo}] ${item.nombre}"
        holder.binding.btnDeleteCat.setOnClickListener { onDelete(item) }
    }

    class ViewHolder(val binding: ItemCategoriaBinding) : RecyclerView.ViewHolder(binding.root)

    class CategoriaDiffCallback : DiffUtil.ItemCallback<Categoria>() {
        override fun areItemsTheSame(oldItem: Categoria, newItem: Categoria) = oldItem.docId == newItem.docId
        override fun areContentsTheSame(oldItem: Categoria, newItem: Categoria) = oldItem == newItem
    }
}
