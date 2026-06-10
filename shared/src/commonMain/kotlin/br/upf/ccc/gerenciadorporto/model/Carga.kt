package br.upf.ccc.gerenciadorporto.model

abstract class Carga(
    val id: String,
    val nome: String,
    val destino: String,
    val destinatario: String,
    val volume: Double,
    val metodoTransporte: MetodoTransporte
) {
    abstract fun calcularTarifaBase(): Double
}

class CargaConteiner(
    id: String,
    nome: String,
    destino: String,
    destinatario: String,
    metodoTransporte: MetodoTransporte,
    val tamanho: Int, // 6 ou 12 metros
    val tipo: TipoConteiner,
    val diasNoPatio: Int,
    val qtdContaineres: Int
) : Carga(id, nome, destino, destinatario, metodoTransporte) {
    val volume: Double get() = tamanho == 12 ? (30 * qtdContaineres) : (15 * qtdContaineres)
    // container de 12m -> 30m³
    // container de 6m -> 15m³

    override fun calcularTarifaBase(): Double {
        // base: 6m = 1, 12m = 2
        val mult = if (tamanho == 12) 2.0 else 1.0
        val taxaMovimentacaoBase = 500.0 * mult

        val taxaAdicional = when (tipo) {
            TipoConteiner.REFRIGERADO -> 300.0 // energia
            TipoConteiner.PERIGOSO ->   450.0  // segurança
            TipoConteiner.PADRAO ->     0.0
        }

        val taxaArmazenamento = diasNoPatio * 120.0

        return taxaMovimentacaoBase + taxaAdicional + taxaArmazenamento
    }
}

enum class TipoConteiner { PADRAO, REFRIGERADO, PERIGOSO }


class CargaGranel(
    id: String,
    nome: String,
    destino: String,
    destinatario: String,
    metodoTransporte: MetodoTransporte,
    val volume: Double,
    val tipoGranel: TipoGranel
) : Carga(id, nome, destino, destinatario, metodoTransporte) {

    override fun calcularTarifaBase(): Double {
        val taxaPorMetro = 0.15
        return volume * taxaPorTonelada
    }
}

enum class TipoGranel { GRAOS, MINERIO, LIQUIDO }
enum class MetodoTransporte { MARITIMO, FERROVIARIO, RODOVIARIO }