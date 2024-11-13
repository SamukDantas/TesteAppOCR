package com.example.testeappocrjw.camera

import androidx.appcompat.app.AppCompatActivity
import com.example.testeappocrjw.R
import java.io.File

class DiretorioHandler(private val activity: AppCompatActivity) {

    val diretorioSaida: File by lazy { criarDiretorioSaida() }

    private fun criarDiretorioSaida(): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return mediaDir?.takeIf { it.exists() } ?: activity.filesDir
    }
}