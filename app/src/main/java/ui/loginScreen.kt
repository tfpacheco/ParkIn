package com.parkin.app.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkin.app.R
import com.parkin.app.supabase
import com.parkin.app.ui.theme.LightBg
import com.parkin.app.ui.theme.BorderGray
import com.parkin.app.ui.theme.JakartaSans
import com.parkin.app.ui.theme.SurfaceWhite
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Image(
            painter = painterResource(id = R.drawable.parkin_logo_f),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "ParkIn",
            fontSize = 28.sp,
            fontFamily = JakartaSans,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D52BA)
        )

        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Chegue. Estacione. Saia.",
            fontSize = 14.sp,
            fontFamily = JakartaSans,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6C757D)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceWhite)
                .border(2.dp, BorderGray, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        unfocusedBorderColor = Color(0xFF4169E1),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Palavra-Passe") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4169E1),
                        unfocusedBorderColor = Color(0xFF4169E1),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                            mensagem = "Preencha todos os campos"
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            try {
                                supabase.auth.signInWith(Email) {
                                    email = emailInput
                                    password = passwordInput
                                }
                                val sharedPref = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                                sharedPref.edit().apply {
                                    putBoolean("is_logged_in", true)
                                    putString("user_email", emailInput)
                                    apply()
                                }
                                isLoading = false
                                onNavigateToHome()
                            } catch (e: Exception) {
                                isLoading = false
                                mensagem = "Erro: Dados inválidos"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Entrar", fontFamily = JakartaSans, fontWeight = FontWeight.Bold)
                    }
                }

                if (mensagem.isNotEmpty()) {
                    Text(
                        text = mensagem,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                TextButton(onClick = { onNavigateToRegister() }) {
                    Text(text = "Não tens conta? ", color = Color(0xFF64748B))
                    Text(
                        text = "Regista-te",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4169E1),
                    )
                }
            }
        }
    }
}