
package com.parkin.app.ui.mainPageResorces

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.nfcEntry.criarCartao
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Duration
import java.time.Instant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavController) {

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedState =
        navController.previousBackStackEntry?.savedStateHandle

    val cardType = savedState?.get<String>("card_type")
    val cardPrice = savedState?.get<Double>("card_price")
    val parkId = savedState?.get<String>("park_id")

    val isCardPurchase = cardType != null

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showSuccessDialog by remember {
        mutableStateOf(false)
    }

    var amountCents by remember {
        mutableIntStateOf(0)
    }

    val amountEuros = if (isCardPurchase) {
        cardPrice ?: 0.0
    } else {
        amountCents / 100.0
    }

    val formattedAmount = String.format(
        Locale.getDefault(),
        "%.2f",
        amountEuros
    )

    LaunchedEffect(isCardPurchase, cardType, cardPrice, parkId) {

        if (isCardPurchase) {

            if (
                cardPrice == null ||
                cardPrice <= 0.0 ||
                cardType !in listOf("universal", "especifico")
            ) {
                errorMessage = "Dados do cartão inválidos."
            } else if (
                cardType == "especifico" &&
                parkId.isNullOrBlank()
            ) {
                errorMessage = "Não foi selecionado nenhum parque."
            }

        } else {

            isLoading = true
            errorMessage = null

            try {
                val user = supabase.auth.currentUserOrNull()

                if (user == null) {
                    errorMessage = "Inicia sessão novamente."
                    return@LaunchedEffect
                }

                val sessions = supabase
                    .from("sessoes")
                    .select {
                        filter {
                            eq("user_id", user.id)
                            eq("estado", "ativa")
                        }
                    }
                    .decodeList<JsonObject>()

                val session = sessions.firstOrNull()

                if (session == null) {
                    errorMessage = "Nenhuma sessão ativa encontrada."
                    return@LaunchedEffect
                }

                val entryTime = session["hora_entrada"]
                    ?.jsonPrimitive
                    ?.contentOrNull

                if (entryTime == null) {
                    errorMessage = "Hora de entrada não encontrada."
                    return@LaunchedEffect
                }

                val start = Instant.parse(entryTime)

                val minutes = Duration.between(
                    start,
                    Instant.now()
                ).toMinutes().coerceAtLeast(0)

                val total = (minutes / 60.0 * 1.50)
                    .coerceAtLeast(0.50)

                amountCents = (total * 100).toInt()

            } catch (e: Exception) {
                errorMessage =
                    "Erro ao calcular pagamento: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pagamento",
                        fontWeight = FontWeight.Bold,
                        color = onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary
                ),
                modifier = Modifier
                    .height(64.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(background),
            contentAlignment = Alignment.Center
        ) {

            when {

                isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = primary)

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "A carregar pagamento...",
                            color = onSurface
                        )
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary
                            )
                        ) {
                            Text("Voltar")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                outline
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text = if (isCardPurchase) {
                                        "Compra / renovação de cartão"
                                    } else {
                                        "Estacionamento"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface
                                )

                                Spacer(Modifier.height(20.dp))

                                Text(
                                    text = if (isCardPurchase) {
                                        if (cardType == "universal") {
                                            "Cartão Universal"
                                        } else {
                                            "Cartão Específico"
                                        }
                                    } else {
                                        "Pagamento da sessão"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = onSurface
                                )

                                Spacer(Modifier.height(24.dp))

                                Text(
                                    text = "$formattedAmount €",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Valor total",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isCardPurchase) {
                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = "Duração da subscrição: 1 mês",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        Button(
                            onClick = {

                                if (isProcessing) {
                                    return@Button
                                }

                                if (isCardPurchase) {
                                    scope.launch {
                                        isProcessing = true
                                        errorMessage = null

                                        try {
                                            // Simulação do pagamento.
                                            delay(1200)

                                            val resultado = criarCartao(
                                                context = context,
                                                tipo = cardType ?: "universal",
                                                parqueId = if (
                                                    cardType == "especifico"
                                                ) {
                                                    parkId
                                                } else {
                                                    null
                                                }
                                            )

                                            resultado.fold(
                                                onSuccess = {
                                                    showSuccessDialog = true
                                                },
                                                onFailure = { erro ->
                                                    errorMessage =
                                                        "Erro ao criar ou renovar cartão: " +
                                                                (erro.message
                                                                    ?: "Erro desconhecido.")

                                                    Toast.makeText(
                                                        context,
                                                        errorMessage,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )

                                        } catch (e: Exception) {
                                            errorMessage =
                                                "Erro no pagamento: ${e.message}"

                                            Toast.makeText(
                                                context,
                                                errorMessage,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } finally {
                                            isProcessing = false
                                        }
                                    }

                                } else {
                                    showSuccessDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = amountEuros > 0.0 &&
                                    !isProcessing &&
                                    !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary
                            )
                        ) {

                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = onPrimary
                                )
                            } else {
                                Text(
                                    text = "Confirmar pagamento",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {

        AlertDialog(
            onDismissRequest = {
                // Impedir fechar o diálogo sem confirmar a navegação.
            },
            title = {
                Text("Pagamento concluído")
            },
            text = {
                Text(
                    if (isCardPurchase) {
                        "O cartão ParkIn foi criado ou renovado com sucesso. A validade da subscrição foi atualizada."
                    } else {
                        "O pagamento do estacionamento foi simulado com sucesso."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false

                        if (isCardPurchase) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("card_created", true)

                            navController.popBackStack()

                        } else {
                            navController.navigate("home") {
                                popUpTo("home") {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}
