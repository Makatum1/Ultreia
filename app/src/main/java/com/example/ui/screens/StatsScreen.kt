package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoStats
import com.example.ui.CaminoViewModel
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    camino: CaminoEntity? = null,
    stages: List<StageEntity> = emptyList(),
    stats: CaminoStats? = null,
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTravel = camino?.isTravelGuide == true
    val stageTerm = if (isTravel) "Día" else "Etapa"
    val stageTermPlural = if (isTravel) "Días" else "Etapas"
    val routeTerm = if (isTravel) "Viaje" else "Camino"

    // Sub-pestaña seleccionada: 0 = "Datos de la Etapa" (por defecto), 1 = "Datos Totales"
    var selectedSubTab by remember { mutableIntStateOf(0) }

    // Alojamientos asociados al camino activo para cálculo de gastos
    val caminoAccommodations by viewModel.caminoAccommodations.collectAsStateWithLifecycle()

    // Etapas ordenadas por número de día
    val sortedStages = remember(stages) {
        stages.sortedBy { it.dayNumber }
    }

    // --- SEPARACIÓN DE ESTADOS: DATOS DE LA ETAPA ---
    // Etapa seleccionada actualmente para la pestaña "Datos de la Etapa"
    var selectedStageId by remember(stages) {
        // Por defecto: la primera pendiente o la última etapa disponible
        val defaultId = stages.firstOrNull { !it.isCompleted }?.id
            ?: stages.lastOrNull()?.id
        mutableStateOf(defaultId)
    }

    val currentStage = remember(sortedStages, selectedStageId) {
        sortedStages.firstOrNull { it.id == selectedStageId } ?: sortedStages.lastOrNull()
    }

    var showStageDropdown by remember { mutableStateOf(false) }

    // Alojamientos y gastos de la etapa seleccionada exclusivamente
    val currentStageAccommodations = remember(caminoAccommodations, currentStage) {
        if (currentStage != null) {
            caminoAccommodations.filter { it.stageId == currentStage.id }
        } else {
            emptyList()
        }
    }

    val currentStageExpenseEur = remember(currentStageAccommodations) {
        currentStageAccommodations.sumOf { acc ->
            val options = viewModel.parseRoomOptions(acc.roomOptionsJson)
            if (options.isNotEmpty()) options.sumOf { it.priceEur } else 0.0
        }
    }

    // --- SEPARACIÓN DE ESTADOS: DATOS TOTALES DEL CAMINO/VIAJE ---
    val totalCaminoKm = remember(stages) {
        stages.sumOf { it.distanceKm }
    }
    val completedCaminoKm = remember(stages) {
        stages.filter { it.isCompleted }.sumOf { it.distanceKm }
    }
    val totalCaminoElevationGain = remember(stages) {
        stages.sumOf { it.elevationGainM }
    }
    val completedCaminoElevationGain = remember(stages) {
        stages.filter { it.isCompleted }.sumOf { it.elevationGainM }
    }
    val totalCaminoStagesCount = remember(stages) {
        stages.size
    }
    val completedCaminoStagesCount = remember(stages) {
        stages.count { it.isCompleted }
    }
    val pendingCaminoStagesCount = remember(totalCaminoStagesCount, completedCaminoStagesCount) {
        (totalCaminoStagesCount - completedCaminoStagesCount).coerceAtLeast(0)
    }
    val totalCaminoProgressPercent = remember(totalCaminoKm, completedCaminoKm) {
        if (totalCaminoKm > 0) ((completedCaminoKm / totalCaminoKm) * 100).toInt() else 0
    }
    val totalCaminoExpenseEur = remember(caminoAccommodations) {
        caminoAccommodations.sumOf { acc ->
            val options = viewModel.parseRoomOptions(acc.roomOptionsJson)
            if (options.isNotEmpty()) options.sumOf { it.priceEur } else 0.0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ENCABEZADO SUPERIOR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = viewModel.string("tab_stats"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlue
                        )
                        Text(
                            text = if (selectedSubTab == 0) {
                                "Métricas individuales por $stageTerm"
                            } else {
                                "Métricas acumuladas del $routeTerm"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.exportStatsPdf(context, isTotal = (selectedSubTab == 1)) },
                        modifier = Modifier.testTag("btn_export_stats_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar Datos a PDF",
                            tint = CaminoBlue
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // SELECTOR DE PESTAÑAS (TabRow): "Datos de la Etapa" vs "Datos Totales"
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CaminoBlue
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ViewDay,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Datos de la $stageTerm",
                                    fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_datos_etapa")
                    )

                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Datos Totales",
                                    fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_datos_totales")
                    )
                }
            }
        }

        // CONTENIDO SEGÚN LA SUB-PESTAÑA SELECCIONADA
        AnimatedContent(
            targetState = selectedSubTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "datos_tab_content",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            if (tabIndex == 0) {
                // VISTA: DATOS DE LA ETAPA INDIVIDUAL
                StageDataContent(
                    stage = currentStage,
                    stages = sortedStages,
                    stageTerm = stageTerm,
                    stageAccommodations = currentStageAccommodations,
                    stageExpenseEur = currentStageExpenseEur,
                    showStageDropdown = showStageDropdown,
                    onToggleDropdown = { showStageDropdown = it },
                    onSelectStage = {
                        selectedStageId = it.id
                        showStageDropdown = false
                    }
                )
            } else {
                // VISTA: DATOS TOTALES ACUMULADOS DEL VIAJE/CAMINO
                TotalCaminoDataContent(
                    camino = camino,
                    routeTerm = routeTerm,
                    stageTerm = stageTerm,
                    stageTermPlural = stageTermPlural,
                    totalKm = totalCaminoKm,
                    completedKm = completedCaminoKm,
                    totalElevationGain = totalCaminoElevationGain,
                    completedElevationGain = completedCaminoElevationGain,
                    totalStages = totalCaminoStagesCount,
                    completedStages = completedCaminoStagesCount,
                    pendingStages = pendingCaminoStagesCount,
                    progressPercent = totalCaminoProgressPercent,
                    totalExpenseEur = totalCaminoExpenseEur,
                    totalAccommodationsCount = caminoAccommodations.size,
                    onExportPdf = { viewModel.exportStatsPdf(context, isTotal = true) }
                )
            }
        }
    }
}

/**
 * Contenido exclusivo de la pestaña "Datos de la Etapa"
 */
@Composable
private fun StageDataContent(
    stage: StageEntity?,
    stages: List<StageEntity>,
    stageTerm: String,
    stageAccommodations: List<AccommodationEntity>,
    stageExpenseEur: Double,
    showStageDropdown: Boolean,
    onToggleDropdown: (Boolean) -> Unit,
    onSelectStage: (StageEntity) -> Unit
) {
    if (stage == null || stages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CaminoBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Sin $stageTerm registradas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Añade una o más $stageTerm en la pestaña principal para ver el desglose de datos individuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SELECTOR DE ETAPA ACTUAL
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDropdown(!showStageDropdown) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(CaminoBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${stage.dayNumber}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$stageTerm ${stage.dayNumber}: ${stage.origin} → ${stage.destination}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (stage.date.isNotBlank()) "Fecha: ${stage.date} • Toca para cambiar" else "Toca para cambiar de $stageTerm",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar de etapa",
                        tint = CaminoBlue
                    )
                }

                DropdownMenu(
                    expanded = showStageDropdown,
                    onDismissRequest = { onToggleDropdown(false) },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 350.dp)
                ) {
                    stages.forEach { stg ->
                        val isSelected = stg.id == stage.id
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (isSelected) CaminoBlue else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${stg.dayNumber}",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$stageTerm ${stg.dayNumber}: ${stg.origin} → ${stg.destination}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${stg.distanceKm} km • +${stg.elevationGainM}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = { onSelectStage(stg) }
                        )
                    }
                }
            }
        }

        // TARJETA DE ESTADO Y RESUMEN PRINCIPAL DE LA ETAPA
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CaminoBlue),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (stage.isCompleted) CaminoGreen else PilgrimYellow,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (stage.isCompleted) "✓ COMPLETADA" else "PENDIENTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stage.isCompleted) Color.White else CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (stage.difficulty.isNotBlank()) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Dificultad: ${stage.difficulty}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "${stage.origin} → ${stage.destination}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (stage.title.isNotBlank() && stage.title != "${stage.origin} - ${stage.destination}") {
                        Text(
                            text = stage.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distancia", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                            Text("${stage.distanceKm} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PilgrimYellow)
                        }
                        Column {
                            Text("Desnivel", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                            Text("+${stage.elevationGainM} m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Tiempo", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                            Text(stage.estimatedTime.ifBlank { "--" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // MÉTRICAS INDIVIDUALES DE LA ETAPA
        item {
            Text(
                text = "Detalle de la $stageTerm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaminoBlue
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Desnivel Positivo",
                    value = "+${stage.elevationGainM} m",
                    sub = "Subida de la etapa",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Negativo",
                    value = "-${stage.elevationLossM} m",
                    sub = "Bajada de la etapa",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = PilgrimYellowDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Gasto en Alojamiento",
                    value = "${String.format(Locale.US, "%.2f", stageExpenseEur)} €",
                    sub = "${stageAccommodations.size} alojamientos registrados",
                    icon = Icons.Default.Euro,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Trazado GPX",
                    value = if (!stage.gpxTrackJson.isNullOrBlank()) "Grabado" else "Estimado",
                    sub = if (!stage.gpxTrackJson.isNullOrBlank()) "Track real oficial" else "Trazado estándar",
                    icon = Icons.Default.Route,
                    color = if (!stage.gpxTrackJson.isNullOrBlank()) CaminoGreen else CaminoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // DESGLOSE DE ALOJAMIENTOS DE ESTA ETAPA
        if (stageAccommodations.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Alojamientos de este $stageTerm (${stageAccommodations.size})",
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlue
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", stageExpenseEur)} €",
                                fontWeight = FontWeight.Bold,
                                color = CaminoGreen
                            )
                        }

                        HorizontalDivider()

                        stageAccommodations.forEach { acc ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (acc.phone.isNotBlank()) {
                                        Text(
                                            text = "Tel: ${acc.phone}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val options = remember(acc.roomOptionsJson) {
                                    val list = mutableListOf<String>()
                                    try {
                                        val array = org.json.JSONArray(acc.roomOptionsJson)
                                        for (i in 0 until array.length()) {
                                            val o = array.getJSONObject(i)
                                            list.add("${o.optString("roomType")}: ${o.optDouble("priceEur")}€")
                                        }
                                    } catch (_: Exception) {}
                                    list
                                }

                                if (options.isNotEmpty()) {
                                    Text(
                                        text = options.joinToString(", "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CaminoBlueDark
                                    )
                                } else {
                                    Text(
                                        text = "--",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/**
 * Contenido exclusivo de la pestaña "Datos Totales" (suma y agregación del camino/viaje completo)
 */
@Composable
private fun TotalCaminoDataContent(
    camino: CaminoEntity?,
    routeTerm: String,
    stageTerm: String,
    stageTermPlural: String,
    totalKm: Double,
    completedKm: Double,
    totalElevationGain: Int,
    completedElevationGain: Int,
    totalStages: Int,
    completedStages: Int,
    pendingStages: Int,
    progressPercent: Int,
    totalExpenseEur: Double,
    totalAccommodationsCount: Int,
    onExportPdf: () -> Unit
) {
    val totalKmStr = remember(totalKm) { String.format(Locale.US, "%.1f", totalKm) }
    val completedKmStr = remember(completedKm) { String.format(Locale.US, "%.1f", completedKm) }
    val remainingKm = remember(totalKm, completedKm) { kotlin.math.max(0.0, totalKm - completedKm) }
    val remainingKmStr = remember(remainingKm) { String.format(Locale.US, "%.1f", remainingKm) }

    val avgKmPerStage = remember(totalKm, totalStages) {
        if (totalStages > 0) totalKm / totalStages else 0.0
    }
    val avgElevationPerStage = remember(totalElevationGain, totalStages) {
        if (totalStages > 0) totalElevationGain / totalStages else 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HERO CARD: RESUMEN GLOBAL DEL CAMINO/VIAJE ACTIVO
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_hero_card"),
                colors = CardDefaults.cardColors(containerColor = CaminoBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .background(PilgrimYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "$progressPercent% COMPLETADO DEL ${routeTerm.uppercase()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PilgrimYellow
                    )

                    Text(
                        text = "$completedStages de $totalStages $stageTermPlural finalizadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    if (camino != null && camino.title.isNotBlank()) {
                        Text(
                            text = camino.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PilgrimYellow,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // TÍTULO DE SECCIÓN
        item {
            Text(
                text = "Totales Acumulados del $routeTerm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaminoBlue
            )
        }

        // FILA 1: DISTANCIA TOTAL Y DESNIVEL TOTAL ACUMULADO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Distancia Total",
                    value = "$completedKmStr km",
                    sub = "de $totalKmStr km totales",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Total",
                    value = "+$totalElevationGain m",
                    sub = "+$completedElevationGain m completados",
                    icon = Icons.Default.Landscape,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 2: TOTAL DE ETAPAS Y GASTO TOTAL DEL VIAJE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Total de $stageTermPlural",
                    value = "$totalStages",
                    sub = "$completedStages completadas • $pendingStages pendientes",
                    icon = Icons.Default.ViewDay,
                    color = PilgrimYellow,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Gasto Total Estimado",
                    value = "${String.format(Locale.US, "%.2f", totalExpenseEur)} €",
                    sub = "en $totalAccommodationsCount alojamientos",
                    icon = Icons.Default.Euro,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 3: PROMEDIOS POR ETAPA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Media km / $stageTerm",
                    value = "${String.format(Locale.US, "%.1f", avgKmPerStage)} km",
                    sub = "Promedio diario",
                    icon = Icons.Default.Speed,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Media Desnivel",
                    value = "+$avgElevationPerStage m",
                    sub = "Promedio por $stageTerm",
                    icon = Icons.Default.Terrain,
                    color = PilgrimYellowDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // TARJETA DE DETALLE GENERAL DEL CAMINO/VIAJE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Balance General del $routeTerm",
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nombre de la ruta:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(camino?.title ?: "--", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distancia por recorrer:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$remainingKmStr km", fontWeight = FontWeight.Bold, color = CaminoBlueDark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Desnivel positivo restante:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${(totalElevationGain - completedElevationGain).coerceAtLeast(0)} m", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$stageTermPlural pendientes:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$pendingStages", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alojamientos registrados:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalAccommodationsCount", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // BOTÓN DE EXPORTAR INFORME COMPLETO A PDF
        item {
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CaminoBlue)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Informe Completo a PDF", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaminoBlueDark
            )

            if (sub.isNotBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
