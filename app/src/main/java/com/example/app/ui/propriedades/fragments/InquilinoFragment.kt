package com.example.app.ui.propriedades.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.data.entities.Inquilino
import com.example.app.databinding.FragmentInquilinoBinding
import com.example.app.ui.inquilinos.DetalhesInquilinoActivity
import com.example.app.ui.inquilinos.FormInquilinoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InquilinoFragment : Fragment() {

    private var _binding: FragmentInquilinoBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1
    private var adminId: Long = -1
    private var inquilinoVinculado: Inquilino? = null

    private val novoInquilinoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                carregarInquilino()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInquilinoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        propriedadeId = requireArguments().getLong("PROPRIEDADE_ID", -1)
        adminId = requireArguments().getLong("ADMIN_ID", -1)

        carregarInquilino()

        binding.btnVincular.setOnClickListener {
            selecionarInquilino()
        }

        binding.btnDesvincular.setOnClickListener {
            desvincularInquilino()
        }

        binding.btnNovoInquilino.setOnClickListener {
            val intent = Intent(requireContext(), FormInquilinoActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            novoInquilinoLauncher.launch(intent)
        }


        binding.txtInquilino.setOnClickListener {
            inquilinoVinculado?.let {
                val intent = Intent(requireContext(), DetalhesInquilinoActivity::class.java)
                intent.putExtra("INQUILINO_ID", it.id)
                startActivity(intent)
            }
        }
    }

    private fun carregarInquilino() {
        val db = AppDatabaseProvider.getDatabase(requireContext())
        lifecycleScope.launch {
            val propriedade = withContext(Dispatchers.IO) {
                db.propriedadeDao().getPropriedadePorId(propriedadeId)
            }

            if (propriedade?.inquilinoId != null) {
                val inquilino = withContext(Dispatchers.IO) {
                    db.inquilinoDao().buscarPorId(propriedade.inquilinoId)
                }

                inquilinoVinculado = inquilino

                binding.txtInquilino.text = "${inquilino?.nome} (${inquilino?.email})"
                binding.txtInquilino.isClickable = true
                binding.btnDesvincular.visibility = View.VISIBLE
            } else {
                inquilinoVinculado = null
                binding.txtInquilino.text = "Nenhum inquilino vinculado"
                binding.txtInquilino.isClickable = false
                binding.btnDesvincular.visibility = View.GONE
            }
        }
    }

    private fun selecionarInquilino() {
        val db = AppDatabaseProvider.getDatabase(requireContext())
        lifecycleScope.launch {
            val inquilinos = withContext(Dispatchers.IO) {
                db.inquilinoDao().listarPorAdministrador(adminId)
            }

            if (inquilinos.isEmpty()) {
                Toast.makeText(requireContext(), "Nenhum inquilino disponível", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val nomes = inquilinos.map { it.nome }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Selecionar inquilino")
                .setItems(nomes) { _, index ->
                    val escolhido = inquilinos[index]
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.propriedadeDao().vincularInquilino(propriedadeId, escolhido.id)
                        withContext(Dispatchers.Main) {
                            carregarInquilino()
                            Toast.makeText(requireContext(), "Inquilino vinculado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .show()
        }
    }

    private fun desvincularInquilino() {
        val db = AppDatabaseProvider.getDatabase(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            db.propriedadeDao().desvincularInquilino(propriedadeId)
            withContext(Dispatchers.Main) {
                carregarInquilino()
                Toast.makeText(requireContext(), "Inquilino desvinculado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(propriedadeId: Long, adminId: Long): InquilinoFragment {
            val fragment = InquilinoFragment()
            fragment.arguments = Bundle().apply {
                putLong("PROPRIEDADE_ID", propriedadeId)
                putLong("ADMIN_ID", adminId)
            }
            return fragment
        }
    }
}
