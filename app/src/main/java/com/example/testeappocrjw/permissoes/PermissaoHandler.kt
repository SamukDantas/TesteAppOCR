package com.example.testeappocrjw.permissoes

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissaoHandler(private val activity: AppCompatActivity) {

    fun isPermissoesAutorizadas(): Boolean = PERMISSOES_REQUERIDAS.all {
        ContextCompat.checkSelfPermission(activity.baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun solicitarPermissoes() {
        ActivityCompat.requestPermissions(activity, PERMISSOES_REQUERIDAS, REQUISICAO_CODIGO_PERMISSAO)
    }

    fun handlePermissoesResultado(codigoRequisicao: Int, concederResultados: IntArray): Boolean {
        return codigoRequisicao == REQUISICAO_CODIGO_PERMISSAO && concederResultados.all { it == PackageManager.PERMISSION_GRANTED }
    }

    companion object {
        private const val REQUISICAO_CODIGO_PERMISSAO = 10
        private val PERMISSOES_REQUERIDAS = arrayOf(android.Manifest.permission.CAMERA)
    }
}