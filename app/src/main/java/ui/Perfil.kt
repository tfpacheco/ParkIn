package com.parkin.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("") }

    val user = supabase.auth.currentUserOrNull()

    val email = user?.email ?: "Sem email"

    val firstName = user?.userMetadata
        ?.get("first_name")
        ?.toString()
        ?.replace("\"", "")
        ?: ""

    val lastName = user?.userMetadata
        ?.get("last_name")
        ?.toString()
        ?.replace("\"", "")
        ?: ""

    val nomeUsuario = "$firstName $lastName".trim()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Perfil",
                            fontFamily = JakartaSans,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
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
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9F0FA))
                        .padding(8.dp),
                    tint = Color(0xFF1D52BA)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = nomeUsuario.ifEmpty { "Utilizador" },
                        fontFamily = JakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = email,
                        fontFamily = JakartaSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.7f
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Opções da Conta",
                fontFamily = JakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                // 1. MODO ESCURO (PRIMEIRA OPÇÃO)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )

                        Text(
                            text = "Modo escuro",
                            fontFamily = JakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeChange
                    )
                }

                // 2. OS MEUS VEÍCULOS
                ProfileMenuOption(
                    icon = Icons.Default.DirectionsCar,
                    title = "Os meus veículos",
                    onClick = {
                        navController.navigate("cars/1")
                    }
                )

                // 3. NOTIFICAÇÕES
                ProfileMenuOption(
                    icon = Icons.Default.Notifications,
                    title = "Notificações",
                    onClick = { }
                )

                // 4. HISTÓRICO DE ESTACIONAMENTOS
                ProfileMenuOption(
                    icon = Icons.Default.History,
                    title = "Histórico de estacionamentos",
                    onClick = { navController.navigate("historical") }
                )

                // 5. TERMINAR SESSÃO
                ProfileMenuOption(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Terminar sessão",
                    tint = Color(0xFFD32F2F),
                    textColor = Color(0xFFD32F2F),
                    onClick = { if (!isLoading) showLogoutDialog = true }
                )
            }

            if (mensagem.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = mensagem,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) showLogoutDialog = false
            },
            title = {
                Text(
                    text = "Terminar sessão",
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Tens a certeza de que queres terminar a sessão?",
                    fontFamily = JakartaSans
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            mensagem = ""

                            try {
                                supabase.auth.signOut()

                                val preferences =
                                    navController.context.getSharedPreferences(
                                        "preferences",
                                        Context.MODE_PRIVATE
                                    )

                                preferences.edit()
                                    .remove("is_logged_in")
                                    .remove("user_email")
                                    .apply()

                                showLogoutDialog = false

                                navController.navigate("login") {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                mensagem =
                                    "Não foi possível terminar sessão. Tenta novamente."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isLoading) "A sair..." else "Terminar sessão",
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuOption(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = title,
                fontFamily = JakartaSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}