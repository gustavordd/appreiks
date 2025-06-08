package com.example.app.ui.propriedades

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityFormEditarPropriedadeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream



class FormEditarPropriedadeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormEditarPropriedadeBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var imagemSelecionadaUri: Uri? = null
    private var propriedadeId: Long = -1

    private val selecionarImagemLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val uriCopiada = salvarImagemInternamente(it)
                if (uriCopiada != null) {
                    imagemSelecionadaUri = uriCopiada
                    binding.imgEditarPropriedade.setImageURI(uriCopiada)
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormEditarPropriedadeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        propriedadeId = intent.getLongExtra("PROPRIEDADE_ID", -1)

        carregarPropriedade()

        binding.imgEditarPropriedade.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        binding.btnAtualizar.setOnClickListener {
            val nome = binding.edtNome.text.toString()
            val endereco = binding.edtEndereco.text.toString()
            val descricao = binding.edtDescricao.text.toString()

            if (nome.isBlank() || endereco.isBlank()) {
                Toast.makeText(this, "Nome e endereço são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val propriedadeAtual = db.propriedadeDao().getPropriedadePorId(propriedadeId)
                    if (propriedadeAtual != null) {
                        val propriedadeAtualizada = propriedadeAtual.copy(
                            nome = nome,
                            endereco = endereco,
                            descricao = descricao,
                            fotoUri = imagemSelecionadaUri?.toString() ?: propriedadeAtual.fotoUri
                        )
                        db.propriedadeDao().atualizar(propriedadeAtualizada)
                    }
                }

                Toast.makeText(this@FormEditarPropriedadeActivity, "Propriedade atualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun carregarPropriedade() {
        lifecycleScope.launch {
            val propriedade = withContext(Dispatchers.IO) {
                db.propriedadeDao().getPropriedadePorId(propriedadeId)
            }

            propriedade?.let {
                binding.edtNome.setText(it.nome)
                binding.edtEndereco.setText(it.endereco)
                binding.edtDescricao.setText(it.descricao)
                it.fotoUri?.let { uriStr ->
                    val uri = Uri.parse(uriStr)
                    imagemSelecionadaUri = uri
                    binding.imgEditarPropriedade.setImageURI(uri)
                }
            }
        }
    }

    private fun salvarImagemInternamente(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nomeArquivo = "img_edit_${System.currentTimeMillis()}.jpg"
            val arquivo = File(filesDir, nomeArquivo)
            val outputStream = FileOutputStream(arquivo)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            Uri.fromFile(arquivo)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
