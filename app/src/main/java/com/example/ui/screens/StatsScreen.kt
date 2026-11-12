package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
    camino: CaminoEntity? = null,
    stages: List<StageEntity> = emptyList(),
    stats: CaminoStats? = null,
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalStats by viewModel.totalGlobalStats.collectAsStateWithLifecycle()
    val totalCaminos by viewModel.totalCaminosCount.collectAsStateWithLifecycle()
    val totalTrips by viewModel.totalTripsCount.collectAsStateWithLifecycle()
    val totalAllRoutes by viewModel.totalAllRoutesCount.collectAsStateWithLifecycle()
    val totalAllStages by viewModel.totalAllStagesCount.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
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
                        text = "Resumen global de todos tus caminos y viajes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.exportStatsPdf(context, isTotal = true) },
                    modifier = Modifier.testTag("btn_export_stats_pdf")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar Datos a PDF",
                        tint = CaminoBlue
                    )
                }
            }
        }

        // Hero Progress Card: Global Summary
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
                            .size(70.dp)
                            .background(PilgrimYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "${totalStats.progressPercent}% COMPLETADO GLOBAL",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PilgrimYellow
                    )

                    Text(
                        text = "${totalStats.completedStages} de $totalAllStages etapas finalizadas en todas tus rutas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (totalStats.progressPercent / 100f).coerceIn(0f, 1f) },
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

        // Section Title: Métricas Globales
        item {
            Text(
                text = "Totales Acumulados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaminoBlue
            )
        }

        // Primary Row: Total de Caminos y Total de Etapas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = viewModel.string("total_caminos"),
                    value = "$totalAllRoutes",
                    sub = "$totalCaminos caminos • $totalTrips viajes",
                    icon = Icons.Default.Hiking,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = viewModel.string("total_stages"),
                    value = "$totalAllStages",
                    sub = "${totalStats.completedStages} completadas",
                    icon = Icons.Default.ViewDay,
                    color = PilgrimYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Secondary Row: Distancia y Desnivel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = viewModel.string("total_distance"),
                    value = "${totalStats.completedKm} km",
                    sub = "de ${totalStats.totalKm} km totales",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    color = CaminoGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Desnivel Total",
                    value = "+${totalStats.totalElevationGainM} m",
                    sub = "+${totalStats.completedElevationGainM} m completados",
                    icon = Icons.Default.Landscape,
                    color = CaminoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Summary Card with detailed stats
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
                        text = "Detalle General",
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total de rutas creadas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalAllRoutes", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total de etapas / días:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalAllStages etapas", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Etapas completadas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${totalStats.completedStages}", fontWeight = FontWeight.Bold, color = CaminoGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Etapas pendientes:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(totalAllStages - totalStats.completedStages).coerceAtLeast(0)}", fontWeight = FontWeight.Bold)
                    }

                    val remainingKm = kotlin.math.max(0.0, totalStats.totalKm - totalStats.completedKm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distancia por recorrer:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(java.util.Locale.US, "%.1f km", remainingKm), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Export PDF Action Button
        item {
            OutlinedButton(
                onClick = { viewModel.exportStatsPdf(context, isTotal = true) },
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
