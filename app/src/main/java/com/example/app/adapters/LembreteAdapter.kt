package com.example.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.entities.Lembrete

class LembreteAdapter(
    private val lista: List<Lembrete>,
    private val onLongClick: (Lembrete) -> Unit
) : RecyclerView.Adapter<LembreteAdapter.LembreteViewHolder>() {

    inner class LembreteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mensagem: TextView = view.findViewById(R.id.tvMensagem)
        val dia: TextView = view.findViewById(R.id.tvDia)
        val repeticao: TextView = view.findViewById(R.id.tvRepeticao)

        fun bind(lembrete: Lembrete) {
            mensagem.text = lembrete.mensagem
            dia.text = "Dia: ${lembrete.dia}"
            repeticao.text = "Repetição: ${lembrete.repeticao}"

            itemView.setOnLongClickListener {
                onLongClick(lembrete)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LembreteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lembrete, parent, false)
        return LembreteViewHolder(view)
    }

    override fun onBindViewHolder(holder: LembreteViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount() = lista.size
}
