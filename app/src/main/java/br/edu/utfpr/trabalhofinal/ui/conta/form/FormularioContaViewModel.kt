package br.edu.utfpr.trabalhofinal.ui.conta.form
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import br.edu.utfpr.trabalhofinal.R
import br.edu.utfpr.trabalhofinal.data.ContaDatasource
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import br.edu.utfpr.trabalhofinal.ui.Arguments
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FormularioContaViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val idConta: Int = savedStateHandle
        .get<String>(Arguments.ID_CONTA)
        ?.toIntOrNull() ?: 0
    var state: FormularioContaState by mutableStateOf(FormularioContaState(idConta = idConta))
        private set
    init {
        if (state.idConta > 0) {
            carregarConta()
        }
    }
    fun carregarConta() {
        state = state.copy(
            carregando = true,
            erroAoCarregar = false
        )
        val conta = ContaDatasource.instance.findOne(state.idConta)
        state = if (conta == null) {
            state.copy(
                carregando = false,
                erroAoCarregar = true
            )
        } else {
            state.copy(
                carregando = false,
                conta = conta,
                descricao = state.descricao.copy(valor = conta.descricao),
                data = state.data.copy(valor = conta.data.toString()),
                valor = state.valor.copy(valor = conta.valor.toString()),
                paga = state.paga.copy(valor = conta.paga.toString()),
                tipo = state.tipo.copy(valor = conta.tipo.name)
            )
        }
    }
    fun onDescricaoAlterada(novaDescricao: String) {
        if (state.descricao.valor != novaDescricao) {
            state = state.copy(
                descricao = state.descricao.copy(
                    valor = novaDescricao,
                    codigoMensagemErro = validarDescricao(novaDescricao)
                )
            )
        }
    }
    private fun validarDescricao(descricao: String): Int = if (descricao.isBlank()) {
        R.string.descricao_obrigatoria
    } else {
        0
    }
    fun onDataAlterada(novaData: String) {
        if (state.data.valor != novaData) {
            state = state.copy(
                data = state.data.copy(
                    valor = novaData
                )
            )
        }
    }
    fun onValorAlterado(novoValor: String) {
        if (state.valor.valor != novoValor) {
            state = state.copy(
                valor = state.valor.copy(
                    valor = novoValor
                )
            )
        }
    }
    fun onStatusPagamentoAlterado(novoStatusPagamento: String) {
        if (state.paga.valor != novoStatusPagamento) {
            state = state.copy(
                paga = state.paga.copy(
                    valor = novoStatusPagamento
                )
            )
        }
    }
    fun onTipoAlterado(novoTipo: String) {
        if (state.tipo.valor != novoTipo) {
            state = state.copy(
                tipo = state.tipo.copy(
                    valor = novoTipo
                )
            )
        }
    }
    fun salvarConta() {
        if (formularioValido()) {
            state = state.copy(
                salvando = true
            )

            // Verifica se a data não está vazia
            if (state.data.valor.isBlank()) {
                // Exiba uma mensagem de erro ou retorne sem salvar
                println("O campo de data não pode estar vazio.")
                state = state.copy(
                    salvando = false,
                    codigoMensagem = R.string.data_obrigatoria // Certifique-se de ter uma mensagem de erro apropriada
                )
                return
            }

            // Defina o formatador para o formato correto
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val conta = state.conta.copy(
                descricao = state.descricao.valor,
                data = LocalDate.parse(state.data.valor, dateFormatter),
                valor = BigDecimal(state.valor.valor),
                paga = state.paga.valor == "true",
                tipo = TipoContaEnum.valueOf(state.tipo.valor)
            )

            ContaDatasource.instance.salvar(conta)
            state = state.copy(
                salvando = false,
                contaPersistidaOuRemovida = true
            )
        }
    }
    private fun formularioValido(): Boolean {
        state = state.copy(
            descricao = state.descricao.copy(
                codigoMensagemErro = validarDescricao(state.descricao.valor)
            )
        )
        return state.formularioValido
    }
    fun mostrarDialogConfirmacao() {
        state = state.copy(mostrarDialogConfirmacao = true)
    }
    fun ocultarDialogConfirmacao() {
        state = state.copy(mostrarDialogConfirmacao = false)
    }
    fun removerConta() {
        state = state.copy(
            excluindo = true,
        )
        ContaDatasource.instance.remover(state.conta)
        state = state.copy(
            excluindo = false,
            contaPersistidaOuRemovida = true
        )
    }
    fun onMensagemExibida() {
        state = state.copy(codigoMensagem = 0)
    }
}