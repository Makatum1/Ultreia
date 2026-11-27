package com.example

import com.example.data.gpx.GpxParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testGpxParser() {
        val sampleGpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="Ultreia">
              <trk>
                <trkseg>
                  <trkpt lat="43.0093" lon="-1.3197"><ele>950.0</ele></trkpt>
                  <trkpt lat="42.9800" lon="-1.4000"><ele>820.0</ele></trkpt>
                  <trkpt lat="42.9298" lon="-1.5034"><ele>526.0</ele></trkpt>
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val result = GpxParser.parse(sampleGpx)
        assertTrue(result.points.size >= 3)
        assertTrue(result.distanceKm > 0)
        assertTrue(result.elevationLossM > 0)
    }

    @Test
    fun testRealisticAutoDifficulty() {
        // Caso real reportado: 28.65 km, +457 m / -386 m -> Exigente
        assertEquals(
            "Exigente",
            com.example.data.repository.CaminoRepository.calculateAutoDifficulty(28.65, 457, 386)
        )
        // 25 km con +1000 m -> Exigente|Muy Exigente (pendiente muy alta)
        assertEquals(
            "Exigente|Muy Exigente",
            com.example.data.repository.CaminoRepository.calculateAutoDifficulty(25.0, 1000, 0)
        )
        // 35 km con +1000 m -> Muy Exigente
        assertEquals(
            "Muy Exigente",
            com.example.data.repository.CaminoRepository.calculateAutoDifficulty(35.0, 1000, 0)
        )
        // Franja 16.5-18.5 km -> Fácil|Moderado
        assertEquals(
            "Fácil|Moderado",
            com.example.data.repository.CaminoRepository.calculateAutoDifficulty(17.5, 120, 100)
        )
        // Franja 25.5-27.5 km -> Moderado|Exigente
        assertEquals(
            "Moderado|Exigente",
            com.example.data.repository.CaminoRepository.calculateAutoDifficulty(26.5, 300, 300)
        )
    }
}
