package com.parkin.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.parkin.app.ui.theme.JakartaSans
import kotlinx.coroutines.launch

data class ParkItem(
    val id: String,
    val name: String,
    val address: String,
    val pricePerHour: String,
    val totalSpots: String,
    val location: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParksScreen(
    navController: NavController,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    val parksList = remember {
        listOf(
            ParkItem("1", "Parque da Avenida", "Av. Infante D. Henrique, Ponta Delgada", "0,80 € / h", "120 lugares", LatLng(37.73966027979891, -25.666723995817502)),
            ParkItem("2", "Parque do Castilho", "Rua do Castilho, Ponta Delgada", "0,70 € / h", "85 lugares", LatLng(37.74191793418096, -25.670657802740894)),
            ParkItem("3", "Parque Teatro Micaelense", "Rua Dr. Francisco Machado Faria e Maia, Ponta Delgada", "0,50 € / h", "50 lugares", LatLng(37.74163289009793, -25.665683279108382)),
            ParkItem("4", "Parque Mercado da Graça", "Ponta Delgada", "0,60 € / h", "90 lugares", LatLng(37.741869925041, -25.663711784551868))
        )
    }

    var selectedPark by remember { mutableStateOf(parksList[0]) }
    var userLocation by remember { mutableStateOf(LatLng(37.7390, -25.6720)) }
    var isMapLoaded by remember { mutableStateOf(false) }

    val saoMiguelBounds = remember {
        LatLngBounds(
            LatLng(37.6800, -25.8800),
            LatLng(37.9200, -25.1200)
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedPark.location, 16f)
    }

    // JSON para o mapa LIMPO em Modo Claro (remove lojas, restaurantes e ícones comerciais)
    val lightMapStyleJson = remember {
        """
        [
          {
            "featureType": "poi",
            "elementType": "labels.icon",
            "stylers": [{"visibility": "off"}]
          },
          {
            "featureType": "poi.business",
            "stylers": [{"visibility": "off"}]
          },
          {
            "featureType": "transit",
            "elementType": "labels.icon",
            "stylers": [{"visibility": "off"}]
          }
        ]
        """.trimIndent()
    }

    // JSON para o mapa LIMPO em Modo Escuro
    val darkMapStyleJson = remember {
        """
        [
          {
            "elementType": "geometry",
            "stylers": [{"color": "#212121"}]
          },
          {
            "elementType": "labels.icon",
            "stylers": [{"visibility": "off"}]
          },
          {
            "elementType": "labels.text.fill",
            "stylers": [{"color": "#757575"}]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [{"color": "#212121"}]
          },
          {
            "featureType": "administrative",
            "elementType": "geometry",
            "stylers": [{"color": "#757575"}]
          },
          {
            "featureType": "poi",
            "elementType": "labels.text.fill",
            "stylers": [{"color": "#d59563"}]
          },
          {
            "featureType": "poi.business",
            "stylers": [{"visibility": "off"}]
          },
          {
            "featureType": "poi.park",
            "elementType": "geometry",
            "stylers": [{"color": "#181818"}]
          },
          {
            "featureType": "road",
            "elementType": "geometry",
            "stylers": [{"color": "#2c2c2c"}]
          },
          {
            "featureType": "road",
            "elementType": "geometry.stroke",
            "stylers": [{"color": "#1a1a1a"}]
          },
          {
            "featureType": "road",
            "elementType": "labels.text.fill",
            "stylers": [{"color": "#8a8a8a"}]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [{"color": "#000000"}]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [{"color": "#3d3d3d"}]
          }
        ]
        """.trimIndent()
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false
        )
    }

    // Aplica o estilo respetivo consoante o tema (claro ou escuro)
    val mapProperties = remember(isDarkTheme) {
        MapProperties(
            latLngBoundsForCameraTarget = saoMiguelBounds,
            minZoomPreference = 12f,
            maxZoomPreference = 18f,
            isBuildingEnabled = false,
            isMyLocationEnabled = false,
            mapStyleOptions = MapStyleOptions(if (isDarkTheme) darkMapStyleJson else lightMapStyleJson)
        )
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                    .setMinUpdateIntervalMillis(1500)
                    .build()
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: SecurityException) {}
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .build()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            locationPermissionLauncher.launch(permission)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Parques",
                        fontFamily = JakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primary
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // GOOGLE MAPS LIMPO
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
                properties = mapProperties,
                onMapLoaded = { isMapLoaded = true }
            ) {
                if (isMapLoaded) {
                    Polyline(
                        points = listOf(userLocation, selectedPark.location),
                        color = colors.primary,
                        width = 10f
                    )

                    Marker(
                        state = MarkerState(position = selectedPark.location),
                        title = selectedPark.name,
                        snippet = selectedPark.address
                    )

                    Marker(
                        state = MarkerState(position = userLocation),
                        title = "A sua localização"
                    )
                }
            }

            // CARROSSEL HORIZONTAL SUPERIOR
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(parksList, key = { it.id }) { park ->
                    val isSelected = park.id == selectedPark.id
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clickable {
                                selectedPark = park
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(park.location, 16f)
                                    )
                                }
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) colors.primary else colors.surface,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = park.name,
                                fontFamily = JakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) colors.onPrimary else colors.onSurface
                            )
                        }
                    }
                }
            }

            // BOTÃO FLUTUANTE DE GPS
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingMapButton(icon = Icons.Default.MyLocation, colors = colors) {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(userLocation, 17f)
                        )
                    }
                }
            }

            // CARTÃO DE DETALHES INFERIOR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalParking,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedPark.name,
                                fontFamily = JakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedPark.pricePerHour,
                                    fontFamily = JakartaSans,
                                    fontSize = 12.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "  •  ${selectedPark.totalSpots}",
                                    fontFamily = JakartaSans,
                                    fontSize = 12.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Morada",
                                fontFamily = JakartaSans,
                                fontSize = 10.sp,
                                color = colors.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = selectedPark.address,
                                fontFamily = JakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // BOTÃO DE DIREÇÕES
                        IconButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${selectedPark.location.latitude},${selectedPark.location.longitude}&mode=d")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${selectedPark.location.latitude},${selectedPark.location.longitude}")
                                    )
                                    context.startActivity(browserIntent)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Navegar",
                                tint = colors.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingMapButton(icon: ImageVector, colors: ColorScheme, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surface,
        shadowElevation = 3.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}