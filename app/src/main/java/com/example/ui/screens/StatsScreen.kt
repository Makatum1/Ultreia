package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoStats
import com.example.ui.CaminoViewModel
import com.example.ui.theme.*

@Composable
fun StatsScreen(
    camino: CaminoEntity,
    stages: List<StageEntity>,
    stats: CaminoStats,
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStatsScope by remember { mutableIntStateOf(0) } // 0: Este Camino/Viaje, 1: Estadísticas Totales
    val totalStats by viewModel.totalGlobalStats.collectAsStateWithLifecycle()

    val currentStats = if (selectedStatsScope == 0) stats else totalStats

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top active Camino indicator
        item {
            Surface(
                color = PilgrimYellow.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (camino.isTravelGuide) Icons.Default.Luggage else Icons.Default.Hiking,
                        contentDescription = null,
                        tint = CaminoBlueDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (camino.isTravelGuide) "Estás en el viaje: ${camino.title}" else "Estás en: ${camino.title}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlueDark
                    )
                }
            }
        }

        // Two-option Scope Selector: Este Camino/Viaje vs Estadísticas Totales
        item {
            TabRow(
                selectedTabIndex = selectedStatsScope,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CaminoBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedStatsScope == 0,
                    onClick = { selectedStatsScope = 0 },
                    text = {
                        Text(
                            text = if (camino.isTravelGuide) "Este Viaje" else "Este Camino",
                            fontWeight = if (selectedStatsScope == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedStatsScope == 1,
                    onClick = { selectedStatsScope = 1 },
                    text = {
                        Text(
                            text = "Estadísticas Totales",
                            fontWeight = if (selectedStatsScope == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Hero Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_hero_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedStatsScope == 0) CaminoBlue else CaminoBlueDark
                ),
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
                            .size(76.dp)
                            .background(PilgrimYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedStatsScope == 0) Icons.Default.EmojiEvents else Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (selectedStatsScope == 0)
                            "${currentStats.progressPercent}% COMPLETADO"
                        else
                            "PROGRESO TOTAL ACUMULADO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PilgrimYellow
                    )

                    Text(
                        text = if (selectedStatsScope == 0)
                            "${currentStats.completedStages} de ${currentStats.totalStages} etapas finalizadas"
                        else
                            "${currentStats.completedStages} etapas completadas en todas tus rutas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { currentStats.progressPercent / 100f },
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

        // Metrics Grid (2x2 Cards)
        item {
            Text(
                text = if (selectedStatsScope == 0) "Métricas de ${camino.title}" else "Métricas Acumuladas Globales",
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
                    title = "Distancia Recorrida",
                    value = "${currentStats.completedKm} km",
                    sub = "de ${currentStats.totalKm} km totales",
                    icon = Icons.Default.DirectionsWalk,
                    tint = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Positivo",
                    value = "+${currentStats.completedElevationGainM} m",
                    sub = "de +${currentStats.totalElevationGainM} m totales",
                    icon = Icons.Default.Landscape,
                    tint = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val remainingKm = (Math.round((currentStats.totalKm - currentStats.completedKm) * 10.0) / 10.0).coerceAtLeast(0.0)
                StatMetricCard(
                    title = "Por Recorrer",
                    value = "$remainingKm km",
                    sub = "${currentStats.totalStages - currentStats.completedStages} etapas restantes",
                    icon = Icons.Default.Flag,
                    tint = PilgrimYellowDark,
                    modifier = Modifier.weight(1f)
                )

                val avgKm = if (currentStats.completedStages > 0) {
                    (Math.round((currentStats.completedKm / currentStats.completedStages) * 10.0) / 10.0)
                } else 0.0
                StatMetricCard(
                    title = "Media por Etapa",
                    value = "$avgKm km",
                    sub = "ritmo de marcha",
                    icon = Icons.Default.Speed,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action banner: Compartir estadística del viaje (PDF de solo estadística)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_export_pdf_banner"),
                colors = CardDefaults.cardColors(containerColor = CaminoBlueLight),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Informe de Estadísticas en PDF",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlueDark
                            )
                            Text(
                                text = if (selectedStatsScope == 0)
                                    "Genera un PDF con el resumen de métricas y etapas de este viaje."
                                else
                                    "Genera un PDF con las estadísticas globales de todos tus caminos y viajes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.exportStatsPdf(context, isTotal = selectedStatsScope == 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_stats_export_pdf")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedStatsScope == 0) "Compartir estadística del viaje" else "Compartir estadísticas totales",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // If viewing single camino, show its completed stages list
        if (selectedStatsScope == 0) {
            item {
                Text(
                    text = "Etapas Completadas (${stats.completedStages})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue
                )
            }

            val completedStagesList = stages.filter { it.isCompleted }
            if (completedStagesList.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aún no has marcado ninguna etapa como completada. Puedes marcarlas al terminarlas o subiendo tu GPX.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(completedStagesList) { stage ->
                    CompletedStageCard(stage = stage)
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CompletedStageCard(stage: StageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(CaminoGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Column {
                    Text(
                        text = "Día ${stage.dayNumber}: ${stage.origin} a ${stage.destination}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stage.distanceKm} km  •  +${stage.elevationGainM}m  •  ${stage.estimatedTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (stage.diaryNotes.isNotBlank()) {
                Icon(
                    Icons.Default.AutoStories,
                    contentDescription = "Tiene vivencias escritas",
                    tint = PilgrimYellowDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
