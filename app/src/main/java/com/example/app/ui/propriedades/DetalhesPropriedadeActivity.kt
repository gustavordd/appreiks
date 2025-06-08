package com.example.app.ui.propriedades

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityDetalhesPropriedadeBinding
import com.example.app.ui.propriedades.fragments.*
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesPropriedadeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhesPropriedadeBinding
    private var propriedadeId: Long = -1
    private var adminId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPropriedadeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        propriedadeId = intent.getLongExtra("PROPRIEDADE_ID", -1)
        adminId = intent.getLongExtra("ADMIN_ID", -1)

        carregarDadosPropriedade()

        val fragments = listOf(
            ContratoFragment.newInstance(propriedadeId),
            PagamentosFragment.newInstance(propriedadeId),
            DespesasFragment.newInstance(propriedadeId),
            AgendamentosFragment.newInstance(propriedadeId),
            RelatoriosFragment.newInstance(propriedadeId),
            InquilinoFragment.newInstance(propriedadeId, adminId),
            LembreteFragment.newInstance(propriedadeId),
        )

        val titulos = listOf("Contrato", "Pagamentos", "Despesas", "Agendamentos", "Relatórios", "Inquilino", "Lembretes")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titulos[position]
        }.attach()

        binding.fabEditarPropriedade.setOnClickListener {
            val intent = Intent(this, FormEditarPropriedadeActivity::class.java)
            intent.putExtra("PROPRIEDADE_ID", propriedadeId)
            intent.putExtra("ADMIN_ID", adminId)
            startActivity(intent)
        }

        binding.fabExcluirPropriedade.setOnClickListener {
            mostrarDialogoConfirmacao()
        }
    }

    private fun carregarDadosPropriedade() {
        val db = AppDatabaseProvider.getDatabase(this)
        lifecycleScope.launch {
            val propriedade = withContext(Dispatchers.IO) {
                db.propriedadeDao().getPropriedadePorId(propriedadeId)
            }

            propriedade?.let {
                binding.txtNomePropriedade.text = it.nome

                it.fotoUri?.let { uriStr ->
                    val uri = Uri.parse(uriStr)
                    loadImage(uri, binding.imgDetalhePropriedade)
                }
            }
        }
    }

    private fun loadImage(uri: Uri, imageView: ImageView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageURI(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mostrarDialogoConfirmacao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Propriedade")
            .setMessage("Tem certeza que deseja excluir esta propriedade? Essa ação não poderá ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                excluirPropriedade()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirPropriedade() {
        val db = AppDatabaseProvider.getDatabase(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val propriedade = db.propriedadeDao().getPropriedadePorId(propriedadeId)
                if (propriedade != null) {
                    db.propriedadeDao().deletar(propriedade)
                }
            }
            finish()
        }
    }
}
