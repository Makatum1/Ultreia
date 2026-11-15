package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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

data class FaqItem(
    val id: String,
    val categoryEs: String,
    val categoryEn: String,
    val questionEs: String,
    val questionEn: String,
    val answerEs: String,
    val answerEn: String
)

val INITIAL_FAQS = listOf(
    FaqItem(
        id = "faq_gpx",
        categoryEs = "GPX y Mapas",
        categoryEn = "GPX & Maps",
        questionEs = "¿Cómo importar un archivo GPX a una etapa o día?",
        questionEn = "How to import a GPX file into a stage or day?",
        answerEs = "Entra en el detalle de la etapa o día donde quieras añadir el recorrido. En la tarjeta del mapa o en el menú de opciones (tres puntos), pulsa en 'Subir o Actualizar GPX'. Selecciona el archivo .gpx desde el explorador de archivos de tu dispositivo. La aplicación calculará automáticamente la distancia, el desnivel positivo, el desnivel negativo y dibujará el track en 2D en el mapa.",
        answerEn = "Open the details of the stage or day where you want to add the route. On the map card or in the options menu (three dots), tap 'Upload or Update GPX'. Select the .gpx file from your device file manager. The app will automatically calculate distance, elevation gain, elevation loss and draw the 2D track on the map."
    ),
    FaqItem(
        id = "faq_backup",
        categoryEs = "Copias y Datos",
        categoryEn = "Backups & Data",
        questionEs = "¿Cómo funciona la Copia de Seguridad (Backup) y Restauración?",
        questionEn = "How does Backup and Restore work?",
        answerEs = "En la pestaña 'Ajustes', dirígete a la sección 'Copia de Seguridad y Restauración'. Puedes exportar una copia global completa o individual (solo Caminos o solo Viajes) en formato ZIP o JSON. Para restaurar, pulsa en 'Restaurar Copia' y selecciona tu archivo. Si una ruta importada ya existe en la base de datos (por ejemplo, 'Baztán' y 'Baztan'), el sistema la reconocerá inteligentemente y te preguntará si deseas sobreescribirla o mantener la actual.",
        answerEn = "In the 'Settings' tab, go to the 'Backup and Restore' section. You can export a complete global backup or individual backups (Caminos only or Trips only) as ZIP or JSON. To restore, tap 'Restore Backup' and select your file. If an imported route already exists (e.g. 'Baztán' and 'Baztan'), the system will smartly detect it and ask whether to overwrite or keep the current one."
    ),
    FaqItem(
        id = "faq_new_stage",
        categoryEs = "Etapas y Rutas",
        categoryEn = "Stages & Routes",
        questionEs = "¿Cómo añado una nueva etapa o día de viaje?",
        questionEn = "How to add a new stage or travel day?",
        answerEs = "En la pestaña de 'Etapas' (o 'Días' en el perfil de Viajes), pulsa el botón flotante circular con el signo '+' en la esquina inferior derecha. Se abrirá un cuadro de diálogo donde puedes indicar el título, localidad de origen, localidad de destino, fecha prevista, distancia en kilómetros y dificultad estimada.",
        answerEn = "In the 'Stages' tab (or 'Days' in the Trips profile), tap the floating circular button with the '+' symbol in the lower right corner. A dialog will open where you can enter the title, origin, destination, planned date, distance in kilometers and estimated difficulty."
    ),
    FaqItem(
        id = "faq_stats",
        categoryEs = "Estadísticas",
        categoryEn = "Statistics",
        questionEs = "¿Cómo se calculan las estadísticas y los balances generales?",
        questionEn = "How are statistics and general balances calculated?",
        answerEs = "En la pestaña 'Datos', dispones de dos vistas independientes: 'Datos del Camino / Viaje' (estadísticas de la ruta actualmente abierta) y 'Datos Totales' (balance acumulado de todas tus rutas). Los cálculos respetan estrictamente la separación bimodal: las rutas de Caminos solo suman a los totales de Caminos, y los Viajes solo suman a los totales de Viajes. Las distancias y desniveles se actualizan automáticamente cuando marcas etapas como completadas.",
        answerEn = "In the 'Data' tab, you have two independent views: 'Route Data' (statistics for the currently selected route) and 'Total Data' (cumulative balance across all your routes). Calculations strictly follow the bimodal separation: Camino routes only sum into Camino totals, and Trips only sum into Trip totals. Distances and elevation gains update automatically as you mark stages as completed."
    ),
    FaqItem(
        id = "faq_pdf",
        categoryEs = "Exportación y PDF",
        categoryEn = "Export & PDF",
        questionEs = "¿Cómo exportar una guía completa en PDF?",
        questionEn = "How to export a complete guide in PDF?",
        answerEs = "Puedes generar un documento PDF listo para imprimir desde el menú de opciones (icono de tres puntos) en la cabecera o desde 'Ajustes' -> 'Exportar PDF'. La guía incluye portada personalizada, índice, mapa cenital 2D con Google Roadmap, consejos clave del día, agenda y fichas completas de alojamientos.",
        answerEn = "You can generate a print-ready PDF document from the options menu (three dots icon) in the header or from 'Settings' -> 'Export PDF'. The guide includes a custom cover, table of contents, 2D top-down Google Roadmap maps, key daily tips, scheduled agenda and complete accommodation listings."
    ),
    FaqItem(
        id = "faq_profiles",
        categoryEs = "Perfiles y Navegación",
        categoryEn = "Profiles & Navigation",
        questionEs = "¿Cómo cambiar entre el perfil de Caminos y el perfil de Viajes?",
        questionEn = "How to switch between Caminos and Travel profiles?",
        answerEs = "En la parte superior de la pantalla principal, encontrarás el selector de perfil con los iconos de Caminante (Caminos) y Maleta (Viajes). Al cambiar de perfil, la aplicación adapta automáticamente la interfaz, los colores, los nombres de las pestañas ('Etapas' vs 'Días') y recuerda la última ruta abierta en cada perfil de forma independiente.",
        answerEn = "At the top of the main screen, you will find the profile selector with the Hiker (Caminos) and Suitcase (Trips) icons. Switching profiles automatically adapts the user interface, color palette, tab labels ('Stages' vs 'Days'), and independently remembers the last active route opened in each profile."
    ),
    FaqItem(
        id = "faq_cover_photo",
        categoryEs = "Personalización",
        categoryEn = "Customization",
        questionEs = "¿Cómo personalizar la foto de portada de un camino o viaje?",
        questionEn = "How to customize a route cover photo?",
        answerEs = "Al abrir una ruta en 'Etapas' o 'Días', en la cabecera superior encontrarás un botón para cambiar imagen. Al pulsarlo, podrás seleccionar cualquier fotografía de tu galería para establecerla como portada visual tanto en la aplicación como en la primera página de la guía PDF.",
        answerEn = "When opening a route under 'Stages' or 'Days', you will see an image button in the top banner. Tapping it allows you to choose any picture from your device gallery to use as the visual cover both in the app and on the front page of the PDF guide."
    ),
    FaqItem(
        id = "faq_rewards",
        categoryEs = "Recompensas",
        categoryEn = "Rewards",
        questionEs = "¿Qué significan los reconocimientos al completar el 100%?",
        questionEn = "What do the 100% completion rewards mean?",
        answerEs = "Cuando completas todas las etapas de una ruta o todas las rutas guardadas, la aplicación te recompensa con un distintivo adaptado al perfil: en Caminos obtendrás la tradicional 'Compostela', mientras que en Viajes conseguirás el 'Pasaporte de Viajero'. Este reconocimiento se muestra en el panel de Datos Totales.",
        answerEn = "When you complete all stages of a route or all saved routes, the app rewards you with a profile-specific badge: in Caminos you earn the traditional 'Compostela', whereas in Trips you achieve the 'Traveler Passport'. This reward is proudly displayed in the Total Data panel."
    ),
    FaqItem(
        id = "faq_accommodations",
        categoryEs = "Alojamientos",
        categoryEn = "Accommodations",
        questionEs = "¿Cómo registrar alojamientos y reservas?",
        questionEn = "How to manage accommodations and bookings?",
        answerEs = "Dentro de cada etapa o día, pulsa en 'Añadir Alojamiento'. Podrás registrar el nombre, dirección, teléfono con marcación directa, opciones de habitación, precios y marcar si la reserva ya está confirmada. Todos estos datos se reflejan en el cálculo de gasto acumulado.",
        answerEn = "Inside each stage or day, tap 'Add Accommodation'. You can save the name, address, direct-call phone number, room options, prices and toggle whether the booking is confirmed. All these figures are reflected in your cumulative accommodation spending."
    ),
    FaqItem(
        id = "faq_agenda",
        categoryEs = "Viajes y Agenda",
        categoryEn = "Trips & Agenda",
        questionEs = "¿Cómo funciona la agenda y vivencias en Viajes?",
        questionEn = "How do the agenda and daily memories work in Trips?",
        answerEs = "En el perfil de Viajes, cada día dispone de una sección de 'Agenda y Actividades' con horarios programados y archivos adjuntos (entradas, billetes, reservas), además de un diario de vivencias con notas y fotografías para documentar cada jornada.",
        answerEn = "In the Trips profile, each day includes a 'Scheduled Agenda & Activities' section with planned times and attached files (tickets, boarding passes, vouchers), as well as a daily travel journal with notes and photos to document your journey."
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expandedFaqIds by remember { mutableStateOf(setOf<String>()) }

    // Lista de categorías únicas según idioma
    val allCategoriesLabel = viewModel.string("faq_category_all")
    val categories = remember(isEnglish) {
        val unique = INITIAL_FAQS.map { if (isEnglish) it.categoryEn else it.categoryEs }.distinct()
        listOf(allCategoriesLabel) + unique
    }

    // Filtrado en tiempo real con normalización estricta (sin acentos, diéresis ni mayúsculas)
    val filteredFaqs = remember(searchQuery, selectedCategory, isEnglish) {
        val normQuery = normalizeSearchText(searchQuery)
        val queryTerms = if (normQuery.isBlank()) emptyList() else normQuery.split(" ").filter { it.isNotBlank() }

        INITIAL_FAQS.filter { item ->
            val category = if (isEnglish) item.categoryEn else item.categoryEs
            val question = if (isEnglish) item.questionEn else item.questionEs
            val answer = if (isEnglish) item.answerEn else item.answerEs

            // Filtro por categoría seleccionada
            val matchesCategory = selectedCategory == null || selectedCategory == allCategoriesLabel || category == selectedCategory

            if (!matchesCategory) return@filter false

            if (queryTerms.isEmpty()) return@filter true

            // Normalización para comparación inteligente
            val normQuestion = normalizeSearchText(question)
            val normAnswer = normalizeSearchText(answer)
            val normCategory = normalizeSearchText(category)
            // Soporte cruzado adicional para búsquedas bilingües
            val normAltQuestion = normalizeSearchText(if (isEnglish) item.questionEs else item.questionEn)
            val normAltAnswer = normalizeSearchText(if (isEnglish) item.answerEs else item.answerEn)

            val searchableCorpus = "$normQuestion $normAnswer $normCategory $normAltQuestion $normAltAnswer"

            // Todos los términos de búsqueda deben estar presentes en la pregunta o respuesta
            queryTerms.all { term -> searchableCorpus.contains(term) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = viewModel.string("help_title"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlueDark
                        )
                        Text(
                            text = viewModel.string("help_subtitle"),
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
                        text = viewModel.string("search_faq_placeholder"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
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
                                contentDescription = viewModel.string("faq_clear_search"),
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

            // 2. Chips de Categorías
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = (selectedCategory == null && cat == allCategoriesLabel) || selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = if (cat == allCategoriesLabel) null else cat
                        },
                        label = {
                            Text(
                                text = cat,
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
                                text = "${viewModel.string("faq_no_results")} \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = viewModel.string("faq_no_results_hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = viewModel.string("faq_clear_search"),
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
                                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
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
                                        Divider(
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
