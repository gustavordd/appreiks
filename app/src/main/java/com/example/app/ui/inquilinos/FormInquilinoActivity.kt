package com.example.app.ui.inquilinos

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.data.entities.Inquilino
import com.example.app.databinding.ActivityFormInquilinoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream



class FormInquilinoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormInquilinoBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var adminId: Long = -1
    private var imagemSelecionadaUri: Uri? = null

    private val selecionarImagemLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val uriCopiada = salvarImagemInternamente(it)
                if (uriCopiada != null) {
                    imagemSelecionadaUri = uriCopiada
                    binding.imgInquilino.setImageURI(uriCopiada)
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormInquilinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getLongExtra("ADMIN_ID", -1)

        binding.imgInquilino.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        binding.btnSalvar.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val cpf = binding.edtCpf.text.toString().trim()
            val contato = binding.edtContato.text.toString().trim()
            val contatoEmergencia = binding.edtContatoEmergencia.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || cpf.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inquilino = Inquilino(
                nome = nome,
                email = email,
                cpf = cpf,
                contato = contato,
                contatoEmergencia = contatoEmergencia,
                administradorId = adminId,
                fotoUri = imagemSelecionadaUri?.toString()
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.inquilinoDao().inserir(inquilino)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormInquilinoActivity, "Inquilino salvo!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun salvarImagemInternamente(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nomeArquivo = "img_inquilino_${System.currentTimeMillis()}.jpg"
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
