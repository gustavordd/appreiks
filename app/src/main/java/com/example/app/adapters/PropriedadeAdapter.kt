package com.example.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.data.entities.Propriedade

class PropriedadeAdapter(
    private val onItemClick: (Propriedade) -> Unit
) : ListAdapter<Propriedade, PropriedadeAdapter.ViewHolder>(PropriedadeDiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nome: TextView = itemView.findViewById(R.id.txtNome)
        private val endereco: TextView = itemView.findViewById(R.id.txtEndereco)

        fun bind(propriedade: Propriedade) {
            nome.text = propriedade.nome
            endereco.text = propriedade.endereco
            itemView.setOnClickListener { onItemClick(propriedade) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_propriedade, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PropriedadeDiffCallback : DiffUtil.ItemCallback<Propriedade>() {
    override fun areItemsTheSame(oldItem: Propriedade, newItem: Propriedade): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Propriedade, newItem: Propriedade): Boolean {
        return oldItem == newItem
    }
}
