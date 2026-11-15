package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.BackupManager
import com.example.ui.CaminoViewModel
import com.example.ui.settings.LanguageMode
import com.example.ui.theme.*

data class FaqCategory(
    val key: String,
    val labelEs: String,
    val labelEn: String
)

data class FaqItem(
    val id: String,
    val categoryKey: String,
    val categoryEs: String,
    val categoryEn: String,
    val questionEs: String,
    val questionEn: String,
    val answerEs: String,
    val answerEn: String
)

val FAQ_CATEGORIES = listOf(
    FaqCategory("gpx", "GPX y Mapas", "GPX & Maps"),
    FaqCategory("stages", "Etapas y Rutas", "Stages & Routes"),
    FaqCategory("accommodations", "Alojamientos", "Accommodations"),
    FaqCategory("journal", "Vivencias y Fotos", "Journal & Photos"),
    FaqCategory("agenda", "Viajes y Agenda", "Trips & Agenda"),
    FaqCategory("stats", "Estadísticas", "Statistics"),
    FaqCategory("rewards", "Recompensas", "Rewards"),
    FaqCategory("pdf", "Exportación y PDF", "Export & PDF"),
    FaqCategory("backup", "Copias y Datos", "Backups & Data"),
    FaqCategory("profiles", "Perfiles y Portada", "Profiles & Cover"),
    FaqCategory("settings", "Ajustes y Consejos", "Settings & Tips")
)

val INITIAL_FAQS = listOf(
    // 1. GPX y Mapas
    FaqItem(
        id = "faq_gpx_import",
        categoryKey = "gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Cómo importar un archivo GPX a una etapa o día?",
        questionEn = "How to import a GPX file into a stage or day?",
        answerEs = "Accede al detalle de la etapa o día donde quieras añadir el trazado. En la tarjeta del mapa o en el menú de opciones (icono de tres puntos), pulsa en 'Subir o Actualizar GPX'. Selecciona el archivo .gpx desde el explorador de archivos de tu dispositivo. La app calculará automáticamente la distancia exacta, el desnivel positivo acumulado y el desnivel negativo, trazando el recorrido en vista 2D sobre el mapa.",
        answerEn = "Open the details of the stage or day where you want to add the route. On the map card or in the options menu (three dots icon), tap 'Upload or Update GPX'. Choose the .gpx file from your device file manager. The app will automatically calculate the exact distance, cumulative elevation gain and loss, drawing the 2D route track onto the map."
    ),
    FaqItem(
        id = "faq_map_layers",
        categoryKey = "gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Qué capas de mapa están disponibles y cómo cambiarlas?",
        questionEn = "What map layers are available and how to switch between them?",
        answerEs = "En la pestaña de 'Mapa' dispones de un botón de capas en la esquina superior. Puedes alternar entre cuatro estilos: Callejero (Google Roadmap, óptimo para entornos urbanos y carreteras), Satélite (imágenes aéreas de alta resolución), Relieve (curvas de nivel y topografía del terreno) y OpenStreetMap (red colaborativa de caminos y senderos de montaña).",
        answerEn = "In the 'Map' tab, tap the layers button in the upper corner. You can toggle between four visual styles: Roadmap (Google Roadmap, ideal for urban navigation and roads), Satellite (high-resolution aerial imagery), Terrain (topographic elevation and contour lines) and OpenStreetMap (community hiking trails and mountain paths)."
    ),
    FaqItem(
        id = "faq_map_navigation",
        categoryKey = "gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Cómo navegar una ruta o abrirla en Google Maps?",
        questionEn = "How to navigate a route or open it in Google Maps?",
        answerEs = "En el detalle de cada etapa dispones de dos accesos directos: 'Abrir en Mapas', que abre la ubicación de origen y destino en tu app de mapas preferida para consultar servicios cercanos, y 'Navegar Ruta en Mapas', que inicia directamente el guiado paso a paso por GPS hacia el final de la jornada.",
        answerEn = "Inside each stage detail screen, you will find two direct actions: 'Open in Maps', which opens the origin and destination coordinates in your favorite map application to check nearby services, and 'Navigate Route in Maps', which immediately launches turn-by-turn GPS navigation towards your destination."
    ),
    FaqItem(
        id = "faq_map_gps_center",
        categoryKey = "gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Cómo centrar el mapa en mi ubicación actual mientras camino?",
        questionEn = "How to center the map on my current location while hiking?",
        answerEs = "En la pantalla del mapa interactivo, pulsa el botón flotante circular con el icono de mirilla/GPS en la esquina inferior. La aplicación solicitará permiso de ubicación (si no fue otorgado previamente) y centrará la vista de inmediato sobre tus coordenadas actuales con un marcador pulsante.",
        answerEn = "On the interactive map screen, tap the floating round button with the GPS crosshair icon in the lower corner. The app will request location permissions (if not already granted) and instantly center the map on your current real-time coordinates with a pulsing marker."
    ),
    FaqItem(
        id = "faq_map_scope",
        categoryKey = "gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Cómo ver todos los tracks de mi Camino o Viaje juntos en el mapa?",
        questionEn = "How to view all tracks of my Camino or Trip together on the map?",
        answerEs = "En la barra superior de la pestaña 'Mapa', pulsa en el botón selector de visualización (situado entre 'Etapas' y 'Ocultar'). Puedes elegir ver solo la etapa seleccionada o la opción 'Total Caminos' / 'Total Viajes'. Al seleccionarla, el mapa cargará y encuadrará en conjunto todos los archivos GPX que hayas vinculado a tus etapas.",
        answerEn = "In the top bar of the 'Map' tab, tap the view selector button (located between 'Stages' and 'Hide'). You can choose to display either the currently selected stage or 'Total Caminos' / 'Total Trips'. Selecting the total view will load and frame all GPX tracks linked to your stages into a unified map overview."
    ),

    // 2. Etapas y Rutas
    FaqItem(
        id = "faq_new_stage",
        categoryKey = "stages",
        categoryEs = "Etapas y Rutas",
        categoryEn = "Stages & Routes",
        questionEs = "¿Cómo añado una nueva etapa o día de viaje?",
        questionEn = "How to add a new stage or travel day?",
        answerEs = "En la pestaña de 'Etapas' (o 'Días' en el perfil de Viajes), pulsa el botón flotante circular con el signo '+' en la esquina inferior derecha. Se abrirá un cuadro de diálogo donde puedes indicar el título, localidad de origen, localidad de destino, fecha prevista, distancia en kilómetros y dificultad estimada.",
        answerEn = "In the 'Stages' tab (or 'Days' in the Trips profile), tap the floating circular button with the '+' symbol in the lower right corner. A dialog will open where you can enter the title, origin, destination, planned date, distance in kilometers and estimated difficulty."
    ),
    FaqItem(
        id = "faq_cascade_dates",
        categoryKey = "stages",
        categoryEs = "Etapas y Rutas",
        categoryEn = "Stages & Routes",
        questionEs = "¿Cómo funciona la actualización de fechas en cascada?",
        questionEn = "How does cascading date update work?",
        answerEs = "Si modificas la fecha de una etapa intermedia desde el botón de editar (icono de lápiz), la aplicación detectará el cambio y te preguntará si deseas aplicar una actualización en cascada. Si aceptas, reajustará automáticamente las fechas de todas las etapas posteriores sumando los días correspondientes para mantener tu calendario perfectamente ordenado sin tener que editarlas una por una.",
        answerEn = "If you edit the date of any intermediate stage using the pencil icon, the app detects the change and prompts whether you want to apply a cascading date update. If confirmed, it automatically re-aligns the dates of all subsequent stages by advancing them day-by-day to keep your entire itinerary synchronized without manual edits."
    ),
    FaqItem(
        id = "faq_mark_completed",
        categoryKey = "stages",
        categoryEs = "Etapas y Rutas",
        categoryEn = "Stages & Routes",
        questionEs = "¿Cómo marcar una etapa o día como completado y para qué sirve?",
        questionEn = "How to mark a stage or day as completed and what is it for?",
        answerEs = "En la tarjeta de la etapa o dentro de su ficha detallada, pulsa el botón 'Marcar Hecha' (o el icono de verificación). La etapa se marcará con un distintivo verde, sus kilómetros y desniveles se sumarán al balance general de completados y avanzará la barra de progreso de tu Camino o Viaje hacia el 100%.",
        answerEn = "On the stage card or inside its detail view, tap the 'Mark as Done' button (or checkmark icon). The stage will display a green completed badge, its kilometers and elevation gains will be credited to your completed general balance, and your overall route progress bar will advance towards 100%."
    ),

    // 3. Alojamientos y Gastos
    FaqItem(
        id = "faq_accommodations",
        categoryKey = "accommodations",
        categoryEs = "Alojamientos",
        categoryEn = "Accommodations",
        questionEs = "¿Cómo registrar alojamientos y reservas?",
        questionEn = "How to manage accommodations and bookings?",
        answerEs = "Dentro de cada etapa o día, pulsa en 'Añadir Alojamiento'. Podrás registrar el nombre, dirección, teléfono con marcación directa, opciones de habitación, precios y marcar si la reserva ya está confirmada. Todos estos datos se reflejan en el cálculo de gasto acumulado.",
        answerEn = "Inside each stage or day, tap 'Add Accommodation'. You can save the name, address, direct-call phone number, room options, prices and toggle whether the booking is confirmed. All these figures are reflected in your cumulative accommodation spending."
    ),
    FaqItem(
        id = "faq_call_accommodation",
        categoryKey = "accommodations",
        categoryEs = "Alojamientos",
        categoryEn = "Accommodations",
        questionEs = "¿Cómo llamar por teléfono directamente a un alojamiento?",
        questionEn = "How to call an accommodation directly from the app?",
        answerEs = "Al guardar el teléfono de un alojamiento en su ficha, aparecerá un botón 'Llamar' junto a sus datos. Al pulsarlo, la app abrirá directamente el marcador telefónico de tu dispositivo con el número listo para llamar con un solo toque, sin necesidad de copiarlo ni memorizarlo.",
        answerEn = "When you save an accommodation phone number, a 'Call' button appears next to it. Tapping it immediately launches your device telephone dialer with the number pre-filled and ready to call in one tap, without having to copy or memorize it."
    ),
    FaqItem(
        id = "faq_booking_budget",
        categoryKey = "accommodations",
        categoryEs = "Alojamientos",
        categoryEn = "Accommodations",
        questionEs = "¿Cómo controlar el estado de la reserva y el gasto acumulado?",
        questionEn = "How to track booking status and cumulative spending?",
        answerEs = "En cada ficha de alojamiento puedes marcar la casilla 'Reserva confirmada'. La app sumará de forma automática el importe de tus alojamientos registrados en el 'Balance General' de la pestaña Datos, permitiéndote llevar un control financiero exacto del presupuesto de tu aventura.",
        answerEn = "On each accommodation card you can toggle the 'Booking Confirmed' switch. The app automatically calculates and updates your cumulative accommodation spending in the 'General Balance' within the Data tab, giving you precise financial control over your journey budget."
    ),

    // 4. Vivencias y Fotos
    FaqItem(
        id = "faq_journal_memories",
        categoryKey = "journal",
        categoryEs = "Vivencias y Fotos",
        categoryEn = "Journal & Photos",
        questionEs = "¿Cómo escribir mi diario de vivencias y reflexiones?",
        questionEn = "How to write my journal of daily reflections and memories?",
        answerEs = "En el detalle de cada jornada encontrarás el apartado 'Vivencias Diarias' (o 'Diario de Viaje'). Puedes redactar tus experiencias, sensaciones, anécdotas con otros compañeros de ruta o momentos especiales. Al pulsar 'Guardar Notas', el texto se guardará de forma persistente y se incluirá en tu guía PDF.",
        answerEn = "Inside each daily detail screen you will find the 'Daily Journal & Memories' section. You can write your experiences, emotions, conversations with fellow travelers and special highlights. Tapping 'Save Journal' stores your reflections persistently and includes them in your exported PDF guide."
    ),
    FaqItem(
        id = "faq_photos",
        categoryKey = "journal",
        categoryEs = "Vivencias y Fotos",
        categoryEn = "Journal & Photos",
        questionEs = "¿Cómo adjuntar fotos a una etapa o día?",
        questionEn = "How to attach photos to a stage or travel day?",
        answerEs = "En la sección de vivencias, pulsa en 'Adjuntar Foto' (o 'Cambiar Foto'). Se abrirá la galería multimedia de tu teléfono para que elijas la imagen más representativa de la jornada. La fotografía seleccionada se mostrará en la ficha del día y quedará incorporada a tu memoria gráfica.",
        answerEn = "In the journal section, tap 'Attach Photo' (or 'Change Photo'). Your device gallery will open allowing you to select the most representative snapshot of the day. The chosen picture will appear on the day card and become part of your personal travel memoir."
    ),

    // 5. Viajes y Agenda
    FaqItem(
        id = "faq_agenda",
        categoryKey = "agenda",
        categoryEs = "Viajes y Agenda",
        categoryEn = "Trips & Agenda",
        questionEs = "¿Cómo funciona la agenda y vivencias en Viajes?",
        questionEn = "How do the agenda and daily memories work in Trips?",
        answerEs = "En el perfil de Viajes, cada día dispone de una sección de 'Agenda y Actividades' con horarios programados y archivos adjuntos (entradas, billetes, reservas), además de un diario de vivencias con notas y fotografías para documentar cada jornada.",
        answerEn = "In the Trips profile, each day includes a 'Scheduled Agenda & Activities' section with planned times and attached files (tickets, boarding passes, vouchers), as well as a daily travel journal with notes and photos to document your journey."
    ),

    // 6. Estadísticas y Balances
    FaqItem(
        id = "faq_stats",
        categoryKey = "stats",
        categoryEs = "Estadísticas",
        categoryEn = "Statistics",
        questionEs = "¿Cómo se calculan las estadísticas y los balances generales?",
        questionEn = "How are statistics and general balances calculated?",
        answerEs = "En la pestaña 'Datos', dispones de dos vistas independientes: 'Datos del Camino / Viaje' (estadísticas de la ruta actualmente abierta) y 'Datos Totales' (balance acumulado de todas tus rutas). Los cálculos respetan estrictamente la separación bimodal: las rutas de Caminos solo suman a los totales de Caminos, y los Viajes solo suman a los totales de Viajes. Las distancias y desniveles se actualizan automáticamente cuando marcas etapas como completadas.",
        answerEn = "In the 'Data' tab, you have two independent views: 'Route Data' (statistics for the currently selected route) and 'Total Data' (cumulative balance across all your routes). Calculations strictly follow the bimodal separation: Camino routes only sum into Camino totals, and Trips only sum into Trip totals. Distances and elevation gains update automatically as you mark stages as completed."
    ),

    // 7. Recompensas y Logros
    FaqItem(
        id = "faq_rewards",
        categoryKey = "rewards",
        categoryEs = "Recompensas",
        categoryEn = "Rewards",
        questionEs = "¿Qué significan los reconocimientos al completar el 100%?",
        questionEn = "What do the 100% completion rewards mean?",
        answerEs = "Cuando completas todas las etapas de una ruta o todas las rutas guardadas, la aplicación te recompensa con un distintivo adaptado al perfil: en Caminos obtendrás la tradicional 'Compostela', mientras que en Viajes conseguirás el 'Pasaporte de Viajero'. Este reconocimiento se muestra en el panel de Datos Totales.",
        answerEn = "When you complete all stages of a route or all saved routes, the app rewards you with a profile-specific badge: in Caminos you earn the traditional 'Compostela', whereas in Trips you achieve the 'Traveler Passport'. This reward is proudly displayed in the Total Data panel."
    ),

    // 8. Exportación y PDF
    FaqItem(
        id = "faq_pdf",
        categoryKey = "pdf",
        categoryEs = "Exportación y PDF",
        categoryEn = "Export & PDF",
        questionEs = "¿Cómo exportar una guía completa en PDF?",
        questionEn = "How to export a complete guide in PDF?",
        answerEs = "Puedes generar un documento PDF listo para imprimir desde el menú de opciones (icono de tres puntos) en la cabecera o desde 'Ajustes' -> 'Exportar PDF'. La guía incluye portada personalizada, índice, mapa cenital 2D con Google Roadmap, consejos clave del día, agenda y fichas completas de alojamientos.",
        answerEn = "You can generate a print-ready PDF document from the options menu (three dots icon) in the header or from 'Settings' -> 'Export PDF'. The guide includes a custom cover, table of contents, 2D top-down Google Roadmap maps, key daily tips, scheduled agenda and complete accommodation listings."
    ),
    FaqItem(
        id = "faq_pdf_2d_maps",
        categoryKey = "pdf",
        categoryEs = "Exportación y PDF",
        categoryEn = "Export & PDF",
        questionEs = "¿Por qué los mapas del PDF se imprimen en vista cenital plana 2D?",
        questionEn = "Why are PDF maps rendered in 2D flat top-down view?",
        answerEs = "Los mapas del PDF están especialmente optimizados para su impresión en papel físico. Una vista cenital plana en 2D (vista desde arriba con capa Callejero Google Roadmap) asegura máxima nitidez de los nombres de localidades y carreteras, evita deformaciones de perspectiva 3D y ahorra tinta al imprimir en comparación con vistas inclinadas o satelitales densas.",
        answerEn = "PDF maps are specifically optimized for physical paper printing. A flat 2D top-down perspective (bird's-eye view using Google Roadmap) ensures maximum legibility of town names and roads, prevents 3D perspective distortion and conserves ink compared to heavy satellite or angled renders."
    ),

    // 9. Copias de Seguridad y Datos
    FaqItem(
        id = "faq_backup",
        categoryKey = "backup",
        categoryEs = "Copias y Datos",
        categoryEn = "Backups & Data",
        questionEs = "¿Cómo funciona la Copia de Seguridad (Backup) y Restauración?",
        questionEn = "How does Backup and Restore work?",
        answerEs = "En la pestaña 'Ajustes', dirígete a la sección 'Copia de Seguridad y Restauración'. Puedes exportar una copia global completa o individual (solo Caminos o solo Viajes) en formato ZIP o JSON. Para restaurar, pulsa en 'Restaurar Copia' y selecciona tu archivo. Si una ruta importada ya existe en la base de datos (por ejemplo, 'Baztán' y 'Baztan'), el sistema la reconocerá inteligentemente y te preguntará si deseas sobreescribirla o mantener la actual.",
        answerEn = "In the 'Settings' tab, go to the 'Backup and Restore' section. You can export a complete global backup or individual backups (Caminos only or Trips only) as ZIP or JSON. To restore, tap 'Restore Backup' and select your file. If an imported route already exists (e.g. 'Baztán' and 'Baztan'), the system will smartly detect it and ask whether to overwrite or keep the current one."
    ),
    FaqItem(
        id = "faq_backup_duplicates",
        categoryKey = "backup",
        categoryEs = "Copias y Datos",
        categoryEn = "Backups & Data",
        questionEs = "¿Cómo gestiona la app los nombres duplicados al importar copias?",
        questionEn = "How does the app handle duplicate route names during import?",
        answerEs = "La aplicación cuenta con normalización inteligente: al importar una ruta, compara su nombre ignorando tildes, mayúsculas y caracteres especiales. Si en la base de datos existe 'Baztán' y el archivo a importar se llama 'Baztan', el sistema los identificará como idénticos y te preguntará si prefieres sobreescribirla o mantener tu versión actual.",
        answerEn = "The application features smart name normalization: when importing a route, it compares names ignoring accents, case and special characters. If 'Baztán' exists in your database and the imported file is named 'Baztan', the system recognizes them as identical and asks whether to overwrite or preserve your current version."
    ),

    // 10. Perfiles y Personalización
    FaqItem(
        id = "faq_profiles",
        categoryKey = "profiles",
        categoryEs = "Perfiles y Portada",
        categoryEn = "Profiles & Cover",
        questionEs = "¿Cómo cambiar entre el perfil de Caminos y el perfil de Viajes?",
        questionEn = "How to switch between Caminos and Travel profiles?",
        answerEs = "En la parte superior de la pantalla principal, encontrarás el selector de perfil con los iconos de Caminante (Caminos) y Maleta (Viajes). Al cambiar de perfil, la aplicación adapta automáticamente la interfaz, los colores, los nombres de las pestañas ('Etapas' vs 'Días') y recuerda la última ruta abierta en cada perfil de forma independiente.",
        answerEn = "At the top of the main screen, you will find the profile selector with the Hiker (Caminos) and Suitcase (Trips) icons. Switching profiles automatically adapts the user interface, color palette, tab labels ('Stages' vs 'Days'), and independently remembers the last active route opened in each profile."
    ),
    FaqItem(
        id = "faq_cover_photo",
        categoryKey = "profiles",
        categoryEs = "Perfiles y Portada",
        categoryEn = "Profiles & Cover",
        questionEs = "¿Cómo personalizar la foto de portada de un camino o viaje?",
        questionEn = "How to customize a route cover photo?",
        answerEs = "Al abrir una ruta en 'Etapas' o 'Días', en la cabecera superior encontrarás un botón para cambiar imagen. Al pulsarlo, podrás seleccionar cualquier fotografía de tu galería para establecerla como portada visual tanto en la aplicación como en la primera página de la guía PDF.",
        answerEn = "When opening a route under 'Stages' or 'Days', you will see an image button in the top banner. Tapping it allows you to choose any picture from your device gallery to use as the visual cover both in the app and on the front page of the PDF guide."
    ),

    // 11. Ajustes y Consejos
    FaqItem(
        id = "faq_settings_theme_lang",
        categoryKey = "settings",
        categoryEs = "Ajustes y Consejos",
        categoryEn = "Settings & Tips",
        questionEs = "¿Cómo cambiar el idioma y el tema visual (Modo Oscuro / Claro)?",
        questionEn = "How to change language and display theme (Dark / Light Mode)?",
        answerEs = "En la pestaña 'Ajustes' puedes alternar entre Español e Inglés con aplicación instantánea en toda la interfaz sin necesidad de reiniciar la app. Asimismo, puedes elegir entre Tema del Sistema, Modo Claro o Modo Oscuro para optimizar el contraste de pantalla y ahorrar batería en exteriores.",
        answerEn = "In the 'Settings' tab you can switch between Spanish and English with immediate effect across the entire UI without restarting the app. You can also select between System Default, Light Mode or Dark Mode to optimize screen contrast and battery life outdoors."
    ),
    FaqItem(
        id = "faq_stage_tips",
        categoryKey = "settings",
        categoryEs = "Ajustes y Consejos",
        categoryEn = "Settings & Tips",
        questionEs = "¿Qué son los Consejos del Día y cómo editarlos?",
        questionEn = "What are Tips of the Day and how to edit them?",
        answerEs = "En cada etapa dispones de un bloque de 'Consejos del Día' (o 'Resumen de la Jornada' en Viajes) con sugerencias sobre fuentes de agua potable, calzado recomendado, paradas de avituallamiento o variantes históricas de la ruta. Puedes pulsar en 'Editar Consejos' para añadir tus propias anotaciones y recomendaciones prácticas.",
        answerEn = "Inside each stage you have a 'Tips of the Day' block (or 'Day Summary' in Trips) with suggestions about drinking water fountains, recommended footwear, rest stops or historical path variations. You can tap 'Edit Tips' to add your own notes and practical advice."
    )
)

private fun normalizeSearchText(text: String): String {
    return BackupManager.stripAccents(text.trim())
        .lowercase()
        .replace("[^a-z0-9\\s]".toRegex(), " ")
        .replace("\\s+".toRegex(), " ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()
    val isEnglish = languageMode == LanguageMode.EN

    fun tr(key: String): String = viewModel.string(key, languageMode)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    var expandedFaqIds by remember { mutableStateOf(setOf<String>()) }

    // Filtrado en tiempo real con normalización estricta (sin acentos, diéresis ni mayúsculas)
    val filteredFaqs = remember(searchQuery, selectedCategoryKey, isEnglish) {
        val normQuery = normalizeSearchText(searchQuery)
        val queryTerms = if (normQuery.isBlank()) emptyList() else normQuery.split(" ").filter { it.isNotBlank() }

        INITIAL_FAQS.filter { item ->
            // Filtro por clave de categoría seleccionada
            val matchesCategory = selectedCategoryKey == null || item.categoryKey == selectedCategoryKey

            if (!matchesCategory) return@filter false

            if (queryTerms.isEmpty()) return@filter true

            // Normalización para comparación inteligente
            val category = if (isEnglish) item.categoryEn else item.categoryEs
            val question = if (isEnglish) item.questionEn else item.questionEs
            val answer = if (isEnglish) item.answerEn else item.answerEs

            val normQuestion = normalizeSearchText(question)
            val normAnswer = normalizeSearchText(answer)
            val normCategory = normalizeSearchText(category)
            // Soporte cruzado adicional para búsquedas bilingües
            val normAltQuestion = normalizeSearchText(if (isEnglish) item.questionEs else item.questionEn)
            val normAltAnswer = normalizeSearchText(if (isEnglish) item.answerEs else item.answerEn)
            val normAltCategory = normalizeSearchText(if (isEnglish) item.categoryEs else item.categoryEn)

            val searchableCorpus = "$normQuestion $normAnswer $normCategory $normAltQuestion $normAltAnswer $normAltCategory"

            // Todos los términos de búsqueda deben coincidir en el corpus
            queryTerms.all { term -> searchableCorpus.contains(term) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tr("help_title"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlueDark
                        )
                        Text(
                            text = tr("help_subtitle"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("help_top_app_bar")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Buscador Inteligente en tiempo real
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("help_search_field"),
                placeholder = {
                    Text(
                        text = tr("search_faq_placeholder"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = tr("search"),
                        tint = CaminoBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("btn_clear_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = tr("faq_clear_search"),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CaminoBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(Modifier.height(12.dp))

            // 2. Chips de Categorías reactivos al idioma
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Chip 0: Todas / All
                item {
                    val isAllSelected = selectedCategoryKey == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedCategoryKey = null },
                        label = {
                            Text(
                                text = tr("faq_category_all"),
                                fontSize = 12.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PilgrimYellow,
                            selectedLabelColor = CaminoBlueDark,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            0.8.dp,
                            if (isAllSelected) PilgrimYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                items(FAQ_CATEGORIES, key = { it.key }) { cat ->
                    val isSelected = selectedCategoryKey == cat.key
                    val label = if (isEnglish) cat.labelEn else cat.labelEs
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryKey = if (isSelected) null else cat.key
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PilgrimYellow,
                            selectedLabelColor = CaminoBlueDark,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) PilgrimYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3. Lista de Preguntas Frecuentes (Acordeón expandible)
            if (filteredFaqs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PilgrimYellowLight,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = CaminoBlueDark,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "${tr("faq_no_results")} \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = tr("faq_no_results_hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryKey = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = tr("faq_clear_search"),
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("help_faq_list"),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredFaqs, key = { it.id }) { item ->
                        val isExpanded = expandedFaqIds.contains(item.id)
                        val rotationState by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "chevron_rotation"
                        )
                        val question = if (isEnglish) item.questionEn else item.questionEs
                        val answer = if (isEnglish) item.answerEn else item.answerEs
                        val category = if (isEnglish) item.categoryEn else item.categoryEs

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isExpanded) CaminoBlue.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    expandedFaqIds = if (isExpanded) {
                                        expandedFaqIds - item.id
                                    } else {
                                        expandedFaqIds + item.id
                                    }
                                }
                                .testTag("faq_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = PilgrimYellowLight,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CaminoBlueDark,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) tr("collapse") else tr("expand"),
                                        tint = CaminoBlue,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .rotate(rotationState)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )

                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        Spacer(Modifier.height(10.dp))
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            thickness = 0.8.dp
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 21.sp
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
}
