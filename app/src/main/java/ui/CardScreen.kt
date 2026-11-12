package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.parkin.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

@Serializable
private data class VirtualCardData(
    @SerialName("numero_cartao")
    val cardNumber: String,
    val estado: String,
    @SerialName("tipo_cartao")
    val cardType: String,
    @SerialName("parque_id")
    val parkId: String? = null
)

@Serializable
private data class CardParkData(
    val nome: String
)

@Composable
fun CardScreen(
    onNavigateToLogin: () -> Unit,
    onAddCard: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    var card by remember { mutableStateOf<VirtualCardData?>(null) }
    var parkName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadCard() {
        loading = true
        errorMessage = null

        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw Exception("Sessão não encontrada.")

            card = supabase
                .from("cartoes")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<VirtualCardData>()

            parkName = if (card?.parkId != null) {
                supabase
                    .from("parques")
                    .select {
                        filter {
                            eq("id", card!!.parkId!!)
                        }
                    }
                    .decodeSingleOrNull<CardParkData>()
                    ?.nome
            } else {
                null
            }

        } catch (e: Exception) {
            errorMessage = e.message ?: "Erro ao carregar o cartão."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            loading -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator()
                Spacer(Modifier.weight(1f))
            }

            errorMessage != null -> {
                Spacer(Modifier.weight(1f))
                Text(
                    errorMessage ?: "",
                    color = colors.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch { loadCard() }
                }) {
                    Text("Tentar novamente")
                }
                Spacer(Modifier.weight(1f))
            }

            card == null -> {
                Spacer(Modifier.weight(1f))

                Icon(
                    Icons.Default.AddCard,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Ainda não tens um cartão",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Cria o teu cartão digital ParkIn para poderes utilizar nos parques aderentes.",
                    textAlign = TextAlign.Center,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onAddCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar cartão")
                }

                Spacer(Modifier.weight(1f))
            }

            else -> {
                Spacer(Modifier.height(24.dp))

                Text(
                    "O meu cartão ParkIn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1565C0),
                                        Color(0xFF0D47A1),
                                        Color(0xFF082D65)
                                    )
                                )
                            )
                            .padding(22.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "ParkIn",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Column {
                            Text(
                                "CARTÃO DIGITAL",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                card!!.cardNumber.chunked(4).joinToString(" "),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            if (card!!.estado == "ativo") "CARTÃO ATIVO" else card!!.estado.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Detalhes do cartão",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tipo", color = colors.onSurfaceVariant)
                            Text(
                                if (card!!.cardType == "universal")
                                    "Parque Universal"
                                else
                                    "Parque Específico",
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (card!!.cardType == "especifico") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Parque", color = colors.onSurfaceVariant)
                                Text(
                                    parkName ?: "Não identificado",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estado", color = colors.onSurfaceVariant)
                            Text(
                                card!!.estado.replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "A funcionalidade de comunicação NFC com os leitores será ativada numa próxima fase.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
