package com.example.app.ui.propriedades.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabase
import com.example.app.databinding.FragmentRelatoriosBinding
import kotlinx.coroutines.launch

class RelatoriosFragment : Fragment() {
    private var _binding: FragmentRelatoriosBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRelatoriosBinding.inflate(inflater, container, false)


        binding.btnExibirFaturamento.setOnClickListener {
            exibirFaturamento()
        }

        return binding.root
    }

    private fun exibirFaturamento() {

        binding.txtFaturamento.visibility = View.VISIBLE
        binding.txtLucro.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {

                val totalPagamentos = db.pagamentoDao().getTotalPagamentos(propriedadeId) ?: 0.0
                val totalDespesas = db.despesaDao().getTotalDespesas(propriedadeId) ?: 0.0


                val lucro = totalPagamentos - totalDespesas


                binding.txtFaturamento.text = "Faturamento: R$ %.2f".format(totalPagamentos)
                binding.txtLucro.text = "Lucro: R$ %.2f".format(lucro)
            } catch (e: Exception) {
                binding.txtFaturamento.text = "Erro ao calcular o faturamento."
                binding.txtLucro.text = "Erro ao calcular o lucro."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long) = RelatoriosFragment().apply {
            arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
            }
        }
    }
}
