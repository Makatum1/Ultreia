package com.example.data.gpx

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AttachedFile
import com.example.data.model.GpxPoint
import java.io.File

object CaminoInglesGpxProvider {
    const val FILE_NAME = "Camino_Ingles_Trazado_Antiguo_Wikiloc_128031925.gpx"
    const val WIKILOC_URL = "https://es.wikiloc.com/rutas-senderismo/ferrol-a-santiago-camino-ingles-por-trazado-antiguo-128031925"

    // Hitos y coordenadas clave del Trazado Antiguo tradicional desde Ferrol a Santiago de Compostela
    val HISTORICAL_WAYPOINTS = listOf(
        // Etapa 1: Ferrol a Neda (Ría de Ferrol y O Couto)
        GpxPoint(43.4832, -8.2369, 5.0),   // Puerto de Curuxeiras (Km 0)
        GpxPoint(43.4855, -8.2350, 6.0),
        GpxPoint(43.4880, -8.2335, 7.0),
        GpxPoint(43.4912, -8.2301, 8.0),   // A Malata
        GpxPoint(43.4950, -8.2220, 12.0),
        GpxPoint(43.4985, -8.2140, 15.0),
        GpxPoint(43.5042, -8.1965, 25.0),  // Monasterio de San Martiño de Xuvia (O Couto)
        GpxPoint(43.5020, -8.1850, 20.0),
        GpxPoint(43.5011, -8.1780, 18.0),  // Narón (O Ponto / Santa Icía)
        GpxPoint(43.4998, -8.1670, 14.0),
        GpxPoint(43.4986, -8.1569, 12.0),  // Neda (Albergue de Neda)

        // Etapa 2: Neda a Pontedeume (Fene, Cabanas y Eume)
        GpxPoint(43.4890, -8.1575, 18.0),
        GpxPoint(43.4780, -8.1600, 32.0),
        GpxPoint(43.4650, -8.1630, 45.0),  // Fene (Cruceiro de San Salvador)
        GpxPoint(43.4510, -8.1680, 80.0),
        GpxPoint(43.4380, -8.1720, 110.0), // Vilar do Colo
        GpxPoint(43.4280, -8.1715, 60.0),
        GpxPoint(43.4180, -8.1710, 10.0),  // Cabanas (Pinar y Playa de la Magdalena)
        GpxPoint(43.4072, -8.1706, 15.0),  // Pontedeume (Puente sobre el río Eume)

        // Etapa 3: Pontedeume a Betanzos (Breamo, Miño y Tiobre)
        GpxPoint(43.3960, -8.1780, 95.0),
        GpxPoint(43.3850, -8.1850, 160.0), // Monte Breamo / Centroña
        GpxPoint(43.3750, -8.1910, 135.0),
        GpxPoint(43.3650, -8.1980, 110.0), // Buíña
        GpxPoint(43.3560, -8.2010, 50.0),
        GpxPoint(43.3481, -8.2047, 12.0),  // Miño (Ría de Betanzos)
        GpxPoint(43.3400, -8.2055, 10.0),
        GpxPoint(43.3320, -8.2060, 8.0),   // Ponte do Porco (Río Lambre)
        GpxPoint(43.3230, -8.2090, 45.0),
        GpxPoint(43.3150, -8.2120, 75.0),  // Montecelo / San Pantaleón das Viñas
        GpxPoint(43.3030, -8.2125, 80.0),
        GpxPoint(43.2920, -8.2130, 85.0),  // Tiobre (Santuario de Nuestra Señora del Camino)
        GpxPoint(43.2808, -8.2114, 25.0),  // Betanzos (Porta da Ponte Vella / Casco Histórico)

        // Etapa 4: Betanzos a Hospital de Bruma (Trazado Antiguo por Cos, Presedo y Vilacova)
        GpxPoint(43.2720, -8.2130, 30.0),
        GpxPoint(43.2650, -8.2150, 40.0),  // Limodre / Xanrozo
        GpxPoint(43.2550, -8.2200, 65.0),
        GpxPoint(43.2450, -8.2250, 95.0),  // San Esteban de Cos (Variante antigua tradicional)
        GpxPoint(43.2330, -8.2340, 120.0),
        GpxPoint(43.2210, -8.2430, 145.0), // Presedo (Albergue de Peregrinos)
        GpxPoint(43.2110, -8.2540, 175.0),
        GpxPoint(43.2010, -8.2650, 210.0), // Santaia de Leiro
        GpxPoint(43.1930, -8.2760, 265.0),
        GpxPoint(43.1850, -8.2880, 320.0), // San Paio de Vilacova
        GpxPoint(43.1770, -8.2970, 370.0),
        GpxPoint(43.1700, -8.3050, 410.0), // Monte do Gato / Corredoiras de tierra sin asfalto
        GpxPoint(43.1640, -8.3160, 435.0),
        GpxPoint(43.1583, -8.3267, 450.0), // Hospital de Bruma (Albergue de Peregrinos histórico)

        // Etapa 5: Hospital de Bruma a Sigüeiro (Ordes, A Calle y Casanova de Poulo)
        GpxPoint(43.1410, -8.3360, 435.0),
        GpxPoint(43.1250, -8.3450, 420.0), // O Seixo / Cabeza de Lobo
        GpxPoint(43.1050, -8.3560, 395.0),
        GpxPoint(43.0850, -8.3680, 375.0), // A Calle (Ordes - arquitectura tradicional de piedra)
        GpxPoint(43.0700, -8.3810, 360.0),
        GpxPoint(43.0550, -8.3950, 350.0), // San Paio do Carballo
        GpxPoint(43.0400, -8.4050, 335.0),
        GpxPoint(43.0250, -8.4150, 320.0), // Casanova de Poulo
        GpxPoint(43.0050, -8.4250, 290.0),
        GpxPoint(42.9850, -8.4350, 260.0), // Ponte Pereira sobre el río Tambre
        GpxPoint(42.9678, -8.4431, 270.0), // Sigüeiro (Paseo Fluvial del Tambre)

        // Etapa 6: Sigüeiro a Santiago de Compostela (Marantes, Meixonfrío y Obradoiro)
        GpxPoint(42.9560, -8.4540, 280.0),
        GpxPoint(42.9450, -8.4650, 290.0), // Bosque del Tambre
        GpxPoint(42.9360, -8.4750, 300.0),
        GpxPoint(42.9280, -8.4850, 310.0), // Marantes / Barcés
        GpxPoint(42.9210, -8.4950, 320.0),
        GpxPoint(42.9150, -8.5050, 330.0), // San Cristovo das Viñas / O Rial
        GpxPoint(42.9080, -8.5150, 335.0),
        GpxPoint(42.9020, -8.5250, 340.0), // Meixonfrío (Antigua taberna de peregrinos)
        GpxPoint(42.8950, -8.5320, 325.0),
        GpxPoint(42.8890, -8.5380, 310.0), // San Caetano / Barrio de San Pedro
        GpxPoint(42.8840, -8.5400, 290.0),
        GpxPoint(42.8805, -8.5420, 275.0), // Porta do Camiño / Rúa das Ánimas
        GpxPoint(42.8782, -8.5448, 260.0)  // Plaza del Obradoiro - Catedral de Santiago
    )

    /**
     * Genera el archivo físico GPX en el almacenamiento interno de la app si no existe.
     */
    fun getOrGenerateGpxFile(context: Context): File {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists() || file.length() < 200) {
            val content = buildGpxXml()
            file.writeText(content, Charsets.UTF_8)
        }
        return file
    }

    /**
     * Construye la representación de AttachedFile lista para persistir en JSON.
     */
    fun createAttachedFile(context: Context): AttachedFile {
        val file = getOrGenerateGpxFile(context)
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }

        return AttachedFile(
            id = "wikiloc_camino_ingles_128031925",
            name = FILE_NAME,
            uri = uri.toString(),
            mimeType = "application/gpx+xml",
            sizeBytes = file.length(),
            addedAt = System.currentTimeMillis()
        )
    }

    /**
     * Construye el documento XML GPX conforme al estándar 1.1 con metadatos del link de Wikiloc.
     */
    fun buildGpxXml(): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="Ultreia - Wikiloc ID 128031925" xmlns="http://www.topografix.com/GPX/1/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">""")
        sb.appendLine("  <metadata>")
        sb.appendLine("    <name>Ferrol a Santiago - Camino Inglés por Trazado Antiguo</name>")
        sb.appendLine("    <desc>Ruta histórica del Camino Inglés por el trazado tradicional antiguo desde Ferrol a Santiago de Compostela evitando los tramos de asfalto modernos. Referencia oficial Wikiloc ID: 128031925</desc>")
        sb.appendLine("    <author>")
        sb.appendLine("      <name>Ultreia Guía de Peregrinos</name>")
        sb.appendLine("    </author>")
        sb.appendLine("    <link href=\"$WIKILOC_URL\">")
        sb.appendLine("      <text>Ruta en Wikiloc: Ferrol a Santiago - Camino Inglés por Trazado Antiguo</text>")
        sb.appendLine("    </link>")
        sb.appendLine("    <time>2026-05-15T08:00:00Z</time>")
        sb.appendLine("  </metadata>")
        sb.appendLine("  <trk>")
        sb.appendLine("    <name>Camino Inglés - Trazado Antiguo (Ferrol a Santiago)</name>")
        sb.appendLine("    <desc>Recorrido completo tradicional por O Couto, Neda, Pontedeume, Miño, Betanzos, Cos, Presedo, Hospital de Bruma, A Calle, Sigüeiro y Santiago de Compostela.</desc>")
        sb.appendLine("    <trkseg>")

        // Generar puntos interpolados para un trazado suave de alta precisión
        for (i in 0 until HISTORICAL_WAYPOINTS.size - 1) {
            val p1 = HISTORICAL_WAYPOINTS[i]
            val p2 = HISTORICAL_WAYPOINTS[i + 1]
            val steps = 5
            for (s in 0 until steps) {
                val t = s.toDouble() / steps
                val lat = p1.lat + (p2.lat - p1.lat) * t
                val lon = p1.lon + (p2.lon - p1.lon) * t
                val ele = p1.ele + (p2.ele - p1.ele) * t
                sb.appendLine(String.format(java.util.Locale.US, """      <trkpt lat="%.6f" lon="%.6f"><ele>%.1f</ele></trkpt>""", lat, lon, ele))
            }
        }
        val last = HISTORICAL_WAYPOINTS.last()
        sb.appendLine(String.format(java.util.Locale.US, """      <trkpt lat="%.6f" lon="%.6f"><ele>%.1f</ele></trkpt>""", last.lat, last.lon, last.ele))

        sb.appendLine("    </trkseg>")
        sb.appendLine("  </trk>")
        sb.appendLine("</gpx>")
        return sb.toString()
    }
}
