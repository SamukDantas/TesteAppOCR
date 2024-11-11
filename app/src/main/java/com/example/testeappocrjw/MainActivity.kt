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

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        val captureButton = findViewById<Button>(R.id.buttonCapture)
        captureButton.setOnClickListener { takePhoto() }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Inicializa o CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configura o preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = findViewById<PreviewView>(R.id.viewFinder).surfaceProvider
                }

            // Configuração de captura de imagem
            imageCapture = ImageCapture.Builder().build()

            // Seleciona a câmera traseira como padrão
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Abre a câmera
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Erro ao abrir a câmera: ${exc.message}", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Cria um arquivo temporário para armazenar a foto
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Opções de saída de imagem
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Erro ao salvar a imagem: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Toast.makeText(baseContext, "Imagem salva em: $savedUri", Toast.LENGTH_SHORT).show()
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    extractTextFromImage(bitmap)
                }
            }
        )

    }

    private fun extractTextFromImage(imageBitmap: Bitmap) {
        val image = InputImage.fromBitmap(imageBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                findViewById<TextView>(R.id.textViewResult).text = visionText.text
            }
            .addOnFailureListener { e ->
                findViewById<TextView>(R.id.textViewResult).text = "Erro ao reconhecer texto: ${e.message}"
            }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Define o diretório de saída para as imagens capturadas
    // Usando a palavra-chave 'by lazy' para definir a variável 'outputDirectory'
    val outputDirectory: File by lazy {
        createOutputDirectory() // Chama a função para criar o diretório
    }

    // Função para criar e retornar o diretório de saída
    private fun createOutputDirectory(): File {
        // Usa o diretório de mídia externo se disponível, senão o diretório interno privado
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        // Retorna o diretório ou, se não for possível usar o diretório externo, usa o interno
        return mediaDir?.takeIf { it.exists() } ?: filesDir
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }
}