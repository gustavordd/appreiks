package com.example.app.ui.propriedades.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.R
import com.example.app.adapters.TarefaAdapter
import com.example.app.data.AppDatabase
import com.example.app.databinding.FragmentAgendamentosBinding
import com.example.app.entities.Tarefa
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgendamentosFragment : Fragment() {

    private var _binding: FragmentAgendamentosBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1

    private lateinit var tarefaAdapter: TarefaAdapter
    private lateinit var dao: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAgendamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dao = AppDatabase.getDatabase(requireContext())
        binding.recyclerTarefas.layoutManager = LinearLayoutManager(requireContext())

        binding.btnAdicionarTarefa.setOnClickListener {
            mostrarDialogAdicionar()
        }

        carregarTarefas()
    }

    private fun carregarTarefas() {
        lifecycleScope.launch {
            val tarefas = dao.tarefaDao().getTarefasPorPropriedade(propriedadeId)
            tarefaAdapter = TarefaAdapter(
                tarefas = tarefas,
                onLongClick = { mostrarDialogEditarOuExcluir(it) },
                onCheckedChange = { tarefa, concluida ->
                    lifecycleScope.launch {
                        val atualizada = tarefa.copy(concluida = concluida)
                        dao.tarefaDao().atualizar(atualizada)
                        carregarTarefas()
                    }
                }
            )
            binding.recyclerTarefas.adapter = tarefaAdapter
        }
    }

    private fun mostrarDialogAdicionar() {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_tarefa, null)

        val edtTitulo = view.findViewById<EditText>(R.id.edtTitulo)
        val edtData = view.findViewById<EditText>(R.id.edtData)
        val edtHora = view.findViewById<EditText>(R.id.edtHora)
        val edtObs = view.findViewById<EditText>(R.id.edtObservacoes)

        configurarPickers(edtData, edtHora)

        AlertDialog.Builder(context)
            .setTitle("Nova Tarefa")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val novaTarefa = Tarefa(
                    propriedadeId = propriedadeId,
                    titulo = edtTitulo.text.toString(),
                    data = edtData.text.toString(),
                    hora = edtHora.text.toString(),
                    observacoes = edtObs.text.toString(),
                    concluida = false
                )
                lifecycleScope.launch {
                    dao.tarefaDao().inserir(novaTarefa)
                    carregarTarefas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogEditarOuExcluir(tarefa: Tarefa) {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_tarefa, null)

        val edtTitulo = view.findViewById<EditText>(R.id.edtTitulo)
        val edtData = view.findViewById<EditText>(R.id.edtData)
        val edtHora = view.findViewById<EditText>(R.id.edtHora)
        val edtObs = view.findViewById<EditText>(R.id.edtObservacoes)

        edtTitulo.setText(tarefa.titulo)
        edtData.setText(tarefa.data)
        edtHora.setText(tarefa.hora)
        edtObs.setText(tarefa.observacoes)

        configurarPickers(edtData, edtHora)

        AlertDialog.Builder(context)
            .setTitle("Editar ou Excluir Tarefa")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val tarefaAtualizada = tarefa.copy(
                    titulo = edtTitulo.text.toString(),
                    data = edtData.text.toString(),
                    hora = edtHora.text.toString(),
                    observacoes = edtObs.text.toString()
                )
                lifecycleScope.launch {
                    dao.tarefaDao().atualizar(tarefaAtualizada)
                    carregarTarefas()
                    Toast.makeText(context, "Tarefa atualizada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    dao.tarefaDao().deletar(tarefa)
                    carregarTarefas()
                    Toast.makeText(context, "Tarefa excluída", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarPickers(edtData: EditText, edtHora: EditText) {
        val calendar = Calendar.getInstance()

        edtData.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val dataFormatada = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    edtData.setText(dataFormatada)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        edtHora.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val horaFormatada = String.format("%02d:%02d", hourOfDay, minute)
                    edtHora.setText(horaFormatada)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long) = AgendamentosFragment().apply {
            arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
            }
        }
    }
}
