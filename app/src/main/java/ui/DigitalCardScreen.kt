package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    object NoCard : CardState()
    object NoUser : CardState()

    data class Error(
        val message: String
    ) : CardState()
}



class CardViewModel : ViewModel() {

    private val _cardState =
        MutableStateFlow<CardState>(CardState.Loading)

    val cardState: StateFlow<CardState> =
        _cardState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _cardState.value = CardState.Loading

            try {
                val user = supabase.auth.currentUserOrNull()

                if (user == null) {
                    _cardState.value = CardState.NoUser
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
                    _cardState.value = CardState.Error(
                        "Não foi possível encontrar o perfil do utilizador."
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


                if (card == null) { _cardState.value = CardState.NoCard
                    return@launch
                }


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
                    _cardState.value = CardState.Error(
                        "Não tens uma subscrição ativa associada ao cartão."
                    )
                    return@launch
                }

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
                    _cardState.value = CardState.Error(
                        "Não foi possível encontrar o plano associado ao cartão."
                    )
                    return@launch
                }

                _cardState.value = CardState.Success(
                    userName = "${profile.firstName} ${profile.lastName}",
                    planName = plan.nome,
                    cardNumber = card.cardNumber
                )

            } catch (e: Exception) {
                _cardState.value = CardState.Error(
                    e.message ?: "Ocorreu um erro ao carregar o cartão."
                )
            }
        }
    }
}


@Composable
fun CardScreen(
    viewModel: CardViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onAddCard: () -> Unit
) {

    val primary = MaterialTheme.colorScheme.primary
    val state by viewModel.cardState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {

            is CardState.Loading -> {
                CircularProgressIndicator(
                    color = primary
                )
            }

            is CardState.NoUser -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sessão não iniciada")
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = onNavigateToLogin) {
                        Text("Iniciar sessão")
                    }
                }
            }

            is CardState.NoCard -> {
                NoCardContent(
                    onAddCard = onAddCard
                )
            }

            is CardState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentState.message,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFB3261E)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.loadUserData() }
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }

            is CardState.Success -> {
                ParkInCardContent(
                    userName = currentState.userName,
                    planName = currentState.planName,
                    cardNumber = currentState.cardNumber
                )
            }
        }
    }
}



@Composable
fun NoCardContent(
    onAddCard: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = "Sem cartão",
            modifier = Modifier.size(76.dp),
            tint = primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ainda não tens um cartão",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Adiciona um cartão ParkIn para começares a utilizar os serviços da aplicação.",
            fontSize = 15.sp,
            color = Color(0xFF687783),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onAddCard,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary
            )
        ) {
            Text(
                text = "Adicionar cartão",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun ParkInCardContent(
    userName: String,
    planName: String,
    cardNumber: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "O meu cartão",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF172B3A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF176B87),
                                Color(0xFF123B55)
                            )
                        )
                    )
                    .padding(22.dp)
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
                        Text(
                            text = "ParkIn",
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.Default.Contactless,
                            contentDescription = "NFC",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = planName,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = cardNumber,
                            color = Color.White,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Cartão ativo",
            color = Color(0xFF24845A),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}
