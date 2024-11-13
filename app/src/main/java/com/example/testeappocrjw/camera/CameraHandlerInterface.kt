package com.example.testeappocrjw.camera

import android.graphics.Bitmap

interface CameraHandlerInterface {
    fun iniciarCamera()
    fun tirarFoto(fotoCapturada: (bitmap: Bitmap) -> Unit)
}