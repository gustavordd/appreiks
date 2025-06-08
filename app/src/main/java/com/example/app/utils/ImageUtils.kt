package com.example.app.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

fun salvarImagemInternamente(context: Context, imageUri: Uri, nomeArquivo: String): String? {
    try {
        val contentResolver: ContentResolver = context.contentResolver
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val pastaImagens = File(context.filesDir, "imagens")
        if (!pastaImagens.exists()) {
            pastaImagens.mkdir()
        }

        val arquivoImagem = File(pastaImagens, "$nomeArquivo.jpg")
        val fos = FileOutputStream(arquivoImagem)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()

        return arquivoImagem.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
