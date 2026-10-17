package com.parkin.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.ui.theme.JakartaSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkDetailsScreen(
    navController: NavController,
    parkId: String
) {
    val context = LocalContext.current
    val park = getParkDetails(parkId)

    val primary = MaterialTheme.colorScheme.primary
    val text = MaterialTheme.colorScheme.onSurface
    val secondary = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val AccentBlue = Color(0xFF4169E1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Parques",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
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
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

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
                        color = AccentBlue
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
                            val query = Uri.encode("${park.name}, ${park.location}")
                            val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }

                            try {
                                context.startActivity(mapIntent)
                            } catch (e: ActivityNotFoundException) {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
                                )
                                context.startActivity(webIntent)
                            }
                        }
                    ) {
                        Text(
                            text = "Ver localização",
                            fontFamily = JakartaSans
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                value = "Cartão"
            )
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