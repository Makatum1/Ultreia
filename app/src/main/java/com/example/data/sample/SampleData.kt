package com.example.data.sample

import com.example.data.gpx.GpxParser
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity
import com.example.data.repository.CaminoRepository
import com.example.ui.screens.TownCoordinates

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
        // 1. Camino Francés (últimos 100 km) - Primero en la lista
        PredefinedRouteInfo(
            title = "Camino Francés (últimos 100 km)",
            subtitle = "Los últimos 100 km de Sarria a Santiago de Compostela",
            totalKm = 114.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Sarria a Portomarín", "Sarria", "Portomarín", 22.2, 350, 420, "5h 30m", "Moderada", "Comienzo de los últimos 100 km tradicionales gallegos."),
                PredefinedStageInfo(2, "Etapa 2: Portomarín a Palas de Rei", "Portomarín", "Palas de Rei", 24.8, 450, 320, "6h 00m", "Moderada", "Ascenso hacia la Sierra de Ligonde y cruceiros."),
                PredefinedStageInfo(3, "Etapa 3: Palas de Rei a Arzúa", "Palas de Rei", "Arzúa", 28.5, 410, 480, "6h 45m", "Moderada", "Paso por Melide y unión con el Camino Primitivo."),
                PredefinedStageInfo(4, "Etapa 4: Arzúa a O Pedrouzo", "Arzúa", "O Pedrouzo", 19.3, 280, 320, "4h 45m", "Fácil", "Senderos flanqueados por robles y eucaliptos."),
                PredefinedStageInfo(5, "Etapa 5: O Pedrouzo a Santiago de Compostela", "O Pedrouzo", "Santiago de Compostela", 19.4, 310, 350, "4h 45m", "Fácil", "Subida al Monte do Gozo y llegada triunfal a la Catedral de Santiago.")
            )
        ),

        // 2. Camino Francés Completo
        PredefinedRouteInfo(
            title = "Camino Francés",
            subtitle = "De Saint-Jean-Pied-de-Port a Santiago por la ruta jacobea histórica",
            totalKm = 779.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Saint-Jean-Pied-de-Port a Roncesvalles", "Saint-Jean-Pied-de-Port", "Roncesvalles", 25.1, 1250, 480, "7h 30m", "Muy exigente", "Ruta de Napoleón por el Collado de Lepoeder."),
                PredefinedStageInfo(2, "Etapa 2: Roncesvalles a Zubiri", "Roncesvalles", "Zubiri", 21.4, 290, 750, "5h 45m", "Moderada", "Bosques de hayas navarros y Puente de la Rabia."),
                PredefinedStageInfo(3, "Etapa 3: Zubiri a Pamplona", "Zubiri", "Pamplona", 20.4, 210, 310, "5h 00m", "Fácil", "Entrada por el portal de Francia y las murallas históricas."),
                PredefinedStageInfo(4, "Etapa 4: Pamplona a Puente la Reina", "Pamplona", "Puente la Reina", 23.9, 430, 490, "5h 45m", "Moderada", "Subida al Alto del Perdón y vistas panorámicas."),
                PredefinedStageInfo(5, "Etapa 5: Puente la Reina a Estella", "Puente la Reina", "Estella", 21.6, 380, 350, "5h 15m", "Moderada", "Paso por Cirauqui y su calzada romana."),
                PredefinedStageInfo(6, "Etapa 6: Estella a Los Arcos", "Estella", "Los Arcos", 21.3, 290, 310, "5h 00m", "Fácil", "Monasterio de Irache y su fuente del vino."),
                PredefinedStageInfo(7, "Etapa 7: Los Arcos a Logroño", "Los Arcos", "Logroño", 27.6, 390, 460, "6h 30m", "Moderada", "Paso por Viana y entrada a La Rioja."),
                PredefinedStageInfo(8, "Etapa 8: Logroño a Nájera", "Logroño", "Nájera", 29.0, 410, 360, "6h 45m", "Moderada", "Entre viñedos riojanos y parque de La Grajera."),
                PredefinedStageInfo(9, "Etapa 9: Nájera a Santo Domingo de la Calzada", "Nájera", "Santo Domingo de la Calzada", 20.7, 320, 210, "5h 00m", "Fácil", "Catedral de Santo Domingo y el milagro del gallo y la gallina."),
                PredefinedStageInfo(10, "Etapa 10: Santo Domingo de la Calzada a Belorado", "Santo Domingo de la Calzada", "Belorado", 22.0, 280, 190, "5h 15m", "Fácil", "Entrada en la provincia de Burgos."),
                PredefinedStageInfo(11, "Etapa 11: Belorado a San Juan de Ortega", "Belorado", "San Juan de Ortega", 23.9, 420, 180, "5h 45m", "Moderada", "Montes de Oca y monasterio románico."),
                PredefinedStageInfo(12, "Etapa 12: San Juan de Ortega a Burgos", "San Juan de Ortega", "Burgos", 25.8, 190, 310, "6h 00m", "Fácil", "Paso por Atapuerca y entrada a la ciudad gótica."),
                PredefinedStageInfo(13, "Etapa 13: Burgos a Hornillos del Camino", "Burgos", "Hornillos del Camino", 21.0, 220, 210, "5h 00m", "Fácil", "Comienzo de la gran meseta castellana."),
                PredefinedStageInfo(14, "Etapa 14: Hornillos del Camino a Castrojeriz", "Hornillos del Camino", "Castrojeriz", 20.1, 190, 230, "4h 45m", "Fácil", "Paso bajo los arcos góticos de San Antón."),
                PredefinedStageInfo(15, "Etapa 15: Castrojeriz a Frómista", "Castrojeriz", "Frómista", 24.7, 280, 310, "5h 45m", "Moderada", "Alto de Mostelares y Canal de Castilla."),
                PredefinedStageInfo(16, "Etapa 16: Frómista a Carrión de los Condes", "Frómista", "Carrión de los Condes", 18.8, 110, 80, "4h 30m", "Fácil", "Iglesia románica de San Martín y llanura de Tierra de Campos."),
                PredefinedStageInfo(17, "Etapa 17: Carrión de los Condes a Ledigos", "Carrión de los Condes", "Ledigos", 23.4, 140, 90, "5h 30m", "Fácil", "Antigua vía romana de los peregrinos."),
                PredefinedStageInfo(18, "Etapa 18: Ledigos a Sahagún", "Ledigos", "Sahagún", 16.2, 90, 110, "4h 00m", "Fácil", "Entrada a la provincia de León."),
                PredefinedStageInfo(19, "Etapa 19: Sahagún a El Burgo Ranero", "Sahagún", "El Burgo Ranero", 17.7, 110, 90, "4h 15m", "Fácil", "Senda arbolada tradicional del peregrino."),
                PredefinedStageInfo(20, "Etapa 20: El Burgo Ranero a Mansilla de las Mulas", "El Burgo Ranero", "Mansilla de las Mulas", 18.8, 80, 140, "4h 30m", "Fácil", "Murallas históricas junto al río Esla."),
                PredefinedStageInfo(21, "Etapa 21: Mansilla de las Mulas a León", "Mansilla de las Mulas", "León", 18.5, 150, 110, "4h 30m", "Fácil", "Llegada a la Catedral de León (Pulchra Leonina)."),
                PredefinedStageInfo(22, "Etapa 22: León a San Martín del Camino", "León", "San Martín del Camino", 24.6, 130, 110, "5h 45m", "Fácil", "Páramo leonés y Virgen del Camino."),
                PredefinedStageInfo(23, "Etapa 23: San Martín del Camino a Astorga", "San Martín del Camino", "Astorga", 23.7, 180, 130, "5h 30m", "Fácil", "Puente del Paso Honroso en Hospital de Órbigo."),
                PredefinedStageInfo(24, "Etapa 24: Astorga a Rabanal del Camino", "Astorga", "Rabanal del Camino", 20.1, 310, 40, "5h 00m", "Moderada", "Comienzo del ascenso hacia los Montes de León."),
                PredefinedStageInfo(25, "Etapa 25: Rabanal del Camino a Ponferrada", "Rabanal del Camino", "Ponferrada", 32.2, 450, 980, "7h 45m", "Exigente", "Cruz de Ferro en Foncebadón y descenso por El Bierzo."),
                PredefinedStageInfo(26, "Etapa 26: Ponferrada a Villafranca del Bierzo", "Ponferrada", "Villafranca del Bierzo", 24.2, 210, 190, "5h 45m", "Fácil", "Castillo templario y viñedos de mencía."),
                PredefinedStageInfo(27, "Etapa 27: Villafranca del Bierzo a O Cebreiro", "Villafranca del Bierzo", "O Cebreiro", 27.8, 980, 190, "7h 30m", "Muy exigente", "Mítico ascenso a Galicia y pallozas de O Cebreiro."),
                PredefinedStageInfo(28, "Etapa 28: O Cebreiro a Triacastela", "O Cebreiro", "Triacastela", 20.8, 250, 840, "5h 00m", "Moderada", "Alto de San Roque, monumento al peregrino y Alto do Poio."),
                PredefinedStageInfo(29, "Etapa 29: Triacastela a Sarria", "Triacastela", "Sarria", 18.4, 280, 410, "4h 30m", "Fácil", "Camino tradicional por San Xil y castaños milenarios."),
                PredefinedStageInfo(30, "Etapa 30: Sarria a Portomarín", "Sarria", "Portomarín", 22.2, 350, 420, "5h 30m", "Moderada", "Comienzo de los últimos 100 km tradicionales gallegos."),
                PredefinedStageInfo(31, "Etapa 31: Portomarín a Palas de Rei", "Portomarín", "Palas de Rei", 24.8, 450, 320, "6h 00m", "Moderada", "Ascenso hacia la Sierra de Ligonde y cruceiros."),
                PredefinedStageInfo(32, "Etapa 32: Palas de Rei a Arzúa", "Palas de Rei", "Arzúa", 28.5, 410, 480, "6h 45m", "Moderada", "Paso por Melide y bosques coruñeses."),
                PredefinedStageInfo(33, "Etapa 33: Arzúa a Santiago de Compostela", "Arzúa", "Santiago de Compostela", 38.7, 590, 670, "8h 45m", "Exigente", "Tramo final por O Pedrouzo, Monte do Gozo y Plaza del Obradoiro.")
            )
        ),

        // 3. Camino Primitivo
        PredefinedRouteInfo(
            title = "Camino Primitivo",
            subtitle = "La primera ruta de peregrinación por la montaña astur-galaica",
            totalKm = 309.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Oviedo a Grado", "Oviedo", "Grado", 25.6, 420, 610, "6h 30m", "Moderada", "Salida de la catedral de San Salvador."),
                PredefinedStageInfo(2, "Etapa 2: Grado a Salas", "Grado", "Salas", 22.2, 580, 410, "5h 45m", "Moderada", "Alto del Fresno y santuario de la Virgen."),
                PredefinedStageInfo(3, "Etapa 3: Salas a Tineo", "Salas", "Tineo", 20.2, 690, 180, "5h 30m", "Exigente", "Subida pronunciada hacia las brañas de Tineo."),
                PredefinedStageInfo(4, "Etapa 4: Tineo a Pola de Allande", "Tineo", "Pola de Allande", 28.2, 580, 720, "7h 00m", "Exigente", "Paso por el monasterio de Obona."),
                PredefinedStageInfo(5, "Etapa 5: Pola de Allande a La Mesa", "Pola de Allande", "La Mesa", 23.3, 980, 510, "6h 30m", "Muy exigente", "Paso mítico por el Puerto del Palo y Hospitales."),
                PredefinedStageInfo(6, "Etapa 6: La Mesa a Grandas de Salime", "La Mesa", "Grandas de Salime", 15.5, 290, 780, "4h 30m", "Moderada", "Descenso al embalse de Salime y vistas increíbles."),
                PredefinedStageInfo(7, "Etapa 7: Grandas de Salime a A Fonsagrada", "Grandas de Salime", "A Fonsagrada", 25.2, 850, 310, "7h 00m", "Muy exigente", "Entrada a Galicia por el Alto do Acebo."),
                PredefinedStageInfo(8, "Etapa 8: A Fonsagrada a O Cádavo", "A Fonsagrada", "O Cádavo", 23.4, 560, 710, "6h 30m", "Moderada", "Hospital de Montouto y molinos de viento."),
                PredefinedStageInfo(9, "Etapa 9: O Cádavo a Lugo", "O Cádavo", "Lugo", 30.3, 340, 680, "7h 15m", "Moderada", "Entrada a la ciudad amurallada de Lugo."),
                PredefinedStageInfo(10, "Etapa 10: Lugo a San Romao da Retorta", "Lugo", "San Romao da Retorta", 19.6, 260, 180, "5h 00m", "Fácil", "Vía romana y entorno rural apacible."),
                PredefinedStageInfo(11, "Etapa 11: San Romao a Melide", "San Romao", "Melide", 28.1, 380, 460, "7h 00m", "Moderada", "Unión en Melide con el Camino Francés."),
                PredefinedStageInfo(12, "Etapa 12: Melide a O Pedrouzo", "Melide", "O Pedrouzo", 33.3, 450, 510, "8h 00m", "Exigente", "Paso por Arzúa y bosques gallegos."),
                PredefinedStageInfo(13, "Etapa 13: O Pedrouzo a Santiago de Compostela", "O Pedrouzo", "Santiago de Compostela", 19.2, 310, 350, "4h 45m", "Fácil", "Llegada triunfal a la Plaza del Obradoiro.")
            )
        ),

        // 4. Camino del Norte
        PredefinedRouteInfo(
            title = "Camino del Norte",
            subtitle = "De Ribadeo a Santiago por el interior de Galicia",
            totalKm = 195.9,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Ribadeo a Lourenzá", "Ribadeo", "Lourenzá", 28.4, 620, 480, "7h 00m", "Exigente", "Entrada a Galicia desde la ría del Eo."),
                PredefinedStageInfo(2, "Etapa 2: Lourenzá a Abadín", "Lourenzá", "Abadín", 24.8, 680, 210, "6h 15m", "Moderada", "Mondoñedo y ascenso a la meseta lucense."),
                PredefinedStageInfo(3, "Etapa 3: Abadín a Vilalba", "Abadín", "Vilalba", 20.6, 180, 210, "5h 00m", "Fácil", "Caminata llana por la comarca de Terra Chá."),
                PredefinedStageInfo(4, "Etapa 4: Vilalba a Baamonde", "Vilalba", "Baamonde", 20.7, 160, 180, "5h 00m", "Fácil", "Puente medieval de Saa y castaño milenario."),
                PredefinedStageInfo(5, "Etapa 5: Baamonde a Sobrado dos Monxes", "Baamonde", "Sobrado dos Monxes", 40.3, 620, 540, "9h 30m", "Muy exigente", "Monasterio cisterciense de Santa María."),
                PredefinedStageInfo(6, "Etapa 6: Sobrado dos Monxes a Arzúa", "Sobrado dos Monxes", "Arzúa", 21.4, 260, 340, "5h 15m", "Fácil", "Enlace con el Camino Francés."),
                PredefinedStageInfo(7, "Etapa 7: Arzúa a O Pedrouzo", "Arzúa", "O Pedrouzo", 19.3, 280, 320, "4h 45m", "Fácil", "Senderos comunes arbolados hacia Santiago."),
                PredefinedStageInfo(8, "Etapa 8: O Pedrouzo a Santiago de Compostela", "O Pedrouzo", "Santiago de Compostela", 19.4, 310, 350, "4h 45m", "Fácil", "Tramo final hasta la Plaza del Obradoiro.")
            )
        ),

        // 5. Camino Portugués Central
        PredefinedRouteInfo(
            title = "Camino Portugués Central",
            subtitle = "De Tui a Santiago entre viñedos de albariño y rías gallegas",
            totalKm = 115.5,
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

        // 6. Camino Inglés
        PredefinedRouteInfo(
            title = "Camino Inglés",
            subtitle = "De Ferrol a Santiago de Compostela",
            totalKm = 120.8,
            note = "Se recomienda realizar el Camino por el trazado antiguo",
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Ferrol a Neda", "Ferrol", "Neda", 15.0, 160, 150, "3h 45m", "Fácil", "Paseo por la ría de Ferrol."),
                PredefinedStageInfo(2, "Etapa 2: Neda a Pontedeume", "Neda", "Pontedeume", 16.0, 280, 290, "4h 00m", "Moderada", "Paso por Fene y el puente sobre el río Eume."),
                PredefinedStageInfo(3, "Etapa 3: Pontedeume a Betanzos", "Pontedeume", "Betanzos", 20.2, 450, 430, "5h 15m", "Moderada", "Ciudad medieval de Betanzos de los Caballeros."),
                PredefinedStageInfo(4, "Etapa 4: Betanzos a Hospital de Bruma", "Betanzos", "Hospital de Bruma", 28.3, 670, 210, "7h 00m", "Exigente", "Subida pronunciada hacia el altiplano coruñés."),
                PredefinedStageInfo(5, "Etapa 5: Hospital de Bruma a Sigüeiro", "Hospital de Bruma", "Sigüeiro", 24.8, 190, 380, "6h 00m", "Fácil", "Tramo llano y arbolado hacia las puertas de Santiago."),
                PredefinedStageInfo(6, "Etapa 6: Sigüeiro a Santiago de Compostela", "Sigüeiro", "Santiago de Compostela", 16.5, 260, 210, "4h 15m", "Fácil", "Entrada histórica por el norte a la Plaza del Obradoiro.")
            )
        ),

        // 7. Vía de la Plata
        PredefinedRouteInfo(
            title = "Vía de la Plata",
            subtitle = "La gran calzada romana de Sevilla a Astorga",
            totalKm = 705.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Sevilla a Guillena", "Sevilla", "Guillena", 22.8, 80, 60, "5h 15m", "Fácil", "Salida desde la catedral de Sevilla por Santiponce e Itálica."),
                PredefinedStageInfo(2, "Etapa 2: Guillena a Castilblanco de los Arroyos", "Guillena", "Castilblanco de los Arroyos", 18.2, 340, 40, "4h 30m", "Fácil", "Comienzo de las estribaciones de Sierra Morena."),
                PredefinedStageInfo(3, "Etapa 3: Castilblanco a Almadén de la Plata", "Castilblanco de los Arroyos", "Almadén de la Plata", 28.8, 480, 390, "7h 00m", "Exigente", "Parque Natural de la Sierra Norte y dehesas."),
                PredefinedStageInfo(4, "Etapa 4: Almadén de la Plata a Monesterio", "Almadén de la Plata", "Monesterio", 34.4, 520, 310, "8h 15m", "Exigente", "Paso por El Real de la Jara y entrada en Extremadura."),
                PredefinedStageInfo(5, "Etapa 5: Monesterio a Fuente de Cantos", "Monesterio", "Fuente de Cantos", 21.2, 90, 320, "5h 00m", "Fácil", "Descenso suave hacia los campos de Tentudía."),
                PredefinedStageInfo(6, "Etapa 6: Fuente de Cantos a Zafra", "Fuente de Cantos", "Zafra", 24.6, 140, 180, "5h 45m", "Fácil", "Paso por Calzadilla de los Barros y llegada a la bella Zafra."),
                PredefinedStageInfo(7, "Etapa 7: Zafra a Villafranca de los Barros", "Zafra", "Villafranca de los Barros", 19.2, 110, 170, "4h 30m", "Fácil", "Paso por Los Santos de Maimona y viñedos de Tierra de Barros."),
                PredefinedStageInfo(8, "Etapa 8: Villafranca de los Barros a Almendralejo", "Villafranca de los Barros", "Almendralejo", 13.0, 70, 90, "3h 15m", "Fácil", "Jornada corta entre viñedos y olivares pacenses."),
                PredefinedStageInfo(9, "Etapa 9: Almendralejo a Torremejía", "Almendralejo", "Torremejía", 13.1, 50, 70, "3h 15m", "Fácil", "Llanura extremeña hacia la cuenca del Guadiana."),
                PredefinedStageInfo(10, "Etapa 10: Torremejía a Mérida", "Torremejía", "Mérida", 15.7, 90, 160, "4h 00m", "Fácil", "Entrada triunfal por el puente romano de Mérida."),
                PredefinedStageInfo(11, "Etapa 11: Mérida a Alcuéscar", "Mérida", "Alcuéscar", 36.4, 380, 180, "8h 30m", "Exigente", "Embalse romano de Proserpina y dehesas cacereñas."),
                PredefinedStageInfo(12, "Etapa 12: Alcuéscar a Cáceres", "Alcuéscar", "Cáceres", 38.2, 280, 290, "8h 45m", "Exigente", "Miliarios romanos y llegada al conjunto medieval de Cáceres."),
                PredefinedStageInfo(13, "Etapa 13: Cáceres a Cañaveral", "Cáceres", "Cañaveral", 33.4, 240, 310, "7h 45m", "Exigente", "Cruce del río Tajo por el puente del embalse de Alcántara."),
                PredefinedStageInfo(14, "Etapa 14: Cañaveral a Galisteo", "Cañaveral", "Galisteo", 28.2, 210, 320, "6h 45m", "Moderada", "Puerto de los Castaños y muralla almohade de Galisteo."),
                PredefinedStageInfo(15, "Etapa 15: Galisteo a Cáparra", "Galisteo", "Cáparra", 29.7, 230, 190, "7h 00m", "Moderada", "Yacimiento arqueológico romano y arco tetrapilo de Cáparra."),
                PredefinedStageInfo(16, "Etapa 16: Cáparra a Baños de Montemayor", "Cáparra", "Baños de Montemayor", 28.5, 410, 140, "6h 45m", "Moderada", "Ascenso suave al valle del Ambroz y termas romanas."),
                PredefinedStageInfo(17, "Etapa 17: Baños de Montemayor a Calzada de Béjar", "Baños de Montemayor", "Calzada de Béjar", 15.5, 360, 210, "4h 00m", "Fácil", "Puerto de Béjar y entrada en Castilla y León."),
                PredefinedStageInfo(18, "Etapa 18: Calzada de Béjar a Fuenterroble", "Calzada de Béjar", "Fuenterroble de Salvatierra", 20.2, 210, 180, "5h 00m", "Fácil", "Calzada romana bien conservada y dehesas salmantinas."),
                PredefinedStageInfo(19, "Etapa 19: Fuenterroble a San Pedro de Rozados", "Fuenterroble de Salvatierra", "San Pedro de Rozados", 28.6, 290, 250, "6h 45m", "Moderada", "Subida al Pico Dueña, punto más alto de la Vía de la Plata."),
                PredefinedStageInfo(20, "Etapa 20: San Pedro de Rozados a Salamanca", "San Pedro de Rozados", "Salamanca", 23.0, 90, 280, "5h 30m", "Fácil", "Entrada histórica por el puente romano de Salamanca."),
                PredefinedStageInfo(21, "Etapa 21: Salamanca a El Cubo de la Tierra del Vino", "Salamanca", "El Cubo de la Tierra del Vino", 36.4, 210, 160, "8h 30m", "Exigente", "Entrada en la comarca de la Tierra del Vino zamorana."),
                PredefinedStageInfo(22, "Etapa 22: El Cubo de la Tierra del Vino a Zamora", "El Cubo de la Tierra del Vino", "Zamora", 31.8, 140, 260, "7h 30m", "Exigente", "Llegada al río Duero y la ciudad del románico."),
                PredefinedStageInfo(23, "Etapa 23: Zamora a Riego del Camino", "Zamora", "Riego del Camino", 33.8, 180, 140, "7h 45m", "Exigente", "Paso por las ruinas de Castrotorafe y Tierra de Campos."),
                PredefinedStageInfo(24, "Etapa 24: Riego del Camino a Granja de Moreruela", "Riego del Camino", "Granja de Moreruela", 8.0, 40, 50, "2h 00m", "Fácil", "Bifurcación histórica hacia el Camino Sanabrés."),
                PredefinedStageInfo(25, "Etapa 25: Granja de Moreruela a Benavente", "Granja de Moreruela", "Benavente", 25.5, 120, 160, "6h 00m", "Fácil", "Hacia el río Órbigo y los castillos benaventanos."),
                PredefinedStageInfo(26, "Etapa 26: Benavente a Astorga", "Benavente", "Astorga", 46.0, 310, 180, "10h 30m", "Muy exigente", "Paso por Alija y La Bañeza hasta la unión con el Camino Francés.")
            )
        ),

        // 8. Camino Sanabrés
        PredefinedRouteInfo(
            title = "Camino Sanabrés",
            subtitle = "De Granja de Moreruela a Santiago por las sierras de Zamora y Ourense",
            totalKm = 367.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Granja de Moreruela a Tábara", "Granja de Moreruela", "Tábara", 25.3, 190, 140, "6h 00m", "Moderada", "Salida de la Vía de la Plata hacia la sierra de la Culebra."),
                PredefinedStageInfo(2, "Etapa 2: Tábara a Santa Croya de Tera", "Tábara", "Santa Croya de Tera", 22.5, 160, 180, "5h 15m", "Fácil", "Ribera del río Tera y tierras fértiles zamoranas."),
                PredefinedStageInfo(3, "Etapa 3: Santa Croya de Tera a Rionegro del Puente", "Santa Croya de Tera", "Rionegro del Puente", 27.5, 240, 120, "6h 30m", "Moderada", "Santuario de la Virgen de la Carballeda."),
                PredefinedStageInfo(4, "Etapa 4: Rionegro del Puente a Puebla de Sanabria", "Rionegro del Puente", "Puebla de Sanabria", 40.5, 410, 280, "9h 30m", "Muy exigente", "Villa medieval fortificada sobre el río Castro."),
                PredefinedStageInfo(5, "Etapa 5: Puebla de Sanabria a Lubián", "Puebla de Sanabria", "Lubián", 28.5, 620, 480, "7h 00m", "Exigente", "Puerto del Padornelo y entrada en la Alta Sanabria."),
                PredefinedStageInfo(6, "Etapa 6: Lubián a A Gudiña", "Lubián", "A Gudiña", 24.0, 580, 420, "6h 00m", "Moderada", "Paso por A Canda y entrada en Galicia."),
                PredefinedStageInfo(7, "Etapa 7: A Gudiña a Laza", "A Gudiña", "Laza", 34.5, 460, 890, "8h 15m", "Exigente", "Descenso pronunciado hacia el valle de Monterrei."),
                PredefinedStageInfo(8, "Etapa 8: Laza a Xunqueira de Ambía", "Laza", "Xunqueira de Ambía", 32.2, 590, 480, "7h 45m", "Exigente", "Sierra de San Mamede y colegiata románica."),
                PredefinedStageInfo(9, "Etapa 9: Xunqueira de Ambía a Ourense", "Xunqueira de Ambía", "Ourense", 22.0, 140, 490, "5h 15m", "Fácil", "Llegada al río Miño y las termas de As Burgas."),
                PredefinedStageInfo(10, "Etapa 10: Ourense a Cea", "Ourense", "Cea", 22.2, 580, 210, "5h 45m", "Moderada", "Famoso pan de Cea y monasterio de Oseira."),
                PredefinedStageInfo(11, "Etapa 11: Cea a A Laxe", "Cea", "A Laxe", 37.3, 560, 610, "8h 45m", "Exigente", "Paso por Dozón y tierras de Deza."),
                PredefinedStageInfo(12, "Etapa 12: A Laxe a Outeiro", "A Laxe", "Outeiro", 33.0, 410, 490, "7h 45m", "Exigente", "Silleda, Bandeira y puente medieval sobre el río Ulla."),
                PredefinedStageInfo(13, "Etapa 13: Outeiro a Santiago de Compostela", "Outeiro", "Santiago de Compostela", 16.7, 240, 310, "4h 15m", "Fácil", "Último esfuerzo contemplando las torres de la Catedral.")
            )
        ),

        // 9. Camino Vasco del Interior
        PredefinedRouteInfo(
            title = "Camino Vasco del Interior",
            subtitle = "De Irún a Santo Domingo de la Calzada por el túnel de San Adrián",
            totalKm = 203.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Irún a Hernani", "Irún", "Hernani", 27.2, 620, 580, "6h 45m", "Exigente", "Salida fronteriza hacia Oiartzun y Astigarraga."),
                PredefinedStageInfo(2, "Etapa 2: Hernani a Tolosa", "Hernani", "Tolosa", 19.4, 380, 340, "4h 45m", "Fácil", "Valle del río Oria y centro histórico de Tolosa."),
                PredefinedStageInfo(3, "Etapa 3: Tolosa a Zerain", "Tolosa", "Zerain", 23.1, 540, 280, "5h 45m", "Moderada", "Ordizia, Beasain y entorno rural guipuzcoano."),
                PredefinedStageInfo(4, "Etapa 4: Zerain a Salvatierra / Agurain", "Zerain", "Salvatierra", 28.7, 890, 580, "7h 30m", "Muy exigente", "Paso mítico por el Túnel de San Adrián en la sierra de Aizkorri."),
                PredefinedStageInfo(5, "Etapa 5: Salvatierra a Vitoria-Gasteiz", "Salvatierra", "Vitoria-Gasteiz", 27.8, 160, 240, "6h 30m", "Fácil", "Llanura alavesa hacia la capital de Euskadi."),
                PredefinedStageInfo(6, "Etapa 6: Vitoria-Gasteiz a La Puebla de Arganzón", "Vitoria-Gasteiz", "La Puebla de Arganzón", 18.6, 120, 210, "4h 30m", "Fácil", "Enclave de Treviño junto al río Zadorra."),
                PredefinedStageInfo(7, "Etapa 7: La Puebla de Arganzón a Haro", "La Puebla de Arganzón", "Haro", 31.0, 310, 360, "7h 15m", "Moderada", "Conchas de Haro y entrada en la comarca del vino de Rioja."),
                PredefinedStageInfo(8, "Etapa 8: Haro a Santo Domingo de la Calzada", "Haro", "Santo Domingo de la Calzada", 27.2, 290, 180, "6h 30m", "Moderada", "Unión en Santo Domingo de la Calzada con el Camino Francés.")
            )
        ),

        // 10. Camino Baztanés
        PredefinedRouteInfo(
            title = "Camino Baztanés",
            subtitle = "De Bayona a Pamplona a través del valle atlántico de Baztán",
            totalKm = 109.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Bayona a Ustaritz", "Bayona", "Ustaritz", 14.3, 140, 120, "3h 30m", "Fácil", "Paseo llano y agradable siguiendo la ribera del río Nive."),
                PredefinedStageInfo(2, "Etapa 2: Ustaritz a Urdazubi / Urdax", "Ustaritz", "Urdazubi", 21.2, 480, 390, "5h 15m", "Moderada", "Paso por Ainhoa y cruce de la frontera navarra en Dantxarinea."),
                PredefinedStageInfo(3, "Etapa 3: Urdazubi / Urdax a Elizondo", "Urdazubi", "Elizondo", 18.8, 560, 480, "5h 00m", "Moderada", "Subida al puerto de Otsondo y descenso al corazón de Baztán."),
                PredefinedStageInfo(4, "Etapa 4: Elizondo a Berroeta", "Elizondo", "Berroeta", 9.7, 280, 90, "2h 30m", "Fácil", "Jornada corta entre caseríos tradicionales y verdes prados."),
                PredefinedStageInfo(5, "Etapa 5: Berroeta a Olagüe", "Berroeta", "Olagüe", 20.4, 610, 420, "5h 30m", "Exigente", "Paso por el histórico Puerto de Belate y valle de Ultzama."),
                PredefinedStageInfo(6, "Etapa 6: Olagüe a Pamplona", "Olagüe", "Pamplona", 24.6, 210, 390, "5h 45m", "Fácil", "Enlace en la Trinidad de Arre con el Camino Francés.")
            )
        ),

        // 11. Camino de San Salvador
        PredefinedRouteInfo(
            title = "Camino de San Salvador",
            subtitle = "De León a Oviedo por la cordillera cantábrica y el puerto de Pajares",
            totalKm = 122.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: León a La Robla", "León", "La Robla", 26.3, 240, 80, "6h 15m", "Fácil", "Salida de la catedral leonesa siguiendo el curso del río Bernesga."),
                PredefinedStageInfo(2, "Etapa 2: La Robla a Poladura de la Tercia", "La Robla", "Poladura de la Tercia", 23.9, 450, 110, "5h 45m", "Moderada", "Hoces del Bernesga y aproximación a la alta montaña cantábrica."),
                PredefinedStageInfo(3, "Etapa 3: Poladura de la Tercia a Pajares", "Poladura de la Tercia", "Pajares", 15.8, 620, 810, "5h 00m", "Muy exigente", "Paso del Puerto de Pajares (1.500 m) y entrada en el Principado de Asturias."),
                PredefinedStageInfo(4, "Etapa 4: Pajares a Pola de Lena", "Pajares", "Pola de Lena", 26.2, 280, 1050, "6h 30m", "Exigente", "Fuerte descenso por valles mineros y ermita prerrománica de Santa Cristina."),
                PredefinedStageInfo(5, "Etapa 5: Pola de Lena a Oviedo", "Pola de Lena", "Oviedo", 30.4, 420, 590, "7h 15m", "Moderada", "Paso por Mieres y llegada a la basílica del Salvador de Oviedo.")
            )
        ),

        // 12. Epílogo a Fisterra y Muxía
        PredefinedRouteInfo(
            title = "Finisterre y Muxía",
            subtitle = "El epílogo hacia el Fin del Mundo y la Costa da Morte",
            totalKm = 117.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Santiago de Compostela a Negreira", "Santiago de Compostela", "Negreira", 21.0, 420, 560, "5h 30m", "Moderada", "Ponte Maceira sobre el río Tambre y robledales."),
                PredefinedStageInfo(2, "Etapa 2: Negreira a Olveiroa", "Negreira", "Olveiroa", 33.4, 610, 480, "8h 15m", "Exigente", "Montes gallegos y aldeas de hórreos centenarios."),
                PredefinedStageInfo(3, "Etapa 3: Olveiroa a Cee", "Olveiroa", "Cee", 19.8, 220, 480, "5h 00m", "Fácil", "Primera vista al mar Atlántico desde lo alto del monte."),
                PredefinedStageInfo(4, "Etapa 4: Cee a Fisterra", "Cee", "Fisterra", 13.7, 290, 280, "3h 45m", "Fácil", "Llegada al Faro de Finisterre, el legendario fin de la tierra."),
                PredefinedStageInfo(5, "Etapa 5: Fisterra a Muxía", "Fisterra", "Muxía", 29.3, 590, 590, "7h 30m", "Exigente", "Santuario da Virxe da Barca y la Pedra de Abalar en la Costa da Morte.")
            )
        )
    )

    suspend fun createPredefinedCamino(
        routeInfo: PredefinedRouteInfo,
        repository: CaminoRepository,
        startDate: String = "15/05/2026"
    ): Long {
        val isCaminoIngles = routeInfo.title.contains("Inglés", ignoreCase = true) || routeInfo.title.contains("Ingles", ignoreCase = true)
        val initialAttachedFilesJson = if (isCaminoIngles) {
            try {
                val attached = com.example.data.gpx.CaminoInglesGpxProvider.createAttachedFile(repository.context)
                org.json.JSONArray().apply {
                    val obj = org.json.JSONObject().apply {
                        put("id", attached.id)
                        put("name", attached.name)
                        put("uri", attached.uri)
                        put("mimeType", attached.mimeType)
                        put("sizeBytes", attached.sizeBytes)
                        put("addedAt", attached.addedAt)
                    }
                    put(obj)
                }.toString()
            } catch (_: Exception) {
                "[]"
            }
        } else {
            "[]"
        }

        val camino = CaminoEntity(
            title = routeInfo.title,
            subtitle = routeInfo.subtitle,
            coverPhotoResName = "img_camino_hero",
            pilgrimName = "Peregrino Ultreia",
            startDate = startDate,
            totalEstimatedKm = routeInfo.totalKm,
            isTravelGuide = false,
            attachedFilesJson = initialAttachedFilesJson
        )
        val caminoId = repository.insertCamino(camino)

        for (stage in routeInfo.stages) {
            // Generate test GPX track points between start and end coordinates
            val startCoord = TownCoordinates.findCoordinates(stage.origin)
            val endCoord = TownCoordinates.findCoordinates(stage.destination)

            val (sLat, sLon) = when {
                startCoord != null -> startCoord
                endCoord != null -> Pair(endCoord.first + 0.10, endCoord.second + 0.10)
                else -> Pair(42.88 - (stage.dayNumber * 0.05), -8.54 - (stage.dayNumber * 0.08))
            }
            val (eLat, eLon) = when {
                endCoord != null -> endCoord
                startCoord != null -> Pair(startCoord.first - 0.10, startCoord.second - 0.10)
                else -> Pair(sLat - 0.08, sLon - 0.10)
            }

            val testPoints = generateTrackPoints(
                startLat = sLat,
                startLon = sLon,
                endLat = eLat,
                endLon = eLon,
                startEle = 250.0 + (stage.elevationGainM * 0.25),
                endEle = 250.0 + (stage.elevationLossM * 0.20),
                numPoints = 25
            )
            val gpxJson = GpxParser.serializePoints(testPoints)

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
                stageNotes = if (routeInfo.note != null && stage.dayNumber == 1) "${stage.notes} (${routeInfo.note})" else stage.notes,
                attachedFilesJson = initialAttachedFilesJson,
                gpxTrackJson = gpxJson
            )
            repository.insertStage(stageEntity)
        }
        return caminoId
    }
}
