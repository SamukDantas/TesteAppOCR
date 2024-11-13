package com.example.testeappocrjw.utils

import androidx.appcompat.app.AppCompatActivity
import com.example.testeappocrjw.R
import java.io.File

object DiretorioUtils {

    fun obterDiretorioSaida(activity: AppCompatActivity): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return mediaDir?.takeIf { it.exists() } ?: activity.filesDir
    }
}