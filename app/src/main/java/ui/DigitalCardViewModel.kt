package com.parkin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.QrCode2
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
import kotlinx.serialization.Serializable
import com.parkin.app.data.supabase


@Serializable
data class userProfile(
    val id: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class UserPlan(
    val planName: String,
    val cardNumber: String,
    val isActive: Boolean
)

sealed class DigitalCardState {
    object Loading : DigitalCardState()
    data class Success(
        val userName: String,
        val planName: String,
        val cardNumber: String,
        val isNfcEnabled: Boolean = true
    ) : DigitalCardState()
    data class Error(val message: String) : DigitalCardState()
    object NoUser : DigitalCardState()
}


class DigitalCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DigitalCardState>(DigitalCardState.Loading)
    val uiState: StateFlow<DigitalCardState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = DigitalCardState.Loading
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) {
                    _uiState.value = DigitalCardState.NoUser
                    return@launch
                }

                val profile = supabase.from("profiles")
                    .select(columns = Columns.raw("id, first_name, last_name")) {
                        filter { eq("id", user.id) }
                    }
                    .decodeSingleOrNull<userProfile>()

                val plan = supabase.from("user_plans")
                    .select(columns = Columns.raw("plan_name, card_number, is_active")) {
                        filter {
                            eq("user_id", user.id)
                            eq("is_active", true)
                        }
                    }
                    .decodeSingleOrNull<UserPlan>()

                if (profile != null && plan != null) {
                    _uiState.value = DigitalCardState.Success(
                        userName = "${profile.firstName} ${profile.lastName}",
                        planName = plan.planName,
                        cardNumber = plan.cardNumber
                    )
                } else {
                    _uiState.value = DigitalCardState.Error("Dados do utilizador incompletos.")
                }

            } catch (e: Exception) {
                _uiState.value = DigitalCardState.Error("Erro ao carregar dados: ${e.localizedMessage}")
            }

        }

    }

    fun toggleAccessMode() {
        val currentState = _uiState.value
        if (currentState is DigitalCardState.Success) {
            _uiState.value = currentState.copy(isNfcEnabled = !currentState.isNfcEnabled)
        }
    }
}
@Composable
fun DigitalCardScreen(
    viewModel: DigitalCardViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DigitalCardState.Loading -> {
                CircularProgressIndicator()
            }
            is DigitalCardState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "O Teu Cartão ParkIn",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    DigitalCardContent(
                        userName = state.userName,
                        planName = state.planName,
                        cardNumber = state.cardNumber,
                        isNfcEnabled = state.isNfcEnabled,
                        onToggleAccessMode = { viewModel.toggleAccessMode() }
                    )
                }
            }
            is DigitalCardState.Error -> {
                Text(text = "Erro: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
            is DigitalCardState.NoUser -> {
                onNavigateToLogin()
            }
        }
    }
}

// --- 4. COMPONENTE VISUAL DO CARTÃO ---
@Composable
fun DigitalCardContent(
    userName: String,
    planName: String,
    cardNumber: String,
    isNfcEnabled: Boolean,
    onToggleAccessMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ParkIn",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = planName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isNfcEnabled) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contactless,
                                    contentDescription = "NFC Ativo",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aproxime da coluna de acesso",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code Access",
                                    tint = Color.Black,
                                    modifier = Modifier.size(58.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Apresente no leitor do parque",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
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
                    }

                    Text(
                        text = cardNumber,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isNfcEnabled) Icons.Default.Contactless else Icons.Default.QrCode2,
                        contentDescription = "Tipo de Acesso",
                        tint = Color(0xFF1D52BA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isNfcEnabled) "Modo NFC (Sem contacto)" else "Modo QR Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = if (isNfcEnabled) "Aproxime o telemóvel da cancela" else "Mostre o ecrã ao leitor ótico",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Switch(
                    checked = isNfcEnabled,
                    onCheckedChange = { onToggleAccessMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1D52BA),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF64748B)
                    )
                )
            }
        }
    }
}