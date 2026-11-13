package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.gpx.GpxParser
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.data.sample.SampleData
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

val STAGE_COLORS = listOf(
    "#2563EB", // Azul vibrante
    "#16A34A", // Verde esmeralda
    "#DC2626", // Rojo
    "#D97706", // Ámbar / Naranja
    "#9333EA", // Púrpura
    "#0891B2", // Cian
    "#EA580C", // Naranja Vivo
    "#4F46E5", // Índigo
    "#059669", // Verde bosque
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

/**
 * Coordenadas de referencia para poblaciones del Camino de Santiago y principales ciudades
 */
object TownCoordinates {
    private val COORDINATES = mapOf(
        "saint-jean-pied-de-port" to Pair(43.1636, -1.2378),
        "saint jean pied de port" to Pair(43.1636, -1.2378),
        "st jean" to Pair(43.1636, -1.2378),
        "roncesvalles" to Pair(42.9897, -1.3197),
        "orregaga" to Pair(42.9897, -1.3197),
        "zubiri" to Pair(42.9308, -1.5039),
        "pamplona" to Pair(42.8125, -1.6458),
        "iruña" to Pair(42.8125, -1.6458),
        "puente la reina" to Pair(42.6738, -1.8152),
        "estella" to Pair(42.6713, -2.0305),
        "lizarra" to Pair(42.6713, -2.0305),
        "los arcos" to Pair(42.5694, -2.1932),
        "viana" to Pair(42.5167, -2.3719),
        "logroño" to Pair(42.4627, -2.4449),
        "logrono" to Pair(42.4627, -2.4449),
        "navarrete" to Pair(42.4289, -2.5608),
        "nájera" to Pair(42.4162, -2.7346),
        "najera" to Pair(42.4162, -2.7346),
        "santo domingo de la calzada" to Pair(42.4410, -2.9536),
        "santo domingo" to Pair(42.4410, -2.9536),
        "grañón" to Pair(42.4500, -3.0278),
        "belorado" to Pair(42.4194, -3.1906),
        "tosantos" to Pair(42.4139, -3.2417),
        "villafranca montes de oca" to Pair(42.3889, -3.3083),
        "san juan de ortega" to Pair(42.3789, -3.4356),
        "atapuerca" to Pair(42.3769, -3.5078),
        "burgos" to Pair(42.3440, -3.6969),
        "tardajos" to Pair(42.3486, -3.8189),
        "rabé de las calzadas" to Pair(42.3397, -3.8361),
        "hornillos del camino" to Pair(42.3392, -3.9248),
        "hornillos" to Pair(42.3392, -3.9248),
        "hontanas" to Pair(42.3125, -4.0458),
        "castrojeriz" to Pair(42.2886, -4.1394),
        "itero de la vega" to Pair(42.2847, -4.2583),
        "boadilla del camino" to Pair(42.2589, -4.3472),
        "frómista" to Pair(42.2683, -4.4042),
        "fromista" to Pair(42.2683, -4.4042),
        "población de campos" to Pair(42.2667, -4.4489),
        "villalcázar de sirga" to Pair(42.3167, -4.5428),
        "carrión de los condes" to Pair(42.3397, -4.6033),
        "carrion de los condes" to Pair(42.3397, -4.6033),
        "carrion" to Pair(42.3397, -4.6033),
        "calzadilla de la cueza" to Pair(42.3683, -4.7861),
        "ledigos" to Pair(42.3551, -4.8647),
        "terradillos de los templarios" to Pair(42.3625, -4.8967),
        "moratinos" to Pair(42.3606, -4.9242),
        "sahagún" to Pair(42.3712, -5.0298),
        "sahagun" to Pair(42.3712, -5.0298),
        "bercianos del real camino" to Pair(42.3861, -5.1472),
        "el burgo ranero" to Pair(42.4239, -5.2217),
        "reliegos" to Pair(42.4764, -5.3375),
        "mansilla de las mulas" to Pair(42.4975, -5.4169),
        "león" to Pair(42.5987, -5.5671),
        "leon" to Pair(42.5987, -5.5671),
        "la virgen del camino" to Pair(42.5786, -5.6444),
        "villadangos del páramo" to Pair(42.5181, -5.7656),
        "san martín del camino" to Pair(42.4925, -5.8058),
        "san martin del camino" to Pair(42.4925, -5.8058),
        "hospital de órbigo" to Pair(42.4633, -5.9189),
        "hospital de orbigo" to Pair(42.4633, -5.9189),
        "astorga" to Pair(42.4578, -6.0560),
        "santa catalina de somoza" to Pair(42.4578, -6.1583),
        "el ganso" to Pair(42.4639, -6.2083),
        "rabanal del camino" to Pair(42.4820, -6.2842),
        "foncebadón" to Pair(42.4989, -6.3425),
        "cruz de ferro" to Pair(42.4908, -6.3689),
        "manjarín" to Pair(42.4939, -6.3889),
        "el acebo" to Pair(42.5089, -6.4172),
        "riego de ambrós" to Pair(42.5208, -6.4633),
        "molinaseca" to Pair(42.5381, -6.5202),
        "ponferrada" to Pair(42.5466, -6.5909),
        "camponaraya" to Pair(42.5800, -6.6700),
        "cacabelos" to Pair(42.5994, -6.7264),
        "villafranca del bierzo" to Pair(42.6074, -6.8106),
        "villafranca" to Pair(42.6074, -6.8106),
        "pereje" to Pair(42.6167, -6.8833),
        "trabadelo" to Pair(42.6458, -6.8833),
        "vega de valcarce" to Pair(42.6644, -6.9483),
        "amasmestas" to Pair(42.6750, -6.9667),
        "las herrerías" to Pair(42.6847, -7.0042),
        "o cebreiro" to Pair(42.7077, -7.0425),
        "cebreiro" to Pair(42.7077, -7.0425),
        "liñares" to Pair(42.7158, -7.0864),
        "hospital da condesa" to Pair(42.7172, -7.0989),
        "alto do poio" to Pair(42.7214, -7.1158),
        "fonfría" to Pair(42.7250, -7.1361),
        "triacastela" to Pair(42.7561, -7.1952),
        "san xil" to Pair(42.7667, -7.2667),
        "samos" to Pair(42.7314, -7.3278),
        "sarria" to Pair(42.7766, -7.4144),
        "barbadelo" to Pair(42.7667, -7.4667),
        "ferreiros" to Pair(42.7722, -7.5389),
        "portomarín" to Pair(42.8075, -7.6186),
        "portomarin" to Pair(42.8075, -7.6186),
        "gonzar" to Pair(42.8458, -7.6833),
        "castromaior" to Pair(42.8542, -7.7083),
        "hospital da cruz" to Pair(42.8611, -7.7333),
        "ventas de narón" to Pair(42.8667, -7.7556),
        "ligonde" to Pair(42.8750, -7.7778),
        "airexe" to Pair(42.8792, -7.7944),
        "palas de rei" to Pair(42.8736, -7.8689),
        "palas" to Pair(42.8736, -7.8689),
        "san xulián do camiño" to Pair(42.8833, -7.9000),
        "casanova" to Pair(42.8917, -7.9417),
        "coto" to Pair(42.9000, -7.9667),
        "leboreiro" to Pair(42.9083, -7.9833),
        "furelos" to Pair(42.9125, -8.0042),
        "melide" to Pair(42.9150, -8.0164),
        "boente" to Pair(42.9167, -8.0778),
        "castañeda" to Pair(42.9222, -8.1111),
        "ribadiso" to Pair(42.9250, -8.1400),
        "arzúa" to Pair(42.9269, -8.1639),
        "arzua" to Pair(42.9269, -8.1639),
        "calzada" to Pair(42.9200, -8.2167),
        "salceda" to Pair(42.9111, -8.2833),
        "santa irene" to Pair(42.9125, -8.3278),
        "a rúa" to Pair(42.9083, -8.3500),
        "o pedrouzo" to Pair(42.9064, -8.3614),
        "pedrouzo" to Pair(42.9064, -8.3614),
        "arca do pino" to Pair(42.9064, -8.3614),
        "amenal" to Pair(42.9028, -8.4111),
        "san paio" to Pair(42.8972, -8.4389),
        "lavacolla" to Pair(42.8989, -8.4489),
        "vilamaior" to Pair(42.8944, -8.4694),
        "san marcos" to Pair(42.8889, -8.4889),
        "monte do gozo" to Pair(42.8894, -8.4947),
        "santiago de compostela" to Pair(42.8782, -8.5448),
        "santiago" to Pair(42.8782, -8.5448),

        // Camino Portugués
        "tui" to Pair(42.0461, -8.6444),
        "valença do minho" to Pair(42.0319, -8.6444),
        "o porriño" to Pair(42.1611, -8.6200),
        "o porrino" to Pair(42.1611, -8.6200),
        "porriño" to Pair(42.1611, -8.6200),
        "mos" to Pair(42.1969, -8.6203),
        "redondela" to Pair(42.2831, -8.6094),
        "cesantes" to Pair(42.3014, -8.6075),
        "arcade" to Pair(42.3392, -8.6111),
        "ponte sampaio" to Pair(42.3486, -8.6083),
        "pontevedra" to Pair(42.4310, -8.6444),
        "alba" to Pair(42.4667, -8.6417),
        "barro" to Pair(42.5222, -8.6389),
        "briallos" to Pair(42.5444, -8.6444),
        "caldas de reis" to Pair(42.6047, -8.6425),
        "caldas" to Pair(42.6047, -8.6425),
        "valga" to Pair(42.6889, -8.6489),
        "pontecesures" to Pair(42.7208, -8.6536),
        "padrón" to Pair(42.7383, -8.6608),
        "padron" to Pair(42.7383, -8.6608),
        "iria flavia" to Pair(42.7472, -8.6583),
        "a esfarrapada" to Pair(42.7667, -8.6389),
        "rúa" to Pair(42.7778, -8.6250),
        "esclavitud" to Pair(42.8000, -8.6083),
        "teo" to Pair(42.7933, -8.5833),
        "o milladoiro" to Pair(42.8483, -8.5772),
        "milladoiro" to Pair(42.8483, -8.5772),

        // Camino del Norte
        "ribadeo" to Pair(43.5369, -7.0408),
        "lourenzá" to Pair(43.4717, -7.2978),
        "lourenza" to Pair(43.4717, -7.2978),
        "mondoñedo" to Pair(43.4286, -7.3622),
        "mondonedo" to Pair(43.4286, -7.3622),
        "abadín" to Pair(43.3644, -7.4764),
        "abadin" to Pair(43.3644, -7.4764),
        "vilalba" to Pair(43.2981, -7.6811),
        "baamonde" to Pair(43.1764, -7.7533),
        "sobrado dos monxes" to Pair(43.0403, -8.0228),
        "sobrado" to Pair(43.0403, -8.0228),
        "boimorto" to Pair(43.0078, -8.1264),

        // Camino Primitivo
        "oviedo" to Pair(43.3619, -5.8494),
        "grado" to Pair(43.3892, -6.0744),
        "cornellana" to Pair(43.4111, -6.1556),
        "salas" to Pair(43.4119, -6.2592),
        "la espina" to Pair(43.3917, -6.3472),
        "tineo" to Pair(43.3364, -6.4117),
        "borres" to Pair(43.2917, -6.5333),
        "pola de allande" to Pair(43.2731, -6.6111),
        "la mesa" to Pair(43.2208, -6.8208),
        "grandas de salime" to Pair(43.2167, -6.8744),
        "grandas" to Pair(43.2167, -6.8744),
        "a fonsagrada" to Pair(43.1256, -7.0694),
        "fonsagrada" to Pair(43.1256, -7.0694),
        "o cádavo" to Pair(43.0189, -7.2622),
        "o cadavo" to Pair(43.0189, -7.2622),
        "cadavo" to Pair(43.0189, -7.2622),
        "castroverde" to Pair(43.0300, -7.3250),
        "lugo" to Pair(43.0097, -7.5567),
        "san romao da retorta" to Pair(42.9697, -7.7022),
        "san romao" to Pair(42.9697, -7.7022),
        "ferreira" to Pair(42.9367, -7.8683),

        // Finisterre y Muxía
        "negreira" to Pair(42.9094, -8.7358),
        "olveiroa" to Pair(42.9772, -9.0433),
        "cee" to Pair(42.9558, -9.1894),
        "corcubión" to Pair(42.9439, -9.1947),
        "corcubion" to Pair(42.9439, -9.1947),
        "fisterra" to Pair(42.9078, -9.2628),
        "finisterre" to Pair(42.9078, -9.2628),
        "lires" to Pair(43.0039, -9.2458),
        "muxía" to Pair(43.1056, -9.2181),
        "muxia" to Pair(43.1056, -9.2181),

        // Camino Inglés
        "ferrol" to Pair(43.4832, -8.2369),
        "neda" to Pair(43.4986, -8.1569),
        "pontedeume" to Pair(43.4072, -8.1706),
        "miño" to Pair(43.3481, -8.2047),
        "betanzos" to Pair(43.2808, -8.2114),
        "hospital de bruma" to Pair(43.1583, -8.3267),
        "bruma" to Pair(43.1583, -8.3267),
        "sigüeiro" to Pair(42.9678, -8.4431),
        "sigueiro" to Pair(42.9678, -8.4431),

        // Ciudades Principales
        "madrid" to Pair(40.4168, -3.7038),
        "barcelona" to Pair(41.3879, 2.1699),
        "valencia" to Pair(39.4699, -0.3763),
        "sevilla" to Pair(37.3891, -5.9845),
        "zaragoza" to Pair(41.6488, -0.8891),
        "málaga" to Pair(36.7213, -4.4214),
        "malaga" to Pair(36.7213, -4.4214),
        "bilbao" to Pair(43.2630, -2.9350),
        "santander" to Pair(43.4623, -3.8099),
        "san sebastián" to Pair(43.3183, -1.9812),
        "san sebastian" to Pair(43.3183, -1.9812),
        "donostia" to Pair(43.3183, -1.9812),
        "irún" to Pair(43.3390, -1.7894),
        "irun" to Pair(43.3390, -1.7894),
        "gijón" to Pair(43.5322, -5.6611),
        "gijon" to Pair(43.5322, -5.6611),
        "a coruña" to Pair(43.3623, -8.4115),
        "la coruña" to Pair(43.3623, -8.4115),
        "vigo" to Pair(42.2406, -8.7207),
        "ourense" to Pair(42.3364, -7.8639),
        "orense" to Pair(42.3364, -7.8639),
        "zamora" to Pair(41.5036, -5.7467),
        "salamanca" to Pair(40.9701, -5.6635),
        "mérida" to Pair(38.9161, -6.3437),
        "merida" to Pair(38.9161, -6.3437),
        "cáceres" to Pair(39.4753, -6.3722),
        "caceres" to Pair(39.4753, -6.3722),
        "granada" to Pair(37.1773, -3.5986),
        "córdoba" to Pair(37.8882, -4.7794),
        "cordoba" to Pair(37.8882, -4.7794),
        "toledo" to Pair(39.8628, -4.0273),
        "segovia" to Pair(40.9429, -4.1088),
        "ávila" to Pair(40.6565, -4.6818),
        "avila" to Pair(40.6565, -4.6818),
        "huesca" to Pair(42.1362, -0.4087),
        "jaca" to Pair(42.5707, -0.5500),
        "canfranc" to Pair(42.7167, -0.5167),
        "somport" to Pair(42.7972, -0.5283)
    )

    fun findCoordinates(name: String): Pair<Double, Double>? {
        val clean = name.trim().lowercase()
            .replace(Regex("[^a-záéíóúñü0-9\\s]"), "")
        if (COORDINATES.containsKey(clean)) return COORDINATES[clean]
        for ((k, v) in COORDINATES) {
            if (clean.contains(k) || k.contains(clean)) return v
        }
        return null
    }
}

/**
 * Obtiene los puntos del recorrido:
 * 1. Si existe GPX en la etapa, utiliza sus coordenadas exactas.
 * 2. Si no hay GPX subido, resuelve los nombres de origen y destino e interpola
 *    un recorrido con curva natural y elevación para que siempre se visualice en el mapa.
 */
fun getStageRoutePoints(stg: StageEntity): List<GpxPoint> {
    val existing = GpxParser.deserializePoints(stg.gpxTrackJson)
    if (existing.isNotEmpty()) return existing

    val startCoord = TownCoordinates.findCoordinates(stg.origin)
    val endCoord = TownCoordinates.findCoordinates(stg.destination)

    val (sLat, sLon) = when {
        startCoord != null -> startCoord
        endCoord != null -> Pair(endCoord.first + 0.12, endCoord.second + 0.12)
        else -> Pair(42.88 - (stg.dayNumber * 0.06), -8.54 - (stg.dayNumber * 0.09))
    }

    val (eLat, eLon) = when {
        endCoord != null -> endCoord
        startCoord != null -> Pair(startCoord.first - 0.12, startCoord.second - 0.12)
        else -> Pair(sLat - 0.08, sLon - 0.12)
    }

    return SampleData.generateTrackPoints(
        startLat = sLat,
        startLon = sLon,
        endLat = eLat,
        endLon = eLon,
        startEle = 250.0,
        endEle = 320.0,
        numPoints = 25
    )
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
    // Sorted stages by day number
    val sortedStages = remember(stages) {
        stages.sortedBy { it.dayNumber }
    }

    // Individual or all stages marked/visible on the map
    var selectedStageIds by remember(stages) {
        mutableStateOf(stages.map { it.id }.toSet())
    }

    // Currently focused stage for detailed popup / zoom
    var focusedStage by remember {
        mutableStateOf<StageEntity?>(null)
    }

    var showStagesDropdown by remember { mutableStateOf(false) }

    // Estadísticas para el encabezado idéntico a la pantalla de Etapas
    val totalKm = remember(stages, camino) {
        if (stages.isNotEmpty()) stages.sumOf { it.distanceKm } else camino.totalEstimatedKm
    }
    val completedKm = remember(stages) {
        stages.filter { it.isCompleted }.sumOf { it.distanceKm }
    }
    val progressPercent = remember(totalKm, completedKm) {
        if (totalKm > 0) ((completedKm / totalKm) * 100).toInt() else 0
    }
    val totalKmStr = remember(totalKm) {
        String.format(java.util.Locale.US, "%.1f", totalKm)
    }
    val completedKmStr = remember(completedKm) {
        String.format(java.util.Locale.US, "%.1f", completedKm)
    }

    // Prepare JSON payload for the Leaflet WebView with stage-specific colors and route points
    val stagesJson = remember(stages, selectedStageIds, focusedStage) {
        val root = JSONArray()
        for (stg in sortedStages) {
            val isVisible = selectedStageIds.contains(stg.id)
            val isFocused = focusedStage?.id == stg.id
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
            obj.put("isFocused", isFocused)

            if (isVisible) {
                val points = getStageRoutePoints(stg)
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

    val htmlContent = remember(stagesJson) {
        generateOsmMapHtml(stagesJson)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Mapa interactivo integrado con capa estándar de Google Maps
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("osm_interactive_map_webview"),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("if (typeof adjustView === 'function') { adjustView(); }", null)
                        }
                    }
                    loadDataWithBaseURL("https://www.google.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://www.google.com", htmlContent, "text/html", "UTF-8", null)
            }
        )

        // TARJETA DE TÍTULO SUPERIOR: Idéntica a la vista en "Etapas" (CaminoHeaderBanner)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                .align(Alignment.TopCenter)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Imagen de fondo / Marca de agua
                if (!camino.coverPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = camino.coverPhotoUri,
                        contentDescription = camino.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ultreia_logo),
                        contentDescription = camino.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .background(CaminoBlue),
                        contentScale = ContentScale.Fit
                    )
                }

                // Superposición de degradado oscuro para legibilidad óptima
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 25f
                            )
                        )
                )

                // Textos de cabecera
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PilgrimYellow,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (camino.isTravelGuide) "ULTREIA VIAJE" else "ULTREIA GUÍA",
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
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (camino.subtitle.isNotBlank()) {
                        Text(
                            text = camino.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${completedKmStr} / ${totalKmStr} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = PilgrimYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${progressPercent}% completado",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // PANEL DE CONTROL INFERIOR (Solo los 3 botones, sin carrusel ni ficha):
        // 1. Etapas (con desplegable vertical)
        // 2. Todos
        // 3. Ocultar Etapas
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón 1: "Etapas" con menú desplegable vertical
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
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
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

                // Botón 2: "Todos" (Muestra todas las etapas en el mapa)
                Button(
                    onClick = {
                        selectedStageIds = sortedStages.map { it.id }.toSet()
                        focusedStage = null
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

                // Botón 3: "Ocultar Etapas" (Oculta las líneas y puntos del mapa)
                OutlinedButton(
                    onClick = {
                        selectedStageIds = emptySet()
                        focusedStage = null
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
        }
    }
}

/**
 * Genera el documento HTML con la capa estándar de Google Maps y renderiza los trazados de las etapas.
 */
private fun generateOsmMapHtml(stagesJson: String): String {
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
                    box-shadow: 0 2px 6px rgba(0,0,0,0.45);
                }
                .dest-badge {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 50%;
                    background: #ffffff;
                    border: 2px solid #0E4C99;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.35);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { 
                    zoomControl: false,
                    attributionControl: false
                }).setView([42.8, -6.0], 7);

                // Capa estándar oficial de Google Maps (vía satélites/servidores de Google)
                L.tileLayer('https://{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                    maxZoom: 20,
                    subdomains: ['mt0', 'mt1', 'mt2', 'mt3']
                }).addTo(map);

                // Control de zoom en la parte derecha
                L.control.zoom({ position: 'topright' }).addTo(map);

                var stagesData = ${stagesJson};
                var allLatLngs = [];
                var focusedLatLngs = [];

                stagesData.forEach(function(stage) {
                    if (!stage.visible) return;
                    var pts = stage.points;
                    if (pts && pts.length > 0) {
                        var latlngs = pts.map(function(p) { return [p.lat, p.lon]; });
                        allLatLngs = allLatLngs.concat(latlngs);
                        if (stage.isFocused) {
                            focusedLatLngs = latlngs;
                        }

                        var color = stage.color || '#0E4C99';

                        // Trazado de la etapa con el color asignado
                        var polyline = L.polyline(latlngs, {
                            color: color,
                            weight: stage.isFocused ? 6 : 4,
                            opacity: stage.isFocused ? 1.0 : 0.85
                        }).addTo(map);

                        polyline.bindPopup("<b>Día " + stage.dayNumber + ": " + stage.origin + " → " + stage.destination + "</b><br>" + stage.distanceKm + " km • +" + stage.elevationGainM + "m");

                        // Marcador de salida con el número de etapa
                        var pStart = pts[0];
                        var markerHtml = "<div class='custom-badge' style='background:" + color + "; width:26px; height:26px;'>" + stage.dayNumber + "</div>";
                        var iconStart = L.divIcon({ className: '', html: markerHtml, iconSize: [26, 26], iconAnchor: [13, 13] });
                        L.marker([pStart.lat, pStart.lon], { icon: iconStart }).addTo(map)
                            .bindPopup("<b>Día " + stage.dayNumber + ": " + stage.origin + "</b><br>Salida de etapa");

                        // Marcador de llegada
                        var pEnd = pts[pts.length - 1];
                        var endHtml = "<div class='dest-badge' style='border-color:" + color + "; width:16px; height:16px;'><div style='width:8px; height:8px; background:" + color + "; border-radius:50%;'></div></div>";
                        var iconEnd = L.divIcon({ className: '', html: endHtml, iconSize: [16, 16], iconAnchor: [8, 8] });
                        L.marker([pEnd.lat, pEnd.lon], { icon: iconEnd }).addTo(map)
                            .bindPopup("<b>Llegada Día " + stage.dayNumber + ": " + stage.destination + "</b>");
                    }
                });

                function adjustView() {
                    try {
                        map.invalidateSize();
                        if (focusedLatLngs.length > 0) {
                            var bounds = L.latLngBounds(focusedLatLngs);
                            map.fitBounds(bounds, { padding: [175, 45], maxZoom: 13 });
                        } else if (allLatLngs.length > 0) {
                            var bounds = L.latLngBounds(allLatLngs);
                            map.fitBounds(bounds, { padding: [175, 45] });
                        }
                    } catch(e) {}
                }

                setTimeout(adjustView, 250);
                setTimeout(adjustView, 750);
            </script>
        </body>
        </html>
    """.trimIndent()
}
