package com.example.app.ui.propriedades.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.adapters.LembreteAdapter
import com.example.app.data.AppDatabase
import com.example.app.databinding.FragmentLembretesBinding
import com.example.app.entities.Lembrete
import kotlinx.coroutines.launch

class LembreteFragment : Fragment() {

    private var _binding: FragmentLembretesBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLembretesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerLembretes.layoutManager = LinearLayoutManager(requireContext())
        binding.btnNovoLembrete.setOnClickListener { mostrarDialogNovoLembrete() }
        carregarLembretes()
    }

    private fun carregarLembretes() {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val lista = db.lembreteDao().getPorPropriedade(propriedadeId)
            binding.recyclerLembretes.adapter = LembreteAdapter(lista) { lembrete ->
                mostrarDialogEditarOuExcluir(lembrete)
            }
        }
    }

    private fun mostrarDialogNovoLembrete() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val inputMensagem = EditText(requireContext()).apply { hint = "Mensagem do lembrete" }
        val inputDia = EditText(requireContext()).apply {
            hint = "Dia do mês (1 a 31)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val spinner = Spinner(requireContext())
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Mensal", "Trimestral", "Anual"))
        spinner.adapter = adapter

        layout.apply {
            addView(inputMensagem)
            addView(inputDia)
            addView(spinner)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Lembrete")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val mensagem = inputMensagem.text.toString().trim()
                val dia = inputDia.text.toString().toIntOrNull()
                val repeticao = spinner.selectedItem.toString()

                if (mensagem.isEmpty() || dia == null || dia !in 1..31) {
                    Toast.makeText(requireContext(), "Preencha os dados corretamente.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val lembrete = Lembrete(
                    propriedadeId = propriedadeId,
                    mensagem = mensagem,
                    dia = dia,
                    repeticao = repeticao
                )
                salvarLembrete(lembrete)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogEditarOuExcluir(lembrete: Lembrete) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 20)
        }

        val inputMensagem = EditText(requireContext()).apply {
            setText(lembrete.mensagem)
        }

        val inputDia = EditText(requireContext()).apply {
            setText(lembrete.dia.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val spinner = Spinner(requireContext())
        val opcoes = listOf("Mensal", "Trimestral", "Anual")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opcoes)
        spinner.adapter = adapter
        spinner.setSelection(opcoes.indexOf(lembrete.repeticao))

        layout.apply {
            addView(inputMensagem)
            addView(inputDia)
            addView(spinner)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ou Excluir Lembrete")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val novaMensagem = inputMensagem.text.toString().trim()
                val novoDia = inputDia.text.toString().toIntOrNull()
                val novaRepeticao = spinner.selectedItem.toString()

                if (novaMensagem.isEmpty() || novoDia == null || novoDia !in 1..31) {
                    Toast.makeText(requireContext(), "Preencha os dados corretamente.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val lembreteAtualizado = lembrete.copy(
                    mensagem = novaMensagem,
                    dia = novoDia,
                    repeticao = novaRepeticao
                )

                atualizarLembrete(lembreteAtualizado)
            }
            .setNeutralButton("Excluir") { _, _ ->
                excluirLembrete(lembrete)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarLembrete(lembrete: Lembrete) {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.lembreteDao().inserir(lembrete)
            carregarLembretes()
        }
    }

    private fun atualizarLembrete(lembrete: Lembrete) {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.lembreteDao().atualizar(lembrete)
            Toast.makeText(requireContext(), "Lembrete atualizado.", Toast.LENGTH_SHORT).show()
            carregarLembretes()
        }
    }

    private fun excluirLembrete(lembrete: Lembrete) {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.lembreteDao().deletar(lembrete)
            Toast.makeText(requireContext(), "Lembrete excluído.", Toast.LENGTH_SHORT).show()
            carregarLembretes()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long) = LembreteFragment().apply {
            arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
            }
        }
    }
}
