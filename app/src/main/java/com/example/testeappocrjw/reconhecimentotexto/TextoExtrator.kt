package com.example.testeappocrjw.reconhecimentotexto

import android.graphics.Bitmap
import android.widget.TextView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextoExtrator(private val resultTextView: TextView) {

    fun extrairTexto(imageBitmap: Bitmap) {
        val imagem = InputImage.fromBitmap(imageBitmap, 0)
        val reconhecedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        reconhecedor.process(imagem)
            .addOnSuccessListener { visionText ->
                resultTextView.text = visionText.text
            }
            .addOnFailureListener { e ->
                resultTextView.text = "Erro ao reconhecer texto: ${e.message}"
            }
    }
}