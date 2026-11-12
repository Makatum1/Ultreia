package com.example.data.sample

import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoRepository

data class PredefinedRouteInfo(
    val title: String,
    val subtitle: String,
    val totalKm: Double,
    val note: String? = null,
    val stages: List<PredefinedStageInfo>
)

data class PredefinedStageInfo(
    val dayNumber: Int,
    val title: String,
    val origin: String,
    val destination: String,
    val distanceKm: Double,
    val elevationGainM: Int,
    val elevationLossM: Int,
    val estimatedTime: String,
    val difficulty: String,
    val notes: String = ""
)

object SampleData {

    fun generateTrackPoints(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        startEle: Double,
        endEle: Double,
        numPoints: Int = 25
    ): List<GpxPoint> {
        val points = mutableListOf<GpxPoint>()
        for (i in 0..numPoints) {
            val t = i.toDouble() / numPoints
            val curveFactor = Math.sin(t * Math.PI) * 0.02
            val lat = startLat + (endLat - startLat) * t + curveFactor * 0.5
            val lon = startLon + (endLon - startLon) * t + curveFactor
            val ele = startEle + (endEle - startEle) * t + Math.sin(t * Math.PI) * 120.0
            points.add(GpxPoint(lat = lat, lon = lon, ele = ele))
        }
        return points
    }

    val PREDEFINED_ROUTES = listOf(
        PredefinedRouteInfo(
            title = "Camino Francés",
            subtitle = "De los Pirineos a la tumba del Apóstol Santiago",
            totalKm = 780.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Saint-Jean-Pied-de-Port a Roncesvalles", "Saint-Jean-Pied-de-Port", "Roncesvalles", 25.1, 1250, 480, "7h 30m", "Muy exigente", "Ruta de Napoleón por el Collado de Lepoeder."),
                PredefinedStageInfo(2, "Etapa 2: Roncesvalles a Zubiri", "Roncesvalles", "Zubiri", 21.4, 290, 750, "5h 45m", "Moderada", "Bosques de hayas navarros y Puente de la Rabia."),
                PredefinedStageInfo(3, "Etapa 3: Zubiri a Pamplona", "Zubiri", "Pamplona", 20.4, 210, 310, "5h 00m", "Fácil", "Entrada por el portal de Francia y las murallas históricas."),
                PredefinedStageInfo(4, "Etapa 4: Sarria a Portomarín", "Sarria", "Portomarín", 22.2, 350, 420, "5h 30m", "Moderada", "Comienzo de los últimos 100 km tradicionales gallegos."),
                PredefinedStageInfo(5, "Etapa 5: Portomarín a Palas de Rei", "Portomarín", "Palas de Rei", 24.8, 450, 320, "6h 00m", "Moderada", "Ascenso hacia la Sierra de Ligonde y cruceiros."),
                PredefinedStageInfo(6, "Etapa 6: Palas de Rei a Arzúa", "Palas de Rei", "Arzúa", 28.5, 410, 480, "6h 45m", "Moderada", "Unión con el Camino del Norte en Arzúa."),
                PredefinedStageInfo(7, "Etapa 7: Arzúa a O Pedrouzo", "Arzúa", "O Pedrouzo", 19.3, 280, 320, "4h 45m", "Fácil", "Senderos flanqueados por robles y eucaliptos."),
                PredefinedStageInfo(8, "Etapa 8: O Pedrouzo a Santiago de Compostela", "O Pedrouzo", "Santiago de Compostela", 19.4, 310, 350, "4h 45m", "Fácil", "Subida al Monte do Gozo y llegada a la Plaza del Obradoiro.")
            )
        ),
        PredefinedRouteInfo(
            title = "Camino Portugués Central",
            subtitle = "De Tui a Santiago entre viñedos de albariño y rías gallegas",
            totalKm = 118.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Tui a O Porriño", "Tui", "O Porriño", 16.5, 190, 180, "4h 00m", "Fácil", "Cruce del Puente Internacional y catedral de Tui."),
                PredefinedStageInfo(2, "Etapa 2: O Porriño a Redondela", "O Porriño", "Redondela", 15.2, 240, 260, "3h 45m", "Fácil", "Paso por Mos y vistas de la ensenada de San Simón."),
                PredefinedStageInfo(3, "Etapa 3: Redondela a Pontevedra", "Redondela", "Pontevedra", 19.8, 360, 350, "5h 15m", "Moderada", "Ponte Sampaio sobre el río Verdugo y centro histórico."),
                PredefinedStageInfo(4, "Etapa 4: Pontevedra a Caldas de Reis", "Pontevedra", "Caldas de Reis", 21.1, 220, 240, "5h 15m", "Fácil", "Paso por viñedos y aguas termales en Caldas."),
                PredefinedStageInfo(5, "Etapa 5: Caldas de Reis a Padrón", "Caldas de Reis", "Padrón", 18.6, 210, 220, "4h 30m", "Fácil", "Tierra de Rosalía de Castro y los famosos pimientos."),
                PredefinedStageInfo(6, "Etapa 6: Padrón a Santiago de Compostela", "Padrón", "Santiago de Compostela", 24.3, 410, 160, "6h 00m", "Moderada", "Último esfuerzo hasta la Catedral de Santiago.")
            )
        ),
        PredefinedRouteInfo(
            title = "Camino del Norte",
            subtitle = "Por la costa cantábrica hacia Santiago de Compostela",
            totalKm = 195.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Ribadeo a Lourenzá", "Ribadeo", "Lourenzá", 28.4, 620, 480, "7h 00m", "Exigente", "Entrada a Galicia desde la ría del Eo."),
                PredefinedStageInfo(2, "Etapa 2: Lourenzá a Abadín", "Lourenzá", "Abadín", 24.8, 680, 210, "6h 15m", "Moderada", "Mondoñedo y ascenso a la meseta lucense."),
                PredefinedStageInfo(3, "Etapa 3: Abadín a Vilalba", "Abadín", "Vilalba", 20.6, 180, 210, "5h 00m", "Fácil", "Caminata llana por la comarca de Terra Chá."),
                PredefinedStageInfo(4, "Etapa 4: Vilalba a Baamonde", "Vilalba", "Baamonde", 20.7, 160, 180, "5h 00m", "Fácil", "Puente medieval de Saa y castaño milenario."),
                PredefinedStageInfo(5, "Etapa 5: Baamonde a Sobrado dos Monxes", "Baamonde", "Sobrado dos Monxes", 40.3, 620, 540, "9h 30m", "Muy exigente", "Monasterio cisterciense de Santa María."),
                PredefinedStageInfo(6, "Etapa 6: Sobrado dos Monxes a Arzúa", "Sobrado dos Monxes", "Arzúa", 21.4, 260, 340, "5h 15m", "Fácil", "Enlace con el Camino Francés."),
                PredefinedStageInfo(7, "Etapa 7: Arzúa a Santiago de Compostela", "Arzúa", "Santiago de Compostela", 38.7, 590, 670, "8h 30m", "Exigente", "Tramo final hasta la Plaza del Obradoiro.")
            )
        ),
        PredefinedRouteInfo(
            title = "Camino Primitivo",
            subtitle = "La primera ruta de peregrinación por la montaña astur-galaica",
            totalKm = 313.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Oviedo a Grado", "Oviedo", "Grado", 25.6, 420, 610, "6h 30m", "Moderada", "Salida de la catedral de San Salvador."),
                PredefinedStageInfo(2, "Etapa 2: Grado a Salas", "Grado", "Salas", 22.2, 580, 410, "5h 45m", "Moderada", "Alto del Fresno y santuario de la Virgen."),
                PredefinedStageInfo(3, "Etapa 3: Salas a Tineo", "Salas", "Tineo", 19.8, 690, 180, "5h 30m", "Exigente", "Subida pronunciada hacia las brañas de Tineo."),
                PredefinedStageInfo(4, "Etapa 4: Tineo a Pola de Allande", "Tineo", "Pola de Allande", 28.2, 580, 720, "7h 00m", "Exigente", "Paso por el monasterio de Obona."),
                PredefinedStageInfo(5, "Etapa 5: Pola de Allande a La Mesa", "Pola de Allande", "La Mesa", 21.4, 980, 510, "6h 30m", "Muy exigente", "Paso mítico por el Puerto del Palo y Hospitales."),
                PredefinedStageInfo(6, "Etapa 6: La Mesa a Grandas de Salime", "La Mesa", "Grandas de Salime", 15.3, 290, 780, "4h 30m", "Moderada", "Descenso al embalse de Salime y vistas increíbles."),
                PredefinedStageInfo(7, "Etapa 7: Grandas de Salime a A Fonsagrada", "Grandas de Salime", "A Fonsagrada", 25.2, 850, 310, "7h 00m", "Muy exigente", "Entrada a Galicia por el Alto do Acebo."),
                PredefinedStageInfo(8, "Etapa 8: A Fonsagrada a O Cádavo", "A Fonsagrada", "O Cádavo", 24.3, 560, 710, "6h 30m", "Moderada", "Hospital de Montouto y molinos de viento."),
                PredefinedStageInfo(9, "Etapa 9: O Cádavo a Lugo", "O Cádavo", "Lugo", 29.5, 340, 680, "7h 15m", "Moderada", "Entrada a la ciudad amurallada de Lugo."),
                PredefinedStageInfo(10, "Etapa 10: Lugo a San Romao da Retorta", "Lugo", "San Romao da Retorta", 19.7, 260, 180, "5h 00m", "Fácil", "Vía romana y entorno rural apacible."),
                PredefinedStageInfo(11, "Etapa 11: San Romao a Melide", "San Romao", "Melide", 28.3, 380, 460, "7h 00m", "Moderada", "Unión en Melide con el Camino Francés."),
                PredefinedStageInfo(12, "Etapa 12: Melide a Santiago de Compostela", "Melide", "Santiago de Compostela", 53.3, 810, 920, "12h 00m", "Muy exigente", "Etapa final épica hasta la Catedral de Santiago.")
            )
        ),
        PredefinedRouteInfo(
            title = "Finisterre y Muxía",
            subtitle = "El epílogo hacia el Fin del Mundo y la Costa da Morte",
            totalKm = 116.5,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Santiago de Compostela a Negreira", "Santiago de Compostela", "Negreira", 21.2, 420, 560, "5h 30m", "Moderada", "Ponte Maceira sobre el río Tambre."),
                PredefinedStageInfo(2, "Etapa 2: Negreira a Olveiroa", "Negreira", "Olveiroa", 33.4, 610, 480, "8h 15m", "Exigente", "Montes gallegos y aldeas de hórreos."),
                PredefinedStageInfo(3, "Etapa 3: Olveiroa a Cee / Corcubión", "Olveiroa", "Cee", 19.8, 220, 480, "5h 00m", "Fácil", "Primera vista al mar Atlántico desde lo alto."),
                PredefinedStageInfo(4, "Etapa 4: Cee a Fisterra (Faro)", "Cee", "Fisterra", 13.7, 290, 280, "3h 45m", "Fácil", "Llegada al Faro de Finisterre, el fin de la tierra."),
                PredefinedStageInfo(5, "Etapa 5: Fisterra a Lires", "Fisterra", "Lires", 13.6, 280, 270, "3h 45m", "Fácil", "Playa de O Rostro y acantilados salvajes."),
                PredefinedStageInfo(6, "Etapa 6: Lires a Muxía", "Lires", "Muxía", 14.8, 310, 320, "4h 00m", "Fácil", "Santuario da Virxe da Barca y la Pedra de Abalar.")
            )
        ),
        PredefinedRouteInfo(
            title = "Camino Inglés",
            subtitle = "De Ferrol a Santiago de Compostela",
            totalKm = 110.8,
            note = "Se recomienda realizar el Camino por el trazado antiguo",
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Ferrol a Neda", "Ferrol", "Neda", 15.0, 160, 150, "3h 45m", "Fácil", "Paseo por la ría de Ferrol."),
                PredefinedStageInfo(2, "Etapa 2: Neda a Pontedeume", "Neda", "Pontedeume", 16.0, 280, 290, "4h 00m", "Moderada", "Paso por Fene y el puente sobre el río Eume."),
                PredefinedStageInfo(3, "Etapa 3: Pontedeume a Betanzos", "Pontedeume", "Betanzos", 20.2, 450, 430, "5h 15m", "Moderada", "Ciudad medieval de Betanzos de los Caballeros."),
                PredefinedStageInfo(4, "Etapa 4: Betanzos a Hospital de Bruma", "Betanzos", "Hospital de Bruma", 28.3, 670, 210, "7h 00m", "Exigente", "Subida pronunciada hacia el altiplano coruñés."),
                PredefinedStageInfo(5, "Etapa 5: Hospital de Bruma a Sigüeiro", "Hospital de Bruma", "Sigüeiro", 24.8, 190, 380, "6h 00m", "Fácil", "Tramo llano y arbolado hacia las puertas de Santiago."),
                PredefinedStageInfo(6, "Etapa 6: Sigüeiro a Santiago de Compostela", "Sigüeiro", "Santiago de Compostela", 16.5, 260, 210, "4h 15m", "Fácil", "Entrada histórica por el norte a la Plaza del Obradoiro.")
            )
        )
    )

    suspend fun createPredefinedCamino(
        routeInfo: PredefinedRouteInfo,
        repository: CaminoRepository,
        startDate: String = "15/05/2026"
    ): Long {
        val camino = CaminoEntity(
            title = routeInfo.title,
            subtitle = routeInfo.subtitle,
            coverPhotoResName = "img_camino_hero",
            pilgrimName = "Peregrino Ultreia",
            startDate = startDate,
            totalEstimatedKm = routeInfo.totalKm,
            isTravelGuide = false
        )
        val caminoId = repository.insertCamino(camino)

        for (stage in routeInfo.stages) {
            val stageEntity = StageEntity(
                caminoId = caminoId,
                dayNumber = stage.dayNumber,
                title = stage.title,
                origin = stage.origin,
                destination = stage.destination,
                distanceKm = stage.distanceKm,
                elevationGainM = stage.elevationGainM,
                elevationLossM = stage.elevationLossM,
                estimatedTime = stage.estimatedTime,
                difficulty = stage.difficulty,
                isCompleted = false,
                stageNotes = if (routeInfo.note != null && stage.dayNumber == 1) "${stage.notes} (${routeInfo.note})" else stage.notes
            )
            repository.insertStage(stageEntity)
        }
        return caminoId
    }
}
