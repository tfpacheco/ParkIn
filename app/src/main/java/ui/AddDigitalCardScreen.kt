
package com.parkin.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.util.Locale

private val ParkInBlue = Color(0xFF2563EB)
private val ParkInLightBlue = Color(0xFFF1F5FF)

@Serializable
private data class ParkOption(
    val id: String,
    val nome: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(navController: NavController) {

    val colors = MaterialTheme.colorScheme

    var selectedType by remember {
        mutableStateOf("universal")
    }

    var selectedPark by remember {
        mutableStateOf<ParkOption?>(null)
    }

    var parks by remember {
        mutableStateOf<List<ParkOption>>(emptyList())
    }

    var loadingParks by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val price = if (selectedType == "universal") 19.99 else 9.99

    LaunchedEffect(selectedType) {
        if (selectedType == "especifico" && parks.isEmpty()) {
            loadingParks = true
            errorMessage = null

            try {
                parks = supabase
                    .from("parques")
                    .select()
                    .decodeList<ParkOption>()
            } catch (e: Exception) {
                errorMessage =
                    "Não foi possível carregar os parques: ${e.message}"
            } finally {
                loadingParks = false
            }
        }
    }

    Scaffold(
        containerColor = colors.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escolha o seu cartão",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primary,
                    titleContentColor = Color.White
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
        },

        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF111318),
                shape = RoundedCornerShape(
                    topStart = 30.dp,
                    topEnd = 30.dp
                ),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 25.dp,
                            vertical = 18.dp
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF666A73))
                    )

                    Spacer(Modifier.height(21.dp))

                    Text(
                        text = "RESUMO DO CARTÃO",
                        fontFamily = JakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.2.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (selectedType == "universal") {
                                    "Cartão Universal"
                                } else {
                                    "Cartão Específico"
                                },
                                fontFamily = JakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = if (selectedType == "universal") {
                                    "Todos os parques aderentes"
                                } else {
                                    selectedPark?.nome
                                        ?: "Um parque à tua escolha"
                                },
                                fontFamily = JakartaSans,
                                fontSize = 12.sp,
                                color = Color(0xFFB7BBC4)
                            )
                        }

                        Text(
                            text = String.format(
                                Locale("pt", "PT"),
                                "%.2f €",
                                price
                            ),
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(19.dp))

                    Button(
                        onClick = {
                            val entry = navController.currentBackStackEntry

                            entry?.savedStateHandle?.set(
                                "card_type",
                                selectedType
                            )

                            entry?.savedStateHandle?.set(
                                "card_price",
                                price
                            )

                            entry?.savedStateHandle?.set(
                                "park_id",
                                selectedPark?.id
                            )

                            navController.navigate("payment")
                        },
                        enabled = selectedType == "universal" ||
                                selectedPark != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParkInBlue,
                            disabledContainerColor = Color(0xFF454B57)
                        )
                    ) {
                        Text(
                            text = "Continuar para pagamento",
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 25.dp, bottom = 30.dp)
        ) {

            Text(
                text = "O teu cartão",
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 27.sp,
                color = colors.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Escolhe o cartão ideal para as tuas deslocações.",
                fontFamily = JakartaSans,
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CardTypeOption(
                    modifier = Modifier.weight(1f),
                    title = "Universal",
                    description = "Acesso a todos os parques aderentes",
                    selected = selectedType == "universal",
                    onClick = {
                        selectedType = "universal"
                        selectedPark = null
                        errorMessage = null
                    }
                )

                CardTypeOption(
                    modifier = Modifier.weight(1f),
                    title = "Específico",
                    description = "Acesso a um parque à tua escolha",
                    selected = selectedType == "especifico",
                    onClick = {
                        selectedType = "especifico"
                        errorMessage = null
                    }
                )
            }

            if (selectedType == "especifico") {

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Escolhe o teu parque",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "O cartão ficará associado ao parque selecionado.",
                    fontFamily = JakartaSans,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(17.dp))

                when {
                    loadingParks -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.primary
                            )
                        }
                    }

                    parks.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            color = colors.surface,
                            border = BorderStroke(
                                1.dp,
                                colors.outlineVariant
                            )
                        ) {
                            Text(
                                text = "Não existem parques disponíveis.",
                                modifier = Modifier.padding(17.dp),
                                fontFamily = JakartaSans,
                                fontSize = 13.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            parks.forEach { park ->
                                ParkSelectionItem(
                                    park = park,
                                    selected = selectedPark?.id == park.id,
                                    onClick = {
                                        selectedPark = park
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(15.dp))

                Text(
                    text = it,
                    fontFamily = JakartaSans,
                    fontSize = 12.sp,
                    color = colors.error
                )
            }
        }
    }
}

@Composable
private fun CardTypeOption(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .height(205.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                ParkInBlue
            } else {
                colors.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 3.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ParkInLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    color = ParkInBlue,
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            }

            Column {
                Text(
                    text = title,
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = description,
                    fontFamily = JakartaSans,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) ParkInBlue
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selecionado",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(
                                1.5.dp,
                                colors.outline
                            )
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkSelectionItem(
    park: ParkOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = colors.surface,
        border = BorderStroke(
            width = if (selected) 1.7.dp else 1.dp,
            color = if (selected) {
                ParkInBlue
            } else {
                colors.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 15.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ParkInLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    color = ParkInBlue,
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 27.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = park.nome,
                modifier = Modifier.weight(1f),
                fontFamily = JakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.onSurface
            )

            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ParkInBlue
                )
            )
        }
    }
}
