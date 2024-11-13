package com.example.testeappocrjw.permissoes

interface PermissaoHandlerInterface {
    fun isPermissoesAutorizadas(): Boolean
    fun solicitarPermissoes()
    fun handlePermissoesResultado(codigoRequisicao: Int, concederResultados: IntArray): Boolean
}