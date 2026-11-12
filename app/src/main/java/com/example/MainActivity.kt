package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CaminoViewModel
import com.example.ui.MainTab
import com.example.ui.NavigationDestination
import com.example.ui.components.SettingsActionMenu
import com.example.ui.components.ThemeOptionRow
import com.example.ui.screens.*
import com.example.ui.settings.LanguageMode
import com.example.ui.settings.ThemeMode
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: CaminoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            UltreiaTheme(themeMode = themeMode) {
                UltreiaApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltreiaApp(viewModel: CaminoViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val destination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val caminos by viewModel.caminos.collectAsStateWithLifecycle()
    val selectedCamino by viewModel.selectedCamino.collectAsStateWithLifecycle()
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val selectedStage by viewModel.selectedStage.collectAsStateWithLifecycle()
    val accommodations by viewModel.stageAccommodations.collectAsStateWithLifecycle()
    val stats by viewModel.caminoStats.collectAsStateWithLifecycle()
    val isExportingPdf by viewModel.isExportingPdf.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var showAddStageDialog by remember { mutableStateOf(false) }
    var stageToEdit by remember { mutableStateOf<com.example.data.model.StageEntity?>(null) }
    var showSettingsModal by remember { mutableStateOf(false) }

    // Splash Screen on launch: 4 seconds duration or tap to enter
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(4000)
        showSplash = false
    }

    if (showSplash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showSplash = false }
        ) {
            Image(
                painter = painterResource(id = R.drawable.portada_splash),
                contentDescription = "Portada Ultreia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Default select first or last active Camino if none is selected
    LaunchedEffect(caminos, selectedCamino) {
        if (selectedCamino == null && caminos.isNotEmpty()) {
            val prefs = context.getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
            val lastId = prefs.getLong("last_active_camino_id", -1L)
            if (lastId != -1L && caminos.any { it.id == lastId }) {
                viewModel.selectCamino(lastId)
            } else {
                viewModel.selectCamino(caminos.first().id)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = currentTab.ordinal, pageCount = { 5 })

    // Sync swiping between tabs with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val targetTab = MainTab.values().getOrNull(pagerState.currentPage) ?: MainTab.CAMINOS
        if (currentTab != targetTab) {
            viewModel.selectTab(targetTab)
        }
    }

    // Sync ViewModel programmatic tab selection with Pager animation
    LaunchedEffect(currentTab) {
        if (pagerState.currentPage != currentTab.ordinal) {
            pagerState.animateScrollToPage(currentTab.ordinal)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_navigation_bar")
            ) {
                // Button 1: Caminos
                NavigationBarItem(
                    selected = (pagerState.currentPage == 0 || currentTab == MainTab.CAMINOS) && destination !is NavigationDestination.StageDetail,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        viewModel.selectTab(MainTab.CAMINOS)
                    },
                    icon = { Icon(Icons.Default.Hiking, contentDescription = null) },
                    label = { Text(viewModel.string("tab_caminos")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("btn_bottom_caminos")
                )

                // Button 2: Etapas
                NavigationBarItem(
                    selected = (pagerState.currentPage == 1 || currentTab == MainTab.STAGES) && destination !is NavigationDestination.StageDetail,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        viewModel.selectTab(MainTab.STAGES)
                    },
                    icon = { Icon(Icons.Default.ViewDay, contentDescription = null) },
                    label = { Text(viewModel.string("tab_stages")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("btn_bottom_stages")
                )

                // Button 3: Mapa
                NavigationBarItem(
                    selected = (pagerState.currentPage == 2 || currentTab == MainTab.MAP) && destination !is NavigationDestination.StageDetail,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                        viewModel.selectTab(MainTab.MAP)
                    },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text(viewModel.string("tab_map")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("btn_bottom_map")
                )

                // Button 4: Estadísticas
                NavigationBarItem(
                    selected = (pagerState.currentPage == 3 || currentTab == MainTab.STATS) && destination !is NavigationDestination.StageDetail,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                        viewModel.selectTab(MainTab.STATS)
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text(viewModel.string("tab_stats")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("btn_bottom_stats")
                )

                // Button 5: Ajustes
                NavigationBarItem(
                    selected = (pagerState.currentPage == 4 || currentTab == MainTab.SETTINGS) && destination !is NavigationDestination.StageDetail,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(4) }
                        viewModel.selectTab(MainTab.SETTINGS)
                    },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    label = { Text(viewModel.string("settings")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CaminoBlue,
                        indicatorColor = PilgrimYellowLight
                    ),
                    modifier = Modifier.testTag("btn_bottom_settings")
                )
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 1 && destination !is NavigationDestination.StageDetail && selectedCamino != null) {
                FloatingActionButton(
                    onClick = { showAddStageDialog = true },
                    containerColor = CaminoBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_stage_main")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir etapa")
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (destination is NavigationDestination.StageDetail && selectedStage != null) {
                StageDetailScreen(
                    stage = selectedStage!!,
                    accommodations = accommodations,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            CaminoListScreen(
                                caminos = caminos,
                                viewModel = viewModel,
                                onSelectCamino = { id ->
                                    viewModel.selectCamino(id)
                                }
                            )
                        }

                        1 -> {
                            if (selectedCamino != null) {
                                StagesListTab(
                                    camino = selectedCamino!!,
                                    stages = stages,
                                    stats = stats,
                                    viewModel = viewModel,
                                    onStageClick = { stageId -> viewModel.navigateToStage(stageId) },
                                    onToggleStage = { stage -> viewModel.toggleStageCompletion(stage) },
                                    onEditStage = { stage -> stageToEdit = stage }
                                )
                            } else {
                                NoCaminoSelectedState(
                                    message = "Crea tu primer camino o viaje para ver y gestionar sus etapas diarias.",
                                    onCreateCamino = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                )
                            }
                        }

                        2 -> {
                            if (selectedCamino != null) {
                                InteractiveMapScreen(
                                    camino = selectedCamino!!,
                                    stages = stages,
                                    onStageClick = { stageId -> viewModel.navigateToStage(stageId) }
                                )
                            } else {
                                NoCaminoSelectedState(
                                    message = "Crea tu primer camino o viaje para ver el mapa interactivo y tus rutas.",
                                    onCreateCamino = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                )
                            }
                        }

                        3 -> {
                            if (selectedCamino != null) {
                                StatsScreen(
                                    camino = selectedCamino!!,
                                    stages = stages,
                                    stats = stats,
                                    viewModel = viewModel
                                )
                            } else {
                                NoCaminoSelectedState(
                                    message = "Crea tu primer camino o viaje para seguir tus kilómetros y etapas completadas.",
                                    onCreateCamino = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                )
                            }
                        }

                        4 -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                selectedCamino = selectedCamino
                            )
                        }
                    }
                }
            }

            if (isExportingPdf) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = CaminoBlue)
                                Text("Generando documento PDF...", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStageDialog && selectedCamino != null) {
        val nextDay = stages.size + 1
        val defaultDate = com.example.ui.settings.AppStrings.calculateStageDate(
            selectedCamino!!.startDate.ifBlank { "15/05/2026" },
            nextDay - 1
        )
        AddStageDialog(
            nextDay = nextDay,
            initialDate = defaultDate,
            onDismiss = { showAddStageDialog = false },
            onConfirm = { origin, dest, km, gain, loss, time, diff, date, notes ->
                viewModel.addStage(
                    caminoId = selectedCamino!!.id,
                    title = "Etapa $nextDay: $origin a $dest",
                    origin = origin,
                    destination = dest,
                    distanceKm = km,
                    elevationGainM = gain,
                    elevationLossM = loss,
                    estimatedTime = time,
                    difficulty = diff,
                    date = date,
                    stageNotes = notes
                )
                showAddStageDialog = false
            }
        )
    }

    if (stageToEdit != null) {
        EditStageDialog(
            stage = stageToEdit!!,
            onDismiss = { stageToEdit = null },
            onSave = { updated ->
                viewModel.updateStage(updated)
                stageToEdit = null
            }
        )
    }

    if (showSettingsModal) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsModal = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SettingsScreen(
                viewModel = viewModel,
                selectedCamino = selectedCamino
            )
        }
    }
}

// Collapsible accordion Settings Screen
@Composable
fun SettingsScreen(
    viewModel: CaminoViewModel,
    selectedCamino: com.example.data.model.CaminoEntity? = null
) {
    val context = LocalContext.current
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLang by viewModel.languageMode.collectAsStateWithLifecycle()
    val caminos by viewModel.caminos.collectAsStateWithLifecycle()

    var isThemeExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isBackupExpanded by remember { mutableStateOf(false) }
    var isDataExpanded by remember { mutableStateOf(false) }

    var showConfirmClearDialog by remember { mutableStateOf(false) }

    // JSON file picker launcher for backup restoration
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                if (jsonString != null) {
                    viewModel.importBackup(jsonString)
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error al leer el archivo de copia de seguridad", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = viewModel.string("settings"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CaminoBlue
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Personalización, copias de seguridad y preferencias de la aplicación",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Accordion 1: Tema Visual (Collapsed by default)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isThemeExpanded = !isThemeExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(viewModel.string("theme"), fontWeight = FontWeight.Bold)
                                Text(
                                    when (currentTheme) {
                                        ThemeMode.SYSTEM -> viewModel.string("theme_system")
                                        ThemeMode.LIGHT -> viewModel.string("theme_light")
                                        ThemeMode.DARK -> viewModel.string("theme_dark")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isThemeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isThemeExpanded) "Comprimir" else "Desplegar",
                            tint = CaminoBlue
                        )
                    }

                    AnimatedVisibility(
                        visible = isThemeExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                            ThemeOptionRow(
                                title = viewModel.string("theme_system"),
                                subtitle = "Sigue la configuración de Android",
                                isSelected = currentTheme == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                            )
                            ThemeOptionRow(
                                title = viewModel.string("theme_light"),
                                subtitle = "Fondo claro y tonos frescos",
                                isSelected = currentTheme == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                            )
                            ThemeOptionRow(
                                title = viewModel.string("theme_dark"),
                                subtitle = "Ahorro de batería y visión nocturna",
                                isSelected = currentTheme == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                            )
                        }
                    }
                }
            }
        }

        // Accordion 2: Idioma (Collapsed by default)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLanguageExpanded = !isLanguageExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(viewModel.string("language"), fontWeight = FontWeight.Bold)
                                Text(
                                    when (currentLang) {
                                        LanguageMode.SYSTEM -> viewModel.string("lang_system")
                                        LanguageMode.ES -> viewModel.string("lang_es")
                                        LanguageMode.EN -> viewModel.string("lang_en")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isLanguageExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isLanguageExpanded) "Comprimir" else "Desplegar",
                            tint = CaminoBlue
                        )
                    }

                    AnimatedVisibility(
                        visible = isLanguageExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                            ThemeOptionRow(
                                title = viewModel.string("lang_system"),
                                subtitle = "Default system language",
                                isSelected = currentLang == LanguageMode.SYSTEM,
                                onClick = { viewModel.setLanguageMode(LanguageMode.SYSTEM) }
                            )
                            ThemeOptionRow(
                                title = viewModel.string("lang_es"),
                                subtitle = "Español",
                                isSelected = currentLang == LanguageMode.ES,
                                onClick = { viewModel.setLanguageMode(LanguageMode.ES) }
                            )
                            ThemeOptionRow(
                                title = viewModel.string("lang_en"),
                                subtitle = "English",
                                isSelected = currentLang == LanguageMode.EN,
                                onClick = { viewModel.setLanguageMode(LanguageMode.EN) }
                            )
                        }
                    }
                }
            }
        }

        // Accordion 3: Copia de Seguridad y Restauración JSON (Collapsed by default)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBackupExpanded = !isBackupExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = CaminoBlue, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Copia de Seguridad (Backup)", fontWeight = FontWeight.Bold)
                                Text(
                                    "Importar y exportar datos en formato JSON",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isBackupExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isBackupExpanded) "Comprimir" else "Desplegar",
                            tint = CaminoBlue
                        )
                    }

                    AnimatedVisibility(
                        visible = isBackupExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                            // Export all caminos
                            Button(
                                onClick = { viewModel.exportAllBackup(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Exportar TODOS los caminos y viajes (Completo)")
                            }

                            // Individual camino export section
                            if (caminos.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Exportar un camino o viaje individual:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CaminoBlue
                                )

                                for (cam in caminos) {
                                    OutlinedButton(
                                        onClick = { viewModel.exportSingleBackup(context, cam.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (cam.isTravelGuide) Icons.Default.Luggage else Icons.Default.Hiking,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = CaminoBlue
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Exportar \"${cam.title}\"",
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Import backup JSON
                            FilledTonalButton(
                                onClick = { importBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Importar Copia de Seguridad (JSON)")
                            }
                        }
                    }
                }
            }
        }

        // Accordion 4: Gestión de Datos (Collapsed by default)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDataExpanded = !isDataExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Gestión de Datos", fontWeight = FontWeight.Bold)
                                Text(
                                    "Reiniciar o borrar todos los datos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isDataExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isDataExpanded) "Comprimir" else "Desplegar",
                            tint = CaminoBlue
                        )
                    }

                    AnimatedVisibility(
                        visible = isDataExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                            Text(
                                text = "Si deseas comenzar de nuevo desde cero, puedes borrar todas las rutas y datos guardados.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { showConfirmClearDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text("Borrar todos los caminos y viajes")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("¿Borrar todos los caminos y viajes?") },
            text = { Text("Se eliminarán todas las rutas, etapas, alojamientos y vivencias guardadas. La app volverá al estado inicial.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllCaminos()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sí, borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NoCaminoSelectedState(
    message: String,
    onCreateCamino: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(CaminoBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Hiking,
                        contentDescription = null,
                        tint = CaminoBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "No hay ningún camino o viaje seleccionado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CaminoBlue,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onCreateCamino,
                    colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Crear tu primer Camino o Viaje", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
