package br.upf.ccc.gerenciadorporto.model

class SetorPatio(val id: String, val nome: String, val capacidadeMaxima: Double, val qtdContaineres: Int?, val tipoCarga: Class<out Carga>, val cargasArmazenadas: MutableList<Carga>) {
    val ocupacaoAtual: Double get() = cargasArmazenadas.sumOf { it.volume }

    fun alocarCarga(carga: Carga): Boolean {
        if (carga.javaClass == tipoCarga && ocupacaoAtual + carga.volume <= capacidadeMaxima) {
            cargasArmazenadas.add(carga)
            return true
        }
        return false
    }
}