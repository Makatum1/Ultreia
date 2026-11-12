package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.gpx.GpxParser
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

enum class OsmMapStyle(val label: String, val layerId: String) {
    VOYAGER("Calles (Estilo Google)", "voyager"),
    OPENSTREETMAP("OpenStreetMap", "osm"),
    TOPOGRAPHIC("Topográfico / Relieve", "topo"),
    SATELLITE("Satélite Híbrido", "satellite")
}

val STAGE_COLORS = listOf(
    "#2563EB", // Azul
    "#16A34A", // Verde
    "#DC2626", // Rojo
    "#D97706", // Ámbar / Naranja
    "#9333EA", // Púrpura
    "#0891B2", // Cian
    "#EA580C", // Naranja Vivo
    "#4F46E5", // Índigo
    "#059669", // Esmeralda
    "#C026D3", // Fucsia
    "#0284C7", // Azul Cielo
    "#CA8A04"  // Mostaza
)

fun getStageColor(dayNumber: Int): String {
    val idx = (dayNumber - 1).coerceAtLeast(0) % STAGE_COLORS.size
    return STAGE_COLORS[idx]
}

fun getStageComposeColor(dayNumber: Int): Color {
    val hex = getStageColor(dayNumber)
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        CaminoBlue
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveMapScreen(
    camino: CaminoEntity,
    stages: List<StageEntity>,
    onStageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapStyle by remember { mutableStateOf(OsmMapStyle.VOYAGER) }

    // Sorted stages by day number
    val sortedStages = remember(stages) {
        stages.sortedBy { it.dayNumber }
    }

    // Individual or all stages marked/visible on the map
    var selectedStageIds by remember(stages) {
        mutableStateOf(stages.map { it.id }.toSet())
    }

    // Currently focused stage for detailed card / elevation
    var focusedStage by remember {
        mutableStateOf<StageEntity?>(stages.firstOrNull())
    }

    // Elevation toggle state and dropdown
    var showElevationProfile by remember { mutableStateOf(false) }
    var showStagesDropdown by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Prepare JSON payload for the webview with stage-specific colors
    val stagesJson = remember(stages, selectedStageIds) {
        val root = JSONArray()
        for (stg in sortedStages) {
            val isVisible = selectedStageIds.contains(stg.id)
            val obj = JSONObject()
            obj.put("id", stg.id)
            obj.put("dayNumber", stg.dayNumber)
            obj.put("origin", stg.origin)
            obj.put("destination", stg.destination)
            obj.put("distanceKm", stg.distanceKm)
            obj.put("elevationGainM", stg.elevationGainM)
            obj.put("isCompleted", stg.isCompleted)
            obj.put("color", getStageColor(stg.dayNumber))
            obj.put("visible", isVisible)

            if (isVisible) {
                val points = GpxParser.deserializePoints(stg.gpxTrackJson)
                val ptsArray = JSONArray()
                for (p in points) {
                    val pObj = JSONObject()
                    pObj.put("lat", p.lat)
                    pObj.put("lon", p.lon)
                    ptsArray.put(pObj)
                }
                obj.put("points", ptsArray)
            } else {
                obj.put("points", JSONArray())
            }
            root.put(obj)
        }
        root.toString()
    }

    val htmlContent = remember(stagesJson, mapStyle) {
        generateOsmMapHtml(stagesJson, mapStyle.layerId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Embedded Interactive Map with Street Names & OSM / CARTO Tiles
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("osm_interactive_map_webview"),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://www.openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            update = { webView ->
                webViewInstance = webView
                webView.loadDataWithBaseURL("https://www.openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            }
        )

        // TOP HEADER OVERLAY: Title of Camino (lowered down to match Etapas screen)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 14.dp, end = 14.dp, bottom = 10.dp)
                .align(Alignment.TopCenter)
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header badge matching Stages screen
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

                Spacer(Modifier.height(4.dp))

                Text(
                    text = camino.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (camino.subtitle.isNotBlank()) {
                    Text(
                        text = camino.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Map Layers Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OsmMapStyle.values().forEach { style ->
                        FilterChip(
                            selected = mapStyle == style,
                            onClick = { mapStyle = style },
                            label = { Text(style.label, fontSize = 11.sp) },
                            leadingIcon = {
                                if (mapStyle == style) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CaminoBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Floating Action Button: Elevation Profile Toggle
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 160.dp, end = 14.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { showElevationProfile = !showElevationProfile },
                containerColor = if (showElevationProfile) PilgrimYellow else MaterialTheme.colorScheme.surface,
                contentColor = if (showElevationProfile) CaminoBlueDark else CaminoBlue,
                modifier = Modifier.testTag("map_toggle_elevation_btn")
            ) {
                Icon(Icons.Default.Landscape, contentDescription = "Perfil altimétrico")
            }
        }

        // BOTTOM OVERLAY: Ordered stages in horizontal scroll, allowing marking all or individual
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header of stages controller: Exact buttons: 1. "Etapas", 2. "Todos", 3. "Ocultar Etapas"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button 1: "Etapas" with vertical dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showStagesDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_map_control_etapas")
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Etapas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showStagesDropdown,
                            onDismissRequest = { showStagesDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 350.dp)
                        ) {
                            sortedStages.forEach { stg ->
                                val isChecked = selectedStageIds.contains(stg.id)
                                val stageColor = getStageComposeColor(stg.dayNumber)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(stageColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${stg.dayNumber}",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Día ${stg.dayNumber}: ${stg.origin} → ${stg.destination}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
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
                                    onClick = {
                                        selectedStageIds = setOf(stg.id)
                                        focusedStage = stg
                                        showStagesDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Button 2: "Todos"
                    Button(
                        onClick = {
                            selectedStageIds = sortedStages.map { it.id }.toSet()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PilgrimYellow),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_map_control_todos")
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, tint = CaminoBlueDark, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Todos",
                            color = CaminoBlueDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Button 3: "Ocultar Etapas"
                    OutlinedButton(
                        onClick = {
                            selectedStageIds = emptySet()
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_map_control_ocultar")
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Ocultar Etapas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Optional Elevation Profile for focused stage
                AnimatedVisibility(
                    visible = showElevationProfile && focusedStage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val pts = remember(focusedStage) {
                        GpxParser.deserializePoints(focusedStage?.gpxTrackJson)
                    }
                    if (pts.isNotEmpty()) {
                        Column {
                            ElevationProfileCard(stage = focusedStage!!, points = pts)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                // Horizontal scroll of ordered stages with individual toggle and colored badge
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(sortedStages) { stg ->
                        val isChecked = selectedStageIds.contains(stg.id)
                        val isFocused = focusedStage?.id == stg.id
                        val stageColor = getStageComposeColor(stg.dayNumber)
                        val hasGpx = !stg.gpxTrackJson.isNullOrBlank()

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChecked) stageColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) stageColor else if (isChecked) stageColor.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    // Toggle individual stage visibility
                                    selectedStageIds = if (isChecked) {
                                        selectedStageIds - stg.id
                                    } else {
                                        selectedStageIds + stg.id
                                    }
                                    focusedStage = stg
                                }
                                .testTag("chip_stage_${stg.dayNumber}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color dot / badge with stage day number
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(stageColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${stg.dayNumber}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Día ${stg.dayNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isChecked) CaminoBlueDark else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        if (hasGpx) {
                                            Surface(
                                                color = CaminoGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "GPX",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = CaminoGreen,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${stg.origin} → ${stg.destination}",
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${stg.distanceKm} km",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.width(6.dp))

                                // Checkbox indicating visibility on map
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedStageIds = if (checked) {
                                            selectedStageIds + stg.id
                                        } else {
                                            selectedStageIds - stg.id
                                        }
                                        focusedStage = stg
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = stageColor,
                                        uncheckedColor = Color.Gray
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Focused Stage quick action bar
                if (focusedStage != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Etapa seleccionada: Día ${focusedStage!!.dayNumber} (${focusedStage!!.origin} a ${focusedStage!!.destination})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = { onStageClick(focusedStage!!.id) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Ver Ficha de Etapa →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CaminoBlue)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generates map HTML with OpenStreetMap / CARTO tiles with full street names and each stage in a distinct color
 */
private fun generateOsmMapHtml(stagesJson: String, layerId: String): String {
    val tileUrl = when (layerId) {
        "osm" -> "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        "topo" -> "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png"
        "satellite" -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
        else -> "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png" // CARTO Voyager looks like Google Maps with streets & labels
    }

    val maxZoom = if (layerId == "topo") 17 else 19

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #f1f5f9;
                }
                .custom-badge {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 50%;
                    color: white;
                    border: 2px solid #ffffff;
                    font-weight: bold;
                    font-size: 11px;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.4);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: true }).setView([42.8, -4.5], 7);

                // Base Tile Layer with localized street and town names
                L.tileLayer('${tileUrl}', {
                    maxZoom: ${maxZoom},
                    subdomains: 'abcd',
                    attribution: '© OpenStreetMap / CARTO'
                }).addTo(map);

                var stagesData = ${stagesJson};
                var allLatLngs = [];

                stagesData.forEach(function(stage) {
                    if (!stage.visible) return;
                    var pts = stage.points;
                    if (pts && pts.length > 0) {
                        var latlngs = pts.map(function(p) { return [p.lat, p.lon]; });
                        allLatLngs = allLatLngs.concat(latlngs);

                        var color = stage.color || '#0E4C99';

                        // Draw path polyline with stage's unique vibrant color
                        var polyline = L.polyline(latlngs, {
                            color: color,
                            weight: 5,
                            opacity: 0.95
                        }).addTo(map);

                        polyline.bindPopup("<b>Día " + stage.dayNumber + ": " + stage.origin + " → " + stage.destination + "</b><br>" + stage.distanceKm + " km • +" + stage.elevationGainM + "m");

                        // Waypoint Marker for Origin
                        var pStart = pts[0];
                        var markerHtml = "<div class='custom-badge' style='background:" + color + "; width:26px; height:26px;'>" + stage.dayNumber + "</div>";
                        var icon = L.divIcon({ className: '', html: markerHtml, iconSize: [26, 26], iconAnchor: [13, 13] });
                        L.marker([pStart.lat, pStart.lon], { icon: icon }).addTo(map)
                            .bindPopup("<b>Día " + stage.dayNumber + ": " + stage.origin + "</b><br>" + (stage.isCompleted ? "✓ Etapa Completada" : "Etapa Planificada"));
                    }
                });

                if (allLatLngs.length > 0) {
                    var bounds = L.latLngBounds(allLatLngs);
                    map.fitBounds(bounds, { padding: [100, 30] });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun ElevationProfileCard(
    stage: StageEntity,
    points: List<GpxPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil Altimétrico: Día ${stage.dayNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue
                )
                Text(
                    text = "+${stage.elevationGainM}m  /  -${stage.elevationLossM}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val minEle = points.minOfOrNull { it.ele } ?: 0.0
                val maxEle = points.maxOfOrNull { it.ele } ?: 100.0
                val eleSpan = kotlin.math.max(10.0, maxEle - minEle).toFloat()

                val w = size.width
                val h = size.height

                val path = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()

                fillPath.moveTo(0f, h)

                val step = if (points.size > 1) w / (points.size - 1) else 0f
                points.forEachIndexed { i, p ->
                    val x = i * step
                    val y = h - ((p.ele - minEle).toFloat() / eleSpan * (h * 0.85f))
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(w, h)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    color = CaminoBlue.copy(alpha = 0.2f)
                )

                drawPath(
                    path = path,
                    color = CaminoBlue,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}
