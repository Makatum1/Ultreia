package com.example.data.gpx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxParserTest {

    @Test
    fun testReferenceGpxWithWikilocWaypoints() {
        // Genera una ruta de referencia de aproximadamente 36.34 km
        // Con desnivel +872 m y -610 m (desnivel neto positivo de +262 m)
        // Y 4 waypoints (<wpt>) dispersos a kilómetros de distancia que Wikiloc suele incluir
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="Wikiloc" xmlns="http://www.topografix.com/GPX/1/1">
  <wpt lat="43.5000" lon="-2.0000"><name>Punto Lejano 1</name><ele>1200.0</ele></wpt>
  <wpt lat="41.5000" lon="-1.0000"><name>Punto Lejano 2</name><ele>100.0</ele></wpt>
  <wpt lat="44.0000" lon="-1.5000"><name>Punto Lejano 3</name><ele>800.0</ele></wpt>
  <wpt lat="42.0000" lon="-3.0000"><name>Punto Lejano 4</name><ele>300.0</ele></wpt>
  <trk>
    <name>Etapa Referencia 36.34km</name>
    <trkseg>
""")

        // Base coord: 43.0000, -1.3000
        // Para conseguir 36.34 km con N puntos rectilíneos hacia el norte (1 grado latitud ≈ 111.139 km):
        // 36.34 km ≈ 0.3270 grados latitud
        val totalPoints = 364
        val startLat = 43.00000
        val startLon = -1.30000
        val targetLatDelta = 0.32698 // ~36.34 km

        // Perfil de elevación: arranca en 200m, sube +872m (llega a 1072m), luego baja -610m (llega a 462m)
        val climbPoints = 200
        val descendPoints = totalPoints - climbPoints // 164

        var currentEle = 200.0
        val climbStep = 872.0 / climbPoints
        val descendStep = 610.0 / descendPoints

        var currentTimestamp = 1700000000000L // Epoch base

        for (i in 0 until totalPoints) {
            val progress = i.toDouble() / (totalPoints - 1)
            val lat = startLat + progress * targetLatDelta
            val lon = startLon

            if (i < climbPoints) {
                currentEle += climbStep
            } else {
                currentEle -= descendStep
            }

            // Cada punto avanza 60 segundos (~100m por minuto = 6 km/h > 0.5 m/s)
            val timeIso = java.time.Instant.ofEpochMilli(currentTimestamp).toString()
            sb.append("      <trkpt lat=\"%.5f\" lon=\"%.5f\"><ele>%.1f</ele><time>%s</time></trkpt>\n".format(
                java.util.Locale.US, lat, lon, currentEle, timeIso
            ))
            currentTimestamp += 60000L
        }

        sb.append("""    </trkseg>
  </trk>
</gpx>""")

        val result = GpxParser.parse(sb.toString(), fileName = "referencia_wikiloc.gpx")

        // 1. Validar que los waypoints fueron ignorados
        assertEquals(4, result.ignoredWptCount)
        assertEquals(364, result.totalTrkptCount)

        // 2. REGLA ESTRICTA DE TEST: Si la distancia supera los 40 km, el test DEBE fallar
        assertTrue("La distancia (${result.distanceKm} km) no debe superar los 40 km", result.distanceKm < 40.0)

        // 3. REGLA ESTRICTA DE TEST: Si el desnivel neto es negativo o cero, el test DEBE fallar
        val netElevation = result.elevationGainM - result.elevationLossM
        assertTrue("El desnivel neto ($netElevation m) debe ser positivo", netElevation > 0)

        // 4. Validar precisión con referencia (36.34 km ±0.3 km, +872 m ±10 m, -610 m ±10 m)
        assertTrue("Distancia esperada cerca de 36.34 km, obtenida: ${result.distanceKm}",
            result.distanceKm in 36.0..36.7)
        assertTrue("Desnivel positivo esperado cerca de 872 m, obtenido: ${result.elevationGainM}",
            result.elevationGainM in 860..885)
        assertTrue("Desnivel negativo esperado cerca de 610 m, obtenido: ${result.elevationLossM}",
            result.elevationLossM in 600..620)

        // 5. Validar logs de depuración
        assertTrue(result.debugLog.contains("referencia_wikiloc.gpx"))
        assertTrue(result.debugLog.contains("trkpt leídos"))
        assertTrue(result.debugLog.contains("wpt ignorados: 4"))
    }

    @Test
    fun testElevationHysteresisIgnoresNoise() {
        // Si hay fluctuaciones menores al umbral de 5m (e.g. 100m -> 103m -> 101m -> 104m)
        // no debe acumular falso desnivel
        val gpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <trk><trkseg>
                <trkpt lat="42.000" lon="-1.000"><ele>100.0</ele></trkpt>
                <trkpt lat="42.001" lon="-1.000"><ele>103.0</ele></trkpt>
                <trkpt lat="42.002" lon="-1.000"><ele>101.0</ele></trkpt>
                <trkpt lat="42.003" lon="-1.000"><ele>104.0</ele></trkpt>
                <trkpt lat="42.004" lon="-1.000"><ele>100.0</ele></trkpt>
                <trkpt lat="42.005" lon="-1.000"><ele>120.0</ele></trkpt>
              </trkseg></trk>
            </gpx>
        """.trimIndent()

        val result = GpxParser.parse(gpx)
        // Solo el salto final de 100 a 120 supera el umbral de 5m (+20m)
        assertEquals(20, result.elevationGainM)
        assertEquals(0, result.elevationLossM)
    }

    @Test
    fun testMultiSegmentDoesNotAddJumpDistance() {
        // Dos segmentos distantes entre sí (salto teleport de 1 grado de latitud = 111 km)
        // Cada segmento tiene 2 puntos separados 100m. La distancia total debe ser ~0.2 km, no >111 km!
        val gpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <trk>
                <trkseg>
                  <trkpt lat="42.0000" lon="-1.0000"><ele>100.0</ele></trkpt>
                  <trkpt lat="42.0009" lon="-1.0000"><ele>100.0</ele></trkpt>
                </trkseg>
                <trkseg>
                  <trkpt lat="43.0000" lon="-1.0000"><ele>100.0</ele></trkpt>
                  <trkpt lat="43.0009" lon="-1.0000"><ele>100.0</ele></trkpt>
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val result = GpxParser.parse(gpx)
        assertTrue("La distancia (${result.distanceKm} km) no debe incluir el salto de 111 km entre segmentos",
            result.distanceKm < 1.0)
    }

    @Test
    fun testNoTimestampsYieldsNd() {
        val gpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <trk><trkseg>
                <trkpt lat="42.000" lon="-1.000"><ele>100.0</ele></trkpt>
                <trkpt lat="42.001" lon="-1.000"><ele>150.0</ele></trkpt>
              </trkseg></trk>
            </gpx>
        """.trimIndent()

        val result = GpxParser.parse(gpx)
        assertEquals("n/d", result.formattedTime)
    }
}
