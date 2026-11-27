package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CaminoViewModel

data class DifficultyOption(
    val key: String,
    val color: Color,
    val selectedTextColor: Color = Color.White,
    val unselectedTextColor: Color = color
)

val DIFFICULTY_OPTIONS = listOf(
    DifficultyOption(
        key = "Fácil",
        color = Color(0xFF16A34A), // Verde
        selectedTextColor = Color.White,
        unselectedTextColor = Color(0xFF15803D)
    ),
    DifficultyOption(
        key = "Moderado",
        color = Color(0xFFEAB308), // Amarillo
        selectedTextColor = Color(0xFF1F2937), // Oscuro para óptimo contraste en amarillo
        unselectedTextColor = Color(0xFFB45309) // Ámbar oscuro legible
    ),
    DifficultyOption(
        key = "Exigente",
        color = Color(0xFFF97316), // Naranja vibrante (#F97316)
        selectedTextColor = Color.White,
        unselectedTextColor = Color(0xFFC2410C)
    ),
    DifficultyOption(
        key = "Muy Exigente",
        color = Color(0xFFDC2626), // Rojo
        selectedTextColor = Color.White,
        unselectedTextColor = Color(0xFFB91C1C)
    )
)

fun normalizeDifficulty(diff: String?): String {
    val clean = diff?.trim()?.lowercase() ?: ""
    return when {
        clean.contains("muy") || clean.contains("very") || clean.contains("sehr") || clean.contains("très") || clean.contains("molto") || clean.contains("zeer") || clean.contains("bardzo") || clean.contains("极") || clean.contains("difficult") -> "Muy Exigente"
        clean.contains("exig") || clean.contains("demand") || clean.contains("anspruch") || clean.contains("impeg") || clean.contains("veeleis") || clean.contains("wymag") || clean.contains("挑战") || clean.contains("difícil") || clean.contains("dificil") -> "Exigente"
        clean.contains("fác") || clean.contains("fac") || clean.contains("eas") || clean.contains("leich") || clean.contains("makk") || clean.contains("łatw") || clean.contains("简") || clean.contains("쉬움") -> "Fácil"
        else -> "Moderado"
    }
}

/**
 * Extrae 1 o 2 claves de dificultad cuando viene una dificultad combinada (ej. "Moderado|Exigente").
 */
fun parseDifficultyKeys(diff: String?): List<String> {
    if (diff.isNullOrBlank()) return listOf("Moderado")
    val parts = diff.split("|", "/", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { normalizeDifficulty(it) }
        .distinct()
    return if (parts.isEmpty()) listOf("Moderado") else parts.take(2)
}

@Composable
fun DifficultySelectionGroup(
    selectedDifficulty: String,
    onSelectDifficulty: (String) -> Unit,
    viewModel: CaminoViewModel? = null,
    modifier: Modifier = Modifier
) {
    val selectedKeys = parseDifficultyKeys(selectedDifficulty)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DIFFICULTY_OPTIONS.take(2).forEach { option ->
                val isSelected = selectedKeys.contains(option.key)
                DifficultyButton(
                    option = option,
                    isSelected = isSelected,
                    label = viewModel?.localizeDifficulty(option.key) ?: option.key,
                    onClick = { onSelectDifficulty(option.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DIFFICULTY_OPTIONS.drop(2).forEach { option ->
                val isSelected = selectedKeys.contains(option.key)
                DifficultyButton(
                    option = option,
                    isSelected = isSelected,
                    label = viewModel?.localizeDifficulty(option.key) ?: option.key,
                    onClick = { onSelectDifficulty(option.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyButton(
    option: DifficultyOption,
    isSelected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) option.color else option.color.copy(alpha = 0.08f)
    val borderColor = if (isSelected) option.color else option.color.copy(alpha = 0.40f)
    val textColor = if (isSelected) option.selectedTextColor else option.unselectedTextColor

    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
