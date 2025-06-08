package com.example.app.ui.inquilinos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.adapters.InquilinoAdapter
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityListaInquilinosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ListaInquilinosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListaInquilinosBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var adminId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaInquilinosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvInquilinos.layoutManager = LinearLayoutManager(this)
        binding.rvInquilinos.setHasFixedSize(true)

        adminId = intent.getLongExtra("ADMIN_ID", -1)

        binding.btnNovoInquilino.setOnClickListener {
            val intent = Intent(this, FormInquilinoActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarInquilinos()
    }

    private fun carregarInquilinos() {
        lifecycleScope.launch {
            val inquilinos = withContext(Dispatchers.IO) {
                db.inquilinoDao().listarPorAdministrador(adminId)
            }

            val adapter = InquilinoAdapter(inquilinos) { inquilino ->
                val intent = Intent(this@ListaInquilinosActivity, DetalhesInquilinoActivity::class.java)
                intent.putExtra("INQUILINO_ID", inquilino.id)
                startActivity(intent)
            }

            binding.rvInquilinos.adapter = adapter
        }
    }
}
