package com.example.ui.screens

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
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
import coil.compose.AsyncImage
import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
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
    val isCaminoIngles = remember(activeCamino) {
        val title = activeCamino?.title ?: ""
        title.contains("Inglés", ignoreCase = true) || title.contains("Ingles", ignoreCase = true)
    }
    val hasGpx = !stage.gpxTrackJson.isNullOrBlank()

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

            // Route Metrics Card
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
                                text = "Ficha Técnica de la Etapa",
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
                            val points = remember(stage.gpxTrackJson) {
                                GpxParser.deserializePoints(stage.gpxTrackJson)
                            }
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
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PilgrimYellowDark, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Consejos del Día",
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
                            "No hay consejos específicos registrados para hoy. Toca en 'Editar' para añadir recomendaciones de calzado, fuentes de agua o tramos técnicos."
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

// Dialog for editing Tips of the Day
@Composable
fun EditTipsDialog(
    currentTips: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tips by remember { mutableStateOf(currentTips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PilgrimYellowDark)
                Spacer(Modifier.width(8.dp))
                Text("Editar Consejos del Día")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Añade advertencias sobre el terreno, fuentes de agua, sombras o tramos recomendados.",
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
                    placeholder = { Text("Escribe aquí los consejos y notas (texto ilimitado)...") }
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
