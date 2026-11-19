package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.TownCoordinates
import com.example.ui.theme.CaminoBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TownAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(value) {
        val trimmed = value.trim()
        if (trimmed.length >= 2) {
            delay(250) // Debounce para no saturar peticiones
            val list = withContext(Dispatchers.IO) {
                TownCoordinates.searchTowns(trimmed, context)
            }
            suggestions = list
            expanded = list.isNotEmpty() && !list.any { it.equals(trimmed, ignoreCase = true) }
        } else {
            suggestions = emptyList()
            expanded = false
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { /* controlado por el texto */ },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
        )

        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                suggestions.take(6).forEach { town ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = town,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CaminoBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onValueChange(town)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
