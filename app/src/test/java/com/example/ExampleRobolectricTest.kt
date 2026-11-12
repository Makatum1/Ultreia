package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CaminoEntity
import com.example.data.model.StageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ultreia", appName)
    }

    @Test
    fun `create travel guide entity`() {
        val trip = CaminoEntity(
            id = 1L,
            title = "Viaje a los Picos de Europa",
            subtitle = "Ruta de montaña y valles",
            pilgrimName = "",
            startDate = "15/06/2026",
            isTravelGuide = true
        )
        assertTrue(trip.isTravelGuide)
        assertEquals("Viaje a los Picos de Europa", trip.title)
    }

    @Test
    fun `create stage entity with properties`() {
        val stage = StageEntity(
            id = 101L,
            caminoId = 10L,
            dayNumber = 1,
            title = "Etapa 1: Cangas a Covadonga",
            origin = "Cangas de Onís",
            destination = "Lagos de Covadonga",
            distanceKm = 21.5,
            elevationGainM = 980,
            elevationLossM = 210,
            estimatedTime = "5h 30m",
            difficulty = "Media"
        )
        assertEquals(21.5, stage.distanceKm, 0.01)
        assertEquals("Lagos de Covadonga", stage.destination)
    }
}
