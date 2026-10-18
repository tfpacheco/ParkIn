package com.parkin.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.data.Vehicle
import com.parkin.app.data.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

// Função para formatar a matrícula mantendo o cursor estável
fun formatPortuguesePlate(input: TextFieldValue): TextFieldValue {
    val clean = input.text.uppercase().replace(Regex("[^A-Z0-9]"), "")
    val chars = clean.toCharArray()
    val builder = StringBuilder()

    for (i in chars.indices) {
        if (i == 2 || i == 4) {
            builder.append("-")
        }
        if (builder.length < 8) {
            builder.append(chars[i])
        }
    }

    val formattedText = builder.toString()
    // Mantém o cursor sempre no fim do texto inserido para evitar que salte para trás
    return TextFieldValue(
        text = formattedText,
        selection = TextRange(formattedText.length)
    )
}

// Função para validar se o formato da matrícula segue o padrão português
fun isValidPortuguesePlate(plate: String): Boolean {
    val cleanPlate = plate.replace("-", "").uppercase()
    val regex = Regex("^[A-Z]{2}[0-9]{2}[A-Z]{2}$|^[0-9]{2}[A-Z]{2}[0-9]{2}$|^[0-9]{2}[0-9]{2}[A-Z]{2}$|^[A-Z]{2}[0-9]{2}[0-9]{2}$")
    return regex.matches(cleanPlate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    // Usamos TextFieldValue para gerir o texto e a posição do cursor em simultâneo
    var matriculaField by remember { mutableStateOf(TextFieldValue()) }
    var marcaModelo by remember { mutableStateOf("") }
    var combustivel by remember { mutableStateOf("Gasolina") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Adicionar Veículo",
                        fontFamily = JakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    containerColor = Color(0xFF4169E1)
                ),
                modifier = Modifier
                    .height(64.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Insere os dados do teu veículo para associar à tua conta.",
                fontFamily = JakartaSans,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // 1. Campo de Matrícula com TextFieldValue para fixar o cursor
            OutlinedTextField(
                value = matriculaField,
                onValueChange = { novaMatricula ->
                    if (novaMatricula.text.length <= 8) {
                        matriculaField = formatPortuguesePlate(novaMatricula)
                    }
                },
                label = { Text("Matrícula *") },
                placeholder = { Text("AA-00-BB") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Campo Marca e Modelo
            OutlinedTextField(
                value = marcaModelo,
                onValueChange = { marcaModelo = it },
                label = { Text("Marca e Modelo *") },
                placeholder = { Text("Ex: Renault Clio / Tesla Model 3") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Seleção de Combustível
            Text(
                text = "Combustível",
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Gasolina", "Gasóleo", "Híbrido", "Elétrico").forEach { opcao ->
                    FilterChip(
                        selected = combustivel == opcao,
                        onClick = { combustivel = opcao },
                        label = { Text(opcao, fontSize = 12.sp) }
                    )
                }
            }

            if (errorMessage.isNotBlank()) {
                Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Botão Guardar no Supabase
            Button(
                onClick = {
                    val matriculaTexto = matriculaField.text

                    if (matriculaTexto.isBlank() || marcaModelo.isBlank()) {
                        errorMessage = "Por favor preenche a matrícula e a marca/modelo."
                        return@Button
                    }

                    if (!isValidPortuguesePlate(matriculaTexto)) {
                        errorMessage = "Formato de matrícula inválido (ex correto: AA-00-BB)."
                        return@Button
                    }

                    scope.launch {
                        isSaving = true
                        errorMessage = ""

                        try {
                            val userId = supabase.auth.currentUserOrNull()?.id
                            if (userId == null) {
                                errorMessage = "Sessão inválida. Volta a iniciar sessão."
                                return@launch
                            }

                            val novoVeiculo = Vehicle(
                                userId = userId,
                                licensePlate = matriculaTexto,
                                brandModel = marcaModelo,
                                fuelType = combustivel
                            )

                            supabase.from("vehicles").insert(novoVeiculo)

                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = "Erro ao guardar veículo: ${e.localizedMessage}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Guardar Veículo",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}