package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.ui.theme.JakartaSans

data class Park(
    val id: Int,
    val name: String,
    val location: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParksScreen(navController: NavController) {

    var selectedIsland by remember {
        mutableStateOf("São Miguel")
    }

    val islands = listOf(
        "São Miguel", "Santa Maria", "Terceira",
        "São Jorge", "Pico", "Faial",
        "Graciosa", "Flores", "Corvo"
    )

    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val background = MaterialTheme.colorScheme.background

    val parksSaoMiguel = listOf(
        Park(1, "Parque da Avenida", "Av. Infante D. Henrique, Ponta Delgada"),
        Park(2, "Parque do Castilho", "Rua do Castilho, Ponta Delgada"),
        Park(3, "Parque da Rua do Peru", "São Pedro, Ponta Delgada"),
        Park(4, "Parque Mercado da Graça", "Ponta Delgada"),
    )

    val parksSantaMaria = listOf(
        Park(6, "Parque Vila do Porto", "Vila do Porto")
    )

    val parksTerceira = listOf(
        Park(7, "Parque de Estacionamento de Angra", "Angra do Heroísmo"),
        Park(8, "Parque da Marina", "Praia da Vitória")
    )

    val parksSaoJorge = listOf(
        Park(9, "Parque de Estacionamento das Velas", "Velas"),
        Park(10, "Parque da Calheta", "Calheta")
    )

    val parksPico = listOf(
        Park(11, "Parque de Estacionamento da Madalena", "Madalena"),
        Park(12, "Parque das Lajes do Pico", "Lajes do Pico")
    )

    val parksFaial = listOf(
        Park(13, "Parque de Estacionamento da Horta", "Horta (Marina)")
    )

    val parksGraciosa = listOf(
        Park(14, "Parque de Santa Cruz da Graciosa", "Santa Cruz da Graciosa")
    )

    val parksFlores = listOf(
        Park(15, "Parque de Santa Cruz das Flores", "Santa Cruz das Flores"),
        Park(16, "Parque de Lajes das Flores", "Lajes das Flores")
    )

    val parksCorvo = listOf(
        Park(17, "Zona de Estacionamento Livre", "Vila do Corvo")
    )

    val currentParks = when (selectedIsland) {
        "São Miguel" -> parksSaoMiguel
        "Santa Maria" -> parksSantaMaria
        "Terceira" -> parksTerceira
        "São Jorge" -> parksSaoJorge
        "Pico" -> parksPico
        "Faial" -> parksFaial
        "Graciosa" -> parksGraciosa
        "Flores" -> parksFlores
        "Corvo" -> parksCorvo
        else -> emptyList()
    }

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary
                ),
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
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))




            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                islands.forEach { island ->
                    IslandChip(
                        name = island,
                        selected = selectedIsland == island,
                        onClick = { selectedIsland = island }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = selectedIsland,
                fontFamily = JakartaSans,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (currentParks.isEmpty()) {
                Text(
                    text = "Não existem parques registados para esta ilha.",
                    fontFamily = JakartaSans,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentParks) { park ->
                        ParkCard(
                            park = park,
                            onClick = {
                                navController.navigate("parks")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IslandChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        border = if (!selected)
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
            )
        else
            null
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
            fontFamily = JakartaSans,
            fontSize = 13.sp,
            color = if (selected)
                Color.White
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ParkCard(
    park: Park,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalParking,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = park.name,
                    fontFamily = JakartaSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = park.location,
                        fontFamily = JakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}