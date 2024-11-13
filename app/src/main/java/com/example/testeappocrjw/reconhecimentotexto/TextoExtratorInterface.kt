package com.example.testeappocrjw.reconhecimentotexto

import android.graphics.Bitmap

interface TextoExtratorInterface {
    fun extrairTexto(imagemCapturada: Bitmap)
}