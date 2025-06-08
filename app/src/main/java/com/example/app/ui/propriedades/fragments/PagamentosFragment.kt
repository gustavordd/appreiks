package com.example.app.ui.propriedades.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.adapters.PagamentoAdapter
import com.example.app.data.AppDatabase
import com.example.app.data.entities.Pagamento
import com.example.app.databinding.FragmentPagamentosBinding
import kotlinx.coroutines.launch
import java.util.*

class PagamentosFragment : Fragment() {

    private var _binding: FragmentPagamentosBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagamentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerPagamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.btnAdicionarPagamento.setOnClickListener {
            mostrarDialogNovoPagamento()
        }

        carregarPagamentos()
    }

    private fun carregarPagamentos() {
        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.pagamentoDao()

        lifecycleScope.launch {
            val pagamentos = dao.getPagamentosDaPropriedade(propriedadeId)
            binding.recyclerPagamentos.adapter = PagamentoAdapter(pagamentos, onClick = {}, onLongClick = {
                mostrarDialogEditarOuExcluir(it)
            })
        }
    }

    private fun mostrarDialogNovoPagamento() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val inputValor = EditText(requireContext()).apply {
            hint = "Valor (ex: 1200.50)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val inputData = EditText(requireContext()).apply {
            hint = "Data (dd/MM/yyyy)"
            isFocusable = false
            isClickable = true
        }
        configurarCampoDataComDatePicker(inputData)

        val inputDescricao = EditText(requireContext()).apply {
            hint = "Descrição"
        }

        layout.addView(inputValor)
        layout.addView(inputData)
        layout.addView(inputDescricao)

        AlertDialog.Builder(requireContext())
            .setTitle("Novo Pagamento")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val pagamento = Pagamento(
                    propriedadeId = propriedadeId,
                    valor = inputValor.text.toString().toDoubleOrNull() ?: 0.0,
                    data = inputData.text.toString(),
                    descricao = inputDescricao.text.toString()
                )
                lifecycleScope.launch {
                    AppDatabase.getDatabase(requireContext()).pagamentoDao().inserir(pagamento)
                    carregarPagamentos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogEditarOuExcluir(pagamento: Pagamento) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val inputValor = EditText(requireContext()).apply {
            hint = "Valor"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(pagamento.valor.toString())
        }

        val inputData = EditText(requireContext()).apply {
            hint = "Data (dd/MM/yyyy)"
            setText(pagamento.data)
            isFocusable = false
            isClickable = true
        }
        configurarCampoDataComDatePicker(inputData)

        val inputDescricao = EditText(requireContext()).apply {
            hint = "Descrição"
            setText(pagamento.descricao)
        }

        layout.addView(inputValor)
        layout.addView(inputData)
        layout.addView(inputDescricao)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar ou Excluir Pagamento")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val atualizado = pagamento.copy(
                    valor = inputValor.text.toString().toDoubleOrNull() ?: pagamento.valor,
                    data = inputData.text.toString(),
                    descricao = inputDescricao.text.toString()
                )
                lifecycleScope.launch {
                    AppDatabase.getDatabase(requireContext()).pagamentoDao().atualizar(atualizado)
                    carregarPagamentos()
                    Toast.makeText(context, "Pagamento atualizado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.getDatabase(requireContext()).pagamentoDao().deletar(pagamento)
                    carregarPagamentos()
                    Toast.makeText(context, "Pagamento excluído", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun configurarCampoDataComDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val mesFormatado = (month + 1).toString().padStart(2, '0')
            val diaFormatado = dayOfMonth.toString().padStart(2, '0')
            editText.setText("$diaFormatado/$mesFormatado/$year")
        }

        editText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long) = PagamentosFragment().apply {
            arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
            }
        }
    }
}
