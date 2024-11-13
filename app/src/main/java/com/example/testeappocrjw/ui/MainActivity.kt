package com.example.testeappocrjw.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testeappocrjw.R
import com.example.testeappocrjw.camera.CameraHandler
import com.example.testeappocrjw.camera.DiretorioHandler
import com.example.testeappocrjw.permissoes.PermissaoHandler
import com.example.testeappocrjw.reconhecimentotexto.TextoExtrator

class MainActivity : AppCompatActivity() {

    private lateinit var cameraHandler: CameraHandler
    private lateinit var permissaoHandler: PermissaoHandler
    private lateinit var textoExtrator: TextoExtrator
    private lateinit var diretorioHandler: DiretorioHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        configurarEdgeInsets()

        permissaoHandler = PermissaoHandler(this)
        diretorioHandler = DiretorioHandler(this)
        textoExtrator = TextoExtrator(findViewById(R.id.textoViewResultado))

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
        cameraHandler = CameraHandler(this, findViewById(R.id.viewLocalizadorCamera), diretorioHandler)
        cameraHandler.iniciarCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissaoHandler.handlePermissoesResultado(requestCode, grantResults)) {
            iniciarCamera()
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}