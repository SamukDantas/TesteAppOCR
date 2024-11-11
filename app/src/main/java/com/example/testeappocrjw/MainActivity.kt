package com.example.testeappocrjw

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    
    private lateinit var capturaImagem: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (isPermissoesAutorizadas()) {
            iniciarCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, PERMISSOES_REQUERIDAS, REQUISICAO_CODIGO_PERMISSAO
            )
        }

        val botaoTirarFoto = findViewById<Button>(R.id.botaoTirarFoto)
        botaoTirarFoto.setOnClickListener { dispararFoto() }
    }

    private fun iniciarCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val provedorCamera: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = findViewById<PreviewView>(R.id.viewLocalizadorCamera).surfaceProvider
                }
            capturaImagem = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provedorCamera.unbindAll()
                provedorCamera.bindToLifecycle(
                    this, cameraSelector, preview, capturaImagem
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Erro ao abrir a câmera: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun dispararFoto() {
        val arquivoFoto = File(
            diretorioSaida,
            SimpleDateFormat(FORMATO_NOME_ARQUIVO, Locale("pt", "BR"))
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val saidaOpcoes = ImageCapture.OutputFileOptions.Builder(arquivoFoto).build()

        capturaImagem.takePicture(
            saidaOpcoes, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Erro ao salvar a imagem: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(arquivoFoto)
                    Toast.makeText(baseContext, "Imagem salva em: $savedUri", Toast.LENGTH_SHORT).show()
                    val bitmap = BitmapFactory.decodeFile(arquivoFoto.absolutePath)
                    extrairTextoDaImagem(bitmap)
                }
            }
        )

    }

    private fun extrairTextoDaImagem(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                findViewById<TextView>(R.id.textoViewResultado).text = visionText.text
            }
            .addOnFailureListener { e ->
                findViewById<TextView>(R.id.textoViewResultado).text = "Erro ao reconhecer texto: ${e.message}"
            }
    }

    private fun isPermissoesAutorizadas() = PERMISSOES_REQUERIDAS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUISICAO_CODIGO_PERMISSAO) {
            if (isPermissoesAutorizadas()) {
                iniciarCamera()
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    val diretorioSaida: File by lazy { criarDiretorioSaida() }

    private fun criarDiretorioSaida(): File {
        val diretorioMidia = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return diretorioMidia?.takeIf { it.exists() } ?: filesDir
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FORMATO_NOME_ARQUIVO = "dd-MM-yyyy-HH-mm-ss-SSS"
        private const val REQUISICAO_CODIGO_PERMISSAO = 10
        private val PERMISSOES_REQUERIDAS = arrayOf(android.Manifest.permission.CAMERA)
    }
}