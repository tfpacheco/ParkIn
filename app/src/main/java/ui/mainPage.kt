package com.parkin.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.services.ParkingNotificationService
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    val context = LocalContext.current

    ListenNfcParking()

    var userRole by remember { mutableStateOf("user") }
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
            onDismissRequest = {},
            title = { Text("NFC desativado", fontFamily = JakartaSans, fontWeight = FontWeight.Bold) },
            text = { Text("Para usar o ParkIn, tens de ligar o NFC nas definições.", fontFamily = JakartaSans) },
            confirmButton = {
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }) {
                    Text("Ir para as definições", fontFamily = JakartaSans, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = { ParkInTopAppBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActiveSessionCard(
                navController = navController,
                amountCents = 250,
                hasActiveSession = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acesso rápido",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShortcutCard(
                        title = "Entrar/Sair",
                        subtitle = null,
                        icon = Icons.Default.DirectionsCar,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("nfc") },
                        modifier = Modifier.weight(1f)
                    )

                    ShortcutCard(
                        title = "Histórico",
                        subtitle = null,
                        icon = Icons.Default.History,
                        color = MaterialTheme.colorScheme.primary,
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
                        subtitle = null,
                        icon = Icons.Default.CreditCard,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("payment") },
                        modifier = Modifier.weight(1f)
                    )

                    ShortcutCard(
                        title = "Perfil",
                        subtitle = null,
                        icon = Icons.Default.Person,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShortcutCard(
                        title = "Parques",
                        subtitle = null,
                        icon = Icons.Default.LocalParking,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("parks") },
                        modifier = Modifier.width(160.dp)
                    )
                }

                if (userRole == "admin") {
                    Spacer(modifier = Modifier.height(4.dp))
                    ShortcutCard(
                        title = "Painel Admin",
                        subtitle = null,
                        icon = Icons.Default.AdminPanelSettings,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("admin_panel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkInTopAppBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color(0xFF2563EB),
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ParkIn",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
    )
}

@Composable
fun ActiveSessionCard(
    navController: NavController,
    amountCents: Int,
    hasActiveSession: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (hasActiveSession) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Estacionamento ativo", fontFamily = JakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface)
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SessionInfoItem(label = "Entrada", value = "21:42")
                    SessionInfoItem(label = "Duração", value = "38 min")
                    SessionInfoItem(label = "Valor", value = "1,20 €")
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { navController.navigate("payment") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Pagar e sair", fontFamily = JakartaSans, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(onSurface.copy(alpha = 0.25f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nenhuma sessão ativa", fontFamily = JakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = onSurface)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ainda não estás estacionado.", fontFamily = JakartaSans, fontSize = 12.sp, color = onSurface.copy(alpha = 0.50f))
            }
        }
    }
}

@Composable
fun SessionInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = JakartaSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = JakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun ShortcutCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .height(105.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
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
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.09f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = onSurface.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, fontFamily = JakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = onSurface)
                if (subtitle != null) {
                    Text(subtitle, fontFamily = JakartaSans, fontSize = 10.sp, color = onSurface.copy(alpha = 0.50f))
                }
            }
        }
    }
}

@Composable
fun ListenNfcParking() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = supabase.auth.currentUserOrNull()?.id

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        try {
            val channel = supabase.realtime.channel("active_parking_channel_$userId")

            supabase.realtime.connect()
            channel.subscribe()

        } catch (e: Exception) {

        }
    }
}