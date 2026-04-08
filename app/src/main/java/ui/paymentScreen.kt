package com.parkin.app.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import androidx.navigation.NavController

@SuppressLint("ContextCastToActivity")
@Composable
fun PaymentFlow(navController: NavController, amount: Double) {
    val context = LocalContext.current as ComponentActivity

    val paymentSheet = remember {
        PaymentSheet(context) { paymentResult ->
            when (paymentResult) {
                is PaymentSheetResult.Completed -> {
                    // TODO: Chamar função do Supabase para marcar como pago
                    navController.navigate("success_screen")
                }
                is PaymentSheetResult.Failed -> {
                    // TODO: Mostrar um Snackbar de erro
                    println("Erro no pagamento: ${paymentResult.error}")
                }
                is PaymentSheetResult.Canceled -> {
                    println("Pagamento cancelado pelo utilizador")
                }
            }
        }
    }


    fun presentPaymentSheet(customerConfig: PaymentSheet.CustomerConfiguration, paymentIntentClientSecret: String) {
        val configuration = PaymentSheet.Configuration(
            merchantDisplayName = "ParkIn App",
            customer = customerConfig,
            allowsDelayedPaymentMethods = true
        )
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
    }
}