package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.AgendaItem
import com.example.data.model.AttachedFile
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import com.example.data.model.VivenciaItem
import com.example.data.repository.CaminoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 40f

    // Official Travel Guide Luxury Editorial Palette
    private val COLOR_HEADER_BG = Color.parseColor("#1C4B64") // Azul oscuro exacto de marca
    private val COLOR_NAVY = Color.parseColor("#1C4B64")
    private val COLOR_NAVY_LIGHT = Color.rgb(38, 92, 122)
    private val COLOR_GOLD = Color.rgb(245, 178, 30)
    private val COLOR_GOLD_DARK = Color.rgb(194, 120, 3)
    private val COLOR_GOLD_LIGHT = Color.rgb(254, 243, 199)
    private val COLOR_EMERALD = Color.rgb(22, 163, 74)
    private val COLOR_EMERALD_BG = Color.rgb(240, 253, 244)
    private val COLOR_EMERALD_BORDER = Color.rgb(187, 247, 208)
    private val COLOR_AMBER = Color.rgb(217, 119, 6)
    private val COLOR_AMBER_BG = Color.rgb(255, 247, 237)
    private val COLOR_AMBER_BORDER = Color.rgb(254, 215, 170)
    private val COLOR_BLUE = Color.rgb(37, 99, 235)
    private val COLOR_BLUE_BG = Color.rgb(239, 246, 255)
    private val COLOR_BLUE_BORDER = Color.rgb(191, 219, 254)
    private val COLOR_SLATE = Color.rgb(15, 23, 42)
    private val COLOR_MUTED = Color.rgb(100, 116, 139)
    private val COLOR_CARD_BG = Color.rgb(248, 250, 252)
    private val COLOR_CARD_BORDER = Color.rgb(226, 232, 240)
    private val COLOR_DIVIDER = Color.rgb(238, 242, 246)
    private val COLOR_WARM_BG = Color.rgb(255, 253, 247)
    private val COLOR_WARM_BORDER = Color.rgb(243, 230, 200)

    suspend fun generateAndSharePdf(
        context: Context,
        camino: CaminoEntity,
        stages: List<StageEntity>,
        repository: CaminoRepository
    ): File? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        try {
            var pageNumber = 1

            // ---------------- PAGE 1: PORTADA & RESUMEN GENERAL ----------------
            val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            val canvas1 = page1.canvas
            drawCoverPage(context, canvas1, camino, stages, repository)
            pdfDocument.finishPage(page1)
            pageNumber++

            // ---------------- PAGES 2+: ETAPAS Y VIVENCIAS ----------------
            val allAccommodations = repository.getAllAccommodationsForCaminoOnce(camino.id)
            val accMap = allAccommodations.groupBy { it.stageId }

            for (stage in stages) {
                val accommodations = accMap[stage.id] ?: emptyList()
                pageNumber = drawStagePages(context, pdfDocument, pageNumber, camino, stage, accommodations, repository)
            }

            // Write to file
            val outputDir = File(context.cacheDir, "shared_docs")
            if (!outputDir.exists()) outputDir.mkdirs()

            val sanitizedTitle = camino.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val pdfFile = File(outputDir, "${sanitizedTitle}.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            // Open share intent
            withContext(Dispatchers.Main) {
                sharePdfFile(context, pdfFile, camino.title, camino.isTravelGuide)
            }

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            try { pdfDocument.close() } catch (_: Exception) {}
            null
        }
    }

    suspend fun generateAndShareStatsPdf(
        context: Context,
        isTotal: Boolean,
        camino: CaminoEntity?,
        stats: com.example.data.repository.CaminoStats,
        stages: List<StageEntity> = emptyList()
    ): File? = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val isTravel = camino?.isTravelGuide == true
            val titleText = if (isTotal) {
                if (isTravel) "Estadísticas Totales de Viajes" else "Estadísticas Totales del Camino"
            } else {
                "Estadísticas: ${camino?.title ?: (if (isTravel) "Viaje" else "Camino")}"
            }
            drawStatsPage(context, canvas, isTotal, camino, stats, stages)
            pdfDocument.finishPage(page)

            val outputDir = File(context.cacheDir, "shared_docs")
            if (!outputDir.exists()) outputDir.mkdirs()

            val safeTitle = if (isTotal) "Totales" else (camino?.title?.replace("[^a-zA-Z0-9]".toRegex(), "_") ?: "Ruta")
            val pdfFile = File(outputDir, "Ultreia_Estadisticas_${safeTitle}.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, titleText)
                    putExtra(Intent.EXTRA_TEXT, "Comparto la estadística de viaje generada con Ultreia.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Estadística en PDF"))
            }
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            try { pdfDocument.close() } catch (_: Exception) {}
            null
        }
    }

    private fun loadTitleIconBitmap(context: Context): Bitmap? {
        val decodeOpts = BitmapFactory.Options().apply {
            inScaled = false // Preservar resolución nativa sin downsampling por densidad
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        // 1. Intentar cargar recurso drawable con nombre "icono"
        val iconoResId = context.resources.getIdentifier("icono", "drawable", context.packageName)
        if (iconoResId != 0) {
            try {
                val bmp = BitmapFactory.decodeResource(context.resources, iconoResId, decodeOpts)
                if (bmp != null) return bmp
            } catch (_: Exception) {}
        }

        // 2. Intentar cargar desde assets (icono.png, icono.jpg, etc.)
        for (name in listOf("icono.png", "icono.jpg", "icono.jpeg", "icono.webp")) {
            try {
                context.assets.open(name).use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream, null, decodeOpts)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {}
        }

        // 3. Fallback a ic_ultreia_logo de alta resolución
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_ultreia_logo, decodeOpts)
        } catch (_: Exception) {
            null
        }
    }

    // ----------------------------------------------------
    // COVER PAGE (PORTADA EDITORIAL PROFESIONAL)
    // ----------------------------------------------------
    private fun drawCoverPage(
        context: Context,
        canvas: Canvas,
        camino: CaminoEntity,
        stages: List<StageEntity>,
        repository: CaminoRepository
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val isTravel = camino.isTravelGuide

        // 1. Cargar la imagen del título con alta resolución y sin pixelación
        val titleIcon = loadTitleIconBitmap(context)
        var textLeftOffset = MARGIN
        var iconTargetW = 0f
        var iconTargetH = 0f

        if (titleIcon != null) {
            val maxH = 54f
            val maxW = 90f
            val scale = minOf(maxH / titleIcon.height.toFloat(), maxW / titleIcon.width.toFloat(), 1.0f)
            iconTargetW = titleIcon.width * scale
            iconTargetH = titleIcon.height * scale
            textLeftOffset = MARGIN + iconTargetW + 14f
        }

        // Título del Camino / Viaje con ajuste multilínea si es largo
        paint.color = Color.WHITE
        paint.textSize = 23f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titleMaxWidth = PAGE_WIDTH - textLeftOffset - MARGIN
        val titleWords = camino.title.split(" ")
        val line1 = StringBuilder()
        val line2 = StringBuilder()
        for (word in titleWords) {
            val test = if (line1.isEmpty()) word else "$line1 $word"
            if (paint.measureText(test) <= titleMaxWidth && line2.isEmpty()) {
                line1.append(if (line1.isEmpty()) word else " $word")
            } else {
                line2.append(if (line2.isEmpty()) word else " $word")
            }
        }

        val brandLabel = if (isTravel) "ULTREIA VIAJES  •  GUÍA DE VIAJE" else "ULTREIA  •  GUÍA DEL CAMINO DE SANTIAGO"

        val hasLine2 = line2.isNotEmpty()
        val titleLine1Y = 54f
        val titleLine2Y = if (hasLine2) titleLine1Y + 24f else titleLine1Y
        val lastTitleY = if (hasLine2) titleLine2Y else titleLine1Y

        val subText = camino.subtitle.trim()
        val hasSub = subText.isNotBlank()
        val subY = if (hasSub) lastTitleY + 16f else lastTitleY
        val dateY = if (hasSub) subY + 16f else lastTitleY + 16f

        val bannerHeight = maxOf(dateY + 18f, iconTargetH + 42f)

        // Rectángulo de fondo de cabecera con el color exacto #1C4B64
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), bannerHeight, paint)

        // Doble Franja Dorada Inferior
        paint.color = COLOR_GOLD
        canvas.drawRect(0f, bannerHeight - 5f, PAGE_WIDTH.toFloat(), bannerHeight - 2f, paint)
        paint.color = COLOR_GOLD_DARK
        canvas.drawRect(0f, bannerHeight - 1.5f, PAGE_WIDTH.toFloat(), bannerHeight, paint)

        // Dibujar Imagen del Título dentro del rectángulo azul con filtrado de alta calidad
        if (titleIcon != null) {
            val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }
            val iconTop = 26f
            val destRect = RectF(MARGIN, iconTop, MARGIN + iconTargetW, iconTop + iconTargetH)
            canvas.drawBitmap(titleIcon, null, destRect, iconPaint)
        }

        // Marca en blanco puro para máximo contraste
        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(brandLabel, textLeftOffset, 32f, paint)

        // Título del Camino en blanco puro
        paint.color = Color.WHITE
        paint.textSize = 23f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(line1.toString(), textLeftOffset, titleLine1Y, paint)
        if (hasLine2) {
            canvas.drawText(line2.toString(), textLeftOffset, titleLine2Y, paint)
        }

        // Subtítulo en blanco puro
        if (hasSub) {
            paint.textSize = 10.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.WHITE
            val truncatedSub = if (subText.length > 70) subText.take(67) + "..." else subText
            canvas.drawText(truncatedSub, textLeftOffset, subY, paint)
        }

        // Fecha en blanco puro
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dateValue = camino.startDate.ifBlank { "2026" }
        canvas.drawText("📅 Fecha: $dateValue", textLeftOffset, dateY, paint)

        // 2. RESUMEN ESTADÍSTICO EN 4 TARJETAS MODULARES DE COLOR
        val stats = repository.calculateStats(stages)
        val kpiTop = bannerHeight + 14f
        val kpiHeight = 56f
        val kpiSpacing = 8f
        val kpiWidth = (PAGE_WIDTH - (MARGIN * 2) - (kpiSpacing * 3)) / 4f

        // Tarjeta 1: Distancia (Verde Esmeralda)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN,
            y = kpiTop,
            w = kpiWidth,
            h = kpiHeight,
            label = "DISTANCIA",
            value = "${String.format(java.util.Locale.US, "%.1f", stats.completedKm)} km",
            sub = "de ${String.format(java.util.Locale.US, "%.1f", stats.totalKm)} km",
            accentColor = COLOR_EMERALD,
            bgColor = COLOR_EMERALD_BG,
            borderColor = COLOR_EMERALD_BORDER
        )

        // Tarjeta 2: Jornadas / Etapas (Azul Zafiro)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN + kpiWidth + kpiSpacing,
            y = kpiTop,
            w = kpiWidth,
            h = kpiHeight,
            label = if (isTravel) "JORNADAS" else "ETAPAS",
            value = "${stats.completedStages} / ${stats.totalStages}",
            sub = if (stats.completedStages == stats.totalStages) "Completadas" else "${stats.totalStages - stats.completedStages} pendientes",
            accentColor = COLOR_BLUE,
            bgColor = COLOR_BLUE_BG,
            borderColor = COLOR_BLUE_BORDER
        )

        // Tarjeta 3: Desnivel Positivo (Ámbar)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN + (kpiWidth + kpiSpacing) * 2,
            y = kpiTop,
            w = kpiWidth,
            h = kpiHeight,
            label = "DESNIVEL +",
            value = "+${stats.completedElevationGainM} m",
            sub = "de +${stats.totalElevationGainM} m",
            accentColor = COLOR_AMBER,
            bgColor = COLOR_AMBER_BG,
            borderColor = COLOR_AMBER_BORDER
        )

        // Tarjeta 4: Progreso Global (Dorado)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN + (kpiWidth + kpiSpacing) * 3,
            y = kpiTop,
            w = kpiWidth,
            h = kpiHeight,
            label = "PROGRESO",
            value = "${stats.progressPercent}%",
            sub = if (stats.progressPercent >= 100) "¡Objetivo Cumplido!" else "En itinerario",
            accentColor = COLOR_GOLD_DARK,
            bgColor = COLOR_WARM_BG,
            borderColor = COLOR_WARM_BORDER
        )

        // Barra de progreso estilizada
        val barY = kpiTop + kpiHeight + 12f
        val barRect = RectF(MARGIN, barY, PAGE_WIDTH - MARGIN, barY + 7f)
        paint.color = COLOR_CARD_BORDER
        canvas.drawRoundRect(barRect, 4f, 4f, paint)

        val fillW = (PAGE_WIDTH - (MARGIN * 2)) * (stats.progressPercent / 100f).coerceIn(0f, 1f)
        if (fillW > 0) {
            paint.color = COLOR_EMERALD
            val fillRect = RectF(MARGIN, barY, MARGIN + fillW, barY + 7f)
            canvas.drawRoundRect(fillRect, 4f, 4f, paint)
        }

        // 3. TABLA EDITORIAL DE ITINERARIO Y ETAPAS
        var tableY = barY + 22f
        val tableTitle = if (isTravel) "ITINERARIO Y CRONOGRAMA DE JORNADAS" else "ÍNDICE GENERAL DE ETAPAS DEL CAMINO"
        tableY = drawSectionBanner(canvas, tableY, tableTitle, COLOR_NAVY, if (isTravel) "🗺️" else "🧭")

        // Encabezado de la tabla
        val headerH = 20f
        val headerRect = RectF(MARGIN, tableY, PAGE_WIDTH - MARGIN, tableY + headerH)
        paint.color = COLOR_NAVY_LIGHT
        canvas.drawRoundRect(headerRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("JORNADA", MARGIN + 10f, tableY + 13.5f, paint)
        canvas.drawText("ITINERARIO (ORIGEN → DESTINO)", MARGIN + 68f, tableY + 13.5f, paint)
        canvas.drawText("MÉTRICAS ESTIMADAS", PAGE_WIDTH - MARGIN - 170f, tableY + 13.5f, paint)
        canvas.drawText("ESTADO", PAGE_WIDTH - MARGIN - 50f, tableY + 13.5f, paint)

        tableY += headerH + 2f

        // Filas de etapas
        val rowH = 22f
        for (i in stages.indices) {
            if (tableY + rowH > PAGE_HEIGHT - 45f) break
            val stg = stages[i]

            // Color de fondo alterno (Zebra)
            if (i % 2 == 1) {
                paint.color = COLOR_CARD_BG
                canvas.drawRect(MARGIN, tableY, PAGE_WIDTH - MARGIN, tableY + rowH, paint)
            }

            // Píldora de Día
            val dayBadgeRect = RectF(MARGIN + 8f, tableY + 3f, MARGIN + 48f, tableY + rowH - 3f)
            paint.color = COLOR_BLUE_BG
            canvas.drawRoundRect(dayBadgeRect, 4f, 4f, paint)
            paint.color = COLOR_NAVY
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val dayPrefix = if (isTravel) "Día" else "Et."
            canvas.drawText("$dayPrefix ${stg.dayNumber}", MARGIN + 14f, tableY + 14.5f, paint)

            // Origen → Destino
            paint.color = COLOR_SLATE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val routeText = "${stg.origin} → ${stg.destination}"
            val truncatedRoute = if (paint.measureText(routeText) > 200f) routeText.take(28) + "..." else routeText
            canvas.drawText(truncatedRoute, MARGIN + 68f, tableY + 14.5f, paint)

            // Métricas (Distancia, Desnivel, Tiempo)
            paint.color = COLOR_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            val metricsText = "${stg.distanceKm} km  |  +${stg.elevationGainM}m  |  ${stg.estimatedTime}"
            canvas.drawText(metricsText, PAGE_WIDTH - MARGIN - 170f, tableY + 14.5f, paint)

            // Estado (Completada vs Planificada)
            if (stg.isCompleted) {
                val badgeRect = RectF(PAGE_WIDTH - MARGIN - 54f, tableY + 3.5f, PAGE_WIDTH - MARGIN - 6f, tableY + rowH - 3.5f)
                paint.color = COLOR_EMERALD_BG
                canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
                paint.color = COLOR_EMERALD
                paint.textSize = 7.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("✓ HECHO", PAGE_WIDTH - MARGIN - 48f, tableY + 14f, paint)
            } else {
                val badgeRect = RectF(PAGE_WIDTH - MARGIN - 54f, tableY + 3.5f, PAGE_WIDTH - MARGIN - 6f, tableY + rowH - 3.5f)
                paint.color = COLOR_CARD_BORDER
                canvas.drawRoundRect(badgeRect, 3f, 3f, paint)
                paint.color = COLOR_MUTED
                paint.textSize = 7.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("PLAN", PAGE_WIDTH - MARGIN - 44f, tableY + 14f, paint)
            }

            // Divisor sutil
            paint.color = COLOR_DIVIDER
            canvas.drawLine(MARGIN, tableY + rowH, PAGE_WIDTH - MARGIN, tableY + rowH, paint)
            tableY += rowH
        }

        // Pie de página de la portada
        drawEditorialFooter(canvas, 1, isTravel, camino.title)
    }

    // ----------------------------------------------------
    // STAGE PAGES (ETAPAS / JORNADAS DETALLADAS)
    // ----------------------------------------------------
    private fun drawStagePages(
        context: Context,
        pdfDocument: PdfDocument,
        startPageNumber: Int,
        camino: CaminoEntity,
        stage: StageEntity,
        accommodations: List<AccommodationEntity>,
        repository: CaminoRepository
    ): Int {
        val isTravel = camino.isTravelGuide
        var currentPageNumber = startPageNumber
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawStageHeader(c: Canvas, isContinuation: Boolean = false, subtitleText: String = "") {
            // Fondo Azul Marino exacto #1C4B64
            paint.color = COLOR_HEADER_BG
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 56f, paint)

            // Franja Dorada Inferior
            paint.color = COLOR_GOLD
            c.drawRect(0f, 53f, PAGE_WIDTH.toFloat(), 56f, paint)

            // Marca y Título del Viaje en blanco puro para máximo contraste
            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val brandPrefix = if (isTravel) "ULTREIA VIAJES" else "ULTREIA"
            c.drawText("$brandPrefix  •  ${camino.title.uppercase()}", MARGIN, 22f, paint)

            // Título de la Jornada
            val stageDate = if (stage.date.isNotBlank()) stage.date
            else if (camino.startDate.isNotBlank()) {
                com.example.ui.settings.AppStrings.calculateStageDate(camino.startDate, stage.dayNumber - 1)
            } else ""

            paint.color = Color.WHITE
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val dateLabel = if (stageDate.isNotBlank()) "  •  $stageDate" else ""
            val contLabel = if (isContinuation) " (Continuación: $subtitleText)" else ""
            val dayWord = if (isTravel) "Día" else "Etapa"
            c.drawText("$dayWord ${stage.dayNumber}: ${stage.origin} a ${stage.destination}$dateLabel$contLabel", MARGIN, 42f, paint)

            // Badge de Estado Completada
            if (stage.isCompleted && !isContinuation) {
                paint.color = COLOR_EMERALD
                val badgeRect = RectF(PAGE_WIDTH - MARGIN - 96f, 15f, PAGE_WIDTH - MARGIN, 42f)
                c.drawRoundRect(badgeRect, 5f, 5f, paint)
                paint.color = Color.WHITE
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                c.drawText("✓ COMPLETADA", PAGE_WIDTH - MARGIN - 88f, 32f, paint)
            }
        }

        fun nextSubPage(continuationSubtitle: String) {
            drawEditorialFooter(canvas, currentPageNumber, isTravel, camino.title)
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawStageHeader(canvas, isContinuation = true, subtitleText = continuationSubtitle)
        }

        drawStageHeader(canvas)
        var currentY = 74f

        // 1. FICHA TÉCNICA (En Viajes solo si hay GPX; en Caminos siempre)
        val points = GpxParser.deserializePoints(stage.gpxTrackJson)
        val hasGpx = !stage.gpxTrackJson.isNullOrBlank() && points.isNotEmpty()
        val showFichaTecnica = !isTravel || hasGpx

        if (showFichaTecnica) {
            currentY = drawSectionBanner(canvas, currentY, "FICHA TÉCNICA Y PERFIL DE RUTA", COLOR_BLUE, "📊")

            // 4 Micro-tarjetas Métricas en Fila
            val cardH = 46f
            val cardSpacing = 6f
            val cardW = (PAGE_WIDTH - (MARGIN * 2) - (cardSpacing * 3)) / 4f

            // Micro 1: Distancia
            drawMetricBox(
                canvas = canvas,
                x = MARGIN,
                y = currentY,
                w = cardW,
                h = cardH,
                label = "DISTANCIA",
                value = "${stage.distanceKm} km",
                accentColor = COLOR_EMERALD,
                bgColor = COLOR_EMERALD_BG,
                borderColor = COLOR_EMERALD_BORDER
            )

            // Micro 2: Desnivel (+/-)
            drawMetricBox(
                canvas = canvas,
                x = MARGIN + cardW + cardSpacing,
                y = currentY,
                w = cardW,
                h = cardH,
                label = "DESNIVEL",
                value = "+${stage.elevationGainM} / -${stage.elevationLossM}m",
                accentColor = COLOR_AMBER,
                bgColor = COLOR_AMBER_BG,
                borderColor = COLOR_AMBER_BORDER
            )

            // Micro 3: Tiempo Estimado
            drawMetricBox(
                canvas = canvas,
                x = MARGIN + (cardW + cardSpacing) * 2,
                y = currentY,
                w = cardW,
                h = cardH,
                label = "TIEMPO EST.",
                value = stage.estimatedTime.ifBlank { "—" },
                accentColor = COLOR_BLUE,
                bgColor = COLOR_BLUE_BG,
                borderColor = COLOR_BLUE_BORDER
            )

            // Micro 4: Dificultad
            val diffColor = when (stage.difficulty.lowercase()) {
                "fácil", "facil" -> COLOR_EMERALD
                "moderada" -> COLOR_BLUE
                "difícil", "dificil" -> COLOR_AMBER
                else -> Color.rgb(220, 38, 38)
            }
            drawMetricBox(
                canvas = canvas,
                x = MARGIN + (cardW + cardSpacing) * 3,
                y = currentY,
                w = cardW,
                h = cardH,
                label = "DIFICULTAD",
                value = stage.difficulty.ifBlank { "Moderada" },
                accentColor = diffColor,
                bgColor = COLOR_CARD_BG,
                borderColor = COLOR_CARD_BORDER
            )

            currentY += cardH + 12f

            // Tarjeta de Mapa / Perfil GPX Cartográfico
            val mapBoxH = 100f
            val chartBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + mapBoxH)
            drawCardBox(canvas, chartBox.left, chartBox.top, chartBox.width(), chartBox.height(), COLOR_CARD_BG, COLOR_CARD_BORDER, COLOR_NAVY, 4f, 8f)

            if (points.isNotEmpty()) {
                drawMiniTrackOnCanvas(canvas, points, chartBox)
            } else {
                paint.color = COLOR_NAVY
                paint.strokeWidth = 2.5f
                val path = Path()
                path.moveTo(chartBox.left + 24f, chartBox.bottom - 25f)
                path.cubicTo(
                    chartBox.left + 150f, chartBox.top + 30f,
                    chartBox.left + 300f, chartBox.bottom - 40f,
                    chartBox.right - 24f, chartBox.bottom - 20f
                )
                paint.style = Paint.Style.STROKE
                canvas.drawPath(path, paint)
                paint.style = Paint.Style.FILL

                // Puntos de inicio y fin decorativos
                paint.color = COLOR_EMERALD
                canvas.drawCircle(chartBox.left + 24f, chartBox.bottom - 25f, 5f, paint)
                paint.color = COLOR_GOLD
                canvas.drawCircle(chartBox.right - 24f, chartBox.bottom - 20f, 5f, paint)

                paint.color = COLOR_MUTED
                paint.textSize = 8.5f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Origen: ${stage.origin}", chartBox.left + 35f, chartBox.bottom - 22f, paint)
                val destText = "Destino: ${stage.destination}"
                val destW = paint.measureText(destText)
                canvas.drawText(destText, chartBox.right - 35f - destW, chartBox.bottom - 17f, paint)
            }

            currentY += mapBoxH + 16f
        }

        // 2. CONSEJOS DEL DÍA (Caminos) O RESUMEN EJECUTIVO (Viajes)
        if (stage.stageNotes.isNotBlank()) {
            val contentH = measureMultilineTextHeight(stage.stageNotes, PAGE_WIDTH - 2 * MARGIN - 28f, paint.apply { textSize = 9.5f })
            val cardH = contentH + 24f

            if (currentY + cardH + 30f > PAGE_HEIGHT - MARGIN - 35f) {
                nextSubPage(if (isTravel) "Resumen" else "Consejos")
                currentY = 74f
            }

            val secTitle = if (isTravel) "RESUMEN EJECUTIVO DE LA JORNADA" else "INFORMACIÓN Y CONSEJOS CLAVE DEL DÍA"
            val secAccent = if (isTravel) COLOR_NAVY else COLOR_AMBER
            val secIcon = if (isTravel) "📝" else "💡"
            currentY = drawSectionBanner(canvas, currentY, secTitle, secAccent, secIcon)

            val noteBg = if (isTravel) COLOR_CARD_BG else COLOR_AMBER_BG
            val noteBorder = if (isTravel) COLOR_CARD_BORDER else COLOR_AMBER_BORDER
            drawCardBox(canvas, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, cardH, noteBg, noteBorder, secAccent, 4f, 8f)

            paint.color = COLOR_SLATE
            paint.textSize = 9.5f
            paint.typeface = Typeface.DEFAULT
            drawMultilineText(canvas, stage.stageNotes, MARGIN + 14f, currentY + 16f, PAGE_WIDTH - 2 * MARGIN - 28f, paint)

            currentY += cardH + 16f
        }

        // 3. AGENDA DEL DÍA (Exclusivo Perfil Viajes)
        if (isTravel) {
            val agendaList = parseAgendaItems(stage.agendaJson)
            if (agendaList.isNotEmpty()) {
                if (currentY + 50f > PAGE_HEIGHT - MARGIN - 35f) {
                    nextSubPage("Agenda")
                    currentY = 74f
                }

                currentY = drawSectionBanner(canvas, currentY, "AGENDA Y ACTIVIDADES PROGRAMADAS", COLOR_BLUE, "🗓️")

                for (event in agendaList) {
                    val (imageFiles, docFiles) = event.files.partition { isImageAttachedFile(context, it) }
                    val descHeight = if (event.description.isNotBlank()) {
                        measureMultilineTextHeight(event.description, PAGE_WIDTH - 2 * MARGIN - 28f, paint.apply { textSize = 9f })
                    } else 0f
                    val docsHeight = docFiles.size * 28f
                    val eventBaseH = 34f + descHeight + docsHeight

                    if (currentY + eventBaseH > PAGE_HEIGHT - MARGIN - 35f) {
                        nextSubPage("Agenda")
                        currentY = 74f
                    }

                    // Tarjeta de Actividad de Agenda
                    val eventCardTop = currentY
                    drawCardBox(canvas, MARGIN, eventCardTop, PAGE_WIDTH - 2 * MARGIN, eventBaseH, Color.WHITE, COLOR_CARD_BORDER, COLOR_BLUE, 4f, 6f)

                    // Píldora de Horario
                    val timePillRect = RectF(MARGIN + 12f, eventCardTop + 8f, MARGIN + 68f, eventCardTop + 26f)
                    paint.color = COLOR_NAVY
                    canvas.drawRoundRect(timePillRect, 4f, 4f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("⏰ ${event.time}", MARGIN + 18f, eventCardTop + 20.5f, paint)

                    // Título de la Actividad
                    paint.color = COLOR_SLATE
                    paint.textSize = 10.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(event.title, MARGIN + 76f, eventCardTop + 20.5f, paint)

                    var innerY = eventCardTop + 36f

                    // Descripción
                    if (event.description.isNotBlank()) {
                        paint.color = Color.rgb(55, 65, 81)
                        paint.textSize = 9f
                        paint.typeface = Typeface.DEFAULT
                        innerY = drawMultilineText(canvas, event.description, MARGIN + 14f, innerY, PAGE_WIDTH - 2 * MARGIN - 28f, paint)
                        innerY += 4f
                    }

                    // A. Documentos no-imagen en píldoras estilizadas
                    for (doc in docFiles) {
                        val docName = doc.name.ifBlank { extractFileName(doc.uri) }
                        innerY = drawDocumentChip(canvas, MARGIN + 14f, innerY, PAGE_WIDTH - 2 * MARGIN - 28f, docName)
                    }

                    currentY = eventCardTop + eventBaseH + 8f

                    // B. Imágenes adjuntas adaptadas en tarjeta enmarcada
                    for (photo in imageFiles) {
                        val neededH = estimateImageHeight(context, photo.uri, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                        if (currentY + neededH + 12f > PAGE_HEIGHT - MARGIN - 35f) {
                            nextSubPage("Agenda - Fotos")
                            currentY = 74f
                        }
                        val drawnH = drawImageFromUri(context, canvas, photo.uri, MARGIN + 14f, currentY, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                        if (drawnH > 0f) {
                            currentY += drawnH + 8f
                        }
                    }
                    currentY += 4f
                }
                currentY += 8f
            }
        }

        // 4. ALOJAMIENTOS Y HOSPEDAJE
        if (accommodations.isNotEmpty() || !isTravel) {
            if (currentY + 50f > PAGE_HEIGHT - MARGIN - 35f) {
                nextSubPage("Alojamientos")
                currentY = 74f
            }

            val accTitle = if (isTravel) "HOTELES Y ALOJAMIENTOS SELECCIONADOS" else "ALOJAMIENTOS Y OPCIONES DE HOSPEDAJE"
            currentY = drawSectionBanner(canvas, currentY, accTitle, COLOR_EMERALD, "🏨")

            if (accommodations.isEmpty()) {
                val emptyH = 34f
                drawCardBox(canvas, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, emptyH, COLOR_CARD_BG, COLOR_CARD_BORDER, null, 0f, 6f)
                paint.color = COLOR_MUTED
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("No se registraron alojamientos específicos para esta jornada.", MARGIN + 14f, currentY + 21f, paint)
                currentY += emptyH + 12f
            } else {
                for (acc in accommodations) {
                    val roomOptions = repository.parseRoomOptions(acc.roomOptionsJson)
                    val baseH = 26f + (if (acc.address.isNotBlank()) 14f else 0f) + (if (roomOptions.isNotEmpty()) 18f else 0f) + 8f

                    if (currentY + baseH > PAGE_HEIGHT - MARGIN - 35f) {
                        nextSubPage("Alojamientos")
                        currentY = 74f
                    }

                    drawCardBox(canvas, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, baseH, COLOR_CARD_BG, COLOR_CARD_BORDER, COLOR_EMERALD, 4f, 6f)

                    // Nombre y Teléfono
                    paint.color = COLOR_SLATE
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val phoneTag = if (acc.phone.isNotBlank()) "  •  📞 ${acc.phone}" else ""
                    canvas.drawText("${acc.name}$phoneTag", MARGIN + 14f, currentY + 18f, paint)

                    // Badge de reserva si aplica
                    if (acc.isBooked) {
                        val bookRect = RectF(PAGE_WIDTH - MARGIN - 72f, currentY + 6f, PAGE_WIDTH - MARGIN - 10f, currentY + 22f)
                        paint.color = COLOR_EMERALD_BG
                        canvas.drawRoundRect(bookRect, 3f, 3f, paint)
                        paint.color = COLOR_EMERALD
                        paint.textSize = 7.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("RESERVADO", PAGE_WIDTH - MARGIN - 66f, currentY + 17f, paint)
                    }

                    var innerAccY = currentY + 30f

                    // Dirección
                    if (acc.address.isNotBlank()) {
                        paint.color = COLOR_MUTED
                        paint.textSize = 8.5f
                        paint.typeface = Typeface.DEFAULT
                        canvas.drawText("📍 ${acc.address}", MARGIN + 14f, innerAccY, paint)
                        innerAccY += 14f
                    }

                    // Píldoras de Precios por Habitación
                    if (roomOptions.isNotEmpty()) {
                        var chipX = MARGIN + 14f
                        for (opt in roomOptions) {
                            val pillText = "${opt.roomType}: ${opt.priceEur}€"
                            paint.textSize = 8f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            val pillW = paint.measureText(pillText) + 12f
                            val pillRect = RectF(chipX, innerAccY - 9f, chipX + pillW, innerAccY + 7f)
                            paint.color = COLOR_EMERALD_BG
                            canvas.drawRoundRect(pillRect, 3f, 3f, paint)
                            paint.color = COLOR_EMERALD
                            canvas.drawText(pillText, chipX + 6f, innerAccY + 3f, paint)
                            chipX += pillW + 6f
                        }
                    }

                    currentY += baseH + 8f
                }
                currentY += 6f
            }
        }

        // 5. VIVENCIAS / DIARIO PERSONAL (Estilo Bitácora de Viajero)
        val vivenciasList = parseVivencias(stage.vivenciasJson, stage.diaryNotes, stage.diaryPhotoUri)
        if (vivenciasList.isNotEmpty() || !isTravel) {
            if (currentY + 50f > PAGE_HEIGHT - MARGIN - 35f) {
                nextSubPage(if (isTravel) "Notas" else "Vivencias")
                currentY = 74f
            }

            val vivTitle = if (isTravel) "CUADERNO DE BITÁCORA & NOTAS PERSONALES" else "VIVENCIAS & BITÁCORA DEL PEREGRINO"
            currentY = drawSectionBanner(canvas, currentY, vivTitle, COLOR_GOLD_DARK, "📖")

            if (vivenciasList.isEmpty()) {
                val emptyH = 34f
                drawCardBox(canvas, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, emptyH, COLOR_WARM_BG, COLOR_WARM_BORDER, null, 0f, 6f)
                paint.color = Color.rgb(130, 115, 90)
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText("Sin notas de vivencias registradas aún para este día.", MARGIN + 14f, currentY + 21f, paint)
                currentY += emptyH + 12f
            } else {
                for (vivencia in vivenciasList) {
                    val textH = if (vivencia.note.isNotBlank()) {
                        measureMultilineTextHeight(vivencia.note, PAGE_WIDTH - 2 * MARGIN - 28f, paint.apply { textSize = 9.5f })
                    } else 0f
                    val dateH = if (vivencia.dateStr.isNotBlank()) 16f else 0f
                    val isDoc = !vivencia.photoUri.isNullOrBlank() && !checkIsImage(context, null, extractFileName(vivencia.photoUri), vivencia.photoUri)
                    val docChipH = if (isDoc) 28f else 0f
                    val vivCardH = textH + dateH + docChipH + 20f

                    if (currentY + vivCardH > PAGE_HEIGHT - MARGIN - 35f) {
                        nextSubPage(if (isTravel) "Notas" else "Vivencias")
                        currentY = 74f
                    }

                    val vivCardTop = currentY
                    drawCardBox(canvas, MARGIN, vivCardTop, PAGE_WIDTH - 2 * MARGIN, vivCardH, COLOR_WARM_BG, COLOR_WARM_BORDER, COLOR_GOLD, 4f, 6f)

                    var innerY = vivCardTop + 14f

                    // Fecha si existe
                    if (vivencia.dateStr.isNotBlank()) {
                        paint.color = COLOR_GOLD_DARK
                        paint.textSize = 8.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText("📅 ${vivencia.dateStr}", MARGIN + 14f, innerY, paint)
                        innerY += 14f
                    }

                    // Texto de la vivencia
                    if (vivencia.note.isNotBlank()) {
                        paint.color = COLOR_SLATE
                        paint.textSize = 9.5f
                        paint.typeface = Typeface.DEFAULT
                        innerY = drawMultilineText(canvas, vivencia.note, MARGIN + 14f, innerY, PAGE_WIDTH - 2 * MARGIN - 28f, paint)
                        innerY += 4f
                    }

                    // Archivo no-imagen adjunto en la vivencia
                    if (isDoc) {
                        val fileName = extractFileName(vivencia.photoUri)
                        drawDocumentChip(canvas, MARGIN + 14f, innerY, PAGE_WIDTH - 2 * MARGIN - 28f, fileName)
                    }

                    currentY = vivCardTop + vivCardH + 8f

                    // Foto adjunta a la vivencia
                    if (!vivencia.photoUri.isNullOrBlank() && !isDoc) {
                        val neededH = estimateImageHeight(context, vivencia.photoUri, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                        if (currentY + neededH + 12f > PAGE_HEIGHT - MARGIN - 35f) {
                            nextSubPage(if (isTravel) "Notas - Fotos" else "Vivencias - Fotos")
                            currentY = 74f
                        }
                        val drawnH = drawImageFromUri(context, canvas, vivencia.photoUri, MARGIN + 14f, currentY, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                        if (drawnH > 0f) {
                            currentY += drawnH + 8f
                        }
                    }
                    currentY += 4f
                }
                currentY += 6f
            }
        }

        // 6. ARCHIVOS Y DOCUMENTOS ADJUNTOS GENERALES DE LA ETAPA
        val stageAttachedFiles = parseAttachedFiles(stage.attachedFilesJson)
        if (stageAttachedFiles.isNotEmpty()) {
            if (currentY + 50f > PAGE_HEIGHT - MARGIN - 35f) {
                nextSubPage("Archivos")
                currentY = 74f
            }

            currentY = drawSectionBanner(canvas, currentY, "DOCUMENTOS Y ARCHIVOS DE LA ETAPA", COLOR_SLATE, "📁")
            val (stageImages, stageDocs) = stageAttachedFiles.partition { isImageAttachedFile(context, it) }

            // Documentos no-imagen
            for (doc in stageDocs) {
                if (currentY + 28f > PAGE_HEIGHT - MARGIN - 35f) {
                    nextSubPage("Archivos")
                    currentY = 74f
                }
                val docName = doc.name.ifBlank { extractFileName(doc.uri) }
                currentY = drawDocumentChip(canvas, MARGIN + 14f, currentY, PAGE_WIDTH - 2 * MARGIN - 28f, docName)
            }

            // Imágenes adjuntas
            for (img in stageImages) {
                val neededH = estimateImageHeight(context, img.uri, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                if (currentY + neededH + 12f > PAGE_HEIGHT - MARGIN - 35f) {
                    nextSubPage("Archivos - Fotos")
                    currentY = 74f
                }
                val drawnH = drawImageFromUri(context, canvas, img.uri, MARGIN + 14f, currentY, PAGE_WIDTH - 2 * MARGIN - 28f, 220f)
                if (drawnH > 0f) {
                    currentY += drawnH + 8f
                }
            }
            currentY += 6f
        }

        drawEditorialFooter(canvas, currentPageNumber, isTravel, camino.title)
        pdfDocument.finishPage(page)
        return currentPageNumber + 1
    }

    // ----------------------------------------------------
    // INFORME GLOBAL DE ESTADÍSTICAS
    // ----------------------------------------------------
    private fun drawStatsPage(
        context: Context,
        canvas: Canvas,
        isTotal: Boolean,
        camino: CaminoEntity?,
        stats: com.example.data.repository.CaminoStats,
        stages: List<StageEntity>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val isTravel = camino?.isTravelGuide == true

        // Cabecera Azul Marino exacto #1C4B64
        val bannerH = 175f
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), bannerH, paint)

        // Doble Franja Dorada
        paint.color = COLOR_GOLD
        canvas.drawRect(0f, bannerH - 5f, PAGE_WIDTH.toFloat(), bannerH - 2f, paint)
        paint.color = COLOR_GOLD_DARK
        canvas.drawRect(0f, bannerH - 1.5f, PAGE_WIDTH.toFloat(), bannerH, paint)

        // Logo o emblema de alta calidad sin pixelar
        val statsIcon = loadTitleIconBitmap(context)
        var textLeft = MARGIN
        if (statsIcon != null) {
            val iconH = 56f
            val scale = iconH / statsIcon.height.toFloat()
            val iconW = statsIcon.width * scale
            val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }
            val destRect = RectF(MARGIN, 36f, MARGIN + iconW, 36f + iconH)
            canvas.drawBitmap(statsIcon, null, destRect, iconPaint)
            textLeft = MARGIN + iconW + 14f
        }

        // Títulos de Cabecera en blanco puro para máximo contraste
        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brandPrefix = if (isTravel) "ULTREIA VIAJES" else "ULTREIA"
        canvas.drawText("$brandPrefix  •  INFORME GENERAL DE RENDIMIENTO", textLeft, 48f, paint)

        paint.color = Color.WHITE
        paint.textSize = 19f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val subjectTitle = if (isTotal) {
            if (isTravel) "Balance Acumulado de Viajes" else "Balance Total de Caminos Realizados"
        } else {
            camino?.title ?: "Camino de Santiago"
        }
        canvas.drawText(subjectTitle, textLeft, 76f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.WHITE
        val dateGenerated = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        canvas.drawText("Documento emitido y validado el: $dateGenerated", textLeft, 98f, paint)

        // 4 Tarjetas KPI Principales
        var startY = 198f
        val cardSpacing = 12f
        val cardWidth = (PAGE_WIDTH - (MARGIN * 2) - cardSpacing) / 2f
        val cardHeight = 78f

        // Card 1: Etapas (Azul)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN,
            y = startY,
            w = cardWidth,
            h = cardHeight,
            label = if (isTravel) "JORNADAS COMPLETADAS" else "ETAPAS COMPLETADAS",
            value = "${stats.completedStages} / ${stats.totalStages}",
            sub = "${stats.totalStages - stats.completedStages} pendientes para culminar",
            accentColor = COLOR_BLUE,
            bgColor = COLOR_BLUE_BG,
            borderColor = COLOR_BLUE_BORDER
        )

        // Card 2: Distancia (Verde Esmeralda)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN + cardWidth + cardSpacing,
            y = startY,
            w = cardWidth,
            h = cardHeight,
            label = "DISTANCIA RECORRIDA",
            value = "${String.format(java.util.Locale.US, "%.1f", stats.completedKm)} km",
            sub = "de ${String.format(java.util.Locale.US, "%.1f", stats.totalKm)} km totales proyectados",
            accentColor = COLOR_EMERALD,
            bgColor = COLOR_EMERALD_BG,
            borderColor = COLOR_EMERALD_BORDER
        )

        startY += cardHeight + 12f

        // Card 3: Desnivel (Ámbar)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN,
            y = startY,
            w = cardWidth,
            h = cardHeight,
            label = "DESNIVEL POSITIVO SUPERADO",
            value = "+${stats.completedElevationGainM} m",
            sub = "de +${stats.totalElevationGainM} m de desnivel total",
            accentColor = COLOR_AMBER,
            bgColor = COLOR_AMBER_BG,
            borderColor = COLOR_AMBER_BORDER
        )

        // Card 4: Progreso Global (Dorado)
        drawMetricBox(
            canvas = canvas,
            x = MARGIN + cardWidth + cardSpacing,
            y = startY,
            w = cardWidth,
            h = cardHeight,
            label = "PROGRESO Y CUMPLIMIENTO",
            value = "${stats.progressPercent}%",
            sub = if (stats.progressPercent >= 100) "¡Ruta culminada con éxito!" else "Itinerario en curso",
            accentColor = COLOR_GOLD_DARK,
            bgColor = COLOR_WARM_BG,
            borderColor = COLOR_WARM_BORDER
        )

        // Barra visual de progreso
        startY += cardHeight + 16f
        val barRect = RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + 8f)
        paint.color = COLOR_CARD_BORDER
        canvas.drawRoundRect(barRect, 4f, 4f, paint)

        val fillW = (PAGE_WIDTH - (MARGIN * 2)) * (stats.progressPercent / 100f).coerceIn(0f, 1f)
        if (fillW > 0) {
            paint.color = COLOR_EMERALD
            val fillRect = RectF(MARGIN, startY, MARGIN + fillW, startY + 8f)
            canvas.drawRoundRect(fillRect, 4f, 4f, paint)
        }

        // Sección de Desglose
        startY += 26f
        startY = drawSectionBanner(canvas, startY, if (isTotal) "MÉTRICAS CONSOLIDADAS DE RENDIMIENTO" else "DETALLE DE JORNADAS REALIZADAS", COLOR_NAVY, "📈")

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_SLATE

        if (stages.isNotEmpty() && !isTotal) {
            for (st in stages.take(14)) {
                val statusBadge = if (st.isCompleted) "✓ Completada" else "⏳ Planificada"
                val lineText = "Día ${st.dayNumber}: ${st.origin} → ${st.destination}  |  ${st.distanceKm} km  |  +${st.elevationGainM}m  |  $statusBadge"
                canvas.drawText(lineText, MARGIN + 8f, startY, paint)
                startY += 18f
            }
        } else {
            val avgKm = if (stats.completedStages > 0) (stats.completedKm / stats.completedStages) else 0.0
            canvas.drawText("• Promedio de distancia por etapa completada: ${String.format(java.util.Locale.US, "%.1f", avgKm)} km", MARGIN + 8f, startY, paint)
            startY += 18f
            val remainingKm = (stats.totalKm - stats.completedKm).coerceAtLeast(0.0)
            canvas.drawText("• Distancia pendiente por recorrer: ${String.format(java.util.Locale.US, "%.1f", remainingKm)} km", MARGIN + 8f, startY, paint)
            startY += 18f
            val avgElev = if (stats.completedStages > 0) stats.completedElevationGainM / stats.completedStages else 0
            canvas.drawText("• Desnivel medio superado por jornada: +$avgElev metros", MARGIN + 8f, startY, paint)
            startY += 18f
            canvas.drawText("• Cumplimiento del plan de viaje: ${stats.completedStages} jornadas efectivas registradas en Ultreia", MARGIN + 8f, startY, paint)
        }

        // Cajetín de Certificación y Sello
        val stampBox = RectF(PAGE_WIDTH - MARGIN - 160f, PAGE_HEIGHT - 125f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 45f)
        paint.color = COLOR_WARM_BG
        canvas.drawRoundRect(stampBox, 8f, 8f, paint)
        paint.color = COLOR_WARM_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(stampBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = COLOR_GOLD_DARK
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SELLO Y VALIDACIÓN", PAGE_WIDTH - MARGIN - 146f, PAGE_HEIGHT - 100f, paint)
        paint.color = COLOR_MUTED
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        val certSub = if (isTravel) "CUADERNO DE VIAJE" else "CREDENCIAL DEL PEREGRINO"
        canvas.drawText(certSub, PAGE_WIDTH - MARGIN - 146f, PAGE_HEIGHT - 86f, paint)
        canvas.drawText("ULTREIA ET SUSEIA", PAGE_WIDTH - MARGIN - 146f, PAGE_HEIGHT - 74f, paint)

        // Footer
        drawEditorialFooter(canvas, 1, isTravel, camino?.title ?: "")
    }

    // ----------------------------------------------------
    // COMPONENTES EDITORIALES COMUNES
    // ----------------------------------------------------
    private fun drawSectionBanner(
        canvas: Canvas,
        y: Float,
        title: String,
        accentColor: Int,
        iconLabel: String = ""
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Píldora de color del acento
        paint.color = accentColor
        canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + 4f, y + 15f), 2f, 2f, paint)

        // Título de la sección
        paint.color = COLOR_SLATE
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val text = if (iconLabel.isNotBlank()) "$iconLabel  $title" else title
        canvas.drawText(text, MARGIN + 10f, y + 12f, paint)

        // Línea divisoria elegante
        val textWidth = paint.measureText(text)
        paint.color = COLOR_CARD_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN + 16f + textWidth, y + 8f, PAGE_WIDTH - MARGIN, y + 8f, paint)

        return y + 22f
    }

    private fun drawCardBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        bgColor: Int = COLOR_CARD_BG,
        borderColor: Int = COLOR_CARD_BORDER,
        accentColor: Int? = null,
        accentWidth: Float = 4f,
        cornerRadius: Float = 8f
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(x, y, x + w, y + h)

        // Fondo de la tarjeta
        paint.color = bgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        // Borde
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        // Franja lateral de acento de color
        if (accentColor != null) {
            paint.style = Paint.Style.FILL
            paint.color = accentColor
            val accentRect = RectF(x, y, x + accentWidth, y + h)
            canvas.drawRoundRect(accentRect, 2f, 2f, paint)
        }
    }

    private fun drawMetricBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        label: String,
        value: String,
        sub: String = "",
        accentColor: Int,
        bgColor: Int = COLOR_CARD_BG,
        borderColor: Int = COLOR_CARD_BORDER
    ) {
        drawCardBox(canvas, x, y, w, h, bgColor, borderColor, accentColor, accentWidth = 4f, cornerRadius = 8f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Etiqueta
        paint.color = COLOR_MUTED
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label.uppercase(), x + 10f, y + 16f, paint)

        // Valor principal
        paint.color = COLOR_SLATE
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 10f, y + 34f, paint)

        // Subtítulo
        if (sub.isNotBlank()) {
            paint.color = COLOR_MUTED
            paint.textSize = 7.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(sub, x + 10f, y + 46f, paint)
        }
    }

    private fun drawDocumentChip(
        canvas: Canvas,
        x: Float,
        y: Float,
        maxWidth: Float,
        fileName: String
    ): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val chipH = 22f
        val text = "📎  Archivo adjunto: $fileName"
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT

        val textWidth = paint.measureText(text)
        val chipW = minOf(textWidth + 20f, maxWidth)
        val chipRect = RectF(x, y, x + chipW, y + chipH)

        // Fondo y borde
        paint.color = COLOR_BLUE_BG
        canvas.drawRoundRect(chipRect, 4f, 4f, paint)
        paint.color = COLOR_BLUE_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(chipRect, 4f, 4f, paint)

        // Texto
        paint.style = Paint.Style.FILL
        paint.color = COLOR_SLATE
        val ellipText = if (textWidth > chipW - 14f) text.take(38) + "..." else text
        canvas.drawText(ellipText, x + 8f, y + 14.5f, paint)

        return y + chipH + 6f
    }

    private fun drawEditorialFooter(canvas: Canvas, pageNumber: Int, isTravel: Boolean, caminoTitle: String = "") {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Línea divisoria inferior
        paint.color = COLOR_CARD_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 34f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 34f, paint)

        // Marca de la guía (solo el nombre del Camino sin Ultreia Memorias)
        paint.color = COLOR_MUTED
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        val guideBrand = if (caminoTitle.isNotBlank()) caminoTitle else if (isTravel) "Guía e Itinerario de Viaje" else "Guía del Camino de Santiago"
        canvas.drawText(guideBrand, MARGIN, PAGE_HEIGHT - 20f, paint)

        // Píldora de número de página
        val pageText = "Página $pageNumber"
        val pageW = paint.measureText(pageText)
        val pillRect = RectF(PAGE_WIDTH - MARGIN - pageW - 12f, PAGE_HEIGHT - 29f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 13f)
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(pillRect, 4f, 4f, paint)
        paint.color = COLOR_CARD_BORDER
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(pillRect, 4f, 4f, paint)

        paint.style = Paint.Style.FILL
        paint.color = COLOR_SLATE
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN - pageW - 6f, PAGE_HEIGHT - 19.5f, paint)
    }

    // ----------------------------------------------------
    // DETECCIÓN DE ARCHIVOS (IMÁGENES VS DOCUMENTOS)
    // ----------------------------------------------------
    private fun checkIsImage(
        context: Context,
        mimeType: String?,
        fileName: String?,
        uriStr: String?
    ): Boolean {
        val mime = (mimeType ?: "").lowercase().trim()
        val name = (fileName ?: "").lowercase().trim()
        val uri = (uriStr ?: "").lowercase().trim()

        if (mime.startsWith("image/")) return true

        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif")
        if (imageExtensions.any { name.endsWith(it) || uri.endsWith(it) || uri.contains("$it?") || uri.contains(it) }) {
            return true
        }

        if (!uriStr.isNullOrBlank()) {
            try {
                val parsedUri = Uri.parse(uriStr)
                val resolvedMime = context.contentResolver.getType(parsedUri)
                if (!resolvedMime.isNullOrBlank() && resolvedMime.startsWith("image/")) {
                    return true
                }
            } catch (_: Exception) {}
        }

        return false
    }

    private fun isImageAttachedFile(context: Context, file: AttachedFile): Boolean {
        return checkIsImage(context, file.mimeType, file.name, file.uri)
    }

    private fun extractFileName(uriOrPath: String?): String {
        if (uriOrPath.isNullOrBlank()) return "archivo"
        return try {
            val uri = Uri.parse(uriOrPath)
            val path = uri.path ?: uriOrPath
            val name = path.substringAfterLast('/')
            if (name.isNotBlank()) name else "archivo"
        } catch (_: Exception) {
            "archivo"
        }
    }

    private fun parseAgendaItems(json: String?): List<AgendaItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = org.json.JSONArray(json)
            val res = mutableListOf<AgendaItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val filesArray = obj.optJSONArray("files") ?: org.json.JSONArray()
                val filesList = mutableListOf<AttachedFile>()
                for (j in 0 until filesArray.length()) {
                    val fObj = filesArray.getJSONObject(j)
                    filesList.add(
                        AttachedFile(
                            id = fObj.optString("id", java.util.UUID.randomUUID().toString()),
                            name = fObj.optString("name", "Archivo"),
                            uri = fObj.optString("uri", ""),
                            mimeType = fObj.optString("mimeType", "")
                        )
                    )
                }
                res.add(
                    AgendaItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        time = obj.optString("time", "08:00"),
                        title = obj.optString("title", "Actividad"),
                        description = obj.optString("description", ""),
                        files = filesList,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            res.sortedBy { it.time }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseVivencias(json: String?, fallbackNotes: String = "", fallbackPhotoUri: String? = null): List<VivenciaItem> {
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

    private fun parseAttachedFiles(json: String?): List<AttachedFile> {
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

    private fun openInputStreamForUri(context: Context, uri: Uri): java.io.InputStream? {
        return try {
            if (uri.scheme == "file" || uri.scheme == null) {
                val file = File(uri.path ?: uri.toString())
                if (file.exists()) file.inputStream() else context.contentResolver.openInputStream(uri)
            } else {
                context.contentResolver.openInputStream(uri)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun estimateImageHeight(
        context: Context,
        uriStr: String,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        if (uriStr.isBlank()) return 0f
        return try {
            val uri = Uri.parse(uriStr)
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInputStreamForUri(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            } ?: return 0f

            val origW = boundsOpts.outWidth
            val origH = boundsOpts.outHeight
            if (origW <= 0 || origH <= 0) return 0f

            val scale = minOf(maxWidth / origW.toFloat(), maxHeight / origH.toFloat(), 1.0f)
            (origH * scale) + 12f
        } catch (_: Exception) {
            0f
        }
    }

    private fun drawImageFromUri(
        context: Context,
        canvas: Canvas,
        uriStr: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        if (uriStr.isBlank()) return 0f
        return try {
            val uri = Uri.parse(uriStr)
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInputStreamForUri(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            } ?: return 0f

            val origW = boundsOpts.outWidth
            val origH = boundsOpts.outHeight
            if (origW <= 0 || origH <= 0) return 0f

            var sampleSize = 1
            while (origW / (sampleSize * 2) >= maxWidth && origH / (sampleSize * 2) >= maxHeight) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = openInputStreamForUri(context, uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return 0f

            val scale = minOf(maxWidth / bitmap.width.toFloat(), maxHeight / bitmap.height.toFloat(), 1.0f)
            val drawW = bitmap.width * scale
            val drawH = bitmap.height * scale

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val frameRect = RectF(x, y, x + drawW + 6f, y + drawH + 6f)
            paint.color = COLOR_CARD_BG
            canvas.drawRoundRect(frameRect, 6f, 6f, paint)

            paint.color = COLOR_CARD_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(frameRect, 6f, 6f, paint)

            paint.style = Paint.Style.FILL
            val imageRect = RectF(x + 3f, y + 3f, x + 3f + drawW, y + 3f + drawH)
            canvas.drawBitmap(bitmap, null, imageRect, paint)

            bitmap.recycle()
            drawH + 12f
        } catch (_: Exception) {
            0f
        }
    }

    private fun drawMiniTrackOnCanvas(canvas: Canvas, points: List<com.example.data.model.GpxPoint>, bounds: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val minLat = points.minOf { it.lat }
        val maxLat = points.maxOf { it.lat }
        val minLon = points.minOf { it.lon }
        val maxLon = points.maxOf { it.lon }

        val latSpan = if (maxLat - minLat > 0.0001) maxLat - minLat else 0.01
        val lonSpan = if (maxLon - minLon > 0.0001) maxLon - minLon else 0.01

        val padding = 16f
        val w = bounds.width() - 2 * padding
        val h = bounds.height() - 2 * padding

        val path = Path()
        for (i in points.indices) {
            val p = points[i]
            val x = bounds.left + padding + ((p.lon - minLon) / lonSpan * w).toFloat()
            val y = bounds.bottom - padding - ((p.lat - minLat) / latSpan * h).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Línea del recorrido (Azul Zafiro de Guía)
        paint.color = COLOR_BLUE
        paint.strokeWidth = 2.8f
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        // Punto de Salida / Origen (Verde Esmeralda)
        val pStart = points.first()
        val xStart = bounds.left + padding + ((pStart.lon - minLon) / lonSpan * w).toFloat()
        val yStart = bounds.bottom - padding - ((pStart.lat - minLat) / latSpan * h).toFloat()
        paint.color = COLOR_EMERALD
        canvas.drawCircle(xStart, yStart, 4.5f, paint)

        // Punto de Llegada / Destino (Dorado)
        val pEnd = points.last()
        val xEnd = bounds.left + padding + ((pEnd.lon - minLon) / lonSpan * w).toFloat()
        val yEnd = bounds.bottom - padding - ((pEnd.lat - minLat) / latSpan * h).toFloat()
        paint.color = COLOR_GOLD
        canvas.drawCircle(xEnd, yEnd, 4.5f, paint)

        // Etiquetas
        paint.textSize = 8f
        paint.color = COLOR_MUTED
        canvas.drawText("Inicio", xStart - 8f, yStart - 7f, paint)
        canvas.drawText("Fin", xEnd - 6f, yEnd - 7f, paint)
    }

    private fun measureMultilineTextHeight(
        text: String,
        maxWidth: Float,
        paint: Paint,
        lineSpacingExtra: Float = 4f
    ): Float {
        if (text.isBlank()) return 0f
        var totalH = 0f
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                totalH += paint.textSize * 0.7f
                continue
            }
            val words = paragraph.split(" ")
            var line = StringBuilder()
            for (word in words) {
                if (word.isEmpty()) continue
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    totalH += paint.textSize + lineSpacingExtra
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(testLine)
                }
            }
            if (line.isNotEmpty()) {
                totalH += paint.textSize + lineSpacingExtra
            }
        }
        return totalH
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        var currentY = startY
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                currentY += paint.textSize * 0.7f
                continue
            }
            val words = paragraph.split(" ")
            var line = StringBuilder()

            for (word in words) {
                if (word.isEmpty()) continue
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    canvas.drawText(line.toString(), x, currentY, paint)
                    currentY += paint.textSize + 4f
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(testLine)
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), x, currentY, paint)
                currentY += paint.textSize + 4f
            }
        }
        return currentY
    }

    private fun sharePdfFile(context: Context, file: File, title: String, isTravel: Boolean) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val subject = title
        val bodyText = title

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserTitle = "Compartir: $title"
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
