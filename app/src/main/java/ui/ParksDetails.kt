package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.ui.theme.JakartaSans

@Composable
fun ParkDetailsScreen(
    navController: NavController,
    parkId: String
) {
    // Simulação de busca dos dados do parque com base no ID recebido
    val park = getParkDetails(parkId)

    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val text = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.secondary

    Scaffold(
        bottomBar = {
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 16.dp
                    )
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estacionar aqui",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
        ) {
            // ==========================================
            // TOP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .clickable {
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = text
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Parque",
                    fontFamily = JakartaSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        // Favoritar parque
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = text
                    )
                }
            }

            // ==========================================
            // CONTENT
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ======================================
                // PARK HEADER
                // ======================================
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = park.name,
                            fontFamily = JakartaSans,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = text
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = secondary
                            )

                            Spacer(modifier = Modifier.width(3.dp))

                            Text(
                                text = park.location,
                                fontFamily = JakartaSans,
                                fontSize = 13.sp,
                                color = secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ======================================
                // STATUS
                // ======================================
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "Aberto",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF16A34A)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "· 24h",
                        fontFamily = JakartaSans,
                        fontSize = 13.sp,
                        color = secondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ======================================
                // PRICE / CAPACITY
                // ======================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParkInfoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Euro,
                        value = park.price,
                        label = "Preço"
                    )

                    ParkInfoBox(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DirectionsCar,
                        value = park.capacity,
                        label = "Lugares"
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ======================================
                // MAP
                // ======================================
                Text(
                    text = "Localização",
                    fontFamily = JakartaSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(primary.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = park.island,
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                // Abrir Google Maps
                            }
                        ) {
                            Text(
                                text = "Ver localização",
                                fontFamily = JakartaSans
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ======================================
                // DETAILS
                // ======================================
                Text(
                    text = "Detalhes",
                    fontFamily = JakartaSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = text
                )

                Spacer(modifier = Modifier.height(12.dp))

                ParkDetailRow(
                    icon = Icons.Default.LocalParking,
                    title = "Tipo",
                    value = "Estacionamento público"
                )

                ParkDetailRow(
                    icon = Icons.Default.Schedule,
                    title = "Horário",
                    value = "Aberto 24 horas"
                )

                ParkDetailRow(
                    icon = Icons.Default.CreditCard,
                    title = "Pagamento",
                    value = "Cartão · MB Way · App ParkIn"
                )

                ParkDetailRow(
                    icon = Icons.Default.LocationOn,
                    title = "Morada",
                    value = park.location
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

data class ParkDetails(
    val name: String,
    val location: String,
    val island: String,
    val price: String,
    val capacity: String
)


fun getParkDetails(id: String): ParkDetails {
    return when (id) {
        "1" -> ParkDetails("Parque da Avenida", "Av. Infante D. Henrique, Ponta Delgada", "São Miguel", "0,80 € / h", "120")
        "2" -> ParkDetails("Parque do Castilho", "Rua do Castilho, Ponta Delgada", "São Miguel", "0,70 € / h", "85")
        "3" -> ParkDetails("Parque da Rua do Peru", "São Pedro, Ponta Delgada", "São Miguel", "0,50 € / h", "50")
        "4" -> ParkDetails("Parque Mercado da Graça", "Ponta Delgada", "São Miguel", "0,60 € / h", "90")
        "6" -> ParkDetails("Parque Vila do Porto", "Vila do Porto", "Santa Maria", "Gratuito", "40")
        "7" -> ParkDetails("Parque de Estacionamento de Angra", "Angra do Heroísmo", "Terceira", "0,80 € / h", "150")
        "8" -> ParkDetails("Parque da Marina", "Praia da Vitória", "Terceira", "0,70 € / h", "100")
        "9" -> ParkDetails("Parque de Estacionamento das Velas", "Velas", "São Jorge", "Gratuito", "60")
        "10" -> ParkDetails("Parque da Calheta", "Calheta", "São Jorge", "Gratuito", "45")
        "11" -> ParkDetails("Parque de Estacionamento da Madalena", "Madalena", "Pico", "Gratuito", "80")
        "12" -> ParkDetails("Parque das Lajes do Pico", "Lajes do Pico", "Pico", "Gratuito", "50")
        "13" -> ParkDetails("Parque de Estacionamento da Horta", "Horta (Marina)", "Faial", "0,75 € / h", "110")
        "14" -> ParkDetails("Parque de Santa Cruz da Graciosa", "Santa Cruz da Graciosa", "Graciosa", "Gratuito", "30")
        "15" -> ParkDetails("Parque de Santa Cruz das Flores", "Santa Cruz das Flores", "Flores", "Gratuito", "40")
        "16" -> ParkDetails("Parque de Lajes das Flores", "Lajes das Flores", "Flores", "Gratuito", "35")
        "17" -> ParkDetails("Zona de Estacionamento Livre", "Vila do Corvo", "Corvo", "Gratuito", "20")
        else -> ParkDetails("Parque Desconhecido", "Açores", "Açores", "0,00 € / h", "0")
    }
}


@Composable
fun ParkInfoBox(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontFamily = JakartaSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                fontFamily = JakartaSans,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ParkDetailRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontFamily = JakartaSans,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = value,
                fontFamily = JakartaSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}