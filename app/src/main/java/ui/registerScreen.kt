package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkin.app.R
import com.parkin.app.supabase
import com.parkin.app.ui.theme.BorderGray
import com.parkin.app.ui.theme.JakartaSans
import com.parkin.app.ui.theme.LightBg
import com.parkin.app.ui.theme.SurfaceWhite
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.parkin_logo_f),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp),
        )
        Text(
            text = "ParkIn",
            fontSize = 28.sp,
            fontFamily = JakartaSans,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D52BA),
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Chegue. Estacione. Saia.",
            fontSize = 14.sp,
            fontFamily = JakartaSans,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6C757D),
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceWhite)
                .border(2.dp, BorderGray, RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Nome", fontSize = 15.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4169E1),
                            unfocusedBorderColor = Color(0xFF4169E1),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
                            cursorColor = Color.Black,
                        ),
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Apelido", fontSize = 15.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4169E1),
                            unfocusedBorderColor = Color(0xFF4169E1),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
                            cursorColor = Color.Black,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        cursorColor = Color.Black,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Palavra-Passe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF444444),
                        unfocusedBorderColor = Color(0xFF4169E1),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
                        cursorColor = Color.Black,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordConfirmation,
                    onValueChange = { passwordConfirmation = it },
                    label = { Text("Confirme a Palavra-Passe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
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
                        cursorColor = Color.Black,
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (firstName.isEmpty() || lastName.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty()) {
                            passwordConfirmation.isEmpty()
                            mensagem = "Preencha todos os campos"
                            return@Button
                        }
                        if (passwordInput != passwordConfirmation) {
                            mensagem = "As passwords não coincidem!"
                            return@Button
                        }
                        scope.launch {
                            try {
                                supabase.auth.signUpWith(Email) {
                                    email = emailInput
                                    password = passwordInput
                                    data = buildJsonObject {
                                        put("first_name", firstName)
                                        put("last_name", lastName)
                                    }
                                }
                                mensagem = "Registo efetuado! Verifica o teu email."
                                kotlinx.coroutines.delay(2000)
                                onNavigateBack()
                            } catch (e: Exception) {
                                mensagem = "Erro: ${e.localizedMessage}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1)),
                ) {
                    Text("Registar", fontFamily = JakartaSans, fontWeight = FontWeight.Bold)
                }

                if (mensagem.isNotEmpty()) {
                    Text(
                        text = mensagem,
                        color = if (mensagem.startsWith("Registo")) Color(0xFF4169E1) else Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                TextButton(onClick = { onNavigateBack() }) {
                    Row {
                        Text(text = "Já tens conta? ", color = Color(0xFF64748B))
                        Text(
                            text = "Faz Login",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4169E1),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}