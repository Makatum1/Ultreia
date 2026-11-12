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
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
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
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val accommodations = accMap[stage.id] ?: emptyList()

                drawStagePage(context, canvas, camino, stage, accommodations, repository)
                pdfDocument.finishPage(page)
                pageNumber++
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

    private fun drawStagePage(
        context: Context,
        canvas: Canvas,
        camino: CaminoEntity,
        stage: StageEntity,
        accommodations: List<AccommodationEntity>,
        repository: CaminoRepository
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header strip
        paint.color = Color.rgb(14, 76, 153)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 55f, paint)

        paint.color = Color.rgb(247, 190, 49)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("ULTREIA • ${camino.title.uppercase()}", MARGIN, 24f, paint)

        val stageDate = if (stage.date.isNotBlank()) stage.date
        else if (camino.startDate.isNotBlank()) {
            com.example.ui.settings.AppStrings.calculateStageDate(camino.startDate, stage.dayNumber - 1)
        } else ""

        paint.color = Color.WHITE
        paint.textSize = 14f
        val dateLabel = if (stageDate.isNotBlank()) "  •  $stageDate" else ""
        canvas.drawText("Día ${stage.dayNumber}: ${stage.origin} a ${stage.destination}$dateLabel", MARGIN, 44f, paint)

        // Completion badge in header
        if (stage.isCompleted) {
            paint.color = Color.rgb(46, 125, 50)
            val badgeRect = RectF(PAGE_WIDTH - MARGIN - 95f, 15f, PAGE_WIDTH - MARGIN, 42f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("✓ COMPLETADA", PAGE_WIDTH - MARGIN - 86f, 32f, paint)
        }

        var currentY = 75f

        // Key metrics row
        paint.color = Color.rgb(240, 245, 252)
        val metricsRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 45f)
        canvas.drawRoundRect(metricsRect, 8f, 8f, paint)

        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 11f
        paint.isFakeBoldText = true
        val metricText = "Distancia: ${stage.distanceKm} km   |   Desnivel: +${stage.elevationGainM}m / -${stage.elevationLossM}m   |   Tiempo: ${stage.estimatedTime}   |   Dificultad: ${stage.difficulty}"
        canvas.drawText(metricText, MARGIN + 14f, currentY + 28f, paint)

        currentY += 60f

        // Route Profile / Drawn Track Chart
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Trazado de la Ruta y Perfil de Terreno", MARGIN, currentY, paint)
        currentY += 10f

        // Draw Route Map Box
        val chartBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 110f)
        paint.color = Color.rgb(250, 252, 255)
        canvas.drawRoundRect(chartBox, 8f, 8f, paint)
        paint.color = Color.rgb(218, 228, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(chartBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Draw track curve inside chartBox
        val points = GpxParser.deserializePoints(stage.gpxTrackJson)
        if (points.isNotEmpty()) {
            drawMiniTrackOnCanvas(canvas, points, chartBox)
        } else {
            // Draw default simulated elevation line
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

        // Stage Description / Notes
        if (stage.stageNotes.isNotBlank()) {
            paint.color = Color.rgb(14, 76, 153)
            paint.textSize = 13f
            paint.isFakeBoldText = true
            canvas.drawText("Información y Consejos del Día", MARGIN, currentY, paint)
            currentY += 16f

            paint.color = Color.rgb(55, 65, 81)
            paint.textSize = 10.5f
            paint.isFakeBoldText = false
            currentY = drawMultilineText(canvas, stage.stageNotes, MARGIN, currentY, PAGE_WIDTH - 2 * MARGIN, paint)
            currentY += 12f
        }

        // Accommodations section
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Alojamientos y Opciones de Hospedaje", MARGIN, currentY, paint)
        currentY += 16f

        if (accommodations.isEmpty()) {
            paint.color = Color.rgb(120, 120, 120)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("No se registraron alojamientos específicos para este día.", MARGIN, currentY, paint)
            currentY += 16f
        } else {
            for (acc in accommodations) {
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

        currentY += 12f

        // Vivencias del Peregrino (Daily Journal)
        paint.color = Color.rgb(14, 76, 153)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("Vivencias Personales de la Jornada", MARGIN, currentY, paint)
        currentY += 16f

        val diaryBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 110f)
        paint.color = Color.rgb(255, 252, 245) // warm parchment
        canvas.drawRoundRect(diaryBox, 8f, 8f, paint)
        paint.color = Color.rgb(240, 225, 185)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(diaryBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        val diaryText = if (stage.diaryNotes.isNotBlank()) stage.diaryNotes else "Sin notas de vivencias registradas aún para este día del camino."
        paint.color = Color.rgb(60, 50, 30)
        paint.textSize = 10.5f
        paint.isFakeBoldText = false
        drawMultilineText(canvas, diaryText, MARGIN + 12f, currentY + 18f, PAGE_WIDTH - 2 * MARGIN - 24f, paint)

        // Footer
        paint.color = Color.rgb(156, 163, 175)
        paint.textSize = 9.5f
        canvas.drawText("Ultreia • Memorias del Camino de Santiago  •  Página ${stage.dayNumber + 1}", MARGIN, PAGE_HEIGHT - 25f, paint)
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
        val words = text.split(" ")
        var line = StringBuilder()

        for (word in words) {
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
