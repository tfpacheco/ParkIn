package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.parkin.app.data.supabase
import com.parkin.app.ui.UserProfile


@Serializable
data class AccessCard(
    @SerialName("numero_cartao")
    val cardNumber: String,
    val estado: String
)

@Serializable
data class UserSubscription(
    @SerialName("plano_id")
    val planId: String,
    val estado: String
)

@Serializable
data class Plan(
    val nome: String
)


sealed class CardState {
    object Loading : CardState()

    data class Success(
        val userName: String,
        val planName: String,
        val cardNumber: String
    ) : CardState()

    data class Error(
        val message: String
    ) : CardState()

    object NoUser : CardState()
}


class CardViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<CardState>(CardState.Loading)

    val uiState: StateFlow<CardState> =
        _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {

            _uiState.value = CardState.Loading

            try {
                val user = supabase.auth.currentUserOrNull()

                if (user == null) {
                    _uiState.value = CardState.NoUser
                    return@launch
                }


                val profile = supabase
                    .from("users")
                    .select(
                        columns = Columns.raw(
                            "id, first_name, last_name"
                        )
                    ) {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingleOrNull<UserProfile>()

                if (profile == null) {
                    _uiState.value = CardState.Error(
                        "Não foi encontrado o perfil do utilizador."
                    )
                    return@launch
                }

                val card = supabase
                    .from("cartoes")
                    .select(
                        columns = Columns.raw(
                            "numero_cartao, estado"
                        )
                    ) {
                        filter {
                            eq("user_id", user.id)
                            eq("estado", "ativo")
                        }
                    }
                    .decodeSingleOrNull<AccessCard>()

                if (card == null) {
                    _uiState.value = CardState.Error(
                        "Não tens um cartão digital ativo."
                    )
                    return@launch
                }

                // Procurar subscrição ativa

                val subscription = supabase
                    .from("subscricoes")
                    .select(
                        columns = Columns.raw(
                            "plano_id, estado"
                        )
                    ) {
                        filter {
                            eq("user_id", user.id)
                            eq("estado", "ativa")
                        }
                    }
                    .decodeSingleOrNull<UserSubscription>()

                if (subscription == null) {
                    _uiState.value = CardState.Error(
                        "Não tens uma subscrição ativa."
                    )
                    return@launch
                }

                // Procurar nome do plano

                val plan = supabase
                    .from("planos")
                    .select(
                        columns = Columns.raw("nome")
                    ) {
                        filter {
                            eq("id", subscription.planId)
                        }
                    }
                    .decodeSingleOrNull<Plan>()

                if (plan == null) {
                    _uiState.value = CardState.Error(
                        "Não foi possível encontrar o teu plano."
                    )
                    return@launch
                }

                _uiState.value = CardState.Success(
                    userName = "${profile.firstName} ${profile.lastName}",
                    planName = plan.nome,
                    cardNumber = card.cardNumber
                )

            } catch (e: Exception) {
                _uiState.value = CardState.Error(
                    "Erro ao carregar cartão: ${e.localizedMessage}"
                )
            }
        }
    }
}

// ECRÃ PRINCIPAL

@Composable
fun CardScreen(
    viewModel: CardViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {

            is CardState.Loading -> {
                CircularProgressIndicator()
            }

            is CardState.Success -> {
                ParkInCardContent(
                    userName = state.userName,
                    planName = state.planName,
                    cardNumber = state.cardNumber
                )
            }

            is CardState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.loadUserData()
                        }
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }

            is CardState.NoUser -> {
                LaunchedEffect(Unit) {
                    onNavigateToLogin()
                }

                Text("Utilizador não autenticado")
            }
        }
    }
}

// CARTÃO VISUAL PARKIN

@Composable
fun ParkInCardContent(
    userName: String,
    planName: String,
    cardNumber: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1D52BA),
                            Color(0xFF102A6B)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ParkIn",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                Color.White.copy(alpha = 0.15f)
                            )
                            .padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            )
                    ) {
                        Text(
                            text = planName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = "Cartão digital",
                        tint = Color.White,
                        modifier = Modifier.size(58.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "Cartão Digital ParkIn",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Column {
                    Text(
                        text = "TITULAR",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = userName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = cardNumber,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5F9))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Contactless,
                contentDescription = "NFC",
                tint = Color(0xFF1D52BA),
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column {
                Text(
                    text = "Acesso sem contacto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Aproxime o telemóvel da coluna de acesso",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
