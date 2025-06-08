package com.example.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.data.entities.Administrador
import com.example.app.databinding.ActivityCadastroBinding
import com.example.app.ui.home.HomeActivity
import com.example.app.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.edtNome.text.toString()
            val cpf = binding.edtCpf.text.toString().replace("[^\\d]".toRegex(), "")
            val telefone = binding.edtTelefone.text.toString().replace("[^\\d]".toRegex(), "")
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString()


            if (nome.isBlank() || cpf.isBlank() || telefone.isBlank() || email.isBlank() || senha.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (!isEmailValido(email)) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            if (!isCpfValido(cpf)) {
                Toast.makeText(this, "CPF inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            if (!isTelefoneValido(telefone)) {
                Toast.makeText(this, "Telefone inválido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val senhaHash = SecurityUtils.hashSenha(senha)

            lifecycleScope.launch {

                val existeAdmin = withContext(Dispatchers.IO) {
                    val dao = db.administradorDao()
                    dao.buscarPorCpfOuEmail(cpf, email)
                }

                if (existeAdmin != null) {
                    Toast.makeText(this@CadastroActivity, "Já existe uma conta com este CPF ou Email", Toast.LENGTH_LONG).show()
                    return@launch
                }


                val admin = Administrador(
                    nome = nome,
                    cpf = cpf,
                    telefone = telefone,
                    email = email,
                    senha = senhaHash
                )

                val id = withContext(Dispatchers.IO) {
                    db.administradorDao().inserir(admin)
                }

                Toast.makeText(this@CadastroActivity, "Conta criada com sucesso", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@CadastroActivity, HomeActivity::class.java).apply {
                    putExtra("ADMIN_ID", id)
                })
                finish()
            }
        }
    }


    private fun isEmailValido(email: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com|yahoo\\.com|protonmail\\.com|icloud\\.com)\$", RegexOption.IGNORE_CASE)
        return regex.matches(email) && !email.contains(" ") && !email.startsWith("@")
    }


    private fun isCpfValido(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false

        try {
            val numbers = cpf.map { it.toString().toInt() }

            val dv1 = (0..8).map { (10 - it) * numbers[it] }.sum() % 11
            val check1 = if (dv1 < 2) 0 else 11 - dv1

            val dv2 = (0..8).map { (11 - it) * numbers[it] }.sum() + check1 * 2
            val check2 = if (dv2 % 11 < 2) 0 else 11 - (dv2 % 11)

            return numbers[9] == check1 && numbers[10] == check2
        } catch (e: Exception) {
            return false
        }
    }


    private fun isTelefoneValido(telefone: String): Boolean {
        return telefone.length in 10..11 && telefone.all { it.isDigit() }
    }
}
