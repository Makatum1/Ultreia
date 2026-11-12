package com.example.data.sample

import com.example.data.gpx.GpxParser
import com.example.data.model.AccommodationEntity
import com.example.data.model.CaminoEntity
import com.example.data.model.GpxPoint
import com.example.data.model.StageEntity

object SampleData {

    // Helper to generate simulated realistic curve points between two coordinates
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
            // Add slight natural curve offset
            val curveFactor = Math.sin(t * Math.PI) * 0.02
            val lat = startLat + (endLat - startLat) * t + curveFactor * 0.5
            val lon = startLon + (endLon - startLon) * t + curveFactor
            // elevation peak or valley
            val ele = startEle + (endEle - startEle) * t + Math.sin(t * Math.PI) * 120.0
            points.add(GpxPoint(lat = lat, lon = lon, ele = ele))
        }
        return points
    }

    suspend fun populateInitialDataIfEmpty(db: com.example.data.db.AppDatabase) {
        val count = db.caminoDao().getCaminosCount()
        if (count > 0) return

        // 1. Camino Francés (El más famoso)
        val francesId = db.caminoDao().insertCamino(
            CaminoEntity(
                title = "Camino Francés",
                subtitle = "De los Pirineos a la tumba del Apóstol Santiago",
                coverPhotoResName = "img_camino_hero",
                pilgrimName = "Peregrino Ultreia",
                startDate = "10/05/2026",
                totalEstimatedKm = 780.0
            )
        )

        // Stages for Camino Francés
        val stagesFrances = listOf(
            StageEntity(
                caminoId = francesId,
                dayNumber = 1,
                title = "Etapa 1: Saint-Jean-Pied-de-Port a Roncesvalles",
                origin = "Saint-Jean-Pied-de-Port",
                destination = "Roncesvalles",
                distanceKm = 25.1,
                elevationGainM = 1250,
                elevationLossM = 480,
                estimatedTime = "7h 30m",
                difficulty = "Muy exigente",
                isCompleted = true,
                completedDate = "10/05/2026",
                stageNotes = "Ruta de Napoleón por el Collado de Lepoeder. Subida imponente con vistas panorámicas de los valles vasco-franceses. Asegurar agua antes de salir de Huntto.",
                diaryNotes = "¡Emocionante comienzo! El esfuerzo en los Pirineos ha sido descomunal pero la niebla abriéndose en la cima fue mágico. Conocí a peregrinos de Alemania y Brasil.",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(43.1631, -1.2358, 43.0093, -1.3197, 180.0, 950.0, 30)
                )
            ),
            StageEntity(
                caminoId = francesId,
                dayNumber = 2,
                title = "Etapa 2: Roncesvalles a Zubiri",
                origin = "Roncesvalles",
                destination = "Zubiri",
                distanceKm = 21.4,
                elevationGainM = 290,
                elevationLossM = 750,
                estimatedTime = "5h 45m",
                difficulty = "Moderada",
                isCompleted = true,
                completedDate = "11/05/2026",
                stageNotes = "Bosques de hayas y robledales navarros. Bajada técnica por el Alto de Erro con terreno suelto hacia el puente de la Rabia sobre el río Arga.",
                diaryNotes = "El cruce del puente de la Rabia en Zubiri alivia los pies en el agua helada. La bajada rocosa puso a prueba las rodillas, pero el ambiente en el albergue es único.",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(43.0093, -1.3197, 42.9298, -1.5034, 950.0, 526.0, 25)
                )
            ),
            StageEntity(
                caminoId = francesId,
                dayNumber = 3,
                title = "Etapa 3: Zubiri a Pamplona",
                origin = "Zubiri",
                destination = "Pamplona",
                distanceKm = 20.4,
                elevationGainM = 210,
                elevationLossM = 310,
                estimatedTime = "5h 00m",
                difficulty = "Fácil",
                isCompleted = true,
                completedDate = "12/05/2026",
                stageNotes = "Etapa que acompaña el curso del Arga pasando por Larrasoaña y Trinidad de Arre. Entrada a la capital navarra por el portal de Francia y las murallas históricas.",
                diaryNotes = "Caminata suave y reconfortante. Entrar por el puente levadizo a la ciudadela amurallada te hace sentir en la época medieval. Unos pinchos en la Plaza del Castillo.",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.9298, -1.5034, 42.8125, -1.6458, 526.0, 445.0, 25)
                )
            ),
            StageEntity(
                caminoId = francesId,
                dayNumber = 4,
                title = "Etapa 4: Sarria a Portomarín",
                origin = "Sarria",
                destination = "Portomarín",
                distanceKm = 22.2,
                elevationGainM = 350,
                elevationLossM = 420,
                estimatedTime = "5h 30m",
                difficulty = "Moderada",
                isCompleted = false,
                stageNotes = "Comienzo de los últimos 100 km tradicionales. Correderas gallegas flanqueadas por robles carballos y prados verdes. Entrada cruzando el gran puente sobre el río Miño.",
                diaryNotes = "Etapa planificada con entusiasmo para los tramos finales.",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.7797, -7.4146, 42.8083, -7.6167, 450.0, 380.0, 25)
                )
            ),
            StageEntity(
                caminoId = francesId,
                dayNumber = 5,
                title = "Etapa 5: Portomarín a Palas de Rei",
                origin = "Portomarín",
                destination = "Palas de Rei",
                distanceKm = 24.8,
                elevationGainM = 450,
                elevationLossM = 320,
                estimatedTime = "6h 00m",
                difficulty = "Moderada",
                isCompleted = false,
                stageNotes = "Ascenso paulatino hacia la Sierra de Ligonde. Aldeas tradicionales gallegas con hórreos centenarios y cruceiros tallados en granito.",
                diaryNotes = "",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.8083, -7.6167, 42.8736, -7.8683, 380.0, 560.0, 25)
                )
            ),
            StageEntity(
                caminoId = francesId,
                dayNumber = 6,
                title = "Etapa 6: O Pedrouzo a Santiago de Compostela",
                origin = "O Pedrouzo",
                destination = "Santiago de Compostela",
                distanceKm = 19.4,
                elevationGainM = 310,
                elevationLossM = 350,
                estimatedTime = "4h 45m",
                difficulty = "Fácil",
                isCompleted = false,
                stageNotes = "¡La última etapa! Paso por Lavacolla y subida al Monte do Gozo donde se divisan por primera vez las torres de la Catedral. Llegada a la Plaza del Obradoiro.",
                diaryNotes = "El sueño de llegar al Obradoiro.",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.9050, -8.3610, 42.8805, -8.5456, 300.0, 260.0, 25)
                )
            )
        )

        for (stage in stagesFrances) {
            val stageId = db.caminoDao().insertStage(stage)

            // Add accommodations for sample stages
            when (stage.dayNumber) {
                1 -> {
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Albergue de Peregrinos de Roncesvalles",
                            address = "Calle Única s/n, 31650 Roncesvalles, Navarra",
                            phone = "+34 948 760 000",
                            roomOptionsJson = """[{"roomType":"Litera albergue colegiata","priceEur":14.0},{"roomType":"Habitación doble privada","priceEur":55.0}]""",
                            notes = "Albergue en la histórica Colegiata de Roncesvalles. Dispone de cocina, taquillas y servicio de lavandería.",
                            isBooked = true
                        )
                    )
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Hotel Roncesvalles",
                            address = "Plaza de Roncesvalles, 31650 Navarra",
                            phone = "+34 948 760 105",
                            roomOptionsJson = """[{"roomType":"Habitación individual con baño","priceEur":48.0},{"roomType":"Habitación doble estándar","priceEur":75.0}]""",
                            notes = "Excelente opción si buscas descanso tranquilo tras subir los Pirineos."
                        )
                    )
                }
                2 -> {
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Albergue Municipal de Zubiri",
                            address = "Calle Río Arga 2, 31630 Zubiri, Navarra",
                            phone = "+34 948 304 047",
                            roomOptionsJson = """[{"roomType":"Cama en litera compartida","priceEur":12.0}]""",
                            notes = "Junto al río Arga y el famoso Puente de la Rabia. Muy buen trato al peregrino.",
                            isBooked = true
                        )
                    )
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Pensión Zaldiko",
                            address = "Avenida Valle de Esteribar 14, Zubiri",
                            phone = "+34 676 123 456",
                            roomOptionsJson = """[{"roomType":"Habitación doble privada con baño","priceEur":50.0},{"roomType":"Habitación triple familiar","priceEur":70.0}]""",
                            notes = "Desayuno casero incluido con mermeladas de la zona."
                        )
                    )
                }
                3 -> {
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Albergue Jesús y María de Pamplona",
                            address = "Calle Compañía 4, 31001 Pamplona, Navarra",
                            phone = "+34 948 222 644",
                            roomOptionsJson = """[{"roomType":"Cama en nave histórica con wifi","priceEur":11.0}]""",
                            notes = "Ubicado en una antigua iglesia rehabilitada en pleno casco viejo de Pamplona.",
                            isBooked = true
                        )
                    )
                }
                6 -> {
                    db.caminoDao().insertAccommodation(
                        AccommodationEntity(
                            stageId = stageId,
                            name = "Hospedería San Martín Pinario",
                            address = "Plaza de la Inmaculada 3, 15704 Santiago de Compostela",
                            phone = "+34 981 560 282",
                            roomOptionsJson = """[{"roomType":"Habitación del Peregrino individual","priceEur":32.0},{"roomType":"Habitación doble con vistas","priceEur":65.0}]""",
                            notes = "Frente a la fachada norte de la Catedral. Lugar sagrado y de descanso supremo para celebrar el fin de camino."
                        )
                    )
                }
            }
        }

        // 2. Camino Portugués
        val portuguesId = db.caminoDao().insertCamino(
            CaminoEntity(
                title = "Camino Portugués",
                subtitle = "De Tui a Santiago entre viñedos de albariño y rías gallegas",
                coverPhotoResName = "img_camino_hero",
                pilgrimName = "Peregrino Ultreia",
                startDate = "01/09/2026",
                totalEstimatedKm = 118.0
            )
        )

        val stagesPortugues = listOf(
            StageEntity(
                caminoId = portuguesId,
                dayNumber = 1,
                title = "Etapa 1: Tui a O Porriño",
                origin = "Tui",
                destination = "O Porriño",
                distanceKm = 16.5,
                elevationGainM = 190,
                elevationLossM = 180,
                estimatedTime = "4h 00m",
                difficulty = "Fácil",
                isCompleted = false,
                stageNotes = "Cruce del Puente Internacional sobre el río Miño. Visita obligada a la imponente Catedral fortificada de Santa María de Tui.",
                diaryNotes = "",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.0465, -8.6444, 42.1611, -8.6206, 60.0, 45.0, 20)
                )
            ),
            StageEntity(
                caminoId = portuguesId,
                dayNumber = 2,
                title = "Etapa 2: O Porriño a Redondela",
                origin = "O Porriño",
                destination = "Redondela",
                distanceKm = 15.2,
                elevationGainM = 240,
                elevationLossM = 260,
                estimatedTime = "3h 45m",
                difficulty = "Fácil",
                isCompleted = false,
                stageNotes = "Paso por Mos y su iglesia de Santa Eulalia. Bellas vistas de la ensenada de San Simón en la Ría de Vigo.",
                diaryNotes = "",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.1611, -8.6206, 42.2831, -8.6097, 45.0, 20.0, 20)
                )
            ),
            StageEntity(
                caminoId = portuguesId,
                dayNumber = 3,
                title = "Etapa 3: Redondela a Pontevedra",
                origin = "Redondela",
                destination = "Pontevedra",
                distanceKm = 19.8,
                elevationGainM = 360,
                elevationLossM = 350,
                estimatedTime = "5h 15m",
                difficulty = "Moderada",
                isCompleted = false,
                stageNotes = "Paso por Arcade, famosa por sus ostras, y el histórico puente medieval de Ponte Sampaio sobre el río Verdugo.",
                diaryNotes = "",
                gpxTrackJson = GpxParser.serializePoints(
                    generateTrackPoints(42.2831, -8.6097, 42.4310, -8.6444, 20.0, 30.0, 20)
                )
            )
        )

        for (stg in stagesPortugues) {
            val sId = db.caminoDao().insertStage(stg)
            if (stg.dayNumber == 1) {
                db.caminoDao().insertAccommodation(
                    AccommodationEntity(
                        stageId = sId,
                        name = "Albergue Santo Cristo da Agonía",
                        address = "Rúa Bispo Carrascosa 14, 36700 Tui, Pontevedra",
                        phone = "+34 986 601 234",
                        roomOptionsJson = """[{"roomType":"Litera albergue","priceEur":12.0},{"roomType":"Habitación doble privada","priceEur":42.0}]""",
                        notes = "Cerca de la catedral y de la oficina de acogida al peregrino."
                    )
                )
            }
        }
    }
}
