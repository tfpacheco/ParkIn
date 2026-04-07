package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.supabase
import com.parkin.app.ui.theme.BorderGray
import com.parkin.app.ui.theme.JakartaSans
import com.parkin.app.ui.theme.LightBg
import com.parkin.app.ui.theme.SurfaceWhite
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("nfc_uid") val nfcUid: String? = null,
    val role: String = "user"
)

@Composable
fun ShortcutCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF212529)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var nomeUsuario by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            try {
                val profile = supabase
                    .from("profiles")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<Profile>()
                nomeUsuario = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim()
                if (nomeUsuario.isEmpty()) nomeUsuario = "Utilizador"
            } catch (e: Exception) {
                nomeUsuario = "Utilizador"
            }
        } else {
            nomeUsuario = "Sem sessão"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bem-vindo de volta,",
                            fontFamily = JakartaSans,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = nomeUsuario,
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E56A0)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightBg)
                .padding(16.dp)
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Sessão Ativa",
                        color = Color(0xFF212529),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JakartaSans,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Entrada",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "--:--",
                                color = Color(0xFF212529),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Duração",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "0m",
                                color = Color(0xFF212529),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Valor",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "0,00€",
                                color = Color(0xFF1E56A0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E56A0)
                        )
                    ) {
                        Text(
                            text = "Pagar e sair",
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Acesso rápido",
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF212529),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Linha 1 — NFC e Histórico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutCard(
                    title = "Entrada NFC",
                    icon = Icons.Default.Nfc,
                    color = Color(0xFF1E56A0),
                    onClick = { navController.navigate("nfc") },
                    modifier = Modifier.weight(1f)
                )
                ShortcutCard(
                    title = "Histórico",
                    icon = Icons.Default.History,
                    color = Color(0xFF1E56A0),
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutCard(
                    title = "Pagamentos",
                    icon = Icons.Default.Payment,
                    color = Color(0xFF1E56A0),
                    onClick = { navController.navigate("payments") },
                    modifier = Modifier.weight(1f)
                )
                ShortcutCard(
                    title = "Perfil",
                    icon = Icons.Default.Person,
                    color = Color(0xFF1E56A0),
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}