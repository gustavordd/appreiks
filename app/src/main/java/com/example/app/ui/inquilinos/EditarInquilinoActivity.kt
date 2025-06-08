package com.example.app.ui.inquilinos

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.data.entities.Inquilino
import com.example.app.databinding.ActivityEditarInquilinoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.example.app.R



class EditarInquilinoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditarInquilinoBinding
    private var inquilinoId: Long = -1
    private var fotoSelecionadaUri: Uri? = null
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }

    private val selecionarImagemLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoSelecionadaUri = it
            binding.imgEditarInquilino.setImageURI(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarInquilinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inquilinoId = intent.getLongExtra("INQUILINO_ID", -1)

        carregarDadosInquilino()

        binding.imgEditarInquilino.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        binding.btnSalvar.setOnClickListener {
            salvarEdicao()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun carregarDadosInquilino() {
        lifecycleScope.launch {
            val inquilino = withContext(Dispatchers.IO) {
                db.inquilinoDao().buscarPorId(inquilinoId)
            }

            if (inquilino != null) {
                binding.edtNome.setText(inquilino.nome)
                binding.edtEmail.setText(inquilino.email)
                binding.edtCpf.setText(inquilino.cpf)
                binding.edtTelefone.setText(inquilino.contato)
                binding.edtContatoEmergencia.setText(inquilino.contatoEmergencia)

                inquilino.fotoUri?.let { uriStr ->
                    try {
                        val uri = Uri.parse(uriStr)
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        binding.imgEditarInquilino.setImageDrawable(ImageDecoder.decodeDrawable(source))
                        fotoSelecionadaUri = uri
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.imgEditarInquilino.setImageResource(R.drawable.ic_default_user)
                    }
                }
            }
        }
    }

    private fun salvarEdicao() {
        val nome = binding.edtNome.text.toString()
        val email = binding.edtEmail.text.toString()
        val cpf = binding.edtCpf.text.toString()
        val telefone = binding.edtTelefone.text.toString()
        val contatoEmergencia = binding.edtContatoEmergencia.text.toString()

        if (nome.isBlank() || email.isBlank() || cpf.isBlank()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val inquilinoAtual = withContext(Dispatchers.IO) {
                db.inquilinoDao().buscarPorId(inquilinoId)
            }

            if (inquilinoAtual != null) {
                val fotoUriFinal = fotoSelecionadaUri?.let { copiarImagemParaInterno(it) } ?: inquilinoAtual.fotoUri

                val atualizado = inquilinoAtual.copy(
                    nome = nome,
                    email = email,
                    cpf = cpf,
                    contato = telefone,
                    contatoEmergencia = contatoEmergencia,
                    fotoUri = fotoUriFinal
                )

                withContext(Dispatchers.IO) {
                    db.inquilinoDao().atualizar(atualizado)
                }

                Toast.makeText(this@EditarInquilinoActivity, "Inquilino atualizado!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun copiarImagemParaInterno(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val nomeArquivo = "inquilino_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, nomeArquivo)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }
}
