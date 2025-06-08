package com.example.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.data.entities.Inquilino

class InquilinoAdapter(
    private val inquilinos: List<Inquilino>,
    private val onItemClick: (Inquilino) -> Unit
) : RecyclerView.Adapter<InquilinoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.txtNome)
        val email: TextView = itemView.findViewById(R.id.txtEmail)

        fun bind(inquilino: Inquilino) {
            nome.text = inquilino.nome
            email.text = inquilino.email
            itemView.setOnClickListener { onItemClick(inquilino) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inquilino, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = inquilinos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(inquilinos[position])
    }
}
