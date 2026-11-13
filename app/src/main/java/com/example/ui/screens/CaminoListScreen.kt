package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CaminoEntity
import com.example.ui.CaminoViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaminoListScreen(
    caminos: List<CaminoEntity>,
    viewModel: CaminoViewModel,
    onSelectCamino: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInitialLoadDone by viewModel.isInitialLoadDone.collectAsStateWithLifecycle()
    val isTravelProfile by viewModel.isTravelProfile.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showAddCaminoDialog by remember { mutableStateOf(false) }
    var showMainRoutesDialog by remember { mutableStateOf(false) }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importFromUri(context, uri)
        }
    }

    // Filter list according to the active profile (Caminos vs Viajes)
    val filteredCaminos = remember(caminos, isTravelProfile) {
        caminos.filter { it.isTravelGuide == isTravelProfile }
    }

    var caminoToDelete by remember { mutableStateOf<CaminoEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ultreia_logo),
                            contentDescription = "Ultreia Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ultreia",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = CaminoBlue
                            )
                            Text(
                                text = if (isTravelProfile) "Guías y Rutas de Viaje" else "Guías y Memorias del Camino de Santiago",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Profile Switch Button: Suitcase icon labeled "Viajes" / "Caminos"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleTravelProfile() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("btn_toggle_travel_profile")
                    ) {
                        Icon(
                            imageVector = if (isTravelProfile) Icons.Default.Hiking else Icons.Default.Luggage,
                            contentDescription = if (isTravelProfile) "Cambiar a Caminos" else "Cambiar a Viajes",
                            tint = if (isTravelProfile) PilgrimYellow else CaminoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isTravelProfile) "Caminos" else "Viajes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTravelProfile) CaminoBlueDark else CaminoBlue,
                            fontSize = 10.sp
                        )
                    }

                    com.example.ui.components.SettingsActionMenu(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isTravelProfile) {
                    ExtendedFloatingActionButton(
                        onClick = { showMainRoutesDialog = true },
                        icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Principales Rutas", style = MaterialTheme.typography.labelLarge)
                                Text("(Ejemplos)", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        containerColor = PilgrimYellow,
                        contentColor = CaminoBlueDark,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_main_routes")
                    )
                }

                ExtendedFloatingActionButton(
                    onClick = { showAddCaminoDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(if (isTravelProfile) "Nuevo Viaje" else "Nuevo Camino") },
                    containerColor = CaminoBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_camino")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Mode Indicator Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTravelProfile) CaminoBlueDark else CaminoBlue
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = PilgrimYellow,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isTravelProfile) "¡BUEN VIAJE!" else "¡BUEN CAMINO!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlueDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isTravelProfile) "Tus Viajes y Rutas" else "Tus Rutas y Guías",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isTravelProfile)
                                    "Organiza tus viajes por días, etapas, mapas interactivos, notas y alojamientos."
                                else
                                    "Elige un camino para ver sus etapas organizadas por días, mapa interactivo y memorias.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(PilgrimYellow.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTravelProfile) Icons.Default.Luggage else Icons.Default.Hiking,
                                contentDescription = null,
                                tint = PilgrimYellow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            // Camino / Viaje list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTravelProfile) "Viajes Disponibles (${filteredCaminos.size})" else "Caminos Disponibles (${filteredCaminos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )

                    // Quick seed button if empty
                    if (filteredCaminos.isEmpty()) {
                        FilledTonalButton(
                            onClick = {
                                if (isTravelProfile) {
                                    viewModel.createSampleTrip()
                                } else {
                                    viewModel.createSampleCamino()
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PilgrimYellow.copy(alpha = 0.25f),
                                contentColor = CaminoBlueDark
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isTravelProfile) "Viaje de prueba" else "Camino de prueba",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredCaminos.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(CaminoBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTravelProfile) Icons.Default.Luggage else Icons.Default.Hiking,
                                    contentDescription = null,
                                    tint = CaminoBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = if (isTravelProfile) "No tienes ningún viaje creado" else "No tienes ningún camino creado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlue
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isTravelProfile)
                                    "Comienza importando un archivo o añade un viaje de prueba para ver el funcionamiento."
                                else
                                    "Comienza importando un archivo o añade un camino de prueba para explorar sus etapas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))

                            // 1. Botón "Importar Viaje" / "Importar Camino"
                            Button(
                                onClick = {
                                    importFileLauncher.launch(arrayOf("application/zip", "application/json", "*/*"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_empty_import")
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isTravelProfile) "Importar Viaje" else "Importar Camino",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // 2. Debajo de este: Botón "Añadir Viaje de Prueba" / "Añadir Camino de Prueba"
                            FilledTonalButton(
                                onClick = {
                                    if (isTravelProfile) viewModel.createSampleTrip() else viewModel.createSampleCamino()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_empty_add_sample")
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isTravelProfile) "Añadir Viaje de Prueba" else "Añadir Camino de Prueba",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredCaminos) { camino ->
                    CaminoItemCard(
                        camino = camino,
                        onClick = { onSelectCamino(camino.id) },
                        onDelete = { caminoToDelete = camino }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showMainRoutesDialog) {
        MainRoutesDialog(
            onDismiss = { showMainRoutesDialog = false },
            onSelectRoute = { route ->
                viewModel.addPredefinedCamino(route)
                showMainRoutesDialog = false
            }
        )
    }

    if (showAddCaminoDialog) {
        AddCaminoDialog(
            isFirstCamino = caminos.isEmpty(),
            initialIsTravelGuide = isTravelProfile,
            onDismiss = { showAddCaminoDialog = false },
            onConfirm = { title, sub, date, isTravelGuide, photoUri ->
                viewModel.addCamino(title, sub, date, isTravelGuide, photoUri)
                showAddCaminoDialog = false
            },
            onSeedSample = { isTravel ->
                if (isTravel) viewModel.createSampleTrip() else viewModel.createSampleCamino()
            }
        )
    }

    if (caminoToDelete != null) {
        AlertDialog(
            onDismissRequest = { caminoToDelete = null },
            title = { Text("¿Eliminar ${caminoToDelete!!.title}?") },
            text = { Text("Se borrará esta guía completa junto con todas sus etapas, alojamientos, archivos y vivencias registradas. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCamino(caminoToDelete!!)
                        caminoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { caminoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CaminoItemCard(
    camino: CaminoEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("camino_card_${camino.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover Photo Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (!camino.coverPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = camino.coverPhotoUri,
                        contentDescription = camino.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ultreia_logo),
                        contentDescription = camino.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (camino.isTravelGuide) CaminoBlueDark else CaminoBlue),
                        contentScale = ContentScale.Fit
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 30f
                            )
                        )
                )

                // Badge top-left for travel guide vs camino
                Surface(
                    color = if (camino.isTravelGuide) PilgrimYellow else CaminoBlue,
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (camino.isTravelGuide) Icons.Default.Luggage else Icons.Default.Hiking,
                            contentDescription = null,
                            tint = if (camino.isTravelGuide) CaminoBlueDark else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (camino.isTravelGuide) "Guía de Viaje" else "Camino de Santiago",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (camino.isTravelGuide) CaminoBlueDark else Color.White
                        )
                    }
                }

                // Title and subtitle on image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = camino.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = camino.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                // Delete action button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Card Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = CaminoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (camino.startDate.isNotBlank()) "Inicio: ${camino.startDate}" else "Sin fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ver Etapas",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CaminoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCaminoDialog(
    isFirstCamino: Boolean = false,
    initialIsTravelGuide: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subtitle: String, date: String, isTravelGuide: Boolean, photoUri: String?) -> Unit,
    onSeedSample: ((Boolean) -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("15/05/2026") }
    var isTravelGuide by remember { mutableStateOf(initialIsTravelGuide) }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isTravelGuide) Icons.Default.Luggage else Icons.Default.Hiking,
                    contentDescription = null,
                    tint = CaminoBlue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isFirstCamino) "Crear tu primer Camino o Viaje" else if (isTravelGuide) "Crear Nueva Guía de Viaje" else "Crear Nueva Guía de Camino",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Switch: Guía de viaje
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Guía de viaje",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Si se activa, se transforma en una guía de viaje general y se eliminan las referencias al Camino de Santiago.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = isTravelGuide,
                                onCheckedChange = { isTravelGuide = it }
                            )
                        }
                    }
                }

                if (isFirstCamino && onSeedSample != null) {
                    item {
                        OutlinedButton(
                            onClick = {
                                onSeedSample(isTravelGuide)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isTravelGuide) "Crear un viaje de prueba" else "Crear un camino de prueba",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isTravelGuide) "Nombre del Viaje *" else "Nombre del Camino *") },
                        placeholder = {
                            Text(if (isTravelGuide) "Ej. Ruta de los Picos de Europa, Viaje a la Toscana..." else "Ej. Camino Francés, Vía de la Plata...")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Descripción o Itinerario") },
                        placeholder = {
                            Text(if (isTravelGuide) "Ej. De Cangas de Onís a Bulnes" else "Ej. De Saint-Jean-Pied-de-Port a Santiago")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Fecha de inicio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = if (isTravelGuide) "Foto de Portada del Viaje:" else "Foto de Portada del Camino:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = "Portada seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (selectedPhotoUri == null) "Elegir Foto de Portada" else "Cambiar Foto")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, subtitle, startDate, isTravelGuide, selectedPhotoUri)
                    }
                },
                modifier = Modifier.testTag("btn_confirm_create_camino")
            ) {
                Text(if (isFirstCamino) "Comenzar" else if (isTravelGuide) "Crear Viaje" else "Crear Camino")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun MainRoutesDialog(
    onDismiss: () -> Unit,
    onSelectRoute: (com.example.data.sample.PredefinedRouteInfo) -> Unit
) {
    var selectedRoute by remember { mutableStateOf<com.example.data.sample.PredefinedRouteInfo?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = CaminoBlue)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Principales Rutas", fontWeight = FontWeight.Bold, color = CaminoBlue)
                    Text(
                        "(Ejemplos)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Selecciona una de las rutas históricas oficiales para crear tu guía con sus etapas predefinidas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                com.example.data.sample.SampleData.PREDEFINED_ROUTES.forEach { route ->
                    val isChosen = selectedRoute?.title == route.title
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRoute = route },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChosen) PilgrimYellowLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isChosen) androidx.compose.foundation.BorderStroke(2.dp, CaminoBlue) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = route.title,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlueDark
                                )
                                Text(
                                    text = "${route.totalKm.toInt()} km",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )
                            }
                            Text(
                                text = route.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (route.note != null) {
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    color = PilgrimYellow.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = CaminoBlueDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = route.note,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CaminoBlueDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedRoute?.let { onSelectRoute(it) }
                },
                enabled = selectedRoute != null,
                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue)
            ) {
                Text("Crear Camino")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
