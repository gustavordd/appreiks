package com.example.app.ui.propriedades

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.data.entities.Propriedade
import com.example.app.databinding.ActivityFormPropriedadeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class FormPropriedadeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormPropriedadeBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var imagemSelecionadaUri: Uri? = null
    private var adminId: Long = -1

    private val selecionarImagemLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val uriCopiada = salvarImagemInternamente(it)
                if (uriCopiada != null) {
                    imagemSelecionadaUri = uriCopiada
                    binding.imgPropriedade.setImageURI(uriCopiada)
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormPropriedadeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getLongExtra("ADMIN_ID", -1)

        binding.imgPropriedade.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        binding.btnSalvar.setOnClickListener {
            val nome = binding.edtNome.text.toString()
            val endereco = binding.edtEndereco.text.toString()
            val descricao = binding.edtDescricao.text.toString()

            if (nome.isBlank() || endereco.isBlank()) {
                Toast.makeText(this, "Nome e endereço são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novaPropriedade = Propriedade(
                nome = nome,
                endereco = endereco,
                descricao = descricao,
                fotoUri = imagemSelecionadaUri?.toString(),
                administradorId = adminId
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.propriedadeDao().inserir(novaPropriedade)
                }
                Toast.makeText(this@FormPropriedadeActivity, "Propriedade cadastrada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun salvarImagemInternamente(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nomeArquivo = "img_prop_${System.currentTimeMillis()}.jpg"
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
