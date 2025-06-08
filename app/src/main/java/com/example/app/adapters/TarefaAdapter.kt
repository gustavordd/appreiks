package com.example.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app.databinding.ItemAgendamentoBinding
import com.example.app.entities.Tarefa

class TarefaAdapter(
    private val tarefas: List<Tarefa>,
    private val onLongClick: (Tarefa) -> Unit,
    private val onCheckedChange: (Tarefa, Boolean) -> Unit
) : RecyclerView.Adapter<TarefaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAgendamentoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tarefa: Tarefa) {
            binding.textTitulo.text = tarefa.titulo
            binding.textData.text = "${tarefa.data} às ${tarefa.hora}"
            binding.textObs.text = tarefa.observacoes
            binding.checkConcluida.isChecked = tarefa.concluida

            binding.root.setOnLongClickListener {
                onLongClick(tarefa)
                true
            }

            binding.checkConcluida.setOnCheckedChangeListener(null)
            binding.checkConcluida.isChecked = tarefa.concluida
            binding.checkConcluida.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(tarefa, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAgendamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = tarefas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tarefas[position])
    }
}

