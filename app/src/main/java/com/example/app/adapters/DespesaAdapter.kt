package com.example.app.adapters

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.data.entities.Despesa
import java.util.Locale

class DespesaAdapter(
    private val despesas: List<Despesa>,
    private val onClick: ((Despesa) -> Unit)? = null,
    private val onLongClick: ((Despesa) -> Unit)? = null
) : RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder>() {

    inner class DespesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtValor: TextView = itemView.findViewById(R.id.txtValor)
        private val txtData: TextView = itemView.findViewById(R.id.txtData)
        private val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)

        fun bind(despesa: Despesa) {
            txtValor.text = String.format(Locale.getDefault(), "Valor: R$ %.2f", despesa.valor)
            txtData.text = "Data: ${despesa.data}"
            txtDescricao.text = despesa.descricao


            itemView.setOnClickListener { }


            itemView.setOnLongClickListener {
                onLongClick?.invoke(despesa)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DespesaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_despesa, parent, false)
        return DespesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DespesaViewHolder, position: Int) {
        holder.bind(despesas[position])
    }

    override fun getItemCount(): Int = despesas.size
}
