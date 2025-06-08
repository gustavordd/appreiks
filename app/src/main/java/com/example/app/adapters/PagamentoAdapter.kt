package com.example.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.data.entities.Pagamento

class PagamentoAdapter(
    private val pagamentos: List<Pagamento>,
    private val onClick: (Pagamento) -> Unit,
    private val onLongClick: (Pagamento) -> Unit
) : RecyclerView.Adapter<PagamentoAdapter.PagamentoViewHolder>() {

    inner class PagamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtValor: TextView = itemView.findViewById(R.id.txtValor)
        val txtData: TextView = itemView.findViewById(R.id.txtData)
        val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagamentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pagamento, parent, false)
        return PagamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagamentoViewHolder, position: Int) {
        val pagamento = pagamentos[position]
        holder.txtValor.text = "Valor: R$ %.2f".format(pagamento.valor)
        holder.txtData.text = "Data: ${pagamento.data}"
        holder.txtDescricao.text = pagamento.descricao

        holder.itemView.setOnClickListener { onClick(pagamento) }
        holder.itemView.setOnLongClickListener {
            onLongClick(pagamento)
            true
        }
    }

    override fun getItemCount(): Int = pagamentos.size
}
