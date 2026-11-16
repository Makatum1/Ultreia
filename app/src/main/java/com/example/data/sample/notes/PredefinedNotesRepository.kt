package com.example.data.sample.notes

import com.example.data.sample.PredefinedRoutesData
import com.example.ui.settings.LanguageMode

object PredefinedNotesRepository {

    fun makeKey(origin: String, destination: String): String =
        "${origin.trim().lowercase()}->${destination.trim().lowercase()}"

    fun getStageNotes(origin: String, destination: String, mode: LanguageMode): String? {
        val key = makeKey(origin, destination)
        return when (mode) {
            LanguageMode.ES -> PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.EN -> notesEN[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.FR -> notesFR[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.IT -> notesIT[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.DE -> notesDE[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.NL -> notesNL[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.PL -> notesPL[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
            LanguageMode.ZH -> notesZH[key] ?: PredefinedRoutesData.findNotesByOriginDest(origin, destination)
        }
    }

    fun getAllPredefinedNotesFor(origin: String, destination: String): Set<String> {
        val key = makeKey(origin, destination)
        val list = mutableListOf<String>()
        val es = PredefinedRoutesData.findNotesByOriginDest(origin, destination)
        if (!es.isNullOrBlank()) list.add(es)
        notesEN[key]?.let { list.add(it) }
        notesFR[key]?.let { list.add(it) }
        notesIT[key]?.let { list.add(it) }
        notesDE[key]?.let { list.add(it) }
        notesNL[key]?.let { list.add(it) }
        notesPL[key]?.let { list.add(it) }
        notesZH[key]?.let { list.add(it) }
        return list.map { it.trim() }.toSet()
    }

    fun isUnmodified(notes: String?, origin: String = "", destination: String = ""): Boolean {
        if (notes.isNullOrBlank()) return true
        val trimmed = notes.trim()
        if (origin.isNotBlank() && destination.isNotBlank()) {
            val all = getAllPredefinedNotesFor(origin, destination)
            if (all.isNotEmpty()) {
                return all.any { it == trimmed }
            }
        }
        // Fallback: check if it matches any predefined note across all maps
        return PredefinedRoutesData.isAnyPredefinedNote(trimmed) ||
                notesEN.values.any { it.trim() == trimmed } ||
                notesFR.values.any { it.trim() == trimmed } ||
                notesIT.values.any { it.trim() == trimmed } ||
                notesDE.values.any { it.trim() == trimmed } ||
                notesNL.values.any { it.trim() == trimmed } ||
                notesPL.values.any { it.trim() == trimmed } ||
                notesZH.values.any { it.trim() == trimmed }
    }

    fun getLocalizedNotes(notes: String?, origin: String = "", destination: String = "", mode: LanguageMode): String {
        if (notes.isNullOrBlank()) return ""
        if (isUnmodified(notes, origin, destination)) {
            val localized = if (origin.isNotBlank() && destination.isNotBlank()) {
                getStageNotes(origin, destination, mode)
            } else {
                PredefinedRoutesData.findStageByNotes(notes)?.let { stage ->
                    getStageNotes(stage.origin, stage.destination, mode)
                }
            }
            if (!localized.isNullOrBlank()) return localized
        }
        return notes
    }
}
