package com.example.app.ui.propriedades

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.adapters.PropriedadeAdapter
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityListaPropriedadesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaPropriedadesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListaPropriedadesBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var adminId: Long = -1
    private lateinit var propriedadeAdapter: PropriedadeAdapter
    private lateinit var detalhesLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaPropriedadesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getLongExtra("ADMIN_ID", -1)

        propriedadeAdapter = PropriedadeAdapter { propriedade ->
            val intent = Intent(this, DetalhesPropriedadeActivity::class.java)
            intent.putExtra("PROPRIEDADE_ID", propriedade.id)
            intent.putExtra("ADMIN_ID", adminId)
            detalhesLauncher.launch(intent)
        }

        binding.rvPropriedades.layoutManager = LinearLayoutManager(this)
        binding.rvPropriedades.setHasFixedSize(true)
        binding.rvPropriedades.adapter = propriedadeAdapter


        binding.btnNovaPropriedade.setOnClickListener {
            val intent = Intent(this, FormPropriedadeActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            startActivity(intent)
        }


        detalhesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                carregarPropriedades()
            }
        }

        carregarPropriedades()
    }

    private fun carregarPropriedades() {
        lifecycleScope.launch {
            val propriedades = withContext(Dispatchers.IO) {
                db.propriedadeDao().listarPorAdministrador(adminId)
            }

            propriedadeAdapter.submitList(propriedades)
        }
    }
}
