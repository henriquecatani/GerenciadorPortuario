package br.upf.ccc.gerenciadorporto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.upf.ccc.gerenciadorporto.model.*
import br.upf.ccc.gerenciadorporto.services.GerenciadorPortuario
import java.time.LocalDateTime

@Composable
fun App() {
    MaterialTheme {
        PortoApp()
    }
}

private enum class Tela {
    DASHBOARD,
    REGISTRAR_NAVIO,
    ATRACAR_NAVIO,
    DESCARREGAR_NAVIO,
    SAIDA_CARGA,
    SAIDA_NAVIO,
    ESTADO
}

private enum class TipoCargaForm {
    CONTEINER,
    GRANEL
}

@Composable
fun PortoApp() {
    val vagas = remember {
        mutableStateListOf(
            VagaCais(numero = 1, navio = null),
            VagaCais(numero = 2, navio = null),
            VagaCais(numero = 3, navio = null)
        )
    }

    val setoresPatio = remember {
        mutableStateListOf(
            SetorPatio(
                id = "P1",
                nome = "Pátio Contêineres",
                capacidadeMaxima = 100_000.0,
                tipoCarga = CargaConteiner::class,
                cargasArmazenadas = mutableListOf()
            ),
            SetorPatio(
                id = "P2",
                nome = "Pátio Granel",
                capacidadeMaxima = 200_000.0,
                tipoCarga = CargaGranel::class,
                cargasArmazenadas = mutableListOf()
            )
        )
    }

    val gerenciador = remember { GerenciadorPortuario(vagas, setoresPatio) }
    val navios = remember { mutableStateListOf<Navio>() }
    val logs = remember { mutableStateListOf<String>() }

    var telaAtual by remember { mutableStateOf(Tela.DASHBOARD) }
    var mensagem by remember { mutableStateOf("") }

    fun log(msg: String) {
        logs.add(0, "${LocalDateTime.now()} - $msg")
        if (logs.size > 100) logs.removeLast()
    }

    fun sucesso(msg: String) {
        mensagem = msg
        log(msg)
    }

    fun erro(msg: String) {
        mensagem = msg
        log("ERRO: $msg")
    }

    Row(Modifier.fillMaxSize()) {
        Sidebar(
            selected = telaAtual,
            onSelect = { telaAtual = it }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TopBar(
                title = when (telaAtual) {
                    Tela.DASHBOARD -> "Dashboard"
                    Tela.REGISTRAR_NAVIO -> "Registrar Navio"
                    Tela.ATRACAR_NAVIO -> "Atracar Navio"
                    Tela.DESCARREGAR_NAVIO -> "Descarregar Navio"
                    Tela.SAIDA_CARGA -> "Saída de Carga"
                    Tela.SAIDA_NAVIO -> "Saída de Navio"
                    Tela.ESTADO -> "Estado do Porto"
                },
                mensagem = mensagem,
                onClearMensagem = { mensagem = "" }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (telaAtual) {
                    Tela.DASHBOARD -> DashboardScreen(vagas, setoresPatio, navios)

                    Tela.REGISTRAR_NAVIO -> RegistrarNavioScreen(
                        onRegistrar = { navio ->
                            val ok = gerenciador.registrarEntradaNavio(navio)
                            if (ok) {
                                navios.add(navio)
                                sucesso("Navio registrado com sucesso.")
                            } else {
                                erro("Já existe um navio com esse ID.")
                            }
                        }
                    )

                    Tela.ATRACAR_NAVIO -> AtracarNavioScreen(
                        navios = navios,
                        vagas = vagas,
                        onAtracar = { navioId, vagaNumero ->
                            val ok = gerenciador.atracarNavio(navioId, vagaNumero)
                            if (ok) sucesso("Navio atracado.") else erro("Falha ao atracar o navio.")
                        }
                    )

                    Tela.DESCARREGAR_NAVIO -> DescarregarNavioScreen(
                        navios = navios,
                        onDescarregar = { navioId ->
                            gerenciador.descarregarNavio(navioId)
                            sucesso("Descarregamento executado.")
                        }
                    )

                    Tela.SAIDA_CARGA -> SaidaCargaScreen(
                        setoresPatio = setoresPatio,
                        onRemover = { cargaId ->
                            val ok = gerenciador.registrarSaidaCarga(cargaId)
                            if (ok) sucesso("Carga removida.") else erro("Carga não encontrada.")
                        }
                    )

                    Tela.SAIDA_NAVIO -> SaidaNavioScreen(
                        vagas = vagas,
                        onLiberar = { navioId ->
                            val ok = gerenciador.liberarNavio(navioId)
                            if (ok) sucesso("Navio liberado.") else erro("Navio não encontrado ou não está atracado.")
                        }
                    )

                    Tela.ESTADO -> EstadoPortoScreen(vagas, setoresPatio, navios, logs)
                }
            }
        }
    }
}

@Composable
private fun Sidebar(selected: Tela, onSelect: (Tela) -> Unit) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Gerenciador Portuário", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        NavButton("Dashboard", selected == Tela.DASHBOARD) { onSelect(Tela.DASHBOARD) }
        NavButton("Registrar Navio", selected == Tela.REGISTRAR_NAVIO) { onSelect(Tela.REGISTRAR_NAVIO) }
        NavButton("Atracar Navio", selected == Tela.ATRACAR_NAVIO) { onSelect(Tela.ATRACAR_NAVIO) }
        NavButton("Descarregar Navio", selected == Tela.DESCARREGAR_NAVIO) { onSelect(Tela.DESCARREGAR_NAVIO) }
        NavButton("Saída de Carga", selected == Tela.SAIDA_CARGA) { onSelect(Tela.SAIDA_CARGA) }
        NavButton("Saída de Navio", selected == Tela.SAIDA_NAVIO) { onSelect(Tela.SAIDA_NAVIO) }
        NavButton("Estado do Porto", selected == Tela.ESTADO) { onSelect(Tela.ESTADO) }
    }
}

@Composable
private fun NavButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icons are not available in this shared module, show text only
        Text(text)
    }
}

@Composable
private fun TopBar(title: String, mensagem: String, onClearMensagem: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (mensagem.isNotBlank()) {
                TextButton(onClick = onClearMensagem) { Text("Limpar mensagem") }
            }
        }
        if (mensagem.isNotBlank()) {
            Text(mensagem, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DashboardScreen(vagas: List<VagaCais>, setoresPatio: List<SetorPatio>, navios: List<Navio>) {
    val ocupadas = vagas.count { it.ocupada }
    val cargasTotal = setoresPatio.sumOf { it.cargasArmazenadas.size }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard("Vagas ocupadas", "$ocupadas / ${vagas.size}", modifier = Modifier.weight(1f))
            InfoCard("Navios cadastrados", "${navios.size}", modifier = Modifier.weight(1f))
            InfoCard("Cargas no pátio", "$cargasTotal", modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Panel(title = "Últimos navios") {
                navios.takeLast(5).asReversed().forEach {
                    Text("${it.id} - ${it.nome} | ${it.status}")
                }
            }
            Panel(title = "Resumo das vagas") {
                vagas.forEach {
                    Text("Vaga ${it.numero}: ${if (it.ocupada) it.navio?.nome ?: "Ocupada" else "Livre"}")
                }
            }
        }
    }
}

@Composable
private fun RegistrarNavioScreen(onRegistrar: (Navio) -> Unit) {
    var id by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }

    var tipoCarga by remember { mutableStateOf(TipoCargaForm.CONTEINER) }

    val cargas = remember { mutableStateListOf<Carga>() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        Panel("Dados do navio") {
            FormTextField("ID do navio", id) { id = it }
            FormTextField("Nome do navio", nome) { nome = it }
        }

        Panel("Cadastrar carga") {
            TipoCargaSelector(tipoCarga) { tipoCarga = it }

            if (tipoCarga == TipoCargaForm.CONTEINER) {
                var cargaId by remember { mutableStateOf("") }
                var cargaNome by remember { mutableStateOf("") }
                var qtdContaineres by remember { mutableStateOf("") }
                var tamanho by remember { mutableStateOf("6") }
                var destinatario by remember { mutableStateOf("") }
                var destino by remember { mutableStateOf("") }
                var diasNoPatio by remember { mutableStateOf("") }
                var metodoTransporte by remember { mutableStateOf(MetodoTransporte.MARITIMO) }
                var tipoConteiner by remember { mutableStateOf(TipoConteiner.PADRAO) }

                FormTextField("ID da carga", cargaId) { cargaId = it }
                FormTextField("Nome da carga", cargaNome) { cargaNome = it }
                FormTextField("Quantidade de contêineres", qtdContaineres, KeyboardType.Number) { qtdContaineres = it }
                FormTextField("Tamanho (6 ou 12)", tamanho, KeyboardType.Number) { tamanho = it }
                FormTextField("Destinatário", destinatario) { destinatario = it }
                FormTextField("Destino", destino) { destino = it }
                FormTextField("Dias no pátio", diasNoPatio, KeyboardType.Number) { diasNoPatio = it }

                EnumSelector(
                    label = "Método de transporte",
                    values = MetodoTransporte.entries,
                    selected = metodoTransporte,
                    onSelected = { metodoTransporte = it }
                )

                EnumSelector(
                    label = "Tipo do contêiner",
                    values = TipoConteiner.entries,
                    selected = tipoConteiner,
                    onSelected = { tipoConteiner = it }
                )

                Button(onClick = {
                    val carga = CargaConteiner(
                        id = cargaId.trim(),
                        nome = cargaNome.trim(),
                        qtdContaineres = qtdContaineres.toIntOrNull() ?: 0,
                        tamanho = tamanho.toIntOrNull() ?: 6,
                        destinatario = destinatario.trim(),
                        destino = destino.trim(),
                        metodoTransporte = metodoTransporte,
                        diasNoPatio = diasNoPatio.toIntOrNull() ?: 0,
                        tipo = tipoConteiner
                    )
                    cargas.add(carga)
                }) {
                    Text("Adicionar carga contêiner")
                }
            } else {
                var cargaId by remember { mutableStateOf("") }
                var cargaNome by remember { mutableStateOf("") }
                var volume by remember { mutableStateOf("") }
                var destinatario by remember { mutableStateOf("") }
                var destino by remember { mutableStateOf("") }
                var metodoTransporte by remember { mutableStateOf(MetodoTransporte.MARITIMO) }
                var tipoGranel by remember { mutableStateOf(TipoGranel.GRAOS) }

                FormTextField("ID da carga", cargaId) { cargaId = it }
                FormTextField("Nome da carga", cargaNome) { cargaNome = it }
                FormTextField("Volume (m³)", volume, KeyboardType.Decimal) { volume = it }
                FormTextField("Destinatário", destinatario) { destinatario = it }
                FormTextField("Destino", destino) { destino = it }

                EnumSelector(
                    label = "Método de transporte",
                    values = MetodoTransporte.entries,
                    selected = metodoTransporte,
                    onSelected = { metodoTransporte = it }
                )

                EnumSelector(
                    label = "Tipo de granel",
                    values = TipoGranel.entries,
                    selected = tipoGranel,
                    onSelected = { tipoGranel = it }
                )

                Button(onClick = {
                    val carga = CargaGranel(
                        id = cargaId.trim(),
                        nome = cargaNome.trim(),
                        volume = volume.toDoubleOrNull() ?: 0.0,
                        destinatario = destinatario.trim(),
                        destino = destino.trim(),
                        metodoTransporte = metodoTransporte,
                        tipoGranel = tipoGranel
                    )
                    cargas.add(carga)
                }) {
                    Text("Adicionar carga granel")
                }
            }
        }

        Panel("Cargas adicionadas") {
            if (cargas.isEmpty()) {
                Text("Nenhuma carga adicionada ainda.")
            } else {
                cargas.forEachIndexed { index, carga ->
                    Text("${index + 1}. ${carga.id} - ${carga.nome} | ${carga::class.simpleName} | tarifa: ${carga.calcularTarifaBase()}")
                }
            }
        }

        Button(onClick = {
            val navio = Navio(
                id = id.trim(),
                nome = nome.trim(),
                cargas = cargas.toList(),
                status = StatusNavio.ANCORADO
            )
            onRegistrar(navio)
        }) {
            Text("Registrar navio com cargas")
        }
    }
}

@Composable
private fun TipoCargaSelector(selected: TipoCargaForm, onSelected: (TipoCargaForm) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == TipoCargaForm.CONTEINER,
            onClick = { onSelected(TipoCargaForm.CONTEINER) },
            label = { Text("Contêiner") }
        )
        FilterChip(
            selected = selected == TipoCargaForm.GRANEL,
            onClick = { onSelected(TipoCargaForm.GRANEL) },
            label = { Text("Granel") }
        )
    }
}

@Composable
private fun AtracarNavioScreen(navios: List<Navio>, vagas: List<VagaCais>, onAtracar: (String, Int) -> Unit) {
    var navioId by remember { mutableStateOf("") }
    var vagaNumero by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Navios cadastrados", navios.map { it.id }, navioId) { navioId = it }
        DropdownField("Número da vaga", vagas.map { it.numero.toString() }, vagaNumero) { vagaNumero = it }
        Button(onClick = { onAtracar(navioId, vagaNumero.toIntOrNull() ?: -1) }) { Text("Atracar") }
    }
}

@Composable
private fun DescarregarNavioScreen(navios: List<Navio>, onDescarregar: (String) -> Unit) {
    var navioId by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Navios", navios.map { it.id }, navioId) { navioId = it }
        Button(onClick = { onDescarregar(navioId) }) { Text("Descarregar navio") }
    }
}

@Composable
private fun SaidaCargaScreen(setoresPatio: List<SetorPatio>, onRemover: (String) -> Unit) {
    val cargas = setoresPatio.flatMap { it.cargasArmazenadas }
    var cargaId by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Cargas no pátio", cargas.map { it.id }, cargaId) { cargaId = it }
        Button(onClick = { onRemover(cargaId) }) { Text("Remover carga") }
    }
}

@Composable
private fun SaidaNavioScreen(vagas: List<VagaCais>, onLiberar: (String) -> Unit) {
    val naviosAtracados = vagas.mapNotNull { it.navio }
    var navioId by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DropdownField("Navios atracados", naviosAtracados.map { it.id }, navioId) { navioId = it }
        Button(onClick = { onLiberar(navioId) }) { Text("Liberar navio") }
    }
}

@Composable
private fun EstadoPortoScreen(vagas: List<VagaCais>, setoresPatio: List<SetorPatio>, navios: List<Navio>, logs: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Panel("Vagas de cais") {
            vagas.forEach { vaga ->
                Text("Vaga ${vaga.numero} | ${if (vaga.ocupada) "Ocupada por ${vaga.navio?.nome}" else "Livre"}")
            }
        }
        Panel("Setores do pátio") {
            setoresPatio.forEach { setor ->
                Text("${setor.nome} | ${setor.ocupacaoAtual}/${setor.capacidadeMaxima} | ${setor.cargasArmazenadas.size} cargas ${if (setor.tipoCarga == CargaConteiner::class) "| ${setor.qtdContaineres} containeres" else ""}")
            }
        }
        Panel("Navios registrados") {
            navios.forEach { navio ->
                Text("${navio.id} - ${navio.nome} | ${navio.status}")
                Text("Detalhes: ${navio.toString()}")
            }
        }
        Panel("Logs recentes") {
            logs.take(10).forEach { Text(it) }
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EnumSelector(
    label: String,
    values: List<MetodoTransporte>,
    selected: MetodoTransporte,
    onSelected: (MetodoTransporte) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EnumSelector(
    label: String,
    values: List<TipoConteiner>,
    selected: TipoConteiner,
    onSelected: (TipoConteiner) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EnumSelector(
    label: String,
    values: List<TipoGranel>,
    selected: TipoGranel,
    onSelected: (TipoGranel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val display = when {
        selected.isNotBlank() -> selected
        options.isNotEmpty() -> options.first()
        else -> "Sem opções"
    }

    Box {
        OutlinedButton(
            onClick = { if (options.isNotEmpty()) expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label: $display")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}