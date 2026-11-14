package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CaminoViewModel
import com.example.R
import com.example.data.gpx.GpxParser
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

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
 * Modos de visualización del mapa: Este Camino activo o Red Total de todos los caminos
 */
enum class MapScopeMode {
    CURRENT_CAMINO, // "Este Camino"
    TOTAL           // "Total"
}

/**
 * Paleta de colores predefinida de alta visibilidad para distinguir cada Camino en la vista Total
 */
val CAMINO_PALETTE_HEX = listOf(
    "#2563EB", // Azul Real
    "#DC2626", // Rojo Carmín
    "#16A34A", // Verde Esmeralda
    "#D97706", // Ámbar / Naranja dorado
    "#9333EA", // Púrpura Intenso
    "#0891B2", // Cian / Turquesa
    "#EA580C", // Naranja Vivo
    "#4F46E5", // Índigo Profundo
    "#059669", // Verde Jade
    "#DB2777", // Rosa Fucsia
    "#0284C7", // Azul Océano
    "#CA8A04", // Mostaza
    "#7C3AED", // Violeta
    "#B45309"  // Marrón Cobrizo
)

fun getCaminoColorHex(caminoId: Long, index: Int = -1): String {
    val idx = if (index >= 0) {
        index % CAMINO_PALETTE_HEX.size
    } else {
        kotlin.math.abs(caminoId.hashCode()) % CAMINO_PALETTE_HEX.size
    }
    return CAMINO_PALETTE_HEX[idx]
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
        "somport" to Pair(42.7972, -0.5283),

        // Camino de San Salvador
        "la robla" to Pair(42.8025, -5.6286),
        "poladura de la tercia" to Pair(42.9806, -5.7486),
        "poladura" to Pair(42.9806, -5.7486),
        "pajares" to Pair(43.0039, -5.7867),
        "pola de lena" to Pair(43.1611, -5.8286),

        // Camino Baztanés
        "bayona" to Pair(43.4929, -1.4748),
        "ustaritz" to Pair(43.3986, -1.4558),
        "ainhoa" to Pair(43.3067, -1.4986),
        "dantxarinea" to Pair(43.2986, -1.5033),
        "urdazubi" to Pair(43.2667, -1.5039),
        "urdax" to Pair(43.2667, -1.5039),
        "elizondo" to Pair(43.1469, -1.5208),
        "berroeta" to Pair(43.0767, -1.5750),
        "olagüe" to Pair(42.9667, -1.6033),
        "olague" to Pair(42.9667, -1.6033),
        "trinidad de arre" to Pair(42.8333, -1.6167),

        // Camino Vasco del Interior
        "hernani" to Pair(43.2667, -1.9750),
        "tolosa" to Pair(43.1367, -2.0739),
        "zerain" to Pair(43.0119, -2.2711),
        "beasain" to Pair(43.0486, -2.2039),
        "zegama" to Pair(42.9750, -2.2906),
        "salvatierra" to Pair(42.8517, -2.3906),
        "agurain" to Pair(42.8517, -2.3906),
        "vitoria" to Pair(42.8467, -2.6717),
        "vitoria-gasteiz" to Pair(42.8467, -2.6717),
        "gasteiz" to Pair(42.8467, -2.6717),
        "la puebla de arganzón" to Pair(42.7667, -2.8306),
        "arganzón" to Pair(42.7667, -2.8306),
        "arganzon" to Pair(42.7667, -2.8306),
        "haro" to Pair(42.5767, -2.8472),

        // Camino Sanabrés
        "granja de moreruela" to Pair(41.8106, -5.7578),
        "tábara" to Pair(41.8242, -5.9622),
        "tabara" to Pair(41.8242, -5.9622),
        "santa croya de tera" to Pair(41.9897, -5.9722),
        "santa croya" to Pair(41.9897, -5.9722),
        "rionegro del puente" to Pair(42.0069, -6.2269),
        "rionegro" to Pair(42.0069, -6.2269),
        "mombuey" to Pair(42.0236, -6.3681),
        "puebla de sanabria" to Pair(42.0550, -6.6339),
        "lubián" to Pair(42.0361, -6.9083),
        "lubian" to Pair(42.0361, -6.9083),
        "a gudiña" to Pair(42.0611, -7.1389),
        "a gudina" to Pair(42.0611, -7.1389),
        "laza" to Pair(42.0611, -7.4611),
        "xunqueira de ambía" to Pair(42.2044, -7.7347),
        "xunqueira" to Pair(42.2044, -7.7347),
        "cea" to Pair(42.4639, -8.0125),
        "a laxe" to Pair(42.6167, -8.1500),
        "laxe" to Pair(42.6167, -8.1500),
        "silleda" to Pair(42.6972, -8.2472),
        "bandeira" to Pair(42.7486, -8.2833),
        "outeiro" to Pair(42.7847, -8.3847),

        // Vía de la Plata
        "guillena" to Pair(37.5283, -6.0528),
        "castilblanco de los arroyos" to Pair(37.6719, -5.9892),
        "almadén de la plata" to Pair(37.8736, -6.0825),
        "monesterio" to Pair(38.0872, -6.2750),
        "fuente de cantos" to Pair(38.2467, -6.3117),
        "zafra" to Pair(38.4258, -6.4175),
        "los santos de maimona" to Pair(38.4500, -6.3833),
        "villafranca de los barros" to Pair(38.5611, -6.3389),
        "almendralejo" to Pair(38.6833, -6.4069),
        "torremejía" to Pair(38.7897, -6.3769),
        "torremejia" to Pair(38.7897, -6.3769),
        "alcuéscar" to Pair(39.1806, -6.2306),
        "alcuescar" to Pair(39.1806, -6.2306),
        "cañaveral" to Pair(39.7917, -6.4939),
        "canaveral" to Pair(39.7917, -6.4939),
        "galisteo" to Pair(39.9767, -6.2694),
        "cáparra" to Pair(40.1667, -6.1000),
        "caparra" to Pair(40.1667, -6.1000),
        "oliva de plasencia" to Pair(40.1117, -6.0875),
        "baños de montemayor" to Pair(40.3183, -5.8569),
        "banos de montemayor" to Pair(40.3183, -5.8569),
        "calzada de béjar" to Pair(40.4139, -5.8167),
        "calzada de bejar" to Pair(40.4139, -5.8167),
        "el cubo de la tierra del vino" to Pair(41.2536, -5.7083),
        "el cubo" to Pair(41.2536, -5.7083),
        "riego del camino" to Pair(41.8222, -5.6944),
        "benavente" to Pair(42.0033, -5.6783),
        "alija del infantado" to Pair(42.1361, -5.8306),
        "la bañeza" to Pair(42.2986, -5.8986),
        "la baneza" to Pair(42.2986, -5.8986)
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
 * Capas de mapa disponibles mediante OnlineTileSourceBase
 */
enum class MapLayerType(val label: String, val iconDescription: String) {
    ROADMAP("Callejero", "Mapa Callejero de Google"),
    HYBRID("Satélite", "Satélite con etiquetas de Google"),
    TERRAIN("Relieve", "Relieve y Terreno de Google"),
    OSM("OpenStreetMap", "OpenStreetMap estándar")
}

class GoogleMapsTileSource(
    name: String,
    lyrs: String,
    lang: String = Locale.getDefault().language
) : OnlineTileSourceBase(
    name,
    0,
    20,
    256,
    ".png",
    arrayOf(
        "https://mt0.google.com/vt/lyrs=$lyrs&hl=$lang&",
        "https://mt1.google.com/vt/lyrs=$lyrs&hl=$lang&",
        "https://mt2.google.com/vt/lyrs=$lyrs&hl=$lang&",
        "https://mt3.google.com/vt/lyrs=$lyrs&hl=$lang&"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}x=$x&y=$y&z=$zoom"
    }
}

/**
 * Generador de marcadores personalizados (chinchetas con color y número de etapa)
 */
fun createStageMarkerDrawable(
    context: Context,
    number: Int,
    colorHex: String,
    isDestination: Boolean = false
): Drawable {
    val density = context.resources.displayMetrics.density
    val sizeDp = if (isDestination) 22 else 30
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val colorInt = try {
        android.graphics.Color.parseColor(colorHex)
    } catch (_: Exception) {
        android.graphics.Color.BLUE
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (isDestination) {
        // Marcador de destino: Anillo exterior con punto interior
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        paint.color = colorInt
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * density
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - (1.5f * density), paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 4.5f, paint)
    } else {
        // Marcador de salida: Círculo con borde blanco y número de etapa centrado
        paint.color = colorInt
        paint.style = Paint.Style.FILL
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * density
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - (1.25f * density), paint)

        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 12f * density
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        val yOffset = (sizePx / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText("$number", sizePx / 2f, yOffset, paint)
    }

    return BitmapDrawable(context.resources, bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveMapScreen(
    camino: CaminoEntity,
    stages: List<StageEntity>,
    onStageClick: (Long) -> Unit,
    viewModel: CaminoViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Datos reactivos globales de todos los caminos
    val allCaminosState = viewModel?.caminos?.collectAsStateWithLifecycle(emptyList())
    val allStagesState = viewModel?.allStages?.collectAsStateWithLifecycle(emptyList())
    val allCaminos = allCaminosState?.value?.ifEmpty { listOf(camino) } ?: listOf(camino)
    val allStages = allStagesState?.value?.ifEmpty { stages } ?: stages

    // Modo de visualización: "Este Camino" (por defecto) vs "Total" (todas las rutas)
    var mapScopeMode by remember { mutableStateOf(MapScopeMode.CURRENT_CAMINO) }
    var showScopeDropdown by remember { mutableStateOf(false) }

    // Inicializar configuración de OSMDroid con User-Agent seguro
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("${context.packageName}_osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Etapas ordenadas
    val sortedStages = remember(stages) {
        stages.sortedBy { it.dayNumber }
    }

    // Etapas seleccionadas / visibles
    var selectedStageIds by remember(stages) {
        mutableStateOf(stages.map { it.id }.toSet())
    }

    // Etapa seleccionada para la tarjeta flotante inferior
    var selectedDetailStage by remember {
        mutableStateOf<StageEntity?>(null)
    }

    var showStagesDropdown by remember { mutableStateOf(false) }
    var showLayersMenu by remember { mutableStateOf(false) }
    var currentLayer by remember { mutableStateOf(MapLayerType.ROADMAP) }

    // Referencia al MapView y Overlay de ubicación
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Launcher de permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationOverlayRef?.enableMyLocation()
            centerMapOnUser(context, mapViewRef, locationOverlayRef)
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Estadísticas para la cabecera idéntica a la pantalla de Etapas
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
        String.format(Locale.US, "%.1f", totalKm)
    }
    val completedKmStr = remember(completedKm) {
        String.format(Locale.US, "%.1f", completedKm)
    }

    // Efecto para actualizar overlays del mapa cuando cambian las etapas seleccionadas, capas o modo de filtro
    LaunchedEffect(selectedStageIds, sortedStages, allStages, allCaminos, mapViewRef, currentLayer, mapScopeMode) {
        val map = mapViewRef ?: return@LaunchedEffect

        // Actualizar TileSource según la capa seleccionada
        val tileSource = when (currentLayer) {
            MapLayerType.ROADMAP -> GoogleMapsTileSource("GoogleRoadmap", "m")
            MapLayerType.HYBRID -> GoogleMapsTileSource("GoogleHybrid", "y")
            MapLayerType.TERRAIN -> GoogleMapsTileSource("GoogleTerrain", "p")
            MapLayerType.OSM -> TileSourceFactory.MAPNIK
        }
        map.setTileSource(tileSource)

        // Limpiar overlays previos preservando el locationOverlay si existe (evita problemas de rendimiento y duplicados)
        val locOverlay = locationOverlayRef
        map.overlays.clear()
        if (locOverlay != null) {
            map.overlays.add(locOverlay)
        }

        val allGeoPoints = mutableListOf<GeoPoint>()

        if (mapScopeMode == MapScopeMode.CURRENT_CAMINO) {
            // ==========================================
            // LÓGICA: ESTE CAMINO
            // ==========================================
            for (stg in sortedStages) {
                if (!selectedStageIds.contains(stg.id)) continue

                val colorHex = getStageColor(stg.dayNumber)
                val colorInt = try {
                    android.graphics.Color.parseColor(colorHex)
                } catch (_: Exception) {
                    android.graphics.Color.BLUE
                }

                val gpxPoints = GpxParser.deserializePoints(stg.gpxTrackJson)
                val hasGpx = gpxPoints.size >= 2

                val stagePoints: List<GeoPoint> = if (hasGpx) {
                    gpxPoints.map { GeoPoint(it.lat, it.lon) }
                } else {
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
                    listOf(GeoPoint(sLat, sLon), GeoPoint(eLat, eLon))
                }

                if (stagePoints.size >= 2) {
                    allGeoPoints.addAll(stagePoints)

                    val polyline = Polyline(map)
                    polyline.setPoints(stagePoints)
                    polyline.outlinePaint.color = colorInt

                    if (hasGpx) {
                        polyline.outlinePaint.pathEffect = null
                        polyline.outlinePaint.strokeWidth = 10f
                    } else {
                        polyline.outlinePaint.pathEffect = DashPathEffect(floatArrayOf(24f, 16f), 0f)
                        polyline.outlinePaint.strokeWidth = 6f
                    }

                    polyline.setOnClickListener { _, _, _ ->
                        selectedDetailStage = stg
                        true
                    }
                    map.overlays.add(polyline)

                    val startPoint = stagePoints.first()
                    val startMarker = Marker(map).apply {
                        position = startPoint
                        icon = createStageMarkerDrawable(context, stg.dayNumber, colorHex, isDestination = false)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Día ${stg.dayNumber}: ${stg.origin}"
                        snippet = "${stg.distanceKm} km"
                        setOnMarkerClickListener { _, _ ->
                            selectedDetailStage = stg
                            true
                        }
                    }
                    map.overlays.add(startMarker)

                    val endPoint = stagePoints.last()
                    val endMarker = Marker(map).apply {
                        position = endPoint
                        icon = createStageMarkerDrawable(context, stg.dayNumber, colorHex, isDestination = true)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Llegada: ${stg.destination}"
                        snippet = "Fin de etapa ${stg.dayNumber}"
                        setOnMarkerClickListener { _, _ ->
                            selectedDetailStage = stg
                            true
                        }
                    }
                    map.overlays.add(endMarker)
                }
            }
        } else {
            // ==========================================
            // LÓGICA: TOTAL (DISTINCIÓN POR COLORES DE CADA CAMINO)
            // ==========================================
            val caminoColorMap = allCaminos.mapIndexed { index, c ->
                c.id to getCaminoColorHex(c.id, index)
            }.toMap()

            val stagesToDraw = if (allStages.isNotEmpty()) allStages else sortedStages
            for (stg in stagesToDraw) {
                // Color único e idéntico para todas las etapas, polilíneas y marcadores del mismo camino
                val colorHex = caminoColorMap[stg.caminoId] ?: getCaminoColorHex(stg.caminoId)
                val colorInt = try {
                    android.graphics.Color.parseColor(colorHex)
                } catch (_: Exception) {
                    android.graphics.Color.BLUE
                }

                val gpxPoints = GpxParser.deserializePoints(stg.gpxTrackJson)
                val hasGpx = gpxPoints.size >= 2

                val stagePoints: List<GeoPoint> = if (hasGpx) {
                    gpxPoints.map { GeoPoint(it.lat, it.lon) }
                } else {
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
                    listOf(GeoPoint(sLat, sLon), GeoPoint(eLat, eLon))
                }

                if (stagePoints.size >= 2) {
                    allGeoPoints.addAll(stagePoints)

                    val polyline = Polyline(map)
                    polyline.setPoints(stagePoints)
                    polyline.outlinePaint.color = colorInt

                    if (hasGpx) {
                        polyline.outlinePaint.pathEffect = null
                        polyline.outlinePaint.strokeWidth = 9f
                    } else {
                        polyline.outlinePaint.pathEffect = DashPathEffect(floatArrayOf(24f, 16f), 0f)
                        polyline.outlinePaint.strokeWidth = 6f
                    }

                    polyline.setOnClickListener { _, _, _ ->
                        selectedDetailStage = stg
                        true
                    }
                    map.overlays.add(polyline)

                    val cName = allCaminos.find { it.id == stg.caminoId }?.title ?: ""

                    val startPoint = stagePoints.first()
                    val startMarker = Marker(map).apply {
                        position = startPoint
                        icon = createStageMarkerDrawable(context, stg.dayNumber, colorHex, isDestination = false)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "$cName - Día ${stg.dayNumber}: ${stg.origin}"
                        snippet = "${stg.distanceKm} km"
                        setOnMarkerClickListener { _, _ ->
                            selectedDetailStage = stg
                            true
                        }
                    }
                    map.overlays.add(startMarker)

                    val endPoint = stagePoints.last()
                    val endMarker = Marker(map).apply {
                        position = endPoint
                        icon = createStageMarkerDrawable(context, stg.dayNumber, colorHex, isDestination = true)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "$cName - Llegada: ${stg.destination}"
                        snippet = "Fin de etapa ${stg.dayNumber}"
                        setOnMarkerClickListener { _, _ ->
                            selectedDetailStage = stg
                            true
                        }
                    }
                    map.overlays.add(endMarker)
                }
            }
        }

        // Ajuste automático de cámara mediante BoundingBox con padding (para Este Camino o Total)
        if (allGeoPoints.isNotEmpty()) {
            try {
                val baseBox = BoundingBox.fromGeoPoints(allGeoPoints)
                val latSpan = (baseBox.latNorth - baseBox.latSouth).coerceAtLeast(0.04)
                val lonSpan = (baseBox.lonEast - baseBox.lonWest).coerceAtLeast(0.04)
                val paddedBox = BoundingBox(
                    baseBox.latNorth + (latSpan * 0.45), // espacio para tarjeta superior
                    baseBox.lonEast + (lonSpan * 0.15),
                    baseBox.latSouth - (latSpan * 0.35), // espacio para controles inferiores
                    baseBox.lonWest - (lonSpan * 0.15)
                )
                map.post {
                    map.zoomToBoundingBox(paddedBox, true, 80)
                }
            } catch (_: Exception) {}
        }

        map.invalidate()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // MAPA NATIVO INTERACTIVO JETPACK COMPOSE + OSMDROID
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("osmdroid_map_view"),
            factory = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true

                    val initialTileSource = when (currentLayer) {
                        MapLayerType.ROADMAP -> GoogleMapsTileSource("GoogleRoadmap", "m")
                        MapLayerType.HYBRID -> GoogleMapsTileSource("GoogleHybrid", "y")
                        MapLayerType.TERRAIN -> GoogleMapsTileSource("GoogleTerrain", "p")
                        MapLayerType.OSM -> TileSourceFactory.MAPNIK
                    }
                    setTileSource(initialTileSource)

                    controller.setZoom(8.0)
                    controller.setCenter(GeoPoint(42.88, -7.50))

                    // Overlay de Mi Ubicación
                    val myLocOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    overlays.add(myLocOverlay)
                    locationOverlayRef = myLocOverlay
                    mapViewRef = this
                }
            },
            update = { mapView ->
                mapViewRef = mapView
            }
        )

        // TARJETA DE TÍTULO SUPERIOR: Idéntica a la pantalla de Etapas (CaminoHeaderBanner)
        val mapBannerBg = if (camino.isTravelGuide) CaminoBlueDark else CaminoBlue
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                .align(Alignment.TopCenter)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = mapBannerBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(mapBannerBg)
            ) {
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
                            .background(mapBannerBg),
                        contentScale = ContentScale.Fit
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 25f
                            )
                        )
                )

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
                                text = if (mapScopeMode == MapScopeMode.TOTAL) {
                                    "RED GLOBAL DE RUTAS"
                                } else if (camino.isTravelGuide) {
                                    "ULTREIA VIAJE"
                                } else {
                                    "ULTREIA GUÍA"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CaminoBlueDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (mapScopeMode == MapScopeMode.CURRENT_CAMINO && camino.startDate.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "📅 ${camino.startDate}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (mapScopeMode == MapScopeMode.TOTAL) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "🌍 ${allCaminos.size} caminos guardados",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (mapScopeMode == MapScopeMode.TOTAL) "Red Total de Caminos" else camino.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (mapScopeMode == MapScopeMode.TOTAL) {
                            "Todas las etapas mostradas con colores diferenciados por camino"
                        } else {
                            camino.subtitle
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mapScopeMode == MapScopeMode.TOTAL) {
                                "${allStages.filter { it.isCompleted }.sumOf { it.distanceKm }.toInt()} / ${allStages.sumOf { it.distanceKm }.toInt()} km"
                            } else {
                                "${completedKmStr} / ${totalKmStr} km"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = PilgrimYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (mapScopeMode == MapScopeMode.TOTAL) {
                                "${allStages.size} etapas totales"
                            } else {
                                "${progressPercent}% completado"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // BOTONES FLOTANTES LATERALES: Selector de Capas de Mapa y Mi Ubicación
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Botón Selector de Capas
            Box {
                SmallFloatingActionButton(
                    onClick = { showLayersMenu = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CaminoBlue,
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .testTag("btn_map_layers")
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Capas del mapa")
                }

                DropdownMenu(
                    expanded = showLayersMenu,
                    onDismissRequest = { showLayersMenu = false }
                ) {
                    Text(
                        text = "Capas de Mapa",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CaminoBlue,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider()

                    MapLayerType.values().forEach { layer ->
                        val isSelected = currentLayer == layer
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = layer.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = layer.iconDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                currentLayer = layer
                                showLayersMenu = false
                            }
                        )
                    }
                }
            }

            // Botón "Mi Ubicación"
            SmallFloatingActionButton(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {
                        locationOverlayRef?.enableMyLocation()
                        centerMapOnUser(context, mapViewRef, locationOverlayRef)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CaminoBlue,
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .testTag("btn_my_location")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
            }
        }

        // TARJETA INFERIOR INTERACTIVA: Muestra resumen de la etapa al pulsar marcador o polyline
        AnimatedVisibility(
            visible = selectedDetailStage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 82.dp)
        ) {
            val stage = selectedDetailStage
            if (stage != null) {
                val hasGpx = !stage.gpxTrackJson.isNullOrBlank()
                val stageColor = getStageComposeColor(stage.dayNumber)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(stageColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${stage.dayNumber}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Día ${stage.dayNumber}: ${stage.origin} → ${stage.destination}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${stage.distanceKm} km • +${stage.elevationGainM}m desnivel",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedDetailStage = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (hasGpx) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (hasGpx) "📍 Recorrido GPX Real" else "〰️ Trazado Estándar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasGpx) Color(0xFF16A34A) else CaminoBlueDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                color = if (stage.isCompleted) CaminoBlue.copy(alpha = 0.15f) else PilgrimYellow.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (stage.isCompleted) "✓ Completada" else "Pendiente",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stage.isCompleted) CaminoBlue else PilgrimYellowDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStageClick(stage.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Ver Detalles", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val points = if (hasGpx) {
                                        GpxParser.deserializePoints(stage.gpxTrackJson).map { GeoPoint(it.lat, it.lon) }
                                    } else {
                                        val sCoord = TownCoordinates.findCoordinates(stage.origin)
                                        val eCoord = TownCoordinates.findCoordinates(stage.destination)
                                        if (sCoord != null && eCoord != null) {
                                            listOf(GeoPoint(sCoord.first, sCoord.second), GeoPoint(eCoord.first, eCoord.second))
                                        } else emptyList()
                                    }
                                    if (points.isNotEmpty()) {
                                        try {
                                            val bBox = BoundingBox.fromGeoPoints(points)
                                            mapViewRef?.zoomToBoundingBox(bBox, true, 120)
                                        } catch (_: Exception) {}
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Centrar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // PANEL DE CONTROL INFERIOR (Los 3 botones en el orden exacto solicitado):
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
                                    selectedDetailStage = stg
                                    showStagesDropdown = false
                                }
                            )
                        }
                    }
                }

                // Botón 2: Menú Desplegable con opciones "Este Camino" y "Total"
                Box(modifier = Modifier.weight(1.1f)) {
                    Button(
                        onClick = { showScopeDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PilgrimYellow),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_map_control_todos")
                    ) {
                        Icon(
                            imageVector = if (mapScopeMode == MapScopeMode.TOTAL) Icons.Default.Public else Icons.Default.SelectAll,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (mapScopeMode == MapScopeMode.TOTAL) "Total" else "Este Camino",
                            color = CaminoBlueDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = CaminoBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showScopeDropdown,
                        onDismissRequest = { showScopeDropdown = false },
                        modifier = Modifier
                            .width(180.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Hiking,
                                        contentDescription = null,
                                        tint = if (mapScopeMode == MapScopeMode.CURRENT_CAMINO) CaminoBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Este Camino",
                                        fontWeight = if (mapScopeMode == MapScopeMode.CURRENT_CAMINO) FontWeight.Bold else FontWeight.Normal,
                                        color = if (mapScopeMode == MapScopeMode.CURRENT_CAMINO) CaminoBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                mapScopeMode = MapScopeMode.CURRENT_CAMINO
                                selectedStageIds = sortedStages.map { it.id }.toSet()
                                selectedDetailStage = null
                                showScopeDropdown = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (mapScopeMode == MapScopeMode.TOTAL) CaminoBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Total",
                                        fontWeight = if (mapScopeMode == MapScopeMode.TOTAL) FontWeight.Bold else FontWeight.Normal,
                                        color = if (mapScopeMode == MapScopeMode.TOTAL) CaminoBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                mapScopeMode = MapScopeMode.TOTAL
                                selectedDetailStage = null
                                showScopeDropdown = false
                            }
                        )
                    }
                }

                // Botón 3: "Ocultar Etapas" (Oculta las líneas y puntos del mapa)
                OutlinedButton(
                    onClick = {
                        selectedStageIds = emptySet()
                        selectedDetailStage = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
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
 * Centra el mapa en la ubicación del usuario de forma suave
 */
private fun centerMapOnUser(
    context: Context,
    mapView: MapView?,
    locationOverlay: MyLocationNewOverlay?
) {
    val map = mapView ?: return
    val loc = locationOverlay?.myLocation

    if (loc != null) {
        map.controller.animateTo(loc)
        map.controller.setZoom(15.0)
    } else {
        try {
            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lastGps = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = lastGps ?: lastNet
            if (best != null) {
                val pt = GeoPoint(best.latitude, best.longitude)
                map.controller.animateTo(pt)
                map.controller.setZoom(15.0)
            } else {
                Toast.makeText(context, "Obteniendo posición GPS...", Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(context, "Permiso de ubicación no disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
