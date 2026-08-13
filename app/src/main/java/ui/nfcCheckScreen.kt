package com.parkin.app.ui
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun NfcCheckScreen(context: Context) {
    var nfcStatus by remember { mutableStateOf(getNfcStatus(context)) }

    when (nfcStatus) {
        NfcStatus.NOT_SUPPORTED -> {
            Text("Este dispositivo não suporta NFC.")
        }
        NfcStatus.DISABLED -> {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("NFC desativado") },
                text = { Text("Ative o NFC nas definições do telemóvel.") },
                confirmButton = {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                    }) {
                        Text("Abrir definições")
                    }
                }
            )
        }
        NfcStatus.ENABLED -> {
        }
    }
}

enum class NfcStatus {
    NOT_SUPPORTED,
    DISABLED,
    ENABLED
}

fun getNfcStatus(context: Context): NfcStatus {
    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    return when {
        nfcAdapter == null -> NfcStatus.NOT_SUPPORTED
        !nfcAdapter.isEnabled -> NfcStatus.DISABLED
        else -> NfcStatus.ENABLED
    }
}