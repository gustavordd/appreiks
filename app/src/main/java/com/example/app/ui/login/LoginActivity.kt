package com.example.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityLoginBinding
import com.example.app.ui.home.HomeActivity
import com.example.app.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrar.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val senha = binding.edtSenha.text.toString()

            val senhaHash = SecurityUtils.hashSenha(senha)

            lifecycleScope.launch {
                val admin = withContext(Dispatchers.IO) {
                    db.administradorDao().login(email, senhaHash)
                }

                if (admin != null) {
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("ADMIN_ID", admin.id)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login inválido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
