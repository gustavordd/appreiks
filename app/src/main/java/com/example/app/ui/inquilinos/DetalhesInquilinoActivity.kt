package com.example.app.ui.inquilinos

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.data.AppDatabaseProvider
import com.example.app.databinding.ActivityDetalhesInquilinoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesInquilinoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhesInquilinoBinding
    private val db by lazy { AppDatabaseProvider.getDatabase(this) }
    private var inquilinoId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesInquilinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inquilinoId = intent.getLongExtra("INQUILINO_ID", -1)

        carregarInquilino()

        binding.fabEditarInquilino.setOnClickListener {
            val intent = Intent(this, EditarInquilinoActivity::class.java)
            intent.putExtra("INQUILINO_ID", inquilinoId)
            startActivity(intent)
        }

        binding.fabExcluirInquilino.setOnClickListener {
            mostrarDialogoConfirmacao()
        }
    }

    private fun carregarInquilino() {
        lifecycleScope.launch {
            val inquilino = withContext(Dispatchers.IO) {
                db.inquilinoDao().buscarPorId(inquilinoId)
            }

            if (inquilino != null) {
                binding.txtNome.text = inquilino.nome
                binding.txtEmail.text = inquilino.email
                binding.txtCpf.text = inquilino.cpf
                binding.txtTelefone.text = inquilino.contato
                binding.txtContatoEmergencia.text = inquilino.contatoEmergencia

                inquilino.fotoUri?.let { uriStr ->
                    val uri = Uri.parse(uriStr)
                    carregarImagem(uri, binding.imgInquilino)
                }
            }
        }
    }

    private fun carregarImagem(uri: Uri, imageView: ImageView) {
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
            .setTitle("Excluir Inquilino")
            .setMessage("Tem certeza que deseja excluir este inquilino? Esta ação não poderá ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                excluirInquilino()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirInquilino() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val inquilino = db.inquilinoDao().buscarPorId(inquilinoId)
                if (inquilino != null) {
                    db.inquilinoDao().deletar(inquilino)
                }
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarInquilino()
    }
}
