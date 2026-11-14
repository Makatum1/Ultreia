package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
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

    // Sub-pestaña seleccionada: 0 = "Datos del Camino" (por defecto), 1 = "Datos Totales"
    var selectedSubTab by remember { mutableIntStateOf(0) }

    // Alojamientos asociados al camino activo para cálculo de gastos
    val caminoAccommodations by viewModel.caminoAccommodations.collectAsStateWithLifecycle()

    // Datos globales de todos los caminos
    val allCaminos by viewModel.caminos.collectAsStateWithLifecycle()
    val allStages by viewModel.allStages.collectAsStateWithLifecycle()
    val allAccommodations by viewModel.allAccommodations.collectAsStateWithLifecycle()

    // --- SEPARACIÓN DE ESTADOS: DATOS DEL CAMINO ACTIVO (Basados en etapas completadas) ---
    val completedStagesList = remember(stages) { stages.filter { it.isCompleted } }
    val totalCaminoKm = remember(stages) {
        stages.sumOf { it.distanceKm }
    }
    val completedCaminoKm = remember(completedStagesList) {
        completedStagesList.sumOf { it.distanceKm }
    }
    val totalCaminoElevationGain = remember(stages) {
        stages.sumOf { it.elevationGainM }
    }
    val completedCaminoElevationGain = remember(completedStagesList) {
        completedStagesList.sumOf { it.elevationGainM }
    }
    val completedCaminoElevationLoss = remember(completedStagesList) {
        completedStagesList.sumOf { it.elevationLossM }
    }
    val totalCaminoStagesCount = remember(stages) {
        stages.size
    }
    val completedCaminoStagesCount = remember(completedStagesList) {
        completedStagesList.size
    }
    val pendingCaminoStagesCount = remember(totalCaminoStagesCount, completedCaminoStagesCount) {
        (totalCaminoStagesCount - completedCaminoStagesCount).coerceAtLeast(0)
    }
    val caminoProgressPercent = remember(totalCaminoKm, completedCaminoKm) {
        if (totalCaminoKm > 0) ((completedCaminoKm / totalCaminoKm) * 100).toInt().coerceIn(0, 100) else 0
    }
    val completedCaminoTimeStr = remember(completedStagesList) {
        calculateTotalTimeString(completedStagesList)
    }
    val avgKmPerCaminoCompletedStage = remember(completedCaminoKm, completedCaminoStagesCount) {
        if (completedCaminoStagesCount > 0) completedCaminoKm / completedCaminoStagesCount else 0.0
    }
    val caminoExpenseEur = remember(caminoAccommodations) {
        caminoAccommodations.sumOf { acc ->
            val options = viewModel.parseRoomOptions(acc.roomOptionsJson)
            if (options.isNotEmpty()) options.sumOf { it.priceEur } else 0.0
        }
    }

    // --- SEPARACIÓN DE ESTADOS: DATOS TOTALES GLOBALES (Basados en etapas completadas de todos los caminos) ---
    val completedGlobalStagesList = remember(allStages) { allStages.filter { it.isCompleted } }
    val totalGlobalCaminosRealizados = remember(allCaminos, allStages) {
        allCaminos.count { c ->
            val cStages = allStages.filter { it.caminoId == c.id }
            cStages.isNotEmpty() && cStages.all { it.isCompleted }
        }
    }
    val totalGlobalKm = remember(allStages) {
        allStages.sumOf { it.distanceKm }
    }
    val completedGlobalKm = remember(completedGlobalStagesList) {
        completedGlobalStagesList.sumOf { it.distanceKm }
    }
    val totalGlobalElevationGain = remember(allStages) {
        allStages.sumOf { it.elevationGainM }
    }
    val completedGlobalElevationGain = remember(completedGlobalStagesList) {
        completedGlobalStagesList.sumOf { it.elevationGainM }
    }
    val completedGlobalElevationLoss = remember(completedGlobalStagesList) {
        completedGlobalStagesList.sumOf { it.elevationLossM }
    }
    val totalGlobalStagesCount = remember(allStages) {
        allStages.size
    }
    val completedGlobalStagesCount = remember(completedGlobalStagesList) {
        completedGlobalStagesList.size
    }
    val pendingGlobalStagesCount = remember(totalGlobalStagesCount, completedGlobalStagesCount) {
        (totalGlobalStagesCount - completedGlobalStagesCount).coerceAtLeast(0)
    }
    val completedGlobalTimeStr = remember(completedGlobalStagesList) {
        calculateTotalTimeString(completedGlobalStagesList)
    }
    val avgKmPerGlobalCompletedStage = remember(completedGlobalKm, completedGlobalStagesCount) {
        if (completedGlobalStagesCount > 0) completedGlobalKm / completedGlobalStagesCount else 0.0
    }
    val totalGlobalExpenseEur = remember(allAccommodations) {
        allAccommodations.sumOf { acc ->
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
                                "Métricas acumuladas del $routeTerm activo"
                            } else {
                                "Métricas acumuladas de todos los caminos"
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

                // SELECTOR DE PESTAÑAS (TabRow): "Datos del Camino" vs "Datos Totales"
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
                                    imageVector = if (isTravel) Icons.Default.Luggage else Icons.Default.Hiking,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Datos del $routeTerm",
                                    fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_datos_camino")
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
                // VISTA: DATOS DEL CAMINO ACTIVO
                ActiveCaminoDataContent(
                    camino = camino,
                    routeTerm = routeTerm,
                    stageTerm = stageTerm,
                    stageTermPlural = stageTermPlural,
                    totalKm = totalCaminoKm,
                    completedKm = completedCaminoKm,
                    totalElevationGain = totalCaminoElevationGain,
                    completedElevationGain = completedCaminoElevationGain,
                    completedElevationLoss = completedCaminoElevationLoss,
                    completedTimeStr = completedCaminoTimeStr,
                    avgKmPerStage = avgKmPerCaminoCompletedStage,
                    totalStages = totalCaminoStagesCount,
                    completedStages = completedCaminoStagesCount,
                    pendingStages = pendingCaminoStagesCount,
                    progressPercent = caminoProgressPercent,
                    totalExpenseEur = caminoExpenseEur,
                    caminoAccommodations = caminoAccommodations,
                    viewModel = viewModel,
                    onExportPdf = { viewModel.exportStatsPdf(context, isTotal = false) }
                )
            } else {
                // VISTA: DATOS TOTALES GLOBALES (TODOS LOS CAMINOS)
                GlobalTotalDataContent(
                    totalCaminosRealizados = totalGlobalCaminosRealizados,
                    totalCaminosCount = allCaminos.size,
                    totalKm = totalGlobalKm,
                    completedKm = completedGlobalKm,
                    totalElevationGain = totalGlobalElevationGain,
                    completedElevationGain = completedGlobalElevationGain,
                    completedElevationLoss = completedGlobalElevationLoss,
                    completedTimeStr = completedGlobalTimeStr,
                    avgKmPerStage = avgKmPerGlobalCompletedStage,
                    totalStages = totalGlobalStagesCount,
                    completedStages = completedGlobalStagesCount,
                    pendingStages = pendingGlobalStagesCount,
                    totalExpenseEur = totalGlobalExpenseEur,
                    totalAccommodationsCount = allAccommodations.size,
                    onExportPdf = { viewModel.exportStatsPdf(context, isTotal = true) }
                )
            }
        }
    }
}

/**
 * Contenido de la sub-pestaña "Datos del Camino" (Lógica acumulada de todas las etapas del camino activo)
 */
@Composable
private fun ActiveCaminoDataContent(
    camino: CaminoEntity?,
    routeTerm: String,
    stageTerm: String,
    stageTermPlural: String,
    totalKm: Double,
    completedKm: Double,
    totalElevationGain: Int,
    completedElevationGain: Int,
    completedElevationLoss: Int,
    completedTimeStr: String,
    avgKmPerStage: Double,
    totalStages: Int,
    completedStages: Int,
    pendingStages: Int,
    progressPercent: Int,
    totalExpenseEur: Double,
    caminoAccommodations: List<AccommodationEntity>,
    viewModel: CaminoViewModel,
    onExportPdf: () -> Unit
) {
    if (camino == null || totalStages == 0) {
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
                        text = "Sin $routeTerm activo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Crea o selecciona un $routeTerm para ver sus métricas acumuladas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val completedKmStr = remember(completedKm) { String.format(Locale.US, "%.1f", completedKm) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HERO CARD: BARRA DE PROGRESO Y MEDALLA O COMPOSTELA AL 100%
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_camino_hero_card"),
                colors = CardDefaults.cardColors(containerColor = CaminoBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono: Compostela completa si llega al 100% (sin círculo), Medalla en caso contrario
                    if (progressPercent >= 100) {
                        Image(
                            painter = painterResource(id = R.drawable.compostela),
                            contentDescription = "Compostela",
                            modifier = Modifier
                                .height(74.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
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
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (progressPercent >= 100) {
                            "¡100% COMPLETADO! COMPOSTELA OBTENIDA"
                        } else {
                            "$progressPercent% COMPLETADO DEL ${routeTerm.uppercase()}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PilgrimYellow,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "$completedStages de $totalStages $stageTermPlural finalizadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    if (camino.title.isNotBlank()) {
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

        // FILA 1: DISTANCIA TOTAL Y MEDIA KM/ETAPA (Basados en etapas completadas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Distancia Total",
                    value = "$completedKmStr km",
                    sub = "En etapas completadas",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Media km / $stageTerm",
                    value = "${String.format(Locale.US, "%.1f", avgKmPerStage)} km",
                    sub = "Promedio completado",
                    icon = Icons.Default.Speed,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 2: DESNIVEL POSITIVO Y DESNIVEL NEGATIVO (Basados en etapas completadas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Desnivel Positivo",
                    value = "+$completedElevationGain m",
                    sub = "Ascenso completado",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Negativo",
                    value = "-$completedElevationLoss m",
                    sub = "Descenso completado",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = CaminoBlueDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 3: TIEMPO Y TOTAL DE ETAPAS (Basados en etapas completadas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Tiempo",
                    value = completedTimeStr,
                    sub = "Tiempo en ruta",
                    icon = Icons.Default.Schedule,
                    color = PilgrimYellowDark,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Total de $stageTermPlural",
                    value = "$completedStages de $totalStages",
                    sub = "Etapas completadas",
                    icon = Icons.Default.ViewDay,
                    color = PilgrimYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // BALANCE GENERAL DEL CAMINO/VIAJE (Mostrando lo realizado)
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
                        Text(camino.title, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distancia completada:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedKmStr km", fontWeight = FontWeight.Bold, color = CaminoGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Desnivel positivo superado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+$completedElevationGain m", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Desnivel negativo completado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$completedElevationLoss m", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiempo en ruta completado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(completedTimeStr, fontWeight = FontWeight.Bold, color = CaminoBlueDark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$stageTermPlural completadas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedStages de $totalStages", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alojamientos registrados:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${caminoAccommodations.size}", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gasto en alojamientos:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format(Locale.US, "%.2f", totalExpenseEur)} €", fontWeight = FontWeight.Bold, color = CaminoGreen)
                    }
                }
            }
        }

        // DESGLOSE DE ALOJAMIENTOS DE ESTE CAMINO
        if (caminoAccommodations.isNotEmpty()) {
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
                                text = "Alojamientos registrados (${caminoAccommodations.size})",
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlue
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.2f", totalExpenseEur)} €",
                                fontWeight = FontWeight.Bold,
                                color = CaminoGreen
                            )
                        }

                        HorizontalDivider()

                        caminoAccommodations.forEach { acc ->
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

        // BOTÓN DE EXPORTAR INFORME DEL CAMINO A PDF
        item {
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Exportar Informe del $routeTerm a PDF", color = CaminoBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/**
 * Contenido de la sub-pestaña "Datos Totales" (Suma real y global de absolutamente todos los caminos)
 */
@Composable
private fun GlobalTotalDataContent(
    totalCaminosRealizados: Int,
    totalCaminosCount: Int,
    totalKm: Double,
    completedKm: Double,
    totalElevationGain: Int,
    completedElevationGain: Int,
    completedElevationLoss: Int,
    completedTimeStr: String,
    avgKmPerStage: Double,
    totalStages: Int,
    completedStages: Int,
    pendingStages: Int,
    totalExpenseEur: Double,
    totalAccommodationsCount: Int,
    onExportPdf: () -> Unit
) {
    val completedKmStr = remember(completedKm) { String.format(Locale.US, "%.1f", completedKm) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TARJETA SIMPLE SUPERIOR: TOTAL CAMINOS REALIZADOS (Sin barra de progreso ni % de completado)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_global_hero_card"),
                colors = CardDefaults.cardColors(containerColor = CaminoBlue),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TOTAL CAMINOS Y VIAJES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PilgrimYellow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Total Caminos Realizados: $totalCaminosRealizados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$totalCaminosCount rutas registradas en la aplicación",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(PilgrimYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // TÍTULO DE SECCIÓN: TOTALES ACUMULADOS (Sin 'del Camino')
        item {
            Text(
                text = "Totales Acumulados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaminoBlue
            )
        }

        // FILA 1: DISTANCIA TOTAL Y MEDIA KM/ETAPA (Basados en etapas completadas, sin lo que queda)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Distancia Total",
                    value = "$completedKmStr km",
                    sub = "En etapas completadas",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Media km / Etapa",
                    value = "${String.format(Locale.US, "%.1f", avgKmPerStage)} km",
                    sub = "Promedio global completado",
                    icon = Icons.Default.Speed,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 2: DESNIVEL POSITIVO Y DESNIVEL NEGATIVO (Basados en etapas completadas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Desnivel Positivo",
                    value = "+$completedElevationGain m",
                    sub = "Ascenso completado",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Negativo",
                    value = "-$completedElevationLoss m",
                    sub = "Descenso completado",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = CaminoBlueDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // FILA 3: TIEMPO Y CAMINOS COMPLETADOS (Basados en etapas completadas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Tiempo",
                    value = completedTimeStr,
                    sub = "Tiempo en ruta",
                    icon = Icons.Default.Schedule,
                    color = PilgrimYellowDark,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Caminos Completados",
                    value = "$totalCaminosRealizados de $totalCaminosCount",
                    sub = "Rutas finalizadas",
                    icon = Icons.Default.EmojiEvents,
                    color = PilgrimYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // BALANCE GENERAL GLOBAL (Mostrando lo realizado, no lo que queda)
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
                        text = "Balance General",
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Caminos completados:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalCaminosRealizados de $totalCaminosCount", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distancia total recorrida:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedKmStr km", fontWeight = FontWeight.Bold, color = CaminoGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Desnivel positivo superado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+$completedElevationGain m", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Desnivel negativo completado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-$completedElevationLoss m", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiempo total en ruta:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(completedTimeStr, fontWeight = FontWeight.Bold, color = CaminoBlueDark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Etapas completadas totales:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedStages de $totalStages", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Alojamientos totales registrados:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalAccommodationsCount", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gasto global acumulado:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format(Locale.US, "%.2f", totalExpenseEur)} €", fontWeight = FontWeight.Bold, color = CaminoGreen)
                    }
                }
            }
        }

        // BOTÓN DE EXPORTAR INFORME GLOBAL A PDF
        item {
            OutlinedButton(
                onClick = onExportPdf,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Exportar Informe Global a PDF", color = CaminoBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/**
 * Tarjeta de métrica estadística individual con icono y color
 */
@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
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
                    .size(36.dp)
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
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (sub.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = color
                )
            }
        }
    }
}

/**
 * Calcula el tiempo total acumulado en minutos y lo devuelve en formato "Xh Ym"
 */
private fun calculateTotalTimeString(stages: List<StageEntity>): String {
    var totalMinutes = 0
    for (stage in stages) {
        val str = stage.estimatedTime.trim()
        if (str.isBlank()) continue
        val hMatch = Regex("(\\d+)\\s*h", RegexOption.IGNORE_CASE).find(str)
        val mMatch = Regex("(\\d+)\\s*m", RegexOption.IGNORE_CASE).find(str)
        if (hMatch != null || mMatch != null) {
            val h = hMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val m = mMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            totalMinutes += h * 60 + m
        } else if (str.contains(":")) {
            val parts = str.split(":")
            val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            totalMinutes += h * 60 + m
        }
    }
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0 && mins > 0) "${hours}h ${mins}m"
    else if (hours > 0) "${hours}h"
    else if (mins > 0) "${mins}m"
    else "0h 00m"
}
