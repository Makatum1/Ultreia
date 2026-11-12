package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoStats
import com.example.ui.CaminoViewModel
import com.example.ui.components.SettingsActionMenu
import com.example.ui.settings.AppStrings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaminoDetailScreen(
    camino: CaminoEntity,
    stages: List<StageEntity>,
    stats: CaminoStats,
    viewModel: CaminoViewModel,
    onBack: () -> Unit,
    onStageClick: (Long) -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showAddStageDialog by remember { mutableStateOf(false) }
    var stageToEdit by remember { mutableStateOf<StageEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Surface(
                            color = PilgrimYellow.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (camino.isTravelGuide) "Estás en el viaje: ${camino.title}" else "Estás en: ${camino.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = camino.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("camino_detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la lista")
                    }
                },
                actions = {
                    // Export to Professional PDF button
                    IconButton(
                        onClick = { viewModel.exportToPdf(context) },
                        modifier = Modifier.testTag("camino_export_pdf_button")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = viewModel.string("export_pdf"),
                            tint = CaminoBlue
                        )
                    }

                    // Export Camino (JSON)
                    IconButton(
                        onClick = { viewModel.exportSingleCaminoJson(context, camino.id) },
                        modifier = Modifier.testTag("camino_export_json_button")
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Exportar ${camino.title}",
                            tint = CaminoBlue
                        )
                    }

                    SettingsActionMenu(viewModel = viewModel)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                    label = { Text("${viewModel.string("tab_stages")} (${stages.size})") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("tab_stages")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text(viewModel.string("tab_map")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("tab_map")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text(viewModel.string("tab_stats")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("tab_stats")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddStageDialog = true },
                    containerColor = CaminoBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_stage")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir etapa")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: STAGES LIST ORGANIZED BY DATES
                    StagesListTab(
                        camino = camino,
                        stages = stages,
                        stats = stats,
                        viewModel = viewModel,
                        onStageClick = onStageClick,
                        onToggleStage = { stage -> viewModel.toggleStageCompletion(stage) },
                        onEditStage = { stage -> stageToEdit = stage }
                    )
                }
                1 -> {
                    // TAB 1: GOOGLE MAPS ROUTE
                    InteractiveMapScreen(
                        camino = camino,
                        stages = stages,
                        onStageClick = onStageClick
                    )
                }
                2 -> {
                    // TAB 2: DETAILED STATISTICS
                    StatsScreen(
                        camino = camino,
                        stages = stages,
                        stats = stats,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Add Stage Dialog
    if (showAddStageDialog) {
        val nextDay = stages.size + 1
        val defaultDate = AppStrings.calculateStageDate(camino.startDate.ifBlank { "10/05/2026" }, nextDay - 1)
        AddStageDialog(
            nextDay = nextDay,
            initialDate = defaultDate,
            onDismiss = { showAddStageDialog = false },
            onConfirm = { origin, dest, km, gain, loss, time, diff, date, notes ->
                viewModel.addStage(
                    caminoId = camino.id,
                    title = "Etapa $nextDay: $origin a $dest",
                    origin = origin,
                    destination = dest,
                    distanceKm = km,
                    elevationGainM = gain,
                    elevationLossM = loss,
                    estimatedTime = time,
                    difficulty = diff,
                    date = date,
                    stageNotes = notes
                )
                showAddStageDialog = false
            }
        )
    }

    // Direct Edit Stage Dialog
    if (stageToEdit != null) {
        EditStageDialog(
            stage = stageToEdit!!,
            onDismiss = { stageToEdit = null },
            onSave = { updated ->
                viewModel.updateStage(updated)
                stageToEdit = null
            }
        )
    }
}

@Composable
fun StagesListTab(
    camino: CaminoEntity,
    stages: List<StageEntity>,
    stats: CaminoStats,
    viewModel: CaminoViewModel,
    onStageClick: (Long) -> Unit,
    onToggleStage: (StageEntity) -> Unit,
    onEditStage: (StageEntity) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Camino Header Banner
        item {
            Spacer(Modifier.height(8.dp))
            CaminoHeaderBanner(camino = camino, stats = stats)
        }

        // Export Actions: Exportar PDF & Exportar (JSON)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.exportToPdf(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_pdf_camino")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Exportar PDF",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.exportSingleCaminoJson(context, camino.id) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_json_camino")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Exportar",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Attached Files Section
        item {
            AttachedFilesSection(
                camino = camino,
                viewModel = viewModel
            )
        }

        // Section Title with dates overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Itinerario por Fechas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                    if (camino.startDate.isNotBlank()) {
                        Text(
                            text = "Inicio del Camino: ${camino.startDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = CaminoBlueLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${stats.completedStages}/${stages.size} hechas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlueDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (stages.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Route, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay etapas registradas aún", fontWeight = FontWeight.Medium)
                        Text(
                            "Pulsa el botón + para añadir tu primera etapa de viaje",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(stages) { stage ->
                val stageDate = viewModel.resolveStageDate(stage)
                val isToday = remember(stageDate) { AppStrings.isToday(stageDate) }

                StageCardItem(
                    stage = stage,
                    stageDate = stageDate,
                    isToday = isToday,
                    onClick = { onStageClick(stage.id) },
                    onToggle = { onToggleStage(stage) },
                    onEdit = { onEditStage(stage) }
                )
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun CaminoHeaderBanner(camino: CaminoEntity, stats: CaminoStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Image
            if (!camino.coverPhotoUri.isNullOrBlank()) {
                AsyncImage(
                    model = camino.coverPhotoUri,
                    contentDescription = camino.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_ultreia_logo),
                    contentDescription = camino.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(CaminoBlue),
                    contentScale = ContentScale.Fit
                )
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                            startY = 35f
                        )
                    )
            )

            // Header Texts
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PilgrimYellow,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ULTREIA GUÍA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlueDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (camino.startDate.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "📅 ${camino.startDate}",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = camino.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = camino.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.completedKm} / ${stats.totalKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = PilgrimYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${stats.progressPercent}% completado",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StageCardItem(
    stage: StageEntity,
    stageDate: String,
    isToday: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stage_card_${stage.dayNumber}"),
        colors = CardDefaults.cardColors(
            containerColor = if (stage.isCompleted) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header line: Date pill, Today badge, Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stageDate.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(11.dp), tint = CaminoBlue)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stageDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CaminoBlueDark
                                )
                            }
                        }
                    }

                    if (isToday) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = PilgrimYellow,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "HOY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Quick Edit Action Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar etapa",
                        tint = CaminoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day badge circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (stage.isCompleted) CaminoGreen else CaminoBlueLight,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DÍA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stage.isCompleted) Color.White.copy(alpha = 0.8f) else CaminoBlue
                        )
                        Text(
                            text = "${stage.dayNumber}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (stage.isCompleted) Color.White else CaminoBlueDark
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Main Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stage.origin} → ${stage.destination}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stage.distanceKm} km",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlue
                        )
                        Text("•", color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "+${stage.elevationGainM}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("•", color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = stage.estimatedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (stage.difficulty) {
                                "Fácil" -> CaminoGreen.copy(alpha = 0.12f)
                                "Moderada" -> PilgrimYellow.copy(alpha = 0.25f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = stage.difficulty,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (stage.difficulty) {
                                    "Fácil" -> CaminoGreen
                                    "Moderada" -> PilgrimYellowDark
                                    else -> Color(0xFFDC2626)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (stage.stageNotes.isNotBlank()) {
                            Surface(
                                color = PilgrimYellowLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(10.dp), tint = PilgrimYellowDark)
                                    Spacer(Modifier.width(2.dp))
                                    Text("Consejos", fontSize = 10.sp, color = PilgrimYellowDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Completion Checkbox / Toggle
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_check_stage_${stage.dayNumber}")
                ) {
                    Icon(
                        imageVector = if (stage.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (stage.isCompleted) "Completada" else "Marcar como completada",
                        tint = if (stage.isCompleted) CaminoGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddStageDialog(
    nextDay: Int,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (origin: String, dest: String, km: Double, gain: Int, loss: Int, time: String, diff: String, date: String, notes: String) -> Unit
) {
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(initialDate) }
    var distanceStr by remember { mutableStateOf("") }
    var gainStr by remember { mutableStateOf("") }
    var lossStr by remember { mutableStateOf("") }
    var timeStr by remember { mutableStateOf("5h 00m") }
    var difficulty by remember { mutableStateOf("Moderada") }
    var notes by remember { mutableStateOf("") }

    val difficulties = listOf("Fácil", "Moderada", "Difícil", "Muy exigente")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Día $nextDay al Camino") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origen *") },
                        placeholder = { Text("Ej. Roncesvalles") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destino *") },
                        placeholder = { Text("Ej. Zubiri") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Fecha (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = distanceStr,
                            onValueChange = { distanceStr = it },
                            label = { Text("Distancia (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeStr,
                            onValueChange = { timeStr = it },
                            label = { Text("Tiempo est.") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gainStr,
                            onValueChange = { gainStr = it },
                            label = { Text("Desnivel + (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lossStr,
                            onValueChange = { lossStr = it },
                            label = { Text("Desnivel - (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("Dificultad:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        difficulties.forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = { Text(diff, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Consejos / Notas del día") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (origin.isNotBlank() && destination.isNotBlank()) {
                        onConfirm(
                            origin,
                            destination,
                            distanceStr.toDoubleOrNull() ?: 20.0,
                            gainStr.toIntOrNull() ?: 250,
                            lossStr.toIntOrNull() ?: 250,
                            timeStr,
                            difficulty,
                            dateStr,
                            notes
                        )
                    }
                }
            ) {
                Text("Añadir Etapa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AttachedFilesSection(
    camino: CaminoEntity,
    viewModel: CaminoViewModel
) {
    val context = LocalContext.current
    val attachedFiles = remember(camino.attachedFilesJson) {
        viewModel.parseAttachedFiles(camino.attachedFilesJson)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = "Documento"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                viewModel.addAttachedFile(camino.id, displayName, uri.toString(), mimeType = mimeType)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "No se pudo adjuntar el archivo", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Archivos y Documentos (${attachedFiles.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                }

                FilledTonalButton(
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Subir archivo", fontSize = 12.sp)
                }
            }

            if (attachedFiles.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sube aquí tus billetes, reservas, credenciales o documentos del viaje.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachedFiles.forEach { file ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (file.name.endsWith(".pdf", ignoreCase = true))
                                            Icons.Default.PictureAsPdf
                                        else if (file.mimeType.startsWith("image/"))
                                            Icons.Default.Image
                                        else
                                            Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = CaminoBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(Uri.parse(file.uri), file.mimeType)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "No se puede abrir este tipo de archivo", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "Abrir", tint = CaminoBlue, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeAttachedFile(camino.id, file.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
