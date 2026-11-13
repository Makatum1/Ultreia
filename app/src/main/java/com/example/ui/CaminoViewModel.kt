package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.AttachedFile
import com.example.data.model.CaminoEntity
import com.example.data.model.RoomPriceOption
import com.example.data.model.StageEntity
import com.example.data.model.VivenciaItem
import com.example.data.pdf.PdfExporter
import com.example.data.repository.CaminoRepository
import com.example.data.repository.CaminoStats
import com.example.ui.settings.AppStrings
import com.example.ui.settings.LanguageMode
import com.example.ui.settings.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    CAMINOS,
    STAGES,
    MAP,
    STATS,
    SETTINGS
}

sealed interface NavigationDestination {
    object CaminoList : NavigationDestination
    data class CaminoDetail(val caminoId: Long, val initialTab: Int = 0) : NavigationDestination
    data class StageDetail(val stageId: Long) : NavigationDestination
}

class CaminoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CaminoRepository(application.applicationContext)

    // Bottom Navigation Tabs
    private val _currentTab = MutableStateFlow(MainTab.CAMINOS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Dual Profile: Caminos vs Viajes
    private val _isTravelProfile = MutableStateFlow(false)
    val isTravelProfile: StateFlow<Boolean> = _isTravelProfile.asStateFlow()

    fun toggleTravelProfile() {
        _isTravelProfile.value = !_isTravelProfile.value
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _selectedStageId.value = null
        if ((tab == MainTab.STAGES || tab == MainTab.MAP || tab == MainTab.STATS) && _selectedCaminoId.value == null) {
            val prefs = getApplication<Application>().getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
            val lastId = prefs.getLong("last_active_camino_id", -1L)
            if (lastId != -1L && caminos.value.any { it.id == lastId }) {
                _selectedCaminoId.value = lastId
            } else {
                val first = caminos.value.firstOrNull()
                if (first != null) {
                    _selectedCaminoId.value = first.id
                }
            }
        }
    }

    fun selectCamino(caminoId: Long) {
        _selectedCaminoId.value = caminoId
        _selectedStageId.value = null
        _currentTab.value = MainTab.STAGES
        val prefs = getApplication<Application>().getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putLong("last_active_camino_id", caminoId).apply()
    }

    // Settings: Theme & Language
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _languageMode = MutableStateFlow(LanguageMode.ES)
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("saved_theme_mode", mode.name).apply()
    }

    fun setLanguageMode(mode: LanguageMode) {
        _languageMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("saved_language_mode", mode.name).apply()
    }

    fun string(key: String): String {
        return AppStrings.get(key, _languageMode.value)
    }

    private val _isInitialLoadDone = MutableStateFlow(false)
    val isInitialLoadDone: StateFlow<Boolean> = _isInitialLoadDone.asStateFlow()

    init {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val prefs = context.getSharedPreferences("ultreia_prefs", android.content.Context.MODE_PRIVATE)
            if (!prefs.getBoolean("initial_clean_slate_done_v3", false)) {
                repository.clearAllData()
                prefs.edit().putBoolean("initial_clean_slate_done_v3", true).apply()
            }
            val savedTheme = prefs.getString("saved_theme_mode", null)
            if (savedTheme != null) {
                try { _themeMode.value = ThemeMode.valueOf(savedTheme) } catch (_: Exception) {}
            }
            val savedLang = prefs.getString("saved_language_mode", null)
            if (savedLang != null) {
                try { _languageMode.value = LanguageMode.valueOf(savedLang) } catch (_: Exception) {}
            }
            _isInitialLoadDone.value = true
        }
    }

    val caminos: StateFlow<List<CaminoEntity>> = repository.allCaminos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentDestination = MutableStateFlow<NavigationDestination>(NavigationDestination.CaminoList)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _selectedCaminoId = MutableStateFlow<Long?>(null)
    val selectedCaminoId: StateFlow<Long?> = _selectedCaminoId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCamino: StateFlow<CaminoEntity?> = _selectedCaminoId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getCaminoById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stages: StateFlow<List<StageEntity>> = _selectedCaminoId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getStagesForCamino(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caminoStats: StateFlow<CaminoStats> = stages
        .map { stageList -> repository.calculateStats(stageList) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CaminoStats(0, 0, 0.0, 0.0, 0, 0, 0)
        )

    val totalGlobalStats: StateFlow<CaminoStats> = repository.allStages
        .map { stageList -> repository.calculateStats(stageList) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CaminoStats(0, 0, 0.0, 0.0, 0, 0, 0)
        )

    val totalCaminosCount: StateFlow<Int> = caminos
        .map { list -> list.count { !it.isTravelGuide } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTripsCount: StateFlow<Int> = caminos
        .map { list -> list.count { it.isTravelGuide } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAllRoutesCount: StateFlow<Int> = caminos
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAllStagesCount: StateFlow<Int> = repository.allStages
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Conflict state for import overwriting
    data class ConflictDialogState(
        val conflict: com.example.data.backup.ImportConflict,
        val parsedList: List<com.example.data.backup.ParsedCaminoData>,
        val remainingConflicts: List<com.example.data.backup.ImportConflict>,
        val acceptedOverwriteIds: Set<Long>
    )

    private val _conflictDialogState = MutableStateFlow<ConflictDialogState?>(null)
    val conflictDialogState: StateFlow<ConflictDialogState?> = _conflictDialogState.asStateFlow()

    fun resolveConflict(overwrite: Boolean) {
        val state = _conflictDialogState.value ?: return
        val newAccepted = if (overwrite) state.acceptedOverwriteIds + state.conflict.existingCamino.id else state.acceptedOverwriteIds
        if (state.remainingConflicts.isNotEmpty()) {
            _conflictDialogState.value = ConflictDialogState(
                conflict = state.remainingConflicts.first(),
                parsedList = state.parsedList,
                remainingConflicts = state.remainingConflicts.drop(1),
                acceptedOverwriteIds = newAccepted
            )
        } else {
            _conflictDialogState.value = null
            viewModelScope.launch {
                _isExportingPdf.value = true
                try {
                    val count = com.example.data.backup.BackupManager.applyImport(
                        state.parsedList,
                        newAccepted,
                        repository
                    )
                    _userMessage.value = "¡Importación completada! ($count rutas procesadas)"
                    val first = repository.getAllCaminosOnce().firstOrNull()
                    if (first != null) selectCamino(first.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _userMessage.value = "Error al aplicar importación: ${e.message}"
                } finally {
                    _isExportingPdf.value = false
                }
            }
        }
    }

    fun cancelAllConflicts() {
        _conflictDialogState.value = null
    }

    fun importFromUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val jsonString = com.example.data.backup.BackupManager.readJsonFromUri(context, uri)
                if (jsonString.isNullOrBlank()) {
                    _userMessage.value = "No se pudo leer el archivo o el ZIP no contiene datos válidos."
                    return@launch
                }
                val parsed = com.example.data.backup.BackupManager.parseJsonContent(jsonString)
                if (parsed.isEmpty()) {
                    _userMessage.value = "No se encontraron rutas válidas en el archivo importado."
                    return@launch
                }
                val conflicts = com.example.data.backup.BackupManager.findConflicts(parsed, repository)
                if (conflicts.isNotEmpty()) {
                    _conflictDialogState.value = ConflictDialogState(
                        conflict = conflicts.first(),
                        parsedList = parsed,
                        remainingConflicts = conflicts.drop(1),
                        acceptedOverwriteIds = emptySet()
                    )
                } else {
                    val count = com.example.data.backup.BackupManager.applyImport(parsed, emptySet(), repository)
                    _userMessage.value = "¡Importación completada con éxito! ($count rutas añadidas)"
                    val first = repository.getAllCaminosOnce().firstOrNull()
                    if (first != null) selectCamino(first.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Error al importar: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    fun addPredefinedCamino(route: com.example.data.sample.PredefinedRouteInfo) {
        viewModelScope.launch {
            val newId = com.example.data.sample.SampleData.createPredefinedCamino(route, repository)
            selectCamino(newId)
            _userMessage.value = "¡${route.title} creado correctamente!"
        }
    }

    fun exportZip(context: Context, caminoIds: List<Long>, isAll: Boolean, title: String) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val list = if (isAll) repository.getAllCaminosOnce() else caminoIds.mapNotNull { repository.getCaminoByIdOnce(it) }
                val fileName = if (isAll) {
                    "Ultreia_Rutas_Export.zip"
                } else if (list.size == 1) {
                    com.example.data.backup.BackupManager.getSanitizedFileName(list.first().title, list.first().isTravelGuide, "zip")
                } else {
                    "${title.replace("\\s+".toRegex(), "_")}.zip"
                }
                val file = com.example.data.backup.BackupManager.exportToZip(
                    context = context,
                    caminos = list,
                    zipFileName = fileName,
                    shareSubject = "Exportación: $title",
                    repository = repository
                )
                if (file != null) {
                    _userMessage.value = "ZIP generado y listo para compartir"
                } else {
                    _userMessage.value = "Error al generar archivo ZIP"
                }
            } catch (e: Exception) {
                _userMessage.value = "Error al exportar ZIP: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    fun exportGlobalBackup(context: Context, backupType: com.example.data.backup.GlobalBackupType) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val file = com.example.data.backup.BackupManager.exportGlobalBackupZip(context, backupType, repository)
                if (file != null) {
                    _userMessage.value = "Copia de seguridad exportada correctamente"
                } else {
                    _userMessage.value = "Error al generar copia de seguridad"
                }
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    fun exportAllBackupZip(context: Context) {
        exportGlobalBackup(context, com.example.data.backup.GlobalBackupType.COMPLETE_ALL)
    }

    fun exportSingleCaminoJson(context: Context, caminoId: Long) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val file = com.example.data.backup.BackupManager.exportSingleCaminoJson(context, caminoId, repository)
                if (file != null) {
                    _userMessage.value = "Ruta exportada en formato JSON"
                } else {
                    _userMessage.value = "Error al exportar ruta"
                }
            } catch (e: Exception) {
                _userMessage.value = "Error al exportar: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    private val _selectedStageId = MutableStateFlow<Long?>(null)
    val selectedStageId: StateFlow<Long?> = _selectedStageId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedStage: StateFlow<StageEntity?> = _selectedStageId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getStageById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stageAccommodations: StateFlow<List<AccommodationEntity>> = _selectedStageId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getAccommodationsForStage(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isExportingPdf = MutableStateFlow(false)
    val isExportingPdf: StateFlow<Boolean> = _isExportingPdf.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun navigateToCamino(caminoId: Long, initialTab: Int = 0) {
        _selectedCaminoId.value = caminoId
        _currentDestination.value = NavigationDestination.CaminoDetail(caminoId, initialTab)
    }

    fun navigateToStage(stageId: Long) {
        _selectedStageId.value = stageId
        _currentDestination.value = NavigationDestination.StageDetail(stageId)
    }

    fun navigateBack() {
        when (_currentDestination.value) {
            is NavigationDestination.StageDetail -> {
                val caminoId = _selectedCaminoId.value
                if (caminoId != null) {
                    _selectedStageId.value = null
                    _currentDestination.value = NavigationDestination.CaminoDetail(caminoId)
                } else {
                    _currentDestination.value = NavigationDestination.CaminoList
                }
            }
            is NavigationDestination.CaminoDetail -> {
                _selectedCaminoId.value = null
                _selectedStageId.value = null
                _currentDestination.value = NavigationDestination.CaminoList
            }
            NavigationDestination.CaminoList -> {
                // At root
            }
        }
    }

    /**
     * Resolves the formatted date of a stage according to Camino start date or stage custom date
     */
    fun resolveStageDate(stage: StageEntity): String {
        if (stage.date.isNotBlank()) return stage.date
        val currentCamino = selectedCamino.value
        return if (currentCamino != null && currentCamino.startDate.isNotBlank()) {
            AppStrings.calculateStageDate(currentCamino.startDate, stage.dayNumber - 1)
        } else {
            ""
        }
    }

    fun addCamino(
        title: String,
        subtitle: String,
        startDate: String,
        isTravelGuide: Boolean = false,
        coverPhotoUri: String? = null
    ) {
        viewModelScope.launch {
            val id = repository.insertCamino(
                CaminoEntity(
                    title = title.ifBlank { if (isTravelGuide) "Mi Viaje" else "Mi Camino" },
                    subtitle = subtitle.ifBlank { if (isTravelGuide) "Ruta e itinerario de viaje" else "Ruta hacia Santiago de Compostela" },
                    pilgrimName = "",
                    startDate = startDate.ifBlank { "10/05/2026" },
                    isTravelGuide = isTravelGuide,
                    coverPhotoUri = coverPhotoUri
                )
            )
            _userMessage.value = if (isTravelGuide) "¡Viaje \"$title\" creado con éxito!" else "¡Camino \"$title\" creado con éxito!"
            selectCamino(id)
        }
    }

    fun createSampleTrip() {
        viewModelScope.launch {
            _isTravelProfile.value = true
            val tripId = repository.insertCamino(
                CaminoEntity(
                    title = "Viaje a los Picos de Europa",
                    subtitle = "Ruta panorámica de montaña, valles y pueblos con encanto",
                    pilgrimName = "",
                    startDate = "15/06/2026",
                    isTravelGuide = true,
                    coverPhotoUri = null,
                    attachedFilesJson = "[]"
                )
            )
            repository.insertStage(
                StageEntity(
                    caminoId = tripId,
                    dayNumber = 1,
                    title = "Etapa 1: Cangas de Onís a Lagos de Covadonga",
                    origin = "Cangas de Onís",
                    destination = "Lagos de Covadonga",
                    distanceKm = 21.5,
                    elevationGainM = 980,
                    elevationLossM = 210,
                    estimatedTime = "5h 30m",
                    difficulty = "Media",
                    date = "15/06/2026",
                    isCompleted = true,
                    stageNotes = "Subida a los lagos Enol y Ercina. Imprescindible llevar calzado de senderismo y chaqueta para la cumbre. El amanecer en Covadonga es imperdible.",
                    diaryNotes = "Salimos temprano de Cangas de Onís cruzando el puente romano con su cruz. El ascenso hacia los lagos fue espectacular entre pastizales y nieblas matutinas.",
                    gpxTrackJson = null
                )
            )
            val stage2Id = repository.insertStage(
                StageEntity(
                    caminoId = tripId,
                    dayNumber = 2,
                    title = "Etapa 2: Poncebos a Caín (Ruta del Cares)",
                    origin = "Poncebos",
                    destination = "Caín (Ruta del Cares)",
                    distanceKm = 12.0,
                    elevationGainM = 320,
                    elevationLossM = 290,
                    estimatedTime = "3h 45m",
                    difficulty = "Fácil-Media",
                    date = "16/06/2026",
                    isCompleted = false,
                    stageNotes = "La garganta divina del río Cares tallada en roca vertical. Cruzar puentes colgantes sobre el abismo. Llevar abundante agua fresca y linterna.",
                    diaryNotes = "Uno de los senderos más impresionantes de Europa. Paredes de roca de más de mil metros de altura acompañando el curso del agua.",
                    gpxTrackJson = null
                )
            )
            repository.insertStage(
                StageEntity(
                    caminoId = tripId,
                    dayNumber = 3,
                    title = "Etapa 3: Arenas de Cabrales a Bulnes y Urriellu",
                    origin = "Arenas de Cabrales",
                    destination = "Bulnes y Naranjo de Bulnes",
                    distanceKm = 15.2,
                    elevationGainM = 750,
                    elevationLossM = 400,
                    estimatedTime = "4h 15m",
                    difficulty = "Media-Alta",
                    date = "17/06/2026",
                    isCompleted = false,
                    stageNotes = "Subida a la aldea de Bulnes por la Canal del Tejo. Mirador con panorámica directa al Picu Urriellu. Degustar queso de Cabrales con sidra.",
                    diaryNotes = "Día de alta montaña en una aldea sin coches comunicada únicamente por sendero y funicular subterráneo.",
                    gpxTrackJson = null
                )
            )
            repository.insertAccommodation(
                AccommodationEntity(
                    stageId = stage2Id,
                    name = "Hotel Rural Garganta del Cares",
                    address = "Carretera Poncebos s/n, Cabrales",
                    phone = "+34985845000",
                    roomOptionsJson = "[{\"typeName\":\"Doble\",\"price\":65.0},{\"typeName\":\"Individual\",\"price\":45.0}]",
                    notes = "Al pie del inicio de la ruta del Cares. Vistas sobrecogedoras al río y terraza para cenar tras la caminata."
                )
            )
            _userMessage.value = "¡Viaje de prueba \"Viaje a los Picos de Europa\" creado con éxito!"
            selectCamino(tripId)
        }
    }

    fun createSampleCamino() {
        viewModelScope.launch {
            _isTravelProfile.value = false
            val caminoId = repository.insertCamino(
                CaminoEntity(
                    title = "Camino Francés",
                    subtitle = "Los últimos 100 km de Sarria a Santiago de Compostela",
                    pilgrimName = "",
                    startDate = "10/05/2026",
                    isTravelGuide = false,
                    coverPhotoUri = null,
                    attachedFilesJson = "[]"
                )
            )
            repository.insertStage(
                StageEntity(
                    caminoId = caminoId,
                    dayNumber = 1,
                    title = "Etapa 1: Sarria a Portomarín",
                    origin = "Sarria",
                    destination = "Portomarín",
                    distanceKm = 22.2,
                    elevationGainM = 360,
                    elevationLossM = 410,
                    estimatedTime = "5h 30m",
                    difficulty = "Media",
                    date = "10/05/2026",
                    isCompleted = true,
                    stageNotes = "Caminata entre robledales y aldeas tradicionales gallegas. Descenso final cruzando el gran puente sobre el embalse de Belesar hacia Portomarín.",
                    diaryNotes = "Comenzamos nuestro Camino con entusiasmo en Sarria. Al sellar la credencial en Barbadelo sentimos el auténtico espíritu jacobeo.",
                    gpxTrackJson = null
                )
            )
            val stage2Id = repository.insertStage(
                StageEntity(
                    caminoId = caminoId,
                    dayNumber = 2,
                    title = "Etapa 2: Portomarín a Palas de Rei",
                    origin = "Portomarín",
                    destination = "Palas de Rei",
                    distanceKm = 24.8,
                    elevationGainM = 450,
                    elevationLossM = 320,
                    estimatedTime = "6h 15m",
                    difficulty = "Media",
                    date = "11/05/2026",
                    isCompleted = false,
                    stageNotes = "Subida tendida hacia la sierra de Ligonde. Buenas fuentes de agua y parada recomendada para descansar en Gonzar o Ventas de Narón.",
                    diaryNotes = "Jornada tranquila entre senderos de tierra y cruceiros centenarios.",
                    gpxTrackJson = null
                )
            )
            repository.insertStage(
                StageEntity(
                    caminoId = caminoId,
                    dayNumber = 3,
                    title = "Etapa 3: Palas de Rei a Arzúa",
                    origin = "Palas de Rei",
                    destination = "Arzúa",
                    distanceKm = 28.5,
                    elevationGainM = 380,
                    elevationLossM = 460,
                    estimatedTime = "7h 00m",
                    difficulty = "Media-Alta",
                    date = "12/05/2026",
                    isCompleted = false,
                    stageNotes = "Cruce de valles, riachuelos y bosques de eucaliptos. Parada obligatoria en Melide para degustar el tradicional pulpo á feira.",
                    diaryNotes = "Los bosques gallegos nos acompañan con su sombra y frescor hasta la villa del queso, Arzúa.",
                    gpxTrackJson = null
                )
            )
            repository.insertAccommodation(
                AccommodationEntity(
                    stageId = stage2Id,
                    name = "Albergue de Peregrinos Portomarín",
                    address = "Rúa do Peregrino 12, Portomarín",
                    phone = "+34982545100",
                    roomOptionsJson = "[{\"typeName\":\"Litera\",\"price\":15.0},{\"typeName\":\"Individual\",\"price\":35.0}]",
                    notes = "Albergue acogedor con terraza y vistas panorámicas al río Miño. Lavadero y zona de descanso."
                )
            )
            _userMessage.value = "¡Camino de prueba \"Camino Francés\" creado con éxito!"
            selectCamino(caminoId)
        }
    }

    fun updateCamino(camino: CaminoEntity) {
        viewModelScope.launch {
            repository.updateCamino(camino)
            _userMessage.value = "Camino actualizado"
        }
    }

    fun deleteCamino(camino: CaminoEntity) {
        viewModelScope.launch {
            repository.deleteCamino(camino)
            _userMessage.value = "Camino eliminado"
            navigateBack()
        }
    }

    fun clearAllCaminos() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedCaminoId.value = null
            _selectedStageId.value = null
            _currentDestination.value = NavigationDestination.CaminoList
            _currentTab.value = MainTab.CAMINOS
            _userMessage.value = "Todos los caminos han sido eliminados"
        }
    }

    fun addStage(
        caminoId: Long,
        title: String,
        origin: String,
        destination: String,
        distanceKm: Double,
        elevationGainM: Int,
        elevationLossM: Int,
        estimatedTime: String,
        difficulty: String,
        date: String = "",
        stageNotes: String = ""
    ) {
        viewModelScope.launch {
            val currentStages = repository.getStagesForCaminoOnce(caminoId)
            val nextDay = currentStages.size + 1
            val currentCamino = repository.getCaminoByIdOnce(caminoId)
            val finalDate = if (date.isNotBlank()) date
            else if (currentCamino != null && currentCamino.startDate.isNotBlank()) {
                AppStrings.calculateStageDate(currentCamino.startDate, nextDay - 1)
            } else ""

            repository.insertStage(
                StageEntity(
                    caminoId = caminoId,
                    dayNumber = nextDay,
                    date = finalDate,
                    title = title.ifBlank { "Etapa $nextDay: $origin a $destination" },
                    origin = origin.ifBlank { "Inicio" },
                    destination = destination.ifBlank { "Fin" },
                    distanceKm = distanceKm,
                    elevationGainM = elevationGainM,
                    elevationLossM = elevationLossM,
                    estimatedTime = estimatedTime.ifBlank { "4h 30m" },
                    difficulty = difficulty,
                    stageNotes = stageNotes
                )
            )
            _userMessage.value = "Etapa del día $nextDay añadida correctamente"
        }
    }

    fun toggleStageCompletion(stage: StageEntity) {
        viewModelScope.launch {
            val updated = stage.copy(isCompleted = !stage.isCompleted)
            repository.updateStage(updated)
            _userMessage.value = if (updated.isCompleted) "¡Etapa ${stage.dayNumber} marcada como completada! 🎉" else "Etapa ${stage.dayNumber} desmarcada"
        }
    }

    fun updateStage(stage: StageEntity) {
        viewModelScope.launch {
            repository.updateStage(stage)
            _userMessage.value = "Etapa del día ${stage.dayNumber} actualizada"
        }
    }

    fun updateStageTips(stageId: Long, newTips: String) {
        viewModelScope.launch {
            val stg = repository.getStageByIdOnce(stageId)
            if (stg != null) {
                repository.updateStage(stg.copy(stageNotes = newTips))
                _userMessage.value = "Consejos del día actualizados"
            }
        }
    }

    fun deleteStage(stage: StageEntity) {
        viewModelScope.launch {
            repository.deleteStage(stage)
            _userMessage.value = "Etapa eliminada"
            navigateBack()
        }
    }

    fun importGpxToStage(stageId: Long, gpxContent: String, markAsCompleted: Boolean = false) {
        viewModelScope.launch {
            val updated = repository.importGpxIntoStage(stageId, gpxContent, markAsCompleted)
            if (updated != null) {
                _userMessage.value = "¡GPX sincronizado! Datos actualizados: ${updated.distanceKm} km, +${updated.elevationGainM}m, tiempo ${updated.estimatedTime}."
            } else {
                _userMessage.value = "Error al procesar el archivo GPX"
            }
        }
    }

    fun addAccommodation(
        stageId: Long,
        name: String,
        address: String,
        phone: String,
        roomOptions: List<RoomPriceOption>,
        notes: String
    ) {
        viewModelScope.launch {
            val optionsJson = repository.serializeRoomOptions(roomOptions)
            repository.insertAccommodation(
                AccommodationEntity(
                    stageId = stageId,
                    name = name.ifBlank { "Albergue / Hospedaje" },
                    address = address,
                    phone = phone,
                    roomOptionsJson = optionsJson,
                    notes = notes
                )
            )
            _userMessage.value = "Alojamiento guardado"
        }
    }

    fun updateAccommodation(acc: AccommodationEntity) {
        viewModelScope.launch {
            repository.updateAccommodation(acc)
            _userMessage.value = "Alojamiento actualizado"
        }
    }

    fun deleteAccommodation(acc: AccommodationEntity) {
        viewModelScope.launch {
            repository.deleteAccommodation(acc)
            _userMessage.value = "Alojamiento eliminado"
        }
    }

    fun parseRoomOptions(json: String?): List<RoomPriceOption> {
        return repository.parseRoomOptions(json)
    }

    // Multiple Vivencias per Stage management
    fun parseVivencias(json: String?, fallbackNotes: String = "", fallbackPhotoUri: String? = null): List<VivenciaItem> {
        val list = mutableListOf<VivenciaItem>()
        if (!json.isNullOrBlank()) {
            try {
                val array = org.json.JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        VivenciaItem(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            note = obj.optString("note", ""),
                            photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() },
                            dateStr = obj.optString("dateStr", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (list.isEmpty() && (fallbackNotes.isNotBlank() || !fallbackPhotoUri.isNullOrBlank())) {
            list.add(
                VivenciaItem(
                    id = "legacy_vivencia",
                    note = fallbackNotes,
                    photoUri = fallbackPhotoUri,
                    dateStr = "",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return list
    }

    fun serializeVivencias(list: List<VivenciaItem>): String {
        val array = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject()
            obj.put("id", item.id)
            obj.put("note", item.note)
            obj.put("photoUri", item.photoUri ?: "")
            obj.put("dateStr", item.dateStr)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    fun addVivenciaToStage(stageId: Long, note: String, photoUri: String?, dateStr: String = "") {
        viewModelScope.launch {
            val currentStage = repository.getStageByIdOnce(stageId) ?: return@launch
            val currentList = parseVivencias(currentStage.vivenciasJson, currentStage.diaryNotes, currentStage.diaryPhotoUri).toMutableList()
            currentList.add(
                VivenciaItem(
                    id = java.util.UUID.randomUUID().toString(),
                    note = note,
                    photoUri = photoUri,
                    dateStr = dateStr,
                    timestamp = System.currentTimeMillis()
                )
            )
            val updatedJson = serializeVivencias(currentList)
            repository.updateStage(currentStage.copy(
                vivenciasJson = updatedJson,
                diaryNotes = currentList.firstOrNull()?.note ?: "",
                diaryPhotoUri = currentList.firstOrNull { !it.photoUri.isNullOrBlank() }?.photoUri
            ))
            _userMessage.value = "Vivencia guardada correctamente ✨"
        }
    }

    fun updateVivenciaInStage(stageId: Long, vivencia: VivenciaItem) {
        viewModelScope.launch {
            val currentStage = repository.getStageByIdOnce(stageId) ?: return@launch
            val currentList = parseVivencias(currentStage.vivenciasJson, currentStage.diaryNotes, currentStage.diaryPhotoUri).map {
                if (it.id == vivencia.id) vivencia else it
            }
            val updatedJson = serializeVivencias(currentList)
            repository.updateStage(currentStage.copy(
                vivenciasJson = updatedJson,
                diaryNotes = currentList.firstOrNull()?.note ?: "",
                diaryPhotoUri = currentList.firstOrNull { !it.photoUri.isNullOrBlank() }?.photoUri
            ))
            _userMessage.value = "Vivencia actualizada"
        }
    }

    fun deleteVivenciaFromStage(stageId: Long, vivenciaId: String) {
        viewModelScope.launch {
            val currentStage = repository.getStageByIdOnce(stageId) ?: return@launch
            val currentList = parseVivencias(currentStage.vivenciasJson, currentStage.diaryNotes, currentStage.diaryPhotoUri).filter { it.id != vivenciaId }
            val updatedJson = serializeVivencias(currentList)
            repository.updateStage(currentStage.copy(
                vivenciasJson = updatedJson,
                diaryNotes = currentList.firstOrNull()?.note ?: "",
                diaryPhotoUri = currentList.firstOrNull { !it.photoUri.isNullOrBlank() }?.photoUri
            ))
            _userMessage.value = "Vivencia eliminada"
        }
    }

    // Stage Attached Files Management
    fun addStageAttachedFile(stageId: Long, name: String, uriStr: String, sizeBytes: Long = 0, mimeType: String = "") {
        viewModelScope.launch {
            val stage = repository.getStageByIdOnce(stageId) ?: return@launch
            val list = parseAttachedFiles(stage.attachedFilesJson).toMutableList()
            list.add(
                AttachedFile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    uri = uriStr,
                    sizeBytes = sizeBytes,
                    mimeType = mimeType
                )
            )
            val updatedJson = serializeAttachedFiles(list)
            repository.updateStage(stage.copy(attachedFilesJson = updatedJson))
            _userMessage.value = "Archivo \"$name\" adjuntado a la etapa"
        }
    }

    fun removeStageAttachedFile(stageId: Long, fileId: String) {
        viewModelScope.launch {
            val stage = repository.getStageByIdOnce(stageId) ?: return@launch
            val list = parseAttachedFiles(stage.attachedFilesJson).filter { it.id != fileId }
            val updatedJson = serializeAttachedFiles(list)
            repository.updateStage(stage.copy(attachedFilesJson = updatedJson))
            _userMessage.value = "Archivo de etapa eliminado"
        }
    }

    fun updateVivencias(stageId: Long, diaryNotes: String, photoUri: String?) {
        viewModelScope.launch {
            val currentStage = repository.getStageByIdOnce(stageId)
            if (currentStage != null) {
                val updated = currentStage.copy(
                    diaryNotes = diaryNotes,
                    diaryPhotoUri = photoUri ?: currentStage.diaryPhotoUri
                )
                repository.updateStage(updated)
                _userMessage.value = "Vivencias del día guardadas correctamente ✨"
            }
        }
    }

    fun exportToPdf(context: Context) {
        val currentCamino = selectedCamino.value ?: return
        val currentStages = stages.value

        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val file = PdfExporter.generateAndSharePdf(context, currentCamino, currentStages, repository)
                if (file != null) {
                    _userMessage.value = "Guía de viaje profesional en PDF generada con éxito."
                } else {
                    _userMessage.value = "No se pudo generar el documento PDF"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Error al generar PDF: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    fun exportStatsPdf(context: Context, isTotal: Boolean) {
        viewModelScope.launch {
            _isExportingPdf.value = true
            try {
                val targetCamino = selectedCamino.value
                val targetStats = if (isTotal) totalGlobalStats.value else caminoStats.value
                val stageList = if (isTotal) emptyList() else stages.value
                val file = PdfExporter.generateAndShareStatsPdf(
                    context = context,
                    isTotal = isTotal,
                    camino = targetCamino,
                    stats = targetStats,
                    stages = stageList
                )
                if (file != null) {
                    _userMessage.value = "Estadística del viaje exportada con éxito"
                } else {
                    _userMessage.value = "No se pudo generar el PDF de estadísticas"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Error al exportar estadística: ${e.message}"
            } finally {
                _isExportingPdf.value = false
            }
        }
    }

    // Attached files management
    fun addAttachedFile(caminoId: Long, name: String, uriStr: String, sizeBytes: Long = 0, mimeType: String = "") {
        viewModelScope.launch {
            val camino = repository.getCaminoByIdOnce(caminoId) ?: return@launch
            val list = parseAttachedFiles(camino.attachedFilesJson).toMutableList()
            list.add(
                AttachedFile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    uri = uriStr,
                    sizeBytes = sizeBytes,
                    mimeType = mimeType
                )
            )
            val updatedJson = serializeAttachedFiles(list)
            repository.updateCamino(camino.copy(attachedFilesJson = updatedJson))
            _userMessage.value = "Archivo \"$name\" adjuntado con éxito"
        }
    }

    fun removeAttachedFile(caminoId: Long, fileId: String) {
        viewModelScope.launch {
            val camino = repository.getCaminoByIdOnce(caminoId) ?: return@launch
            val list = parseAttachedFiles(camino.attachedFilesJson).filter { it.id != fileId }
            val updatedJson = serializeAttachedFiles(list)
            repository.updateCamino(camino.copy(attachedFilesJson = updatedJson))
            _userMessage.value = "Archivo eliminado"
        }
    }

    fun parseAttachedFiles(json: String?): List<AttachedFile> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = org.json.JSONArray(json)
            val res = mutableListOf<AttachedFile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                res.add(
                    AttachedFile(
                        id = obj.optString("id", i.toString()),
                        name = obj.optString("name", "Documento"),
                        uri = obj.optString("uri", ""),
                        mimeType = obj.optString("mimeType", ""),
                        sizeBytes = obj.optLong("sizeBytes", 0),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
            res
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeAttachedFiles(files: List<AttachedFile>): String {
        val array = org.json.JSONArray()
        for (f in files) {
            val obj = org.json.JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("uri", f.uri)
            obj.put("mimeType", f.mimeType)
            obj.put("sizeBytes", f.sizeBytes)
            obj.put("addedAt", f.addedAt)
            array.put(obj)
        }
        return array.toString()
    }
}
