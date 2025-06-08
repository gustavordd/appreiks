package com.example.app.ui.propriedades.fragments

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.FragmentContratoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ContratoFragment : Fragment() {

    private var _binding: FragmentContratoBinding? = null
    private val binding get() = _binding!!
    private var propriedadeId: Long = -1

    private val selecionarContratoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copiarArquivoParaInterno(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContratoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        propriedadeId = arguments?.getLong("PROPRIEDADE_ID") ?: -1

        binding.btnSelecionarContrato.setOnClickListener {
            selecionarContratoLauncher.launch("*/*")
        }

        binding.btnRemoverContrato.setOnClickListener {
            removerContrato()
        }

        carregarContrato()
    }

    private fun carregarContrato() {
        val db = AppDatabaseProvider.getDatabase(requireContext())

        lifecycleScope.launch {
            val propriedade = withContext(Dispatchers.IO) {
                db.propriedadeDao().getPropriedadePorId(propriedadeId)
            }

            if (propriedade?.caminhoContrato.isNullOrBlank()) {
                binding.imgContrato.visibility = View.GONE
                binding.btnAbrirPdf.visibility = View.GONE
                binding.btnSelecionarContrato.visibility = View.VISIBLE
                binding.btnRemoverContrato.visibility = View.GONE
                mostrarMensagem("Nenhum contrato anexado.")
            } else {
                val caminho = propriedade!!.caminhoContrato!!
                val file = File(caminho)

                if (file.exists()) {
                    binding.btnSelecionarContrato.visibility = View.GONE
                    binding.btnRemoverContrato.visibility = View.VISIBLE

                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        file
                    )

                    if (caminho.endsWith(".pdf")) {
                        binding.imgContrato.visibility = View.GONE
                        binding.btnAbrirPdf.visibility = View.VISIBLE
                        binding.btnAbrirPdf.setOnClickListener { abrirPdf(uri) }
                    } else {
                        binding.btnAbrirPdf.visibility = View.GONE
                        binding.imgContrato.visibility = View.VISIBLE
                        carregarImagem(uri, binding.imgContrato)


                        binding.imgContrato.setOnClickListener {
                            abrirImagem(uri)
                        }
                    }

                    mostrarMensagem("Contrato carregado com sucesso.")
                } else {
                    binding.imgContrato.visibility = View.GONE
                    binding.btnAbrirPdf.visibility = View.GONE
                    binding.btnSelecionarContrato.visibility = View.VISIBLE
                    binding.btnRemoverContrato.visibility = View.GONE
                    mostrarMensagem("Arquivo de contrato não encontrado.")
                }
            }
        }
    }

    private fun copiarArquivoParaInterno(uri: Uri) {
        val db = AppDatabaseProvider.getDatabase(requireContext())
        val contentResolver = requireContext().contentResolver

        lifecycleScope.launch {
            val extensao = contentResolver.getType(uri)?.substringAfter("/") ?: "pdf"
            val fileName = "contrato_${System.currentTimeMillis()}.$extensao"
            val destino = File(requireContext().filesDir, fileName)

            withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        destino.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    db.propriedadeDao().atualizarCaminhoContrato(propriedadeId, destino.absolutePath)

                    withContext(Dispatchers.Main) {
                        mostrarMensagem("Contrato salvo com sucesso!")
                        carregarContrato()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        mostrarMensagem("Erro ao salvar o contrato.")
                    }
                }
            }
        }
    }

    private fun removerContrato() {
        val db = AppDatabaseProvider.getDatabase(requireContext())

        lifecycleScope.launch {
            try {
                db.propriedadeDao().atualizarCaminhoContrato(propriedadeId, "")
                withContext(Dispatchers.Main) {
                    mostrarMensagem("Contrato removido.")
                    carregarContrato()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    mostrarMensagem("Erro ao remover contrato.")
                }
            }
        }
    }

    private fun abrirPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Abrir PDF com..."))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Nenhum aplicativo para abrir PDF encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirImagem(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(intent, "Abrir imagem com..."))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Nenhum aplicativo para abrir imagem encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarImagem(uri: Uri, imageView: ImageView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageURI(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mostrarMensagem("Erro ao carregar imagem do contrato.")
        }
    }

    private fun mostrarMensagem(msg: String) {
        binding.txtContratoStatus.text = msg
    }

    companion object {
        fun newInstance(propriedadeId: Long): ContratoFragment {
            val fragment = ContratoFragment()
            val args = Bundle()
            args.putLong("PROPRIEDADE_ID", propriedadeId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
