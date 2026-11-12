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
    SYSTEM,
    ES,
    EN
}

object AppStrings {

    fun get(key: String, language: LanguageMode): String {
        val isEn = when (language) {
            LanguageMode.EN -> true
            LanguageMode.ES -> false
            LanguageMode.SYSTEM -> false // Ultreia defaults to Spanish in all default/system settings
        }

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
        "tab_caminos" to "Caminos",
        "tab_stages" to "Etapas",
        "tab_map" to "Mapa",
        "tab_stats" to "Estadísticas",
        "settings" to "Ajustes",
        "theme" to "Tema Visual",
        "theme_system" to "Sistema (por defecto)",
        "theme_light" to "Modo Claro",
        "theme_dark" to "Modo Oscuro",
        "language" to "Idioma",
        "lang_system" to "Sistema (por defecto)",
        "lang_es" to "Español",
        "lang_en" to "English",
        "export_pdf" to "Exportar a PDF",
        "export_pdf_desc" to "Guía de viaje profesional con portada, mapa y consejos.",
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
        "remaining_distance" to "Por Recorrer"
    )

    private val enStrings = mapOf(
        "app_name" to "Ultreia",
        "app_subtitle" to "Travel Guide & Memories for Camino and Trips",
        "welcome_greeting" to "BUEN CAMINO!",
        "your_routes" to "Your Routes & Guides",
        "your_routes_desc" to "Select a Camino or trip to explore daily stages by date, map route, and travel memories.",
        "caminos_title" to "Available Caminos",
        "new_camino" to "New Camino",
        "tab_caminos" to "Caminos",
        "tab_stages" to "Stages",
        "tab_map" to "Map",
        "tab_stats" to "Statistics",
        "settings" to "Settings",
        "theme" to "Display Theme",
        "theme_system" to "System (default)",
        "theme_light" to "Light Mode",
        "theme_dark" to "Dark Mode",
        "language" to "Language",
        "lang_system" to "System (default)",
        "lang_es" to "Español",
        "lang_en" to "English",
        "export_pdf" to "Export to PDF",
        "export_pdf_desc" to "Professional travel guide with cover, maps, and tips.",
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
        "remaining_distance" to "Remaining"
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
