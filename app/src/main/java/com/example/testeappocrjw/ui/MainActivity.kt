package com.example.testeappocrjw.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testeappocrjw.R
import com.example.testeappocrjw.camera.CameraHandlerImpl
import com.example.testeappocrjw.camera.CameraHandlerInterface
import com.example.testeappocrjw.permissoes.PermissaoHandlerImpl
import com.example.testeappocrjw.permissoes.PermissaoHandlerInterface
import com.example.testeappocrjw.reconhecimentotexto.TextoExtratorImpl
import com.example.testeappocrjw.reconhecimentotexto.TextoExtratorInterface

class MainActivity : AppCompatActivity() {

    private lateinit var cameraHandler: CameraHandlerInterface
    private lateinit var permissaoHandler: PermissaoHandlerInterface
    private lateinit var textoExtrator: TextoExtratorInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        configurarEdgeInsets()

        permissaoHandler = PermissaoHandlerImpl(this)
        textoExtrator = TextoExtratorImpl(findViewById(R.id.textoViewResultado))

        if (permissaoHandler.isPermissoesAutorizadas()) {
            iniciarCamera()
        } else {
            permissaoHandler.solicitarPermissoes()
        }

        findViewById<Button>(R.id.botaoTirarFoto).setOnClickListener {
            cameraHandler.tirarFoto { bitmap ->
                textoExtrator.extrairTexto(bitmap)
            }
        }
    }

    private fun configurarEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun iniciarCamera() {
        cameraHandler = CameraHandlerImpl(this, findViewById(R.id.viewLocalizadorCamera))
        cameraHandler.iniciarCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissaoHandler.handlePermissoesResultado(requestCode, grantResults)) {
            iniciarCamera()
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}