
package com.parkin.app.ui.nfcEntry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Cores do BottomSheet: permanecem iguais nos dois temas.
private val SheetBlack = Color(0xFF151922)
private val SheetCard = Color(0xFF222936)
private val SheetBorder = Color(0xFF343D4C)
private val SheetWhite = Color.White
private val SheetMuted = Color(0xFFADB5C4)
private val ParkInBlue = Color(0xFF2563EB)

// Modelos
@Serializable
data class AccessCard(
    @SerialName("numero_cartao")
    val cardNumber: String,
    val estado: String
)

@Serializable
data class UserSubscription(
    @SerialName("plano_id")
    val planId: String,
    val estado: String
)

@Serializable
data class Plan(
    val nome: String
)

@Serializable
private data class ParkOption(
    val id: String,
    val nome: String
)

// Estados do cartão
sealed class CardState {
    object Loading : CardState()

    data class Success(
        val userName: String,
        val planName: String,
        val cardNumber: String
    ) : CardState()

    object NoCard : CardState()
    object NoUser : CardState()

    data class Error(val message: String) : CardState()
}

// ViewModel
class CardViewModel : androidx.lifecycle.ViewModel() {

    private val _state =
        MutableStateFlow<CardState>(CardState.Loading)

    val state: StateFlow<CardState> = _state

    fun loadUserData() {
        viewModelScope.launch {
            _state.value = CardState.Loading

            try {
                val user = supabase.auth.currentSessionOrNull()?.user

                if (user == null) {
                    _state.value = CardState.NoUser
                    return@launch
                }

                val users = supabase
                    .from("users")
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeList<UserProfile>()

                val profile = users.firstOrNull()

                val userName = listOfNotNull(
                    profile?.firstName,
                    profile?.lastName
                ).joinToString(" ").ifBlank {
                    "Utilizador"
                }

                // Procurar o cartão, mesmo que a subscrição tenha expirado.
                val cards = supabase
                    .from("cartoes")
                    .select {
                        filter {
                            eq("user_id", user.id)
                        }
                    }
                    .decodeList<AccessCard>()

                val card = cards.firstOrNull()

                if (card == null) {
                    _state.value = CardState.NoCard
                    return@launch
                }

                val subscriptions = supabase
                    .from("subscricoes")
                    .select {
                        filter {
                            eq("user_id", user.id)
                            eq("estado", "ativo")
                        }
                    }
                    .decodeList<UserSubscription>()

                val subscription = subscriptions.firstOrNull()

                if (subscription == null) {
                    _state.value = CardState.NoCard
                    return@launch
                }

                val plans = supabase
                    .from("planos")
                    .select {
                        filter {
                            eq("id", subscription.planId)
                        }
                    }
                    .decodeList<Plan>()

                val planName = plans.firstOrNull()?.nome
                    ?: "Plano não identificado"

                _state.value = CardState.Success(
                    userName = userName,
                    planName = planName,
                    cardNumber = card.cardNumber
                )

            } catch (e: Exception) {
                _state.value = CardState.Error(
                    e.message
                        ?: "Não foi possível carregar o cartão."
                )
            }
        }
    }
}

// Ecrã principal
@Composable
fun CardScreen(
    navController: NavController,
    viewModel: CardViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = MaterialTheme.colorScheme

    var showAddCardSheet by remember {
        mutableStateOf(false)
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadUserData()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when (val currentState = state) {

            CardState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primary
                )
            }

            CardState.NoUser -> {
                EmptyCardState(
                    title = "Inicia sessão",
                    description = "Inicia sessão para consultares o teu cartão ParkIn.",
                    buttonText = "Iniciar sessão",
                    icon = {
                        Icon(
                            Icons.Default.CardMembership,
                            contentDescription = null,
                            modifier = Modifier.size(58.dp),
                        )
                    },
                    onClick = onNavigateToLogin
                )
            }

            CardState.NoCard -> {
                EmptyCardState(
                    title = "O teu cartão ParkIn",
                    description = "Ainda não tens uma subscrição ativa. Cria o teu cartão ou renova a subscrição para continuares a utilizar os parques ParkIn.",
                    buttonText = "Criar / Renovar cartão",
                    icon = {
                        Icon(
                            Icons.Default.CardMembership,
                            contentDescription = null,
                            modifier = Modifier.size(58.dp),
                        )
                    },
                    onClick = {
                        showAddCardSheet = true
                    }
                )
            }

            is CardState.Error -> {
                EmptyCardState(
                    title = "Não foi possível carregar",
                    description = currentState.message,
                    buttonText = "Tentar novamente",
                    icon = {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(58.dp),
                            tint = colors.error
                        )
                    },
                    onClick = {
                        viewModel.loadUserData()
                    }
                )
            }

            is CardState.Success -> {
                ParkInCardContent(
                    userName = currentState.userName,
                    planName = currentState.planName,
                    cardNumber = currentState.cardNumber
                )
            }
        }
    }

    if (showAddCardSheet) {
        AddCardBottomSheet(
            navController = navController,
            onDismiss = {
                showAddCardSheet = false
            }
        )
    }
}

@Composable
private fun EmptyCardState(
    title: String,
    description: String,
    buttonText: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.primaryContainer,
            modifier = Modifier.size(112.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = title,
            color = colors.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = description,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary
            )
        ) {
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Cartão digital
@Composable
private fun ParkInCardContent(
    userName: String,
    planName: String,
    cardNumber: String
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "O meu cartão",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "O teu acesso aos parques ParkIn",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Spacer(Modifier.height(30.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 270.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant
            ),
            border = BorderStroke(
                1.dp,
                colors.outline.copy(alpha = 0.45f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ParkIn",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "DIGITAL CARD",
                            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = "Cartão ParkIn",
                        tint = colors.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "TITULAR DO CARTÃO",
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = userName,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "PLANO",
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = planName,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(22.dp))

                HorizontalDivider(
                    color = colors.outline.copy(alpha = 0.35f)
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "NÚMERO DO CARTÃO",
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    text = cardNumber,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                Surface(
                    color = colors.tertiaryContainer,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 13.dp,
                            vertical = 7.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(Modifier.width(7.dp))

                        Text(
                            text = "SUBSCRIÇÃO ATIVA",
                            color = colors.onTertiaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Apresenta este cartão para identificar a tua conta ParkIn.",
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(
    navController: NavController,
    onDismiss: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf("universal")
    }

    var parks by remember {
        mutableStateOf<List<ParkOption>>(emptyList())
    }

    var selectedParkId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedParkName by remember {
        mutableStateOf<String?>(null)
    }

    var loadingParks by remember {
        mutableStateOf(false)
    }

    var parkError by remember {
        mutableStateOf<String?>(null)
    }

    var retryCounter by remember {
        mutableIntStateOf(0)
    }

    val price = if (selectedType == "universal") 19.99 else 9.99

    LaunchedEffect(selectedType, retryCounter) {
        if (selectedType == "especifico") {
            loadingParks = true
            parkError = null

            try {
                parks = supabase
                    .from("parques")
                    .select()
                    .decodeList<ParkOption>()

                if (parks.isEmpty()) {
                    parkError = "Não existem parques disponíveis."
                }
            } catch (e: Exception) {
                parkError = e.message
                    ?: "Não foi possível carregar os parques."
            } finally {
                loadingParks = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SheetBlack,
        contentColor = SheetWhite,
        tonalElevation = 0.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.Gray,
                width = 40.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "O teu cartão ParkIn",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SheetWhite
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Escolhe o plano mensal que melhor se adapta às tuas necessidades.",
                style = MaterialTheme.typography.bodyMedium,
                color = SheetMuted
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "ESCOLHE O TEU PLANO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF)
            )

            Spacer(Modifier.height(12.dp))

            CardTypeOption(
                title = "Universal",
                description = "Estaciona em todos os parques ParkIn.",
                price = "19,99 €",
                selected = selectedType == "universal",
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = if (selectedType == "universal") {
                            ParkInBlue
                        } else {
                            SheetMuted
                        },
                        modifier = Modifier.size(27.dp)
                    )
                },
                onClick = {
                    selectedType = "universal"
                    selectedParkId = null
                    selectedParkName = null
                }
            )

            Spacer(Modifier.height(12.dp))

            CardTypeOption(
                title = "Específico",
                description = "Escolhe um parque e estaciona nesse local.",
                price = "9,99 €",
                selected = selectedType == "especifico",
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = if (selectedType == "especifico") {
                            ParkInBlue
                        } else {
                            SheetMuted
                        },
                        modifier = Modifier.size(29.dp)
                    )
                },
                onClick = {
                    selectedType = "especifico"
                    selectedParkId = null
                    selectedParkName = null
                }
            )

            if (selectedType == "especifico") {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Seleciona o parque",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SheetWhite
                )

                Spacer(Modifier.height(12.dp))

                when {
                    loadingParks -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ParkInBlue
                            )
                        }
                    }

                    parkError != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF29212A),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = parkError ?: "",
                                color = Color(0xFFFCA5A5)
                            )

                            TextButton(
                                onClick = {
                                    retryCounter++
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SheetWhite
                                )

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    "Tentar novamente",
                                    color = SheetWhite
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            parks.forEach { park ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedParkId = park.id
                                            selectedParkName = park.nome
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selectedParkId == park.id) {
                                        ParkInBlue.copy(alpha = 0.18f)
                                    } else {
                                        SheetCard
                                    },
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selectedParkId == park.id) {
                                            ParkInBlue
                                        } else {
                                            SheetBorder
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 12.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (selectedParkId == park.id) {
                                                ParkInBlue
                                            } else {
                                                SheetMuted
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )

                                        Spacer(Modifier.width(12.dp))

                                        Text(
                                            text = park.nome,
                                            color = SheetWhite,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        RadioButton(
                                            selected = selectedParkId == park.id,
                                            onClick = {
                                                selectedParkId = park.id
                                                selectedParkName = park.nome
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = ParkInBlue,
                                                unselectedColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(
                color = SheetBorder
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = ParkInBlue,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Resumo da compra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SheetWhite
                )
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SheetCard,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedType == "universal") {
                                "Plano Universal"
                            } else {
                                "Plano Específico"
                            },
                            color = SheetMuted
                        )

                        Text(
                            text = "%.2f €".format(price),
                            color = SheetWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (
                        selectedType == "especifico" &&
                        selectedParkName != null
                    ) {
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Parque selecionado",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = selectedParkName ?: "",
                            color = SheetWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(
                        color = SheetBorder
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SheetMuted,
                                modifier = Modifier.size(17.dp)
                            )

                            Spacer(Modifier.width(7.dp))

                            Text(
                                text = "Duração",
                                color = SheetMuted
                            )
                        }

                        Text(
                            text = "1 mês",
                            color = SheetWhite,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    val savedStateHandle =
                        navController.currentBackStackEntry?.savedStateHandle

                    savedStateHandle?.set("card_type", selectedType)
                    savedStateHandle?.set("card_price", price)
                    savedStateHandle?.set("park_id", selectedParkId)

                    onDismiss()
                    navController.navigate("payment")
                },
                enabled = selectedType == "universal" ||
                        (selectedParkId != null && !loadingParks),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParkInBlue,
                    disabledContainerColor = Color(0xFF414B5B)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(9.dp))

                Text(
                    text = "Continuar para pagamento",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "O pagamento será confirmado antes de ativar ou renovar a subscrição.",
                color = SheetMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Opção de plano
@Composable
private fun CardTypeOption(
    title: String,
    description: String,
    price: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            Color(0xFF1D3155)
        } else {
            SheetCard
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                ParkInBlue
            } else {
                SheetBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ParkInBlue,
                    unselectedColor = Color.Gray
                )
            )

            Spacer(Modifier.width(8.dp))

            icon()

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = SheetWhite,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = description,
                    color = SheetMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = price,
                    color = SheetWhite,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "/ mês",
                    color = SheetMuted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
