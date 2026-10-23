
package com.parkin.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.theme.JakartaSans
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    val titulo: String,
    val mensagem: String,
    val tipo: String,
    val lida: Boolean,

    @SerialName("created_at")
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {

    var notifications by remember {
        mutableStateOf<List<NotificationItem>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var refreshing by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Carrega as notificações do utilizador autenticado.
    fun loadNotifications(showLoading: Boolean = true) {
        scope.launch {
            if (showLoading) {
                isLoading = true
            }

            errorMessage = null

            try {
                val user = supabase.auth.currentUserOrNull()
                    ?: throw IllegalStateException(
                        "Tens de iniciar sessão para ver as notificações."
                    )

                val result = supabase
                    .from("notificacoes")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<NotificationItem>()

                notifications = result.sortedByDescending {
                    it.createdAt
                }

            } catch (e: Exception) {
                errorMessage = e.message
                    ?: "Não foi possível carregar as notificações."
            } finally {
                isLoading = false
                refreshing = false
            }
        }
    }

    // Carregamento inicial.
    LaunchedEffect(Unit) {
        loadNotifications()
    }

    // Atualiza quando o utilizador regressa à página.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                loadNotifications(showLoading = false)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Marca uma notificação como lida no Supabase.
    fun markAsRead(notification: NotificationItem) {
        if (notification.lida) return

        scope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                    ?: throw IllegalStateException("Sessão não encontrada.")

                supabase
                    .from("notificacoes")
                    .update({
                        set("lida", true)
                    }) {
                        filter {
                            eq("id", notification.id)
                            eq("user_id", user.id)
                        }
                    }

                notifications = notifications.map {
                    if (it.id == notification.id) {
                        it.copy(lida = true)
                    } else {
                        it
                    }
                }

            } catch (e: Exception) {
                errorMessage =
                    "Não foi possível marcar a notificação como lida."
            }
        }
    }

    fun markAllAsRead() {
        scope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                    ?: throw IllegalStateException("Sessão não encontrada.")

                supabase
                    .from("notificacoes")
                    .update({
                        set("lida", true)
                    }) {
                        filter {
                            eq("user_id", user.id)
                            eq("lida", false)
                        }
                    }

                notifications = notifications.map {
                    it.copy(lida = true)
                }

            } catch (e: Exception) {
                errorMessage =
                    "Não foi possível atualizar as notificações."
            }
        }
    }

    val unreadCount = notifications.count { !it.lida }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notificações",
                        fontFamily = JakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { markAllAsRead() }
                        ) {
                            Text(
                                text = "Ler todas",
                                color = Color.White,
                                fontFamily = JakartaSans
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4169E1)
                ),
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
            )
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4169E1)
                    )
                }
            }

            errorMessage != null && notifications.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = errorMessage ?: "Ocorreu um erro.",
                        fontFamily = JakartaSans
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { loadNotifications() }
                    ) {
                        Text("Tentar novamente")
                    }
                }
            }

            notifications.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.Gray
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Sem notificações",
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Quando houver novidades, aparecerão aqui.",
                        fontFamily = JakartaSans,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            refreshing = true
                            loadNotifications(showLoading = false)
                        }
                    ) {
                        Text("Atualizar")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { it.id }
                        ) { notification ->

                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    markAsRead(notification)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val icon = when (notification.tipo.lowercase()) {
        "payment", "pagamento" ->
            Icons.Default.Payments

        "receipt", "recibo" ->
            Icons.Default.Receipt

        "session", "sessao", "sessão" ->
            Icons.Default.Schedule

        else ->
            Icons.Default.Notifications
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.lida) {
                Color(0xFFEFF4FF)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCE6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF4169E1)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.titulo,
                    fontFamily = JakartaSans,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = notification.mensagem,
                    fontFamily = JakartaSans,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = notification.createdAt
                        .replace("T", " ")
                        .take(16),
                    fontFamily = JakartaSans,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            if (!notification.lida) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Não lida",
                    tint = Color(0xFF4169E1),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
