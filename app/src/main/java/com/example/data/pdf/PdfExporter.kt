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
            // We group stages or allocate 1 page per stage for rich detailed layout with drawn route and accommodations
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
            val pdfFile = File(outputDir, "Ultreia_Memorias_${sanitizedTitle}.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            // Open share intent
            withContext(Dispatchers.Main) {
                sharePdfFile(context, pdfFile, camino.title)
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

            val titleText = if (isTotal) "Estadísticas Totales del Viaje" else "Estadísticas: ${camino?.title ?: "Camino"}"
            drawStatsPage(context, canvas, isTotal, camino, stats, stages)
            pdfDocument.finishPage(page)

            val outputDir = File(context.cacheDir, "shared_docs")
            if (!outputDir.exists()) outputDir.mkdirs()

            val safeTitle = if (isTotal) "Totales" else (camino?.title?.replace("[^a-zA-Z0-9]".toRegex(), "_") ?: "Camino")
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
                context.startActivity(Intent.createChooser(intent, "Compartir Estadística del Viaje en PDF"))
            }
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            try { pdfDocument.close() } catch (_: Exception) {}
            null
        }
    }

    private fun drawStatsPage(
        context: Context,
        canvas: Canvas,
        isTotal: Boolean,
        camino: CaminoEntity?,
        stats: com.example.data.repository.CaminoStats,
        stages: List<StageEntity>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Navy Header
        paint.color = Color.rgb(14, 76, 153)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 180f, paint)

        // Yellow Accent
        paint.color = Color.rgb(247, 190, 49)
        canvas.drawRect(0f, 175f, PAGE_WIDTH.toFloat(), 180f, paint)

        // App Logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ultreia_logo)
            if (logoBitmap != null) {
                val scaled = Bitmap.createScaledBitmap(logoBitmap, 60, 60, true)
                canvas.drawBitmap(scaled, MARGIN, 35f, paint)
            }
        } catch (_: Exception) {}

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ULTREIA", MARGIN + 72f, 55f, paint)

        paint.textSize = 13f
        paint.color = Color.rgb(247, 190, 49)
        canvas.drawText(if (isTotal) "INFORME GLOBAL DE RENDIMIENTO Y VIAJES" else "ESTADÍSTICAS DETALLADAS DE RUTA", MARGIN + 72f, 75f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        val subjectTitle = if (isTotal) "Resumen Total Acumulado" else (camino?.title ?: "Camino de Santiago")
        canvas.drawText(subjectTitle, MARGIN + 72f, 105f, paint)

        paint.textSize = 10f
        paint.color = Color.rgb(220, 230, 245)
        canvas.drawText("Generado el: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", MARGIN + 72f, 125f, paint)

        // 4 KPI Cards
        var startY = 210f
        val cardWidth = (PAGE_WIDTH - (MARGIN * 2) - 20f) / 2
        val cardHeight = 90f

        // Card 1: Etapas
        drawMetricCard(canvas, MARGIN, startY, cardWidth, cardHeight, "ETAPAS COMPLETADAS", "${stats.completedStages} / ${stats.totalStages}", "${stats.totalStages - stats.completedStages} etapas restantes", Color.rgb(14, 76, 153))
        // Card 2: Distancia
        drawMetricCard(canvas, MARGIN + cardWidth + 20f, startY, cardWidth, cardHeight, "DISTANCIA RECORRIDA", "${String.format("%.1f", stats.completedKm)} km", "de ${String.format("%.1f", stats.totalKm)} km totales", Color.rgb(46, 125, 50))

        startY += cardHeight + 15f
        // Card 3: Desnivel
        drawMetricCard(canvas, MARGIN, startY, cardWidth, cardHeight, "DESNIVEL POSITIVO", "+${stats.completedElevationGainM} m", "de +${stats.totalElevationGainM} m totales", Color.rgb(217, 119, 6))
        // Card 4: Progreso
        drawMetricCard(canvas, MARGIN + cardWidth + 20f, startY, cardWidth, cardHeight, "PROGRESO GLOBAL", "${stats.progressPercent}%", "Objetivo cumplido", Color.rgb(14, 76, 153))

        // Progress bar visual
        startY += cardHeight + 25f
        paint.color = Color.rgb(235, 238, 242)
        val barRect = RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + 14f)
        canvas.drawRoundRect(barRect, 7f, 7f, paint)

        val fillWidth = (PAGE_WIDTH - (MARGIN * 2)) * (stats.progressPercent / 100f).coerceIn(0f, 1f)
        if (fillWidth > 0) {
            paint.color = Color.rgb(46, 125, 50)
            val fillRect = RectF(MARGIN, startY, MARGIN + fillWidth, startY + 14f)
            canvas.drawRoundRect(fillRect, 7f, 7f, paint)
        }

        // Section breakdown title
        startY += 40f
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isTotal) "Resumen de Rutas y Métricas Globales" else "Detalle de Etapas del Camino", MARGIN, startY, paint)

        startY += 20f
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(70, 80, 95)

        if (stages.isNotEmpty() && !isTotal) {
            for (st in stages.take(12)) {
                val statusText = if (st.isCompleted) "✓ Completada" else "⏳ Planificada"
                val line = "Día ${st.dayNumber}: ${st.origin} → ${st.destination}  |  ${st.distanceKm} km  |  +${st.elevationGainM}m  |  $statusText"
                canvas.drawText(line, MARGIN, startY, paint)
                startY += 16f
            }
        } else {
            val avgKm = if (stats.completedStages > 0) (stats.completedKm / stats.completedStages) else 0.0
            canvas.drawText("• Promedio de distancia por etapa completada: ${String.format("%.1f", avgKm)} km", MARGIN, startY, paint)
            startY += 18f
            val remainingKm = (stats.totalKm - stats.completedKm).coerceAtLeast(0.0)
            canvas.drawText("• Distancia pendiente por recorrer: ${String.format("%.1f", remainingKm)} km", MARGIN, startY, paint)
            startY += 18f
            canvas.drawText("• Desnivel medio superado por jornada: ${if (stats.completedStages > 0) stats.completedElevationGainM / stats.completedStages else 0} metros", MARGIN, startY, paint)
        }

        // Stamp and Credential Footer
        paint.color = Color.rgb(230, 235, 245)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        val stampBox = RectF(PAGE_WIDTH - MARGIN - 140f, PAGE_HEIGHT - 120f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 40f)
        canvas.drawRoundRect(stampBox, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(140, 150, 165)
        paint.textSize = 8.5f
        canvas.drawText("SELLO Y VALIDACIÓN", PAGE_WIDTH - MARGIN - 130f, PAGE_HEIGHT - 100f, paint)
        canvas.drawText("OFICINA DEL PEREGRINO", PAGE_WIDTH - MARGIN - 130f, PAGE_HEIGHT - 85f, paint)

        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("¡Ultreia et Suseia! — Guía de Viaje Oficial", MARGIN, PAGE_HEIGHT - 45f, paint)
    }

    private fun drawMetricCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        sub: String,
        accentColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Card background
        paint.color = Color.rgb(248, 250, 252)
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 10f, 10f, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(rect, 10f, 10f, paint)

        // Left accent bar
        paint.style = Paint.Style.FILL
        paint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + 5f, y + h), 3f, 3f, paint)

        // Title
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 16f, y + 24f, paint)

        // Value
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 19f
        canvas.drawText(value, x + 16f, y + 52f, paint)

        // Sub
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(sub, x + 16f, y + 74f, paint)
    }

    private fun drawCoverPage(
        context: Context,
        canvas: Canvas,
        camino: CaminoEntity,
        stages: List<StageEntity>,
        repository: CaminoRepository
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background header banner (Camino Navy Blue)
        paint.color = Color.rgb(14, 76, 153)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 260f, paint)

        // Golden accent stripe
        paint.color = Color.rgb(247, 190, 49)
        canvas.drawRect(0f, 255f, PAGE_WIDTH.toFloat(), 262f, paint)

        // Try drawing user icon or shell logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_ultreia_logo)
            if (logoBitmap != null) {
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 75, 75, true)
                canvas.drawBitmap(scaledLogo, MARGIN, 35f, paint)
            }
        } catch (_: Exception) {}

        // App title / Brand
        paint.color = Color.rgb(247, 190, 49)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("ULTREIA • GUÍA & MEMORIAS DEL CAMINO", MARGIN + 90f, 55f, paint)

        // Camino Title
        paint.color = Color.WHITE
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText(camino.title, MARGIN + 90f, 90f, paint)

        // Subtitle
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(220, 235, 255)
        val sub = if (camino.subtitle.length > 55) camino.subtitle.take(52) + "..." else camino.subtitle
        canvas.drawText(sub, MARGIN + 90f, 112f, paint)

        // Pilgrim info pill
        val pilgrimLabel = "Peregrino: ${camino.pilgrimName}  |  Fecha: ${camino.startDate.ifEmpty { "2026" }}"
        paint.textSize = 11f
        paint.color = Color.rgb(240, 240, 240)
        canvas.drawText(pilgrimLabel, MARGIN, 215f, paint)

        // Statistics Summary Card
        val stats = repository.calculateStats(stages)
        val cardTop = 290f
        val cardBottom = 420f
        val cardRect = RectF(MARGIN, cardTop, PAGE_WIDTH - MARGIN, cardBottom)

        paint.color = Color.rgb(245, 248, 252)
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)

        paint.color = Color.rgb(215, 228, 245)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        // Card header
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen Estadístico del Viaje", MARGIN + 18f, cardTop + 32f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 12f
        paint.color = Color.rgb(55, 65, 81)

        val col1 = MARGIN + 20f
        val col2 = PAGE_WIDTH / 2f + 10f

        canvas.drawText("• Distancia Total: ${stats.totalKm} km", col1, cardTop + 62f, paint)
        canvas.drawText("• Distancia Completada: ${stats.completedKm} km", col1, cardTop + 86f, paint)
        canvas.drawText("• Progreso General: ${stats.progressPercent}%", col1, cardTop + 110f, paint)

        canvas.drawText("• Etapas Totales: ${stats.totalStages}", col2, cardTop + 62f, paint)
        canvas.drawText("• Etapas Completadas: ${stats.completedStages}", col2, cardTop + 86f, paint)
        canvas.drawText("• Desnivel Acumulado: +${stats.totalElevationGainM} m", col2, cardTop + 110f, paint)

        // Itinerary index table
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("Índice de Etapas del Cuaderno", MARGIN, 455f, paint)

        var yPos = 485f
        paint.textSize = 10.5f
        paint.isFakeBoldText = false

        for (i in stages.indices) {
            if (yPos > PAGE_HEIGHT - 60f) break
            val stg = stages[i]

            // Status indicator icon
            paint.color = if (stg.isCompleted) Color.rgb(46, 125, 50) else Color.rgb(156, 163, 175)
            canvas.drawCircle(MARGIN + 6f, yPos - 3.5f, 4f, paint)

            paint.color = Color.rgb(31, 41, 55)
            val stgTitle = "Día ${stg.dayNumber}: ${stg.origin} → ${stg.destination}"
            canvas.drawText(stgTitle, MARGIN + 18f, yPos, paint)

            val meta = "${stg.distanceKm} km  |  +${stg.elevationGainM}m  |  ${stg.estimatedTime}"
            paint.color = Color.rgb(107, 114, 128)
            val metaWidth = paint.measureText(meta)
            canvas.drawText(meta, PAGE_WIDTH - MARGIN - metaWidth, yPos, paint)

            // subtle divider line
            paint.color = Color.rgb(238, 242, 246)
            canvas.drawLine(MARGIN, yPos + 6f, PAGE_WIDTH - MARGIN, yPos + 6f, paint)

            yPos += 24f
        }

        // Footer
        paint.color = Color.rgb(156, 163, 175)
        paint.textSize = 9.5f
        canvas.drawText("Generado con Ultreia • ¡Buen Camino, Peregrino!", MARGIN, PAGE_HEIGHT - 25f, paint)
    }

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
            paint.color = Color.rgb(14, 76, 153)
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 55f, paint)

            paint.color = Color.rgb(247, 190, 49)
            paint.textSize = 11f
            paint.isFakeBoldText = true
            val brandPrefix = if (isTravel) "ULTREIA VIAJES" else "ULTREIA"
            c.drawText("$brandPrefix • ${camino.title.uppercase()}", MARGIN, 24f, paint)

            val stageDate = if (stage.date.isNotBlank()) stage.date
            else if (camino.startDate.isNotBlank()) {
                com.example.ui.settings.AppStrings.calculateStageDate(camino.startDate, stage.dayNumber - 1)
            } else ""

            paint.color = Color.WHITE
            paint.textSize = 14f
            val dateLabel = if (stageDate.isNotBlank()) "  •  $stageDate" else ""
            val contLabel = if (isContinuation) " ($subtitleText)" else ""
            c.drawText("Día ${stage.dayNumber}: ${stage.origin} a ${stage.destination}$dateLabel$contLabel", MARGIN, 44f, paint)

            if (stage.isCompleted && !isContinuation) {
                paint.color = Color.rgb(46, 125, 50)
                val badgeRect = RectF(PAGE_WIDTH - MARGIN - 95f, 15f, PAGE_WIDTH - MARGIN, 42f)
                c.drawRoundRect(badgeRect, 6f, 6f, paint)
                paint.color = Color.WHITE
                paint.textSize = 10f
                paint.isFakeBoldText = true
                c.drawText("✓ COMPLETADA", PAGE_WIDTH - MARGIN - 86f, 32f, paint)
            }
        }

        fun drawFooter(c: Canvas) {
            paint.color = Color.rgb(156, 163, 175)
            paint.textSize = 9.5f
            paint.isFakeBoldText = false
            val footerBrand = if (isTravel) "Ultreia • Guía e Itinerario de Viaje" else "Ultreia • Memorias del Camino de Santiago"
            c.drawText("$footerBrand  •  Página $currentPageNumber", MARGIN, PAGE_HEIGHT - 25f, paint)
        }

        fun nextSubPage(continuationSubtitle: String) {
            drawFooter(canvas)
            pdfDocument.finishPage(page)
            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawStageHeader(canvas, isContinuation = true, subtitleText = continuationSubtitle)
        }

        drawStageHeader(canvas)
        var currentY = 75f

        // 1. FICHA TÉCNICA (Regla estricta: en Viajes SOLO si hay GPX; en Caminos siempre)
        val points = GpxParser.deserializePoints(stage.gpxTrackJson)
        val hasGpx = !stage.gpxTrackJson.isNullOrBlank() && points.isNotEmpty()
        val showFichaTecnica = !isTravel || hasGpx

        if (showFichaTecnica) {
            paint.color = Color.rgb(240, 245, 252)
            val metricsRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 45f)
            canvas.drawRoundRect(metricsRect, 8f, 8f, paint)

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 11f
            paint.isFakeBoldText = true
            val metricText = "Distancia: ${stage.distanceKm} km   |   Desnivel: +${stage.elevationGainM}m / -${stage.elevationLossM}m   |   Tiempo: ${stage.estimatedTime}   |   Dificultad: ${stage.difficulty}"
            canvas.drawText(metricText, MARGIN + 14f, currentY + 28f, paint)

            currentY += 60f

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText(if (isTravel) "Trazado del Recorrido (GPX)" else "Trazado de la Ruta y Perfil de Terreno", MARGIN, currentY, paint)
            currentY += 10f

            val chartBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 110f)
            paint.color = Color.rgb(250, 252, 255)
            canvas.drawRoundRect(chartBox, 8f, 8f, paint)
            paint.color = Color.rgb(218, 228, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(chartBox, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            if (points.isNotEmpty()) {
                drawMiniTrackOnCanvas(canvas, points, chartBox)
            } else {
                paint.color = Color.rgb(14, 76, 153)
                paint.strokeWidth = 2.5f
                val path = Path()
                path.moveTo(chartBox.left + 20f, chartBox.bottom - 25f)
                path.cubicTo(
                    chartBox.left + 150f, chartBox.top + 30f,
                    chartBox.left + 300f, chartBox.bottom - 40f,
                    chartBox.right - 20f, chartBox.bottom - 20f
                )
                paint.style = Paint.Style.STROKE
                canvas.drawPath(path, paint)
                paint.style = Paint.Style.FILL
            }

            currentY += 125f
        }

        // 2. CONSEJOS DEL DÍA (Caminos) O RESUMEN DEL DÍA (Viajes)
        if (stage.stageNotes.isNotBlank()) {
            if (currentY + 60f > PAGE_HEIGHT - MARGIN - 40f) {
                nextSubPage(if (isTravel) "Resumen" else "Consejos")
                currentY = 75f
            }

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            val titleText = if (isTravel) "Resumen del Día" else "Información y Consejos del Día"
            canvas.drawText(titleText, MARGIN, currentY, paint)
            currentY += 16f

            paint.color = Color.rgb(55, 65, 81)
            paint.textSize = 10.5f
            paint.isFakeBoldText = false
            currentY = drawMultilineText(canvas, stage.stageNotes, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, paint)
            currentY += 14f
        }

        // 3. AGENDA DEL DÍA (Solo en Viajes)
        if (isTravel) {
            val agendaList = parseAgendaItems(stage.agendaJson)
            if (agendaList.isNotEmpty()) {
                if (currentY + 50f > PAGE_HEIGHT - MARGIN - 40f) {
                    nextSubPage("Agenda")
                    currentY = 75f
                }

                paint.color = Color.rgb(14, 76, 153)
                paint.textSize = 13f
                paint.isFakeBoldText = true
                canvas.drawText("Agenda del Día", MARGIN, currentY, paint)
                currentY += 16f

                for (event in agendaList) {
                    if (currentY + 45f > PAGE_HEIGHT - MARGIN - 40f) {
                        nextSubPage("Agenda")
                        currentY = 75f
                    }

                    // Hora y Título
                    paint.color = Color.rgb(14, 76, 153)
                    paint.textSize = 11.5f
                    paint.isFakeBoldText = true
                    val eventHeader = "⏰ ${event.time}  —  ${event.title}"
                    canvas.drawText(eventHeader, MARGIN + 4f, currentY, paint)
                    currentY += 14f

                    // Descripción multilínea
                    if (event.description.isNotBlank()) {
                        paint.color = Color.rgb(55, 65, 81)
                        paint.textSize = 10f
                        paint.isFakeBoldText = false
                        currentY = drawMultilineText(canvas, event.description, MARGIN + 12f, currentY, PAGE_WIDTH - 2 * MARGIN - 24f, paint)
                        currentY += 6f
                    }

                    // Separación estricta de archivos: Documentos vs Imágenes
                    val (imageFiles, docFiles) = event.files.partition { isImageAttachedFile(context, it) }

                    // A. Documentos no-imagen: listar como texto
                    for (doc in docFiles) {
                        if (currentY + 20f > PAGE_HEIGHT - MARGIN - 40f) {
                            nextSubPage("Agenda - Documentos")
                            currentY = 75f
                        }
                        paint.color = Color.rgb(75, 85, 99)
                        paint.textSize = 9.5f
                        paint.isFakeBoldText = false
                        val docName = doc.name.ifBlank { extractFileName(doc.uri) }
                        canvas.drawText("Archivo adjunto: $docName", MARGIN + 12f, currentY, paint)
                        currentY += 14f
                    }

                    // B. Imágenes: incrustar justo debajo del texto manteniendo proporción y márgenes
                    for (photo in imageFiles) {
                        val neededH = estimateImageHeight(context, photo.uri, PAGE_WIDTH - 2 * MARGIN - 24f, 220f)
                        if (currentY + neededH + 10f > PAGE_HEIGHT - MARGIN - 40f) {
                            nextSubPage("Agenda - Fotos")
                            currentY = 75f
                        }
                        val drawnH = drawImageFromUri(context, canvas, photo.uri, MARGIN + 12f, currentY, PAGE_WIDTH - 2 * MARGIN - 24f, 220f)
                        if (drawnH > 0f) {
                            currentY += drawnH + 8f
                        }
                    }
                    currentY += 6f
                }
                currentY += 8f
            }
        }

        // 4. ALOJAMIENTOS
        if (accommodations.isNotEmpty() || !isTravel) {
            if (currentY + 60f > PAGE_HEIGHT - MARGIN - 40f) {
                nextSubPage("Alojamientos")
                currentY = 75f
            }

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText(if (isTravel) "Alojamientos y Hoteles" else "Alojamientos y Opciones de Hospedaje", MARGIN, currentY, paint)
            currentY += 16f

            if (accommodations.isEmpty()) {
                paint.color = Color.rgb(120, 120, 120)
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText("No se registraron alojamientos específicos para este día.", MARGIN, currentY, paint)
                currentY += 16f
            } else {
                for (acc in accommodations) {
                    if (currentY + 35f > PAGE_HEIGHT - MARGIN - 40f) {
                        nextSubPage("Alojamientos")
                        currentY = 75f
                    }

                    paint.color = Color.rgb(17, 24, 39)
                    paint.textSize = 11f
                    paint.isFakeBoldText = true
                    val accTitle = "• ${acc.name}" + if (acc.phone.isNotBlank()) " (Tel: ${acc.phone})" else ""
                    canvas.drawText(accTitle, MARGIN + 4f, currentY, paint)
                    currentY += 14f

                    paint.isFakeBoldText = false
                    paint.color = Color.rgb(75, 85, 99)
                    paint.textSize = 10f
                    if (acc.address.isNotBlank()) {
                        canvas.drawText("  Dirección: ${acc.address}", MARGIN + 4f, currentY, paint)
                        currentY += 13f
                    }

                    val roomOptions = repository.parseRoomOptions(acc.roomOptionsJson)
                    if (roomOptions.isNotEmpty()) {
                        val pricesStr = roomOptions.joinToString("  |  ") { "${it.roomType}: ${it.priceEur}€" }
                        paint.color = Color.rgb(46, 125, 50)
                        canvas.drawText("  Precios: $pricesStr", MARGIN + 4f, currentY, paint)
                        currentY += 14f
                    }
                    currentY += 4f
                }
            }
            currentY += 10f
        }

        // 5. VIVENCIAS / NOTAS PERSONALES DE LA JORNADA
        val vivenciasList = parseVivencias(stage.vivenciasJson, stage.diaryNotes, stage.diaryPhotoUri)
        if (vivenciasList.isNotEmpty() || !isTravel) {
            if (currentY + 60f > PAGE_HEIGHT - MARGIN - 40f) {
                nextSubPage(if (isTravel) "Notas" else "Vivencias")
                currentY = 75f
            }

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            val vivenciasTitle = if (isTravel) "Notas Personales de la Jornada" else "Vivencias Personales de la Jornada"
            canvas.drawText(vivenciasTitle, MARGIN, currentY, paint)
            currentY += 16f

            if (vivenciasList.isEmpty()) {
                val boxHeight = 45f
                val diaryBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + boxHeight)
                paint.color = Color.rgb(255, 252, 245)
                canvas.drawRoundRect(diaryBox, 8f, 8f, paint)
                paint.color = Color.rgb(240, 225, 185)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(diaryBox, 8f, 8f, paint)
                paint.style = Paint.Style.FILL

                paint.color = Color.rgb(120, 110, 95)
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText("Sin notas de vivencias registradas aún para este día.", MARGIN + 12f, currentY + 26f, paint)
                currentY += boxHeight + 14f
            } else {
                for (vivencia in vivenciasList) {
                    if (currentY + 45f > PAGE_HEIGHT - MARGIN - 40f) {
                        nextSubPage(if (isTravel) "Notas" else "Vivencias")
                        currentY = 75f
                    }

                    // Fecha si está disponible
                    if (vivencia.dateStr.isNotBlank()) {
                        paint.color = Color.rgb(107, 114, 128)
                        paint.textSize = 9.5f
                        paint.isFakeBoldText = true
                        canvas.drawText("📅 ${vivencia.dateStr}", MARGIN + 4f, currentY, paint)
                        currentY += 13f
                    }

                    // Texto de la vivencia
                    if (vivencia.note.isNotBlank()) {
                        paint.color = Color.rgb(55, 65, 81)
                        paint.textSize = 10f
                        paint.isFakeBoldText = false
                        currentY = drawMultilineText(canvas, vivencia.note, MARGIN + 8f, currentY, PAGE_WIDTH - 2 * MARGIN - 16f, paint)
                        currentY += 6f
                    }

                    // Archivo / Foto adjunta a la vivencia
                    if (!vivencia.photoUri.isNullOrBlank()) {
                        val isImg = checkIsImage(context, null, extractFileName(vivencia.photoUri), vivencia.photoUri)
                        if (isImg) {
                            val neededH = estimateImageHeight(context, vivencia.photoUri, PAGE_WIDTH - 2 * MARGIN - 16f, 220f)
                            if (currentY + neededH + 10f > PAGE_HEIGHT - MARGIN - 40f) {
                                nextSubPage(if (isTravel) "Notas - Fotos" else "Vivencias - Fotos")
                                currentY = 75f
                            }
                            val drawnH = drawImageFromUri(context, canvas, vivencia.photoUri, MARGIN + 8f, currentY, PAGE_WIDTH - 2 * MARGIN - 16f, 220f)
                            if (drawnH > 0f) {
                                currentY += drawnH + 8f
                            }
                        } else {
                            // Documento no-imagen: nunca renderizar como imagen
                            if (currentY + 20f > PAGE_HEIGHT - MARGIN - 40f) {
                                nextSubPage(if (isTravel) "Notas" else "Vivencias")
                                currentY = 75f
                            }
                            val fileName = extractFileName(vivencia.photoUri)
                            paint.color = Color.rgb(75, 85, 99)
                            paint.textSize = 9.5f
                            paint.isFakeBoldText = false
                            canvas.drawText("Archivo adjunto: $fileName", MARGIN + 8f, currentY, paint)
                            currentY += 14f
                        }
                    }
                    currentY += 6f
                }
                currentY += 8f
            }
        }

        // 6. ARCHIVOS Y DOCUMENTOS ADJUNTOS DE LA ETAPA
        val stageAttachedFiles = parseAttachedFiles(stage.attachedFilesJson)
        if (stageAttachedFiles.isNotEmpty()) {
            if (currentY + 50f > PAGE_HEIGHT - MARGIN - 40f) {
                nextSubPage("Archivos Adjuntos")
                currentY = 75f
            }

            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("Archivos y Documentos Adjuntos", MARGIN, currentY, paint)
            currentY += 16f

            val (stageImages, stageDocs) = stageAttachedFiles.partition { isImageAttachedFile(context, it) }

            // Documentos no-imagen
            for (doc in stageDocs) {
                if (currentY + 20f > PAGE_HEIGHT - MARGIN - 40f) {
                    nextSubPage("Archivos Adjuntos")
                    currentY = 75f
                }
                paint.color = Color.rgb(75, 85, 99)
                paint.textSize = 9.5f
                paint.isFakeBoldText = false
                val docName = doc.name.ifBlank { extractFileName(doc.uri) }
                canvas.drawText("Archivo adjunto: $docName", MARGIN + 8f, currentY, paint)
                currentY += 14f
            }

            // Imágenes adjuntas
            for (img in stageImages) {
                val neededH = estimateImageHeight(context, img.uri, PAGE_WIDTH - 2 * MARGIN - 16f, 220f)
                if (currentY + neededH + 10f > PAGE_HEIGHT - MARGIN - 40f) {
                    nextSubPage("Archivos Adjuntos")
                    currentY = 75f
                }
                val drawnH = drawImageFromUri(context, canvas, img.uri, MARGIN + 8f, currentY, PAGE_WIDTH - 2 * MARGIN - 16f, 220f)
                if (drawnH > 0f) {
                    currentY += drawnH + 8f
                }
            }
            currentY += 8f
        }

        drawFooter(canvas)
        pdfDocument.finishPage(page)
        return currentPageNumber + 1
    }

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
            paint.color = Color.rgb(240, 243, 248)
            canvas.drawRoundRect(frameRect, 6f, 6f, paint)

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

        val padding = 18f
        val w = bounds.width() - 2 * padding
        val h = bounds.height() - 2 * padding

        val path = Path()
        for (i in points.indices) {
            val p = points[i]
            val x = bounds.left + padding + ((p.lon - minLon) / lonSpan * w).toFloat()
            val y = bounds.bottom - padding - ((p.lat - minLat) / latSpan * h).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        paint.color = Color.rgb(14, 76, 153)
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        // Start point (Green)
        val pStart = points.first()
        val xStart = bounds.left + padding + ((pStart.lon - minLon) / lonSpan * w).toFloat()
        val yStart = bounds.bottom - padding - ((pStart.lat - minLat) / latSpan * h).toFloat()
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawCircle(xStart, yStart, 5f, paint)

        // End point (Gold Yellow)
        val pEnd = points.last()
        val xEnd = bounds.left + padding + ((pEnd.lon - minLon) / lonSpan * w).toFloat()
        val yEnd = bounds.bottom - padding - ((pEnd.lat - minLat) / latSpan * h).toFloat()
        paint.color = Color.rgb(247, 190, 49)
        canvas.drawCircle(xEnd, yEnd, 5f, paint)

        // Labels
        paint.textSize = 9f
        paint.color = Color.rgb(70, 70, 70)
        canvas.drawText("Origen", xStart - 10f, yStart - 8f, paint)
        canvas.drawText("Destino", xEnd - 10f, yEnd - 8f, paint)
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
                currentY += paint.textSize * 0.8f
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

    private fun sharePdfFile(context: Context, file: File, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Guía y Memorias del Camino: $title")
            putExtra(Intent.EXTRA_TEXT, "Comparto mis memorias de viaje del Camino de Santiago creadas con Ultreia.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir Memorias del Camino en PDF"))
    }
}
