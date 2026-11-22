package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoStats
import com.example.ui.CaminoViewModel
import com.example.ui.components.SettingsActionMenu
import com.example.ui.components.DifficultySelectionGroup
import com.example.ui.components.normalizeDifficulty
import com.example.ui.components.TownAutoCompleteField
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

    // Asegura que el GPX del Camino Inglés por Trazado Antiguo esté en Archivos y Documentos
    LaunchedEffect(camino.id) {
        viewModel.checkAndAttachCaminoInglesGpx(camino)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Surface(
                            color = PilgrimYellow.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            val inPrefix = if (camino.isTravelGuide) viewModel.string("you_are_in_trip") else viewModel.string("you_are_in")
                            val routeTitle = viewModel.localizeRouteTitle(camino.title)
                            Text(
                                text = "$inPrefix: $routeTitle",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = viewModel.localizeRouteTitle(camino.title),
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = viewModel.string("cancel"))
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
                            Icons.Default.FileUpload,
                            contentDescription = viewModel.string("export_json"),
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
                val stagesTabLabel = if (camino.isTravelGuide) viewModel.string("tab_days") else viewModel.string("tab_stages")
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                    label = { Text("$stagesTabLabel (${stages.size})") },
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
                val addStageDesc = if (camino.isTravelGuide) "${viewModel.string("add_btn")} ${viewModel.string("day")}" else "${viewModel.string("add_btn")} ${viewModel.string("stage_prefix")}"
                FloatingActionButton(
                    onClick = {
                        viewModel.resetExportingState()
                        showAddStageDialog = true
                    },
                    containerColor = CaminoBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_stage")
                ) {
                    Icon(Icons.Default.Add, contentDescription = addStageDesc)
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
                        onStageClick = onStageClick,
                        viewModel = viewModel
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
            isTravel = camino.isTravelGuide,
            caminoStartDate = camino.startDate,
            viewModel = viewModel,
            onDismiss = { showAddStageDialog = false },
            onConfirm = { origin, dest, km, gain, loss, time, diff, date, notes, targetDay ->
                val stagePrefix = if (camino.isTravelGuide) viewModel.string("day") else viewModel.string("stage_prefix")
                val toConnector = viewModel.string("to_connector")
                viewModel.addStage(
                    caminoId = camino.id,
                    title = "$stagePrefix $targetDay: $origin $toConnector $dest",
                    origin = origin,
                    destination = dest,
                    distanceKm = km,
                    elevationGainM = gain,
                    elevationLossM = loss,
                    estimatedTime = time,
                    difficulty = diff,
                    date = date,
                    stageNotes = notes,
                    targetDay = targetDay
                )
                showAddStageDialog = false
            }
        )
    }

    // Direct Edit Stage Dialog
    if (stageToEdit != null) {
        EditStageDialog(
            stage = stageToEdit!!,
            viewModel = viewModel,
            isTravel = camino.isTravelGuide,
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
    var stageToDelete by remember { mutableStateOf<StageEntity?>(null) }

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
            CaminoHeaderBanner(
                camino = camino,
                stats = stats,
                viewModel = viewModel,
                onUpdateCoverPhoto = { newUri ->
                    viewModel.updateCamino(camino.copy(coverPhotoUri = newUri))
                }
            )
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
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_pdf_camino")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = viewModel.string("export_pdf"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.exportSingleCaminoJson(context, camino.id) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_json_camino")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = viewModel.string("export_json"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
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
                        text = viewModel.string("itinerary_by_dates"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                    if (camino.startDate.isNotBlank()) {
                        val startLabel = if (camino.isTravelGuide) viewModel.string("trip_start_date") else viewModel.string("camino_start_date")
                        Text(
                            text = "$startLabel: ${camino.startDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val (itineraryDone, itineraryTotal) = remember(stages) {
                    viewModel.calculateItineraryProgress(stages)
                }
                Surface(
                    color = CaminoBlueLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$itineraryDone/$itineraryTotal ${viewModel.string("stages_completed_short")}",
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
                        Text(viewModel.string("no_stages_yet"), fontWeight = FontWeight.Medium)
                        Text(
                            viewModel.string("main_routes_dialog_desc"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(stages, key = { it.id }) { stage ->
                val stageDate = viewModel.resolveStageDate(stage)
                val isToday = remember(stageDate) { AppStrings.isToday(stageDate) }

                StageCardItem(
                    stage = stage,
                    stageDate = stageDate,
                    isToday = isToday,
                    viewModel = viewModel,
                    onClick = { onStageClick(stage.id) },
                    onToggle = { onToggleStage(stage) },
                    onEdit = { onEditStage(stage) },
                    onDelete = { stageToDelete = stage }
                )
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }

    if (stageToDelete != null) {
        AlertDialog(
            onDismissRequest = { stageToDelete = null },
            title = {
                Text(
                    text = viewModel.string("delete_stage_title"),
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue
                )
            },
            text = {
                Text(viewModel.string("delete_stage_confirm"))
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stage = stageToDelete!!
                        viewModel.deleteStage(stage)
                        stageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(viewModel.string("delete"), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { stageToDelete = null }
                ) {
                    Text(viewModel.string("cancel"))
                }
            }
        )
    }
}

@Composable
fun CaminoHeaderBanner(
    camino: CaminoEntity,
    stats: CaminoStats,
    viewModel: CaminoViewModel? = null,
    onUpdateCoverPhoto: ((String?) -> Unit)? = null
) {
    val bannerBg = if (camino.isTravelGuide) Color(0xFF1C4B64) else CaminoBlue
    var showCoverPhotoDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdateCoverPhoto?.invoke(it.toString()) }
    }

    val hasCover = !camino.coverPhotoUri.isNullOrBlank()

    if (showCoverPhotoDialog) {
        val coverTitle = if (camino.isTravelGuide) {
            viewModel?.string("cover_photo_trip_title") ?: "Portada del Viaje"
        } else {
            viewModel?.string("cover_photo_camino_title") ?: "Portada del Camino"
        }

        val coverDesc = if (hasCover) {
            viewModel?.string("manage_cover_desc_has_cover") ?: "Puedes cambiar la foto de portada actual o eliminarla para volver al diseño estándar."
        } else {
            viewModel?.string("manage_cover_desc_no_cover") ?: "Selecciona una foto de tu galería para establecerla como portada de tu guía y de la exportación a PDF."
        }

        val changeOrSelectText = if (hasCover) {
            viewModel?.string("change_cover_btn") ?: "Cambiar foto de portada"
        } else {
            viewModel?.string("select_cover_btn") ?: "Seleccionar foto de portada"
        }

        AlertDialog(
            onDismissRequest = { showCoverPhotoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = CaminoBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = coverTitle,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = coverDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = {
                            showCoverPhotoDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (hasCover) Icons.Default.PhotoLibrary else Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(changeOrSelectText)
                    }

                    if (hasCover) {
                        OutlinedButton(
                            onClick = {
                                showCoverPhotoDialog = false
                                onUpdateCoverPhoto?.invoke(null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(viewModel?.string("delete_cover_btn") ?: "Eliminar portada")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCoverPhotoDialog = false }) {
                    Text(viewModel?.string("cancel") ?: "Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 175.dp)
                .background(bannerBg)
        ) {
            // Background Image / Color
            if (hasCover) {
                AsyncImage(
                    model = camino.coverPhotoUri,
                    contentDescription = camino.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!camino.isTravelGuide) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ultreia_logo),
                    contentDescription = camino.title,
                    modifier = Modifier
                        .matchParentSize()
                        .background(bannerBg),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Perfil Viajes sin foto: solo el color #1C4B64 sin nada más
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(bannerBg)
                )
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 25f
                        )
                    )
            )

            // Contenido completo de la cabecera ceñido a los márgenes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Fila superior: Badge a la izquierda y Fecha a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeText = if (camino.isTravelGuide) {
                        viewModel?.string("badge_trip_guide") ?: "ULTREIA VIAJE"
                    } else {
                        viewModel?.string("badge_camino_guide") ?: "ULTREIA GUÍA"
                    }

                    Surface(
                        color = PilgrimYellow,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlueDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (camino.startDate.isNotBlank()) {
                        Text(
                            text = camino.startDate,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Título del camino/viaje a toda la línea
                Text(
                    text = camino.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                // Descripción o itinerario: usa toda la línea, sin cortar texto, ceñido a los márgenes
                if (camino.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = camino.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Fila inferior: Métricas (km y %) a la izquierda, Botón Portada a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${stats.completedKm} / ${stats.totalKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = PilgrimYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.progressPercent}% ${viewModel?.string("completed_lower") ?: "completado"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    // Botón abajo a la derecha para gestionar portada (añadir, cambiar o eliminar)
                    if (onUpdateCoverPhoto != null) {
                        val baseCoverLabel = viewModel?.string("cover_btn") ?: "Portada"
                        val coverButtonLabel = if (hasCover) "1 $baseCoverLabel" else baseCoverLabel

                        Surface(
                            onClick = { showCoverPhotoDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = coverButtonLabel,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = coverButtonLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
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
    viewModel: CaminoViewModel,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            // Header line: Date pill, Today badge, Edit button, Delete button
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
                                text = viewModel.string("today_day"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Edit Action Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = viewModel.string("edit_stage"),
                            tint = CaminoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(Modifier.width(2.dp))

                    // Quick Delete Action Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = viewModel.string("delete_stage_title"),
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                        .size(48.dp)
                        .background(
                            if (stage.isCompleted) CaminoGreen else CaminoBlueLight,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = viewModel.string("day").uppercase(),
                            style = TextStyle(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stage.isCompleted) Color.White.copy(alpha = 0.85f) else CaminoBlue,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = "${stage.dayNumber}",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stage.isCompleted) Color.White else CaminoBlueDark,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeight = 17.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Main Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.localizeStageTitle("${stage.origin} → ${stage.destination}"),
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
                        val normDiff = normalizeDifficulty(stage.difficulty)
                        val (diffBgColor, diffTextColor) = when (normDiff) {
                            "Fácil" -> Pair(Color(0xFF16A34A), Color.White)
                            "Moderado" -> Pair(Color(0xFFEAB308), Color(0xFF1F2937))
                            "Exigente" -> Pair(Color(0xFFF97316), Color.White)
                            "Muy Exigente" -> Pair(Color(0xFFDC2626), Color.White)
                            else -> Pair(Color(0xFFEAB308), Color(0xFF1F2937))
                        }

                        Surface(
                            color = diffBgColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = viewModel.localizeDifficulty(stage.difficulty),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = diffTextColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
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
                                    Text(viewModel.string("stage_tips_tag"), fontSize = 10.sp, color = PilgrimYellowDark, fontWeight = FontWeight.Bold)
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
    isTravel: Boolean = false,
    caminoStartDate: String = "",
    viewModel: CaminoViewModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (origin: String, dest: String, km: Double, gain: Int, loss: Int, time: String, diff: String, date: String, notes: String, targetDay: Int) -> Unit
) {
    var stageNumberStr by remember { mutableStateOf(nextDay.toString()) }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(initialDate) }
    var distanceStr by remember { mutableStateOf("") }
    var gainStr by remember { mutableStateOf("") }
    var lossStr by remember { mutableStateOf("") }
    var timeStr by remember { mutableStateOf("5h 00m") }
    var difficulty by remember { mutableStateOf("Moderado") }
    var notes by remember { mutableStateOf("") }

    val difficulties = listOf("Fácil", "Moderada", "Difícil", "Muy exigente")
    val titleTerm = if (isTravel) viewModel?.string("day") ?: "Día" else viewModel?.string("stage_prefix") ?: "Etapa"
    val addTerm = viewModel?.string("add_btn") ?: "Añadir"
    val stageNumberLabel = viewModel?.string(if (isTravel) "day_number_label" else "stage_number_label")
        ?: if (isTravel) "Número de Día" else "Número de Etapa"
    val currentDayNum = stageNumberStr.toIntOrNull() ?: nextDay

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$addTerm $titleTerm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                OutlinedTextField(
                    value = stageNumberStr,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }.take(3)
                        stageNumberStr = filtered
                        val parsed = filtered.toIntOrNull()
                        if (parsed != null && parsed > 0 && caminoStartDate.isNotBlank()) {
                            dateStr = AppStrings.calculateStageDate(caminoStartDate, parsed - 1)
                        }
                    },
                    label = { Text(viewModel?.string("number_abbr") ?: "Nº", maxLines = 1) },
                    placeholder = { Text(nextDay.toString()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(72.dp)
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TownAutoCompleteField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = "${viewModel?.string("origin") ?: "Origen"} *",
                        placeholder = "Ej. Roncesvalles",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    TownAutoCompleteField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = "${viewModel?.string("destination") ?: "Destino"} *",
                        placeholder = "Ej. Zubiri",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    val context = LocalContext.current
                    var showDatePicker by remember { mutableStateOf(false) }

                    if (showDatePicker) {
                        val parsed = try {
                            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            LocalDate.parse(dateStr.trim(), formatter)
                        } catch (_: Exception) {
                            try {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                val cal = java.util.Calendar.getInstance()
                                val p = sdf.parse(dateStr.trim())
                                if (p != null) {
                                    cal.time = p
                                    LocalDate.of(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
                                } else LocalDate.now()
                            } catch (_: Exception) {
                                LocalDate.now()
                            }
                        }
                        DisposableEffect(Unit) {
                            val dialog = android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                    dateStr = selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    showDatePicker = false
                                },
                                parsed.year,
                                parsed.monthValue - 1,
                                parsed.dayOfMonth
                            )
                            dialog.setOnCancelListener { showDatePicker = false }
                            dialog.setOnDismissListener { showDatePicker = false }
                            dialog.show()
                            onDispose { dialog.dismiss() }
                        }
                    }

                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("${viewModel?.string("date") ?: "Fecha"} (dd/MM/yyyy)") },
                        placeholder = { Text("Ej. 10/05/2026") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = viewModel?.string("date") ?: "Fecha"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = distanceStr,
                            onValueChange = { distanceStr = it },
                            label = { Text("${viewModel?.string("distance") ?: "Distancia"} (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = timeStr,
                            onValueChange = { timeStr = it },
                            label = { Text(viewModel?.string("est_time") ?: "Tiempo est.") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gainStr,
                            onValueChange = { gainStr = it },
                            label = { Text("${viewModel?.string("elevation_gain_short") ?: "Desnivel +"} (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lossStr,
                            onValueChange = { lossStr = it },
                            label = { Text("${viewModel?.string("elevation_loss_short") ?: "Desnivel -"} (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("${viewModel?.string("difficulty") ?: "Dificultad"}:", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    DifficultySelectionGroup(
                        selectedDifficulty = difficulty,
                        onSelectDifficulty = { difficulty = it },
                        viewModel = viewModel
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(viewModel?.string("notes_label") ?: "Consejos / Notas del día") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (origin.isNotBlank() && destination.isNotBlank()) {
                        val parsed = stageNumberStr.toIntOrNull() ?: nextDay
                        val targetDay = if (parsed > 0) parsed else nextDay
                        onConfirm(
                            origin,
                            destination,
                            distanceStr.toDoubleOrNull() ?: 20.0,
                            gainStr.toIntOrNull() ?: 250,
                            lossStr.toIntOrNull() ?: 250,
                            timeStr,
                            difficulty,
                            dateStr,
                            notes,
                            targetDay
                        )
                    }
                }
            ) {
                Text("$addTerm $titleTerm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(viewModel?.string("cancel") ?: "Cancelar") }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Subir archivo", fontSize = 12.sp, maxLines = 1)
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
                                        else if (file.name.endsWith(".gpx", ignoreCase = true))
                                            Icons.Default.Route
                                        else if (file.mimeType.startsWith("image/"))
                                            Icons.Default.Image
                                        else
                                            Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        tint = if (file.name.endsWith(".gpx", ignoreCase = true)) CaminoGreen else CaminoBlue,
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
                                                    setDataAndType(Uri.parse(file.uri), file.mimeType.ifBlank { "*/*" })
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                try {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = file.mimeType.ifBlank { "*/*" }
                                                        putExtra(Intent.EXTRA_STREAM, Uri.parse(file.uri))
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir o Abrir: ${file.name}"))
                                                } catch (_: Exception) {
                                                    android.widget.Toast.makeText(context, "Archivo disponible en tu dispositivo: ${file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                                }
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
