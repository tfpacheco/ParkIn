package com.parkin.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
data class UserProfile(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("nfc_uid") val nfcUid: String? = null,
    val role: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var nomeUsuario by remember { mutableStateOf("") }
    var userRole: String? by remember { mutableStateOf("user") }

    val context = LocalContext.current
    var nfcStatus by remember { mutableStateOf(getNfcStatus(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                nfcStatus = getNfcStatus(context)
            }
        }
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                nfcStatus = getNfcStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val user = supabase.auth.currentUserOrNull()

    LaunchedEffect(user) {
        if (user != null) {
            val firstName = user.userMetadata?.get("first_name")?.toString()?.replace("\"", "") ?: ""
            val lastName = user.userMetadata?.get("last_name")?.toString()?.replace("\"", "") ?: ""
            nomeUsuario = "$firstName $lastName".trim()

            try {
                val profile = supabase.from("users")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<UserProfile>()
                userRole = profile?.role ?: "user"
            } catch (e: Exception) {
                userRole = "user"
            }
        }
    }

    if (nfcStatus == NfcStatus.DISABLED) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("NFC desativado") },
            text = { Text("Para usar o ParkIn, ativa o NFC nas definições do telemóvel.") },
            confirmButton = {
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }) {
                    Text("Abrir definições")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Bem-vindo de volta,",
                            fontFamily = JakartaSans,
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Text(
                            text = nomeUsuario.ifEmpty { "Utilizador" },
                            fontFamily = JakartaSans,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1),
                ),
                modifier = Modifier
                    .height(80.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 15.dp,
                            bottomEnd = 15.dp,
                        )
                    ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightBg)
                .padding(20.dp)
        ) {

            ActiveSessionCard()

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Acesso rápido",
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF212529),
                modifier = Modifier.padding(bottom = 15.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    ShortcutCard(
                        title = "Entrar/Sair",
                        icon = Icons.Default.Nfc,
                        color = Color(0xFF1D52BA),
                        onClick = { navController.navigate("nfc") },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutCard(
                        title = "Histórico",
                        icon = Icons.Default.History,
                        color = Color(0xFF1D52BA),
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShortcutCard(
                        title = "Pagamentos",
                        icon = Icons.Default.Payment,
                        color = Color(0xFF1D52BA),
                        onClick = { navController.navigate("payments") },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutCard(
                        title = "Perfil",
                        icon = Icons.Default.Person,
                        color = Color(0xFF1D52BA),
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (userRole == "admin") {
                    ShortcutCard(
                        title = "Painel Admin",
                        icon = Icons.Default.AdminPanelSettings,
                        color = Color(0xFF1D52BA),
                        onClick = { navController.navigate("admin_panel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderGray, RoundedCornerShape(20.dp))
            .clickable { onClick() }
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

@Composable
fun ActiveSessionCard() {
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
                SessionInfoItem("Entrada", "--:--")
                SessionInfoItem("Duração", "0m")
                SessionInfoItem("Valor", "0,00€")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1))
            ) {
                Text("Pagar e sair", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SessionInfoItem(label: String, value: String, valueColor: Color = Color(0xFF212529)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 11.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}