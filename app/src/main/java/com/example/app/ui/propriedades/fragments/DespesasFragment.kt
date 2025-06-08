package com.example.app.ui.propriedades.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.R
import com.example.app.adapters.DespesaAdapter
import com.example.app.data.AppDatabase
import com.example.app.data.entities.Despesa
import com.example.app.databinding.FragmentDespesasBinding
import kotlinx.coroutines.launch
import java.util.*

class DespesasFragment : Fragment() {

    private var _binding: FragmentDespesasBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1
    private lateinit var despesaAdapter: DespesaAdapter
    private lateinit var dao: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDespesasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dao = AppDatabase.getDatabase(requireContext())

        binding.recyclerDespesas.layoutManager = LinearLayoutManager(requireContext())

        despesaAdapter = DespesaAdapter(
            despesas = listOf(),
            onClick = { mostrarDialogEditarOuExcluir(it) },
            onLongClick = { mostrarDialogEditarOuExcluir(it) }
        )
        binding.recyclerDespesas.adapter = despesaAdapter

        binding.btnAdicionarDespesa.setOnClickListener {
            mostrarDialogAdicionar()
        }

        carregarDespesas()
    }

    private fun carregarDespesas() {
        lifecycleScope.launch {
            val despesas = dao.despesaDao().getDespesasDaPropriedade(propriedadeId)
            despesaAdapter = DespesaAdapter(
                despesas = despesas,
                onClick = { mostrarDialogEditarOuExcluir(it) },
                onLongClick = { mostrarDialogEditarOuExcluir(it) }
            )
            binding.recyclerDespesas.adapter = despesaAdapter
        }
    }

    private fun mostrarDialogAdicionar() {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_despesa, null)

        val edtValor = view.findViewById<EditText>(R.id.edtValor)
        val edtDescricao = view.findViewById<EditText>(R.id.edtDescricao)
        val edtData = view.findViewById<EditText>(R.id.edtData)

        configurarCampoDataComDatePicker(edtData)

        AlertDialog.Builder(context)
            .setTitle("Nova Despesa")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val novaDespesa = Despesa(
                    propriedadeId = propriedadeId,
                    valor = edtValor.text.toString().toDoubleOrNull() ?: 0.0,
                    descricao = edtDescricao.text.toString(),
                    data = edtData.text.toString()
                )
                lifecycleScope.launch {
                    dao.despesaDao().inserir(novaDespesa)
                    carregarDespesas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogEditarOuExcluir(despesa: Despesa) {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_despesa, null)

        val edtValor = view.findViewById<EditText>(R.id.edtValor)
        val edtDescricao = view.findViewById<EditText>(R.id.edtDescricao)
        val edtData = view.findViewById<EditText>(R.id.edtData)

        edtValor.setText(despesa.valor.toString())
        edtDescricao.setText(despesa.descricao)
        edtData.setText(despesa.data)

        configurarCampoDataComDatePicker(edtData)

        AlertDialog.Builder(context)
            .setTitle("Editar ou Excluir Despesa")
            .setView(view)
            .setPositiveButton("Salvar") { _, _ ->
                val despesaAtualizada = despesa.copy(
                    valor = edtValor.text.toString().toDoubleOrNull() ?: 0.0,
                    descricao = edtDescricao.text.toString(),
                    data = edtData.text.toString()
                )
                lifecycleScope.launch {
                    dao.despesaDao().atualizar(despesaAtualizada)
                    carregarDespesas()
                    Toast.makeText(context, "Despesa atualizada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    dao.despesaDao().deletar(despesa)
                    carregarDespesas()
                    Toast.makeText(context, "Despesa excluída", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarCampoDataComDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val mesFormatado = (month + 1).toString().padStart(2, '0')
            val diaFormatado = dayOfMonth.toString().padStart(2, '0')
            editText.setText("$diaFormatado/$mesFormatado/$year")
        }

        editText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long) = DespesasFragment().apply {
            arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
            }
        }
    }
}
