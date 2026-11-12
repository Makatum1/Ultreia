package com.example.ui.settings

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LanguageMode {
    ES,
    EN
}

object AppStrings {

    fun get(key: String, language: LanguageMode): String {
        val isEn = language == LanguageMode.EN
        return if (isEn) enStrings[key] ?: esStrings[key] ?: key
        else esStrings[key] ?: key
    }

    private val esStrings = mapOf(
        "app_name" to "Ultreia",
        "app_subtitle" to "Guía de Viaje y Memorias del Camino y Rutas",
        "welcome_greeting" to "¡BUEN CAMINO!",
        "your_routes" to "Tus Rutas y Guías",
        "your_routes_desc" to "Elige un camino o viaje para ver sus etapas organizadas por fechas, mapa y memorias.",
        "caminos_title" to "Caminos Disponibles",
        "new_camino" to "Nuevo Camino",
        "new_trip" to "Nuevo Viaje",
        "tab_caminos" to "Caminos",
        "tab_trips" to "Viajes",
        "tab_stages" to "Etapas",
        "tab_days" to "Días",
        "tab_map" to "Mapa",
        "tab_stats" to "Datos",
        "settings" to "Ajustes",
        "theme" to "Tema Visual",
        "theme_system" to "Sistema",
        "theme_light" to "Modo Claro",
        "theme_dark" to "Modo Oscuro",
        "language" to "Idioma",
        "lang_es" to "Español",
        "lang_en" to "English",
        "export_pdf" to "Exportar PDF",
        "export_pdf_desc" to "Guía de viaje profesional con portada, mapa y consejos.",
        "export_json" to "Exportar",
        "stage_tips" to "Consejos del Día",
        "see_more" to "... Ver más",
        "see_less" to "Ver menos",
        "edit_tips" to "Editar Consejos",
        "save" to "Guardar",
        "cancel" to "Cancelar",
        "edit_stage" to "Editar Etapa",
        "upload_gpx" to "Subir o Actualizar GPX",
        "open_in_google_maps" to "Abrir en Mapas",
        "navigate_route" to "Navegar Ruta en Mapas",
        "call" to "Llamar",
        "accommodations" to "Alojamientos",
        "add_accommodation" to "Añadir Alojamiento",
        "daily_memories" to "Vivencias Diarias",
        "attach_photo" to "Adjuntar Foto",
        "change_photo" to "Cambiar Foto",
        "save_notes" to "Guardar Notas",
        "completed" to "Completada",
        "mark_done" to "Marcar Hecha",
        "distance" to "Distancia",
        "elevation_gain" to "Desnivel +",
        "elevation_loss" to "Desnivel -",
        "est_time" to "Tiempo",
        "difficulty" to "Dificultad",
        "today_stage" to "ETAPA DE HOY",
        "day" to "Día",
        "date" to "Fecha",
        "completed_stages" to "Etapas Finalizadas",
        "progress" to "Progreso General",
        "total_distance" to "Distancia Recorrida",
        "remaining_distance" to "Por Recorrer",
        "total_stages" to "Total de etapas",
        "total_caminos" to "Total de caminos",
        "total_trips" to "Total de viajes",
        "main_routes" to "Principales Rutas",
        "empty_import_trip" to "Importar Viaje",
        "empty_add_sample_trip" to "Añadir Viaje de Prueba",
        "empty_import_camino" to "Importar Camino",
        "empty_add_sample_camino" to "Añadir Camino de Prueba",
        "export_import_title" to "Exportar e Importar",
        "export_btn" to "Exportar",
        "import_btn" to "Importar",
        "export_complete_backup" to "Exportar Backup Completo",
        "import_complete_backup" to "Importar Backup Completo",
        "english_route_hint" to "Se recomienda realizar el Camino por el trazado antiguo"
    )

    private val enStrings = mapOf(
        "app_name" to "Ultreia",
        "app_subtitle" to "Travel Guide & Memories for Camino and Trips",
        "welcome_greeting" to "BUEN CAMINO!",
        "your_routes" to "Your Routes & Guides",
        "your_routes_desc" to "Select a Camino or trip to explore daily stages by date, map route, and travel memories.",
        "caminos_title" to "Available Caminos",
        "new_camino" to "New Camino",
        "new_trip" to "New Trip",
        "tab_caminos" to "Caminos",
        "tab_trips" to "Trips",
        "tab_stages" to "Stages",
        "tab_days" to "Days",
        "tab_map" to "Map",
        "tab_stats" to "Data",
        "settings" to "Settings",
        "theme" to "Display Theme",
        "theme_system" to "System",
        "theme_light" to "Light Mode",
        "theme_dark" to "Dark Mode",
        "language" to "Language",
        "lang_es" to "Español",
        "lang_en" to "English",
        "export_pdf" to "Export PDF",
        "export_pdf_desc" to "Professional travel guide with cover, maps, and tips.",
        "export_json" to "Export",
        "stage_tips" to "Tips of the Day",
        "see_more" to "... See more",
        "see_less" to "See less",
        "edit_tips" to "Edit Tips",
        "save" to "Save",
        "cancel" to "Cancel",
        "edit_stage" to "Edit Stage",
        "upload_gpx" to "Upload or Update GPX",
        "open_in_google_maps" to "Open in Maps",
        "navigate_route" to "Navigate Route in Maps",
        "call" to "Call",
        "accommodations" to "Accommodations",
        "add_accommodation" to "Add Accommodation",
        "daily_memories" to "Daily Journal & Memories",
        "attach_photo" to "Attach Photo",
        "change_photo" to "Change Photo",
        "save_notes" to "Save Journal",
        "completed" to "Completed",
        "mark_done" to "Mark as Done",
        "distance" to "Distance",
        "elevation_gain" to "Gain +",
        "elevation_loss" to "Loss -",
        "est_time" to "Est. Time",
        "difficulty" to "Difficulty",
        "today_stage" to "TODAY'S STAGE",
        "day" to "Day",
        "date" to "Date",
        "completed_stages" to "Completed Stages",
        "progress" to "Total Progress",
        "total_distance" to "Distance Covered",
        "remaining_distance" to "Remaining",
        "total_stages" to "Total stages",
        "total_caminos" to "Total Caminos",
        "total_trips" to "Total Trips",
        "main_routes" to "Main Routes",
        "empty_import_trip" to "Import Trip",
        "empty_add_sample_trip" to "Add Sample Trip",
        "empty_import_camino" to "Import Camino",
        "empty_add_sample_camino" to "Add Sample Camino",
        "export_import_title" to "Export and Import",
        "export_btn" to "Export",
        "import_btn" to "Import",
        "export_complete_backup" to "Export Complete Backup",
        "import_complete_backup" to "Import Complete Backup",
        "english_route_hint" to "It is recommended to take the Camino along the ancient route"
    )

    /**
     * Calculates the date of a stage from the start date (DD/MM/YYYY)
     */
    fun calculateStageDate(baseStartDate: String, dayOffset: Int): String {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val parsed = format.parse(baseStartDate)
            if (parsed != null) {
                calendar.time = parsed
                calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
                format.format(calendar.time)
            } else {
                baseStartDate
            }
        } catch (_: Exception) {
            baseStartDate
        }
    }

    /**
     * Checks if a date string (dd/MM/yyyy) is today
     */
    fun isToday(dateStr: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = format.format(Calendar.getInstance().time)
            dateStr.trim() == todayStr
        } catch (_: Exception) {
            false
        }
    }
}
