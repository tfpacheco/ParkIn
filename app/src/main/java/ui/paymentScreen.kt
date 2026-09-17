package com.parkin.app.ui

import android.R.id.primary
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

enum class PaymentMethod { MBWAY, CARD }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    amountCents: Int


) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val onSurface = MaterialTheme.colorScheme.onSurface


    var selectedMethod by remember { mutableStateOf(PaymentMethod.MBWAY) }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var stepMessage by remember { mutableStateOf("") }
    var valorEuros = ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pagamentos", fontFamily = JakartaSans, fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar",  tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary),
                    modifier = Modifier
                        .height(70.dp)
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 14.dp,
                                bottomEnd = 14.dp,
                            )
                        )
                )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Valor do Estacionamento",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${valorEuros} €",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = JakartaSans
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Método de Pagamento",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = JakartaSans
                )

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentMethodSelector(
                        title = "MB WAY",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedMethod == PaymentMethod.MBWAY,
                        onClick = { selectedMethod = PaymentMethod.MBWAY },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodSelector(
                        title = "Cartão",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedMethod == PaymentMethod.CARD,
                        onClick = { selectedMethod = PaymentMethod.CARD },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = selectedMethod == PaymentMethod.MBWAY) {
                    Column {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 9) phoneNumber = it },
                            label = { Text("Número de Telemóvel MB WAY") },
                            placeholder = { Text("xxxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = selectedMethod == PaymentMethod.CARD) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "O pagamento por cartão será processado em ambiente seguro.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = JakartaSans
                        )
                    }
                }

                if (stepMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stepMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = JakartaSans
                    )
                }
            }

            val isButtonEnabled = !isLoading && (selectedMethod == PaymentMethod.CARD || phoneNumber.length == 9)

            Button(
                enabled = isButtonEnabled,
                onClick = {
                    scope.launch {
                        isLoading = true
                        val user = supabase.auth.currentUserOrNull()

                        if (user != null) {
                            try {
                                if (selectedMethod == PaymentMethod.MBWAY) {
                                    stepMessage = "A enviar notificação para a App MB WAY..."
                                    delay(1500)
                                    stepMessage = "A aguardar confirmação..."
                                    delay(1500)
                                } else {
                                    stepMessage = "A validar dados do cartão..."
                                    delay(2000)
                                }

                                val valorDouble = amountCents

                                val novoPagamento = mapOf(
                                    "user_id" to user.id,
                                    "valor" to valorDouble,
                                    "metodo" to selectedMethod.name,
                                    "estado" to "concluido"
                                )
                                supabase.from("pagamentos").insert(novoPagamento)


                                val updateSessao = mapOf(
                                    "hora_saida" to Instant.now().toString(),
                                    "valor_pago" to valorDouble,
                                    "estado" to "finalizada"
                                )
                                supabase.from("sessoes").update(updateSessao) {
                                    filter {
                                        eq("user_id", user.id)
                                        eq("estado", "ativa")
                                    }
                                }

                                Toast.makeText(context, "Pagamento concluído com sucesso!", Toast.LENGTH_LONG).show()

                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }

                            } catch (e: Exception) {
                                android.util.Log.e("PaymentError", "Erro ao processar", e)
                                Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                                stepMessage = ""
                            }
                        } else {
                            Toast.makeText(context, "Sessão expirada.", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (selectedMethod == PaymentMethod.MBWAY && phoneNumber.length < 9) "Insere o número MB WAY" else "Pagar ${valorEuros} €",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = JakartaSans
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelector(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontFamily = JakartaSans
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}