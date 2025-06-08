package com.example.app.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityHomeBinding
import com.example.app.ui.administrador.EditarAdministradorActivity
import com.example.app.ui.inquilinos.ListaInquilinosActivity
import com.example.app.ui.propriedades.ListaPropriedadesActivity
import kotlinx.coroutines.launch
import com.example.app.R

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var adminId: Long = -1

    private val editarAdministradorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val idAtualizado = result.data?.getLongExtra("ADMIN_ID", -1) ?: -1
            if (idAtualizado != -1L) {
                adminId = idAtualizado
                carregarAdministrador(adminId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getLongExtra("ADMIN_ID", -1)

        if (adminId != -1L) {
            carregarAdministrador(adminId)
        }

        binding.btnPropriedades.setOnClickListener {
            val intent = Intent(this, ListaPropriedadesActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            startActivity(intent)
        }

        binding.btnInquilinos.setOnClickListener {
            val intent = Intent(this, ListaInquilinosActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            startActivity(intent)
        }

        binding.imgPerfil.setOnClickListener {
            val intent = Intent(this, EditarAdministradorActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            editarAdministradorLauncher.launch(intent)
        }

        binding.btnEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditarAdministradorActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            editarAdministradorLauncher.launch(intent)
        }
    }

    private fun carregarAdministrador(id: Long) {
        lifecycleScope.launch {
            val dao = AppDatabaseProvider.getDatabase(this@HomeActivity).administradorDao()
            val admin = dao.buscarPorId(id)

            admin?.let {
                binding.txtNomeAdministrador.text = "Olá, ${it.nome}!"

                if (!it.fotoUri.isNullOrEmpty()) {
                    Glide.with(this@HomeActivity)
                        .load(it.fotoUri)
                        .circleCrop()
                        .into(binding.imgPerfil)
                } else {
                    binding.imgPerfil.setImageResource(R.drawable.user_default_home)
                }
            }
        }
    }
}
