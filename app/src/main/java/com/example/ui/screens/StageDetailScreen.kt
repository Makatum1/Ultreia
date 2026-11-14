package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.AgendaItem
import com.example.data.model.AttachedFile
import com.example.data.model.RoomPriceOption
import com.example.data.model.StageEntity
import com.example.data.model.VivenciaItem
import com.example.ui.CaminoViewModel
import com.example.ui.components.SettingsActionMenu
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageDetailScreen(
    stage: StageEntity,
    accommodations: List<AccommodationEntity>,
    viewModel: CaminoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val activeCamino by viewModel.selectedCamino.collectAsStateWithLifecycle()
    val isTravel = activeCamino?.isTravelGuide == true
    val isCaminoIngles = remember(activeCamino) {
        val title = activeCamino?.title ?: ""
        title.contains("Inglés", ignoreCase = true) || title.contains("Ingles", ignoreCase = true)
    }
    val points = remember(stage.gpxTrackJson) {
        GpxParser.deserializePoints(stage.gpxTrackJson)
    }
    val hasGpx = !stage.gpxTrackJson.isNullOrBlank() && points.isNotEmpty()
    val showFichaTecnica = !isTravel || hasGpx

    // Agenda State (Solo en Viajes)
    var showAddAgendaDialog by remember { mutableStateOf(false) }
    var selectedAgendaItemForDetail by remember { mutableStateOf<AgendaItem?>(null) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    val agendaItems = remember(stage.agendaJson) {
        viewModel.parseAgenda(stage.agendaJson)
    }

    var showEditStageDialog by remember { mutableStateOf(false) }
    var showEditTipsDialog by remember { mutableStateOf(false) }
    var showAddAccommodationDialog by remember { mutableStateOf(false) }
    var showGpxImportDialog by remember { mutableStateOf(false) }

    // Consejos del día expanded state
    var isTipsExpanded by remember { mutableStateOf(false) }

    // Asegura que el GPX del Camino Inglés esté adjunto en Archivos y Documentos de la etapa
    LaunchedEffect(stage.id, activeCamino) {
        viewModel.checkAndAttachStageCaminoInglesGpx(stage, activeCamino)
    }

    // Vivencias State
    var showAddVivenciaDialog by remember { mutableStateOf(false) }
    var vivenciaToEdit by remember { mutableStateOf<VivenciaItem?>(null) }
    var vivenciaToDelete by remember { mutableStateOf<VivenciaItem?>(null) }
    val vivencias = remember(stage.vivenciasJson, stage.diaryNotes, stage.diaryPhotoUri) {
        viewModel.parseVivencias(stage.vivenciasJson, stage.diaryNotes, stage.diaryPhotoUri)
    }

    val formattedStageDate = remember(stage) {
        viewModel.resolveStageDate(stage)
    }

    // GPX file picker
    val gpxFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    viewModel.importGpxToStage(stage.id, content, markAsCompleted = false)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer archivo GPX: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val caminoName = activeCamino?.title ?: "Ruta"
                        Text(
                            text = "Estás en: $caminoName",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlue
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Día ${stage.dayNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (formattedStageDate.isNotBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = PilgrimYellow.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = formattedStageDate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PilgrimYellowDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${stage.origin} → ${stage.destination}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditStageDialog = true },
                        modifier = Modifier.testTag("stage_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar etapa", tint = CaminoBlue)
                    }
                    IconButton(
                        onClick = { showGpxImportDialog = true },
                        modifier = Modifier.testTag("stage_gpx_button")
                    ) {
                        Icon(
                            if (hasGpx) Icons.Default.Sync else Icons.Default.FileUpload,
                            contentDescription = if (hasGpx) "Sustituir GPX" else "Subir GPX",
                            tint = CaminoBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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

            // Stage Status and Quick Completion Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (stage.isCompleted) CaminoGreen.copy(alpha = 0.12f) else CaminoBlueLight
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (stage.isCompleted) CaminoGreen else CaminoBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (stage.isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (stage.isCompleted) "¡Etapa Finalizada!" else "Etapa Pendiente",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stage.isCompleted) CaminoGreen else CaminoBlueDark
                                )
                                Text(
                                    text = if (stage.isCompleted) "Sincronizada en el mapa" else "Modifica datos o sube tu GPX",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleStageCompletion(stage) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (stage.isCompleted) CaminoGreen else CaminoBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("btn_toggle_stage_completed")
                        ) {
                            Text(
                                text = if (stage.isCompleted) "Completada ✓" else "Marcar Hecha",
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Route Metrics Card (Ficha Técnica: En Viajes solo visible si tiene GPX; en Caminos siempre)
            if (showFichaTecnica) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isTravel) "Ficha Técnica del Día" else "Ficha Técnica de la Etapa",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )

                                IconButton(
                                    onClick = { showEditStageDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modificar etapa", tint = CaminoBlue, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StageMetricItem("Distancia", "${stage.distanceKm} km", Icons.Default.Straighten)
                                StageMetricItem("Desnivel +", "+${stage.elevationGainM} m", Icons.AutoMirrored.Filled.TrendingUp)
                                StageMetricItem("Desnivel -", "-${stage.elevationLossM} m", Icons.AutoMirrored.Filled.TrendingDown)
                                StageMetricItem("Tiempo", stage.estimatedTime, Icons.Default.Schedule)
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terrain, contentDescription = null, tint = PilgrimYellowDark, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Dificultad: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(stage.difficulty, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }

                                // GPX sync indicator
                                Surface(
                                    color = if (points.isNotEmpty()) CaminoGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Route,
                                            contentDescription = null,
                                            tint = if (points.isNotEmpty()) CaminoGreen else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (points.isNotEmpty()) "${points.size} pts GPX" else "Sin GPX",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (points.isNotEmpty()) CaminoGreen else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Map Navigation & GPX upload buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val uriStr = "geo:0,0?q=" + Uri.encode("${stage.origin} to ${stage.destination}")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val webUri = "https://www.openstreetmap.org/search?query=" + Uri.encode(stage.destination)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUri)))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_stage_map_nav"),
                        colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ver en Mapas", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showGpxImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_stage_upload_gpx"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CaminoBlue)
                    ) {
                        Icon(
                            if (hasGpx) Icons.Default.Sync else Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (hasGpx) "Sustituir GPX" else "Subir GPX", fontSize = 12.sp)
                    }
                }
            }

            // CONSEJOS DEL DÍA (Editable, 3 lines with ellipsis & tap to expand)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stage_tips_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isTravel) Icons.Default.Article else Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = PilgrimYellowDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isTravel) "Resumen del Día" else "Consejos del Día",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )
                            }

                            TextButton(
                                onClick = { showEditTipsDialog = true },
                                modifier = Modifier.testTag("btn_edit_tips")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = CaminoBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("Editar", fontSize = 12.sp, color = CaminoBlue)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        val tipsText = stage.stageNotes.ifBlank {
                            if (isTravel) "No hay un resumen registrado para hoy. Toca en 'Editar' para escribir qué te espera en la jornada de hoy."
                            else "No hay consejos específicos registrados para hoy. Toca en 'Editar' para añadir recomendaciones de calzado, fuentes de agua o tramos técnicos."
                        }

                        // 3-line text with ellipsis and tap to toggle full text
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isTipsExpanded = !isTipsExpanded }
                                .animateContentSize()
                        ) {
                            Text(
                                text = tipsText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isTipsExpanded) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = if (isTipsExpanded) "Ver menos ▲" else "... Ver más ▼",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )
                            }
                        }

                        if (isCaminoIngles) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        tint = CaminoBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Variante recomendada:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CaminoBlue
                                        )
                                        Text(
                                            text = "Ruta por el Trazado Antiguo",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            color = CaminoBlue,
                                            modifier = Modifier.clickable {
                                                try {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://es.wikiloc.com/rutas-senderismo/ferrol-a-santiago-camino-ingles-por-trazado-antiguo-128031925")
                                                    )
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No se pudo abrir el navegador", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // NUEVA SECCIÓN: AGENDA DEL DÍA (Solo en Viajes, debajo del resumen)
            if (isTravel) {
                item {
                    AgendaSectionCard(
                        agendaItems = agendaItems,
                        onAddClick = { showAddAgendaDialog = true },
                        onItemClick = { item -> selectedAgendaItemForDetail = item }
                    )
                }
            }

            // ALOJAMIENTOS SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Alojamientos (${accommodations.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )

                    FilledTonalButton(
                        onClick = { showAddAccommodationDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_accommodation")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir", fontSize = 12.sp)
                    }
                }
            }

            if (accommodations.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Hotel, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No hay alojamientos guardados para este día",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Añade albergues u hostales con sus teléfonos, ubicación en el mapa y precios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(accommodations) { acc ->
                    AccommodationCard(
                        acc = acc,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }

            // VIVENCIAS DIARIAS (Multiple journal entries with notes & photos)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Vivencias Diarias (${vivencias.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlue
                        )
                        Text(
                            text = "Añade tantas anotaciones y fotos como desees",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddVivenciaDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .testTag("btn_add_vivencia")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir vivencia", fontSize = 12.sp, maxLines = 1)
                    }
                }
            }

            if (vivencias.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Sin vivencias registradas aún para este día",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pulsa 'Añadir vivencia' para guardar anécdotas, recuerdos y fotos ilimitadas de la jornada.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(vivencias) { viv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vivencia_item_${viv.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = CaminoBlueLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(12.dp), tint = CaminoBlueDark)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (viv.dateStr.isNotBlank()) viv.dateStr else "Vivencia del día",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CaminoBlueDark
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { vivenciaToEdit = viv },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = CaminoBlue, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { vivenciaToDelete = viv },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (viv.note.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = viv.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!viv.photoUri.isNullOrBlank()) {
                                Spacer(Modifier.height(10.dp))
                                AsyncImage(
                                    model = viv.photoUri,
                                    contentDescription = "Foto de vivencia",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // ARCHIVOS Y DOCUMENTOS DE LA ETAPA (Right below vivencias)
            item {
                StageAttachedFilesSection(
                    stage = stage,
                    viewModel = viewModel
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Edit Stage Dialog (Manual origin, destination, distance, elevation, time, difficulty, date)
    if (showEditStageDialog) {
        EditStageDialog(
            stage = stage,
            onDismiss = { showEditStageDialog = false },
            onSave = { updated ->
                viewModel.updateStage(updated)
                showEditStageDialog = false
            }
        )
    }

    // Edit Tips Dialog
    if (showEditTipsDialog) {
        EditTipsDialog(
            currentTips = stage.stageNotes,
            isTravel = isTravel,
            onDismiss = { showEditTipsDialog = false },
            onSave = { newTips ->
                viewModel.updateStageTips(stage.id, newTips)
                showEditTipsDialog = false
            }
        )
    }

    // Add Accommodation Dialog with Room Type Selector
    if (showAddAccommodationDialog) {
        AddAccommodationDialog(
            stageId = stage.id,
            onDismiss = { showAddAccommodationDialog = false },
            onSave = { name, address, phone, options, notes ->
                viewModel.addAccommodation(stage.id, name, address, phone, options, notes)
                showAddAccommodationDialog = false
            }
        )
    }

    // GPX Import Dialog
    if (showGpxImportDialog) {
        GpxImportDialog(
            stage = stage,
            onDismiss = { showGpxImportDialog = false },
            onPickFile = {
                showGpxImportDialog = false
                gpxFileLauncher.launch("*/*")
            }
        )
    }

    // Add / Edit Vivencia Dialogs
    if (showAddVivenciaDialog) {
        AddEditVivenciaDialog(
            stageDate = formattedStageDate,
            initialVivencia = null,
            onDismiss = { showAddVivenciaDialog = false },
            onSave = { note, photoUri, dateStr ->
                viewModel.addVivenciaToStage(stage.id, note, photoUri, dateStr)
                showAddVivenciaDialog = false
            }
        )
    }

    if (vivenciaToEdit != null) {
        AddEditVivenciaDialog(
            stageDate = formattedStageDate,
            initialVivencia = vivenciaToEdit,
            onDismiss = { vivenciaToEdit = null },
            onSave = { note, photoUri, dateStr ->
                viewModel.updateVivenciaInStage(
                    stage.id,
                    vivenciaToEdit!!.copy(note = note, photoUri = photoUri, dateStr = dateStr)
                )
                vivenciaToEdit = null
            }
        )
    }

    if (vivenciaToDelete != null) {
        AlertDialog(
            onDismissRequest = { vivenciaToDelete = null },
            title = { Text("¿Eliminar vivencia?") },
            text = { Text("¿Deseas eliminar esta anotación y foto de la etapa? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVivenciaFromStage(stage.id, vivenciaToDelete!!.id)
                        vivenciaToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { vivenciaToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Agenda Dialogs (Solo en Viajes)
    if (showAddAgendaDialog) {
        AddAgendaDialog(
            stageId = stage.id,
            viewModel = viewModel,
            onDismiss = { showAddAgendaDialog = false }
        )
    }

    if (selectedAgendaItemForDetail != null) {
        AgendaItemDetailDialog(
            stageId = stage.id,
            item = selectedAgendaItemForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedAgendaItemForDetail = null },
            onPreviewPhoto = { uri -> previewPhotoUri = uri }
        )
    }

    if (previewPhotoUri != null) {
        FullscreenImageViewerDialog(
            photoUri = previewPhotoUri!!,
            onDismiss = { previewPhotoUri = null }
        )
    }
}

@Composable
fun StageMetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AccommodationCard(
    acc: AccommodationEntity,
    viewModel: CaminoViewModel,
    context: Context
) {
    val roomOptions: List<RoomPriceOption> = remember(acc.roomOptionsJson) {
        viewModel.parseRoomOptions(acc.roomOptionsJson)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = acc.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue
                )

                IconButton(
                    onClick = { viewModel.deleteAccommodation(acc) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }

            if (acc.address.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(acc.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (acc.phone.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(acc.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Room Options & Prices list
            if (roomOptions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Tipos de Habitación y Precios:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlueDark
                )
                Spacer(Modifier.height(4.dp))
                for (opt in roomOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${opt.roomType}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${opt.priceEur} €",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CaminoGreen
                        )
                    }
                }
            }

            if (acc.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = acc.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons: Open in Google Maps & Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button to open location in Google Maps
                Button(
                    onClick = {
                        val query = if (acc.address.isNotBlank()) acc.address else acc.name
                        val mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
                        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_acc_open_maps"),
                    colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver en Mapa", fontSize = 12.sp)
                }

                // Button to Call
                OutlinedButton(
                    onClick = {
                        if (acc.phone.isNotBlank()) {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${acc.phone}"))
                            try {
                                context.startActivity(callIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir el marcador telefónico", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "No hay teléfono registrado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_acc_call"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Llamar", fontSize = 12.sp)
                }
            }
        }
    }
}

// Dialog for manual stage edit (origin, destination, distance, elevation, time, difficulty, date, notes)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStageDialog(
    stage: StageEntity,
    onDismiss: () -> Unit,
    onSave: (StageEntity) -> Unit
) {
    var origin by remember { mutableStateOf(stage.origin) }
    var destination by remember { mutableStateOf(stage.destination) }
    var dateStr by remember { mutableStateOf(stage.date) }
    var distanceStr by remember { mutableStateOf(stage.distanceKm.toString()) }
    var gainStr by remember { mutableStateOf(stage.elevationGainM.toString()) }
    var lossStr by remember { mutableStateOf(stage.elevationLossM.toString()) }
    var timeStr by remember { mutableStateOf(stage.estimatedTime) }
    var difficulty by remember { mutableStateOf(stage.difficulty) }
    var notes by remember { mutableStateOf(stage.stageNotes) }

    val difficulties = listOf("Fácil", "Moderada", "Difícil", "Muy exigente")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modificar Etapa Día ${stage.dayNumber}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Origen *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destino *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Fecha de la etapa (dd/MM/yyyy)") },
                        placeholder = { Text("Ej. 10/05/2026") },
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
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Fácil", "Moderada").forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = {
                                    Text(
                                        diff,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 40.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Exigente", "Muy exigente").forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = {
                                    Text(
                                        diff,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 40.dp)
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
                    onSave(
                        stage.copy(
                            origin = origin,
                            destination = destination,
                            date = dateStr,
                            title = "Etapa ${stage.dayNumber}: $origin a $destination",
                            distanceKm = distanceStr.toDoubleOrNull() ?: stage.distanceKm,
                            elevationGainM = gainStr.toIntOrNull() ?: stage.elevationGainM,
                            elevationLossM = lossStr.toIntOrNull() ?: stage.elevationLossM,
                            estimatedTime = timeStr,
                            difficulty = difficulty,
                            stageNotes = notes
                        )
                    )
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Dialog for editing Tips of the Day / Resumen del Día
@Composable
fun EditTipsDialog(
    currentTips: String,
    isTravel: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tips by remember { mutableStateOf(currentTips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isTravel) Icons.Default.Article else Icons.Default.Lightbulb, contentDescription = null, tint = PilgrimYellowDark)
                Spacer(Modifier.width(8.dp))
                Text(if (isTravel) "Editar Resumen del Día" else "Editar Consejos del Día")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isTravel) "Escribe un resumen o introducción de lo que te espera en la jornada." else "Añade advertencias sobre el terreno, fuentes de agua, sombras o tramos recomendados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    placeholder = {
                        Text(if (isTravel) "Escribe aquí el resumen de la jornada (lugares que visitar, planes previstos, expectativas)..." else "Escribe aquí los consejos y notas (texto ilimitado)...")
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(tips) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Dialog for adding accommodation with button room selector: Litera, Individual, Doble, Triple, Personalizable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccommodationDialog(
    stageId: Long,
    onDismiss: () -> Unit,
    onSave: (name: String, address: String, phone: String, options: List<RoomPriceOption>, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val options = remember {
        mutableStateListOf<RoomPriceOption>()
    }

    // Room Type Selector state
    val standardTypes = listOf("Litera", "Individual", "Doble", "Triple", "Personalizable")
    var selectedTypeChoice by remember { mutableStateOf("Litera") }
    var customTypeName by remember { mutableStateOf("") }
    var roomPriceStr by remember { mutableStateOf("15") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Alojamiento") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del Alojamiento *") },
                        placeholder = { Text("Ej. Albergue u Hotel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        placeholder = { Text("Calle, número o localidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono (para llamar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tipo de Habitación y Precios:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                }

                // Existing room options list
                items(options) { opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${opt.roomType}: ${opt.priceEur}€", style = MaterialTheme.typography.bodySmall)
                        IconButton(
                            onClick = { options.remove(opt) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Room Type Selector Button & Menu (Litera, Individual, Doble, Triple, Personalizable)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Añadir opción de habitación:", style = MaterialTheme.typography.labelSmall)

                        // Selector Button
                        Box {
                            OutlinedButton(
                                onClick = { typeDropdownExpanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_select_room_type"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tipo: $selectedTypeChoice",
                                        fontWeight = FontWeight.Bold,
                                        color = CaminoBlue
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CaminoBlue)
                                }
                            }

                            DropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                standardTypes.forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption, fontWeight = if (selectedTypeChoice == typeOption) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (typeOption) {
                                                    "Litera" -> Icons.Default.Bed
                                                    "Individual" -> Icons.Default.Person
                                                    "Doble" -> Icons.Default.Group
                                                    "Triple" -> Icons.Default.Groups
                                                    else -> Icons.Default.EditNote
                                                },
                                                contentDescription = null,
                                                tint = CaminoBlue
                                            )
                                        },
                                        onClick = {
                                            selectedTypeChoice = typeOption
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // If "Personalizable" is selected, show custom text input
                        if (selectedTypeChoice == "Personalizable") {
                            OutlinedTextField(
                                value = customTypeName,
                                onValueChange = { customTypeName = it },
                                label = { Text("Escribe el tipo personalizado") },
                                placeholder = { Text("Ej. Suite con terraza / Habitación cuádruple") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_room_type")
                            )
                        }

                        // Price and Add Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = roomPriceStr,
                                onValueChange = { roomPriceStr = it },
                                label = { Text("Precio (€)") },
                                placeholder = { Text("€") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    val finalType = if (selectedTypeChoice == "Personalizable") {
                                        customTypeName.ifBlank { "Personalizable" }
                                    } else {
                                        selectedTypeChoice
                                    }
                                    val price = roomPriceStr.toDoubleOrNull() ?: 15.0
                                    options.add(RoomPriceOption(finalType, price))
                                    if (selectedTypeChoice == "Personalizable") {
                                        customTypeName = ""
                                        selectedTypeChoice = "Litera"
                                    }
                                    roomPriceStr = "15"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Añadir")
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observaciones (cocina, lavadora, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, address, phone, options.toList(), notes)
                    }
                }
            ) {
                Text("Guardar Alojamiento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Dialog for GPX import options
@Composable
fun GpxImportDialog(
    stage: StageEntity,
    onDismiss: () -> Unit,
    onPickFile: () -> Unit
) {
    val hasGpx = !stage.gpxTrackJson.isNullOrBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (hasGpx) Icons.Default.Sync else Icons.Default.Route, contentDescription = null, tint = CaminoBlue)
                Spacer(Modifier.width(8.dp))
                Text(if (hasGpx) "Sustituir Archivo GPX" else "Sincronizar GPX de Etapa")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (hasGpx) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Esta etapa ya cuenta con un archivo GPX registrado (máximo 1 archivo por etapa). Si seleccionas un nuevo archivo, se sustituirá por completo el recorrido anterior.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    Text(
                        text = "Sube tu archivo .GPX grabado durante la ruta. Se actualizarán automáticamente la distancia, desnivel, tiempo y se sincronizará el recorrido en el mapa (máximo 1 archivo GPX por etapa).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onPickFile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(if (hasGpx) Icons.Default.Sync else Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (hasGpx) "Sustituir por nuevo archivo GPX" else "Seleccionar archivo GPX del dispositivo")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun AddEditVivenciaDialog(
    stageDate: String,
    initialVivencia: VivenciaItem?,
    onDismiss: () -> Unit,
    onSave: (note: String, photoUri: String?, dateStr: String) -> Unit
) {
    var noteText by remember { mutableStateOf(initialVivencia?.note ?: "") }
    var photoUri by remember { mutableStateOf<String?>(initialVivencia?.photoUri) }
    var dateStr by remember { mutableStateOf(initialVivencia?.dateStr ?: stageDate) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = CaminoBlue)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (initialVivencia == null) "Añadir Nueva Vivencia" else "Editar Vivencia",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = "Registra una anécdota, vivencia o recuerdo de la jornada junto con una fotografía.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Fecha / Momento") },
                        placeholder = { Text("Ej. 12/05/2026 o Mañana en el alto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Anotación o recuerdo de la vivencia *") },
                        placeholder = { Text("Escribe aquí lo vivido, anécdotas, sensaciones, compañeros...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        maxLines = 10
                    )
                }

                item {
                    Text("Fotografía de la Vivencia:", style = MaterialTheme.typography.labelSmall)
                    if (!photoUri.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto de vivencia",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { photoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
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
                        Text(if (photoUri.isNullOrBlank()) "Adjuntar Foto" else "Cambiar Foto")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noteText.isNotBlank() || !photoUri.isNullOrBlank()) {
                        onSave(noteText, photoUri, dateStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun StageAttachedFilesSection(
    stage: StageEntity,
    viewModel: CaminoViewModel
) {
    val context = LocalContext.current
    val attachedFiles = remember(stage.attachedFilesJson) {
        viewModel.parseAttachedFiles(stage.attachedFilesJson)
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
                viewModel.addStageAttachedFile(stage.id, displayName, uri.toString(), mimeType = mimeType)
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo adjuntar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stage_attached_files_card"),
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
                    text = "Sube billetes, reservas de albergue, tracks o documentos para esta etapa.",
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
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
                                                    Toast.makeText(context, "Archivo disponible en tu dispositivo: ${file.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
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
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Pulsa para abrir el documento",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.removeStageAttachedFile(stage.id, file.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Eliminar archivo",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENTES DE LA AGENDA DEL DÍA (Exclusivo Perfil Viajes)
// -------------------------------------------------------------

@Composable
fun AgendaSectionCard(
    agendaItems: List<AgendaItem>,
    onAddClick: () -> Unit,
    onItemClick: (AgendaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stage_agenda_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CaminoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Agenda del Día (${agendaItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                }

                FilledTonalButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir a la Agenda", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            if (agendaItems.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay actividades agendadas aún. Pulsa en 'Añadir a la Agenda' para planificar visitas y horarios de hoy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                val sorted = remember(agendaItems) { agendaItems.sortedBy { it.time } }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sorted.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Hora Badge
                                    Surface(
                                        color = PilgrimYellow.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.time,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PilgrimYellowDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    // Título
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (item.files.isNotEmpty()) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = CaminoBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
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
fun AddAgendaDialog(
    stageId: Long,
    viewModel: CaminoViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var time by remember { mutableStateOf("09:00") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachedFiles by remember { mutableStateOf<List<AttachedFile>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = "Documento"
                var sizeBytes = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) displayName = cursor.getString(nameIdx) ?: displayName
                        if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val newFile = AttachedFile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = displayName,
                    uri = uri.toString(),
                    mimeType = mimeType,
                    sizeBytes = sizeBytes
                )
                attachedFiles = attachedFiles + newFile
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo adjuntar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = CaminoBlue)
                Spacer(Modifier.width(8.dp))
                Text("Añadir a la Agenda")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector de Hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hora del evento:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                    OutlinedButton(
                        onClick = {
                            val parts = time.split(":")
                            val initH = parts.getOrNull(0)?.toIntOrNull() ?: 9
                            val initM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            TimePickerDialog(context, { _, hourOfDay, minute ->
                                time = String.format("%02d:%02d", hourOfDay, minute)
                            }, initH, initM, true).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(time, fontWeight = FontWeight.Bold)
                    }
                }

                // Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la actividad *") },
                    placeholder = { Text("Ej. Visita al museo, Comida, Traslado...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Descripción multilínea (ilimitada)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Detalles, direcciones, notas (texto ilimitado)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    singleLine = false,
                    maxLines = Int.MAX_VALUE
                )

                // Botón Añadir Documento/Foto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Archivos (${attachedFiles.size}):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                    FilledTonalButton(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir Documento/Foto", fontSize = 11.sp)
                    }
                }

                // Lista de archivos seleccionados
                if (attachedFiles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        attachedFiles.forEach { file ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val isImg = isImageAttached(file)
                                    Icon(
                                        if (isImg) Icons.Default.Image else Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = CaminoBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = file.name,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { attachedFiles = attachedFiles.filter { it.id != file.id } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
                    if (title.isBlank()) {
                        Toast.makeText(context, "Por favor escribe un título", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val item = AgendaItem(
                        time = time,
                        title = title.trim(),
                        description = description.trim(),
                        files = attachedFiles
                    )
                    viewModel.addAgendaItem(stageId, item)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AgendaItemDetailDialog(
    stageId: Long,
    item: AgendaItem,
    viewModel: CaminoViewModel,
    onDismiss: () -> Unit,
    onPreviewPhoto: (String) -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editTime by remember(item) { mutableStateOf(item.time) }
    var editTitle by remember(item) { mutableStateOf(item.title) }
    var editDescription by remember(item) { mutableStateOf(item.description) }
    var editFiles by remember(item) { mutableStateOf(item.files) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = "Documento"
                var sizeBytes = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) displayName = cursor.getString(nameIdx) ?: displayName
                        if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val newFile = AttachedFile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = displayName,
                    uri = uri.toString(),
                    mimeType = mimeType,
                    sizeBytes = sizeBytes
                )
                editFiles = editFiles + newFile
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo adjuntar el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Estás seguro de que quieres eliminar \"${item.title}\" de la agenda?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAgendaItem(stageId, item.id)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Editar Evento" else "Detalle de Agenda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlue
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isEditing) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = CaminoBlue)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!isEditing) {
                        // MODO LECTURA
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = PilgrimYellow.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = PilgrimYellowDark, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = item.time,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PilgrimYellowDark
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column {
                            Text(
                                text = "Descripción:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            if (item.description.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Sin descripción adicional.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Archivos y fotos
                        val images = item.files.filter { isImageAttached(it) }
                        val otherFiles = item.files.filter { it !in images }

                        if (images.isNotEmpty()) {
                            Text(
                                text = "Fotos adjuntas (Toca una foto para ampliarla):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(images) { imgFile ->
                                    Card(
                                        modifier = Modifier
                                            .size(105.dp)
                                            .clickable { onPreviewPhoto(imgFile.uri) },
                                        shape = RoundedCornerShape(10.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model = imgFile.uri,
                                                contentDescription = imgFile.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ZoomIn,
                                                    contentDescription = "Ampliar",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (otherFiles.isNotEmpty()) {
                            Text(
                                text = "Documentos adjuntos:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                otherFiles.forEach { file ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(Uri.parse(file.uri), file.mimeType.ifBlank { "*/*" })
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(file.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // MODO EDICIÓN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hora:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            OutlinedButton(
                                onClick = {
                                    val parts = editTime.split(":")
                                    val initH = parts.getOrNull(0)?.toIntOrNull() ?: 9
                                    val initM = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    TimePickerDialog(context, { _, hourOfDay, minute ->
                                        editTime = String.format("%02d:%02d", hourOfDay, minute)
                                    }, initH, initM, true).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(editTime, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Título de la actividad *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Archivos y Fotos (${editFiles.size}):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                            FilledTonalButton(
                                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Añadir Documento/Foto", fontSize = 11.sp)
                            }
                        }

                        // Opción de eliminar cada archivo individualmente (papelera)
                        if (editFiles.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                editFiles.forEach { file ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val isImg = isImageAttached(file)
                                            if (isImg) {
                                                AsyncImage(
                                                    model = file.uri,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                )
                                            } else {
                                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { editFiles = editFiles.filter { it.id != file.id } }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar archivo", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botones al editar
                if (isEditing) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editTitle.isBlank()) {
                                    Toast.makeText(context, "Por favor escribe un título", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val updated = item.copy(
                                    time = editTime,
                                    title = editTitle.trim(),
                                    description = editDescription.trim(),
                                    files = editFiles
                                )
                                viewModel.updateAgendaItem(stageId, updated)
                                isEditing = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue)
                        ) {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenImageViewerDialog(
    photoUri: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto ampliada",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Pellizca con dos dedos para hacer zoom",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private fun isImageAttached(file: AttachedFile): Boolean {
    val mime = file.mimeType.lowercase()
    val name = file.name.lowercase()
    val uri = file.uri.lowercase()
    return mime.startsWith("image/") ||
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp") ||
            uri.contains(".jpg") || uri.contains(".jpeg") || uri.contains(".png")
}
