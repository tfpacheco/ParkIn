package com.parkin.app.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScreen(navController: NavController) {

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val successColor = Color(0xFF22C55E)
    val errorColor = MaterialTheme.colorScheme.error
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

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val ringScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ringAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    val ringScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ringAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

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
                    Text(
                        text = "Entrada NFC",
                        style = MaterialTheme.typography.titleLarge,
                        color = onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale1)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = ringAlpha1))
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale2)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = ringAlpha2))
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC",
                        tint = onPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Aproxime o dispositivo do leitor",
                style = MaterialTheme.typography.titleMedium,
                color = onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(52.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(surface)
                    .border(1.dp, outline, MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (nfcStatus == NfcStatus.ENABLED) successColor else errorColor
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (nfcStatus) {
                            NfcStatus.ENABLED -> "NFC ativo"
                            NfcStatus.DISABLED -> "NFC desativado"
                            NfcStatus.NOT_SUPPORTED -> "NFC não suportado"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface
                    )
                }
            }
        }
    }
}