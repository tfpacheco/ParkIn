
package com.parkin.app.ui.nfcEntry

import android.content.Context
import com.parkin.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.time.LocalDate

@Serializable
data class CartaoDb(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("numero_cartao")
    val numeroCartao: String,

    val estado: String = "ativo",

    @SerialName("tipo_cartao")
    val tipoCartao: String = "universal",

    @SerialName("parque_id")
    val parqueId: String? = null
)

@Serializable
data class PlanoDb(
    val id: String,
    val nome: String
)

@Serializable
data class SubscricaoDb(
    @SerialName("user_id")
    val userId: String,

    @SerialName("plano_id")
    val planoId: String,

    @SerialName("parque_id")
    val parqueId: String? = null,

    val estado: String,

    @SerialName("data_inicio")
    val dataInicio: String,

    @SerialName("data_fim")
    val dataFim: String
)

private fun gerarToken(): String {
    val bytes = ByteArray(8)
    SecureRandom().nextBytes(bytes)

    return bytes.joinToString("") {
        "%02X".format(it)
    }
}

object CartaoStore {

    private const val PREFS = "parkin_card"
    private const val KEY = "token"

    fun guardar(context: Context, token: String) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).edit()
            .putString(KEY, token)
            .apply()
    }

    fun ler(context: Context): String? {
        return context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(KEY, null)
    }
}

private suspend fun obterUtilizadorId(): String {
    return supabase.auth.currentSessionOrNull()
        ?.user
        ?.id
        ?: error("Utilizador não autenticado.")
}

private suspend fun obterPlano(tipo: String): PlanoDb {
    val nomePlano = when (tipo) {
        "universal" -> "Parque Universal"
        "especifico" -> "Parkin Parque"
        else -> error("Tipo de cartão inválido.")
    }

    val planos = supabase
        .from("planos")
        .select()
        .decodeList<PlanoDb>()

    return planos.firstOrNull {
        it.nome.trim().equals(nomePlano, ignoreCase = true)
    } ?: error("O plano '$nomePlano' não foi encontrado.")
}

/**
 * Primeira compra:
 * - Cria o cartão.
 * - Cria a primeira subscrição.
 *
 * Renovação:
 * - Reutiliza o cartão existente.
 * - Mantém o mesmo número.
 * - Expira a subscrição anterior.
 * - Cria uma nova subscrição mensal.
 */
suspend fun criarCartao(
    context: Context,
    tipo: String = "universal",
    parqueId: String? = null
): Result<CartaoDb> = runCatching {

    require(tipo == "universal" || tipo == "especifico") {
        "Tipo de cartão inválido."
    }

    if (tipo == "especifico") {
        require(!parqueId.isNullOrBlank()) {
            "É necessário selecionar um parque."
        }
    }

    val userId = obterUtilizadorId()
    val plano = obterPlano(tipo)

    val parqueSelecionado = if (tipo == "especifico") {
        parqueId
    } else {
        null
    }

    // Procurar qualquer cartão já associado ao utilizador.
    val cartoesExistentes = supabase
        .from("cartoes")
        .select {
            filter {
                eq("user_id", userId)
            }
        }
        .decodeList<CartaoDb>()

    val cartaoExistente = cartoesExistentes.firstOrNull()

    val cartao: CartaoDb

    if (cartaoExistente == null) {

        // PRIMEIRA COMPRA: criar o cartão.
        val novoCartao = CartaoDb(
            userId = userId,
            numeroCartao = gerarToken(),
            estado = "ativo",
            tipoCartao = tipo,
            parqueId = parqueSelecionado
        )

        cartao = supabase
            .from("cartoes")
            .insert(novoCartao) {
                select()
            }
            .decodeSingle<CartaoDb>()

    } else {

        // RENOVAÇÃO: reutilizar o cartão existente.
        require(cartaoExistente.estado == "ativo") {
            "O cartão existente não está ativo. É necessário verificar o estado do cartão."
        }

        require(
            cartaoExistente.tipoCartao == tipo &&
                    cartaoExistente.parqueId == parqueSelecionado
        ) {
            "Este cartão já está associado a outro plano ou parque. Para o renovar, seleciona o mesmo plano e parque."
        }

        cartao = cartaoExistente
    }

    // Consultar as subscrições atualmente ativas.
    val subscricoesAtivas = supabase
        .from("subscricoes")
        .select {
            filter {
                eq("user_id", userId)
                eq("estado", "ativo")
            }
        }
        .decodeList<SubscricaoDb>()

    val hoje = LocalDate.now()

    // Se ainda existir uma subscrição válida, a próxima começa
    // no dia seguinte ao seu fim. Caso contrário, começa hoje.
    val ultimaDataFim = subscricoesAtivas
        .mapNotNull {
            runCatching {
                LocalDate.parse(it.dataFim)
            }.getOrNull()
        }
        .maxOrNull()

    val dataInicio = if (
        ultimaDataFim != null && ultimaDataFim.isAfter(hoje)
    ) {
        ultimaDataFim
    } else {
        hoje
    }

    val dataFim = dataInicio.plusMonths(1)

    // Marcar as subscrições anteriores como expiradas.
    if (subscricoesAtivas.isNotEmpty()) {
        supabase
            .from("subscricoes")
            .update({
                set("estado", "expirado")
            }) {
                filter {
                    eq("user_id", userId)
                    eq("estado", "ativo")
                }
            }
    }

    // Criar a nova subscrição.
    val novaSubscricao = SubscricaoDb(
        userId = userId,
        planoId = plano.id,
        parqueId = parqueSelecionado,
        estado = "ativo",
        dataInicio = dataInicio.toString(),
        dataFim = dataFim.toString()
    )

    supabase
        .from("subscricoes")
        .insert(novaSubscricao)

    // Guardar o número do cartão no dispositivo.
    CartaoStore.guardar(
        context = context,
        token = cartao.numeroCartao
    )

    cartao
}

/**
 * Procura o cartão existente do utilizador.
 */
suspend fun buscarCartaoAtivo(
    context: Context
): CartaoDb? {

    val userId = obterUtilizadorId()

    val cartoes = supabase
        .from("cartoes")
        .select {
            filter {
                eq("user_id", userId)
                eq("estado", "ativo")
            }
        }
        .decodeList<CartaoDb>()

    val cartao = cartoes.firstOrNull()

    cartao?.let {
        CartaoStore.guardar(
            context,
            it.numeroCartao
        )
    }

    return cartao
}
