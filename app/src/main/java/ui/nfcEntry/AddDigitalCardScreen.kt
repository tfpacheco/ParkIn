package com.parkin.app.ui.nfcEntry

import android.nfc.NfcAdapter
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddDigitalCardScreen(
    onCardCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tipo by remember { mutableStateOf("pay_per_use") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Criar cartão digital",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        listOf(
            "pay_per_use" to "Pagar por utilização",
            "mensal" to "Passe mensal"
        ).forEach { (valor, titulo) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .selectable(
                        selected = tipo == valor,
                        enabled = !loading,
                        onClick = { tipo = valor }
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (tipo == valor)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = tipo == valor, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(titulo)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            onClick = {
                val nfc = NfcAdapter.getDefaultAdapter(context)
                if (nfc == null || !nfc.isEnabled) {
                    Toast.makeText(
                        context,
                        "Ativa o NFC nas definições do telemóvel",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                scope.launch {
                    loading = true
                    delay(1500)

                    criarCartao(context = context, tipo = tipo)
                        .onSuccess { onCardCreated() }
                        .onFailure {
                            Toast.makeText(
                                context,
                                "Erro: ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    loading = false
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Pagar e criar cartão")
            }
        }
    }
}