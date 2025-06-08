package com.example.app.ui.administrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityEditarAdministradorBinding
import com.example.app.ui.WelcomeActivity
import com.example.app.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditarAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditarAdministradorBinding
    private var adminId: Long = -1
    private var uriFotoSelecionada: Uri? = null

    private val selecionarImagemLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val uriCopiada = salvarImagemInternamente(it)
                if (uriCopiada != null) {
                    uriFotoSelecionada = uriCopiada
                    binding.imgPerfil.setImageURI(uriCopiada)
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getLongExtra("ADMIN_ID", -1)
        if (adminId != -1L) {
            carregarAdministrador()
        } else {
            Toast.makeText(this, "Administrador não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.imgPerfil.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        binding.btnSalvar.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val telefone = binding.edtTelefone.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()

            if (nome.isEmpty() || telefone.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val senhaHash = SecurityUtils.hashSenha(senha)

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val dao = AppDatabaseProvider.getDatabase(this@EditarAdministradorActivity).administradorDao()
                    val admin = dao.buscarPorId(adminId)

                    admin?.let {
                        it.nome = nome
                        it.telefone = telefone
                        it.senha = senhaHash
                        if (uriFotoSelecionada != null) {
                            it.fotoUri = uriFotoSelecionada.toString()
                        }
                        dao.atualizar(it)
                    }
                }

                Toast.makeText(this@EditarAdministradorActivity, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        binding.btnExcluir.setOnClickListener {
            val dialog = android.app.AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Tem certeza que deseja excluir sua conta? Essa ação não poderá ser desfeita.")
                .setPositiveButton("Excluir") { _, _ ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val dao = AppDatabaseProvider.getDatabase(this@EditarAdministradorActivity).administradorDao()
                            dao.deletarPorId(adminId)
                        }

                        Toast.makeText(this@EditarAdministradorActivity, "Conta excluída com sucesso!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@EditarAdministradorActivity, WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()
        }
    }

    private fun carregarAdministrador() {
        lifecycleScope.launch {
            val dao = AppDatabaseProvider.getDatabase(this@EditarAdministradorActivity).administradorDao()
            val admin = withContext(Dispatchers.IO) {
                dao.buscarPorId(adminId)
            }

            admin?.let {
                binding.edtNome.setText(it.nome)
                binding.edtCpf.setText(it.cpf)
                binding.edtEmail.setText(it.email)
                binding.edtTelefone.setText(it.telefone)
                binding.edtSenha.setText("")

                binding.edtCpf.isEnabled = false
                binding.edtEmail.isEnabled = false

                if (!it.fotoUri.isNullOrEmpty()) {
                    Glide.with(this@EditarAdministradorActivity)
                        .load(it.fotoUri)
                        .circleCrop()
                        .into(binding.imgPerfil)
                }
            }
        }
    }

    private fun salvarImagemInternamente(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val nomeArquivo = "foto_admin_${System.currentTimeMillis()}.jpg"
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
