package com.parkin.app.ui.mainPageResorces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parkin.app.ui.theme.JakartaSans


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHystoricalScreen() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color(0xFF2563EB),
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ParkIn",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
    )
}