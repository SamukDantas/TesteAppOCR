package com.example.testeappocrjw.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.testeappocrjw.utils.DiretorioUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraHandlerImpl(
    private val activity: AppCompatActivity,
    private val previewView: PreviewView
) : CameraHandlerInterface {
    private lateinit var imagemCaptura: ImageCapture

    override fun iniciarCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            imagemCaptura = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imagemCaptura)
            } catch (exc: Exception) {
                Log.e(TAG, "Erro ao abrir a câmera: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    override fun tirarFoto(fotoCapturada: (Bitmap) -> Unit) {
        val arquivoFoto = File(
            DiretorioUtils.obterDiretorioSaida(activity),
            SimpleDateFormat(
                FORMATO_NOME_ARQUIVO,
                Locale("pt", "BR")
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val saidaOpcoes = ImageCapture.OutputFileOptions.Builder(arquivoFoto).build()

        imagemCaptura.takePicture(
            saidaOpcoes,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Erro ao salvar a imagem: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uriSalva = Uri.fromFile(arquivoFoto)
                    Toast.makeText(activity, "Imagem salva em: $uriSalva", Toast.LENGTH_SHORT)
                        .show()

                    val bitmap = BitmapFactory.decodeFile(arquivoFoto.absolutePath)
                    fotoCapturada(bitmap)
                }
            }
        )
    }

    companion object {
        private const val TAG = "CameraHandler"
        private const val FORMATO_NOME_ARQUIVO = "dd-MM-yyyy-HH-mm-ss-SSS"
    }
}