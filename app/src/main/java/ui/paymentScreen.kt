package com.parkin.app.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PaymentInsert(
    val user_id: String,
    val amount_cents: Int,
    val status: String,
    val parking_session_id: String? = null
)

private fun formatCardNumber(value: TextFieldValue): TextFieldValue {
    val digits = value.text
        .filter { it.isDigit() }
        .take(16)

    val formatted = digits
        .chunked(4)
        .joinToString(" ")

    val cursorPosition = calculateCursorPosition(
        original = value,
        formatted = formatted
    )
    return TextFieldValue(
        text = formatted,
        selection = TextRange(cursorPosition)
    )
}

private fun formatExpiry(input: String): String {
    val digits = input.filter { it.isDigit() }.take(4)
    return when {
        digits.length >= 3 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
        else -> digits
    }
}

private fun calculateCursorPosition(
    original: TextFieldValue,
    formatted: String
): Int {
    val originalCursor = original.selection.start

    val digitsBeforeCursor = original.text
        .take(originalCursor)
        .count { it.isDigit() }

    if (digitsBeforeCursor == 0) {
        return 0
    }

    var digitCount = 0

    for (i in formatted.indices) {
        if (formatted[i].isDigit()) {
            digitCount++

            if (digitCount == digitsBeforeCursor) {
                return i + 1
            }
        }
    }

    return formatted.length
}

private fun isCardValid(
    number: String,
    expiry: String,
    cvc: String
): Boolean {
    val digits = number.filter { it.isDigit() }
    val expDigits = expiry.filter { it.isDigit() }

    return digits.length == 16 &&
            expDigits.length == 4 &&
            cvc.length in 3..4
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    amountCents: Int,
    parkingSessionId: String? = null
) {
    val scope = rememberCoroutineScope()

    var cardNumber by remember { mutableStateOf(TextFieldValue("")) }
    var expiry by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var paymentDone by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline
    val successColor = Color(0xFF22C55E)

    fun processarPagamento() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            errorMessage = "Sem sessão ativa."
            return
        }
        if (!isCardValid(cardNumber.text, expiry, cvc)) {
            errorMessage = "Verifica os dados do cartão."
            return
        }

        errorMessage = null
        isLoading = true

        scope.launch {
            try {
                delay(1000)

                supabase.from("pagamentos").insert(
                    PaymentInsert(
                        user_id = user.id,
                        amount_cents = amountCents,
                        status = "completed",
                        parking_session_id = parkingSessionId
                    )
                )

                isLoading = false
                paymentDone = true
            } catch (e: Exception) {
                Log.e("PaymentScreen", "Erro ao registar pagamento", e)
                isLoading = false
                errorMessage = "Erro ao processar pagamento. Tenta novamente."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pagamento",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary),
                modifier = Modifier
                    .height(60.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 14.dp,
                            bottomEnd = 14.dp
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
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surface, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Valor a pagar",
                        fontFamily = JakartaSans,
                        color = onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "%.2f €".format(amountCents / 100.0),
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (paymentDone) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(successColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Pagamento concluído",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack("home", inclusive = false) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text("Voltar ao início", fontWeight = FontWeight.Bold, color = onPrimary)
                    }
                }
            } else {
                Text(
                    text = "Dados do cartão",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = onSurface,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->
                        cardNumber = formatCardNumber(newValue)
                    },
                    label = { Text("Número do cartão") },
                    placeholder = { Text("0000 0000 0000 0000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = outline,
                        cursorColor = primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = formatExpiry(it) },
                        label = { Text("Validade") },
                        placeholder = { Text("MM/AA") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor = outline,
                            cursorColor = primary
                        )
                    )
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("CVC") },
                        placeholder = { Text("123") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor = outline,
                            cursorColor = primary
                        )
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontFamily = JakartaSans, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { processarPagamento() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = onPrimary, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Pagar agora", fontWeight = FontWeight.Bold, color = onPrimary)
                    }
                }
            }
        }
    }
}