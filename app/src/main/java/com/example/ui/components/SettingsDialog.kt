package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CaminoViewModel
import com.example.ui.settings.LanguageMode
import com.example.ui.theme.CaminoBlue

@Composable
fun SettingsActionMenu(
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }

    val currentLang by viewModel.languageMode.collectAsState()

    Box(modifier = modifier) {
        IconButton(
            onClick = { expandedMenu = true },
            modifier = Modifier.testTag("btn_top_settings")
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = viewModel.string("settings"),
                tint = CaminoBlue
            )
        }

        DropdownMenu(
            expanded = expandedMenu,
            onDismissRequest = { expandedMenu = false },
            modifier = Modifier.width(220.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text(viewModel.string("language"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val langLabel = when (currentLang) {
                            LanguageMode.ES -> viewModel.string("lang_es")
                            LanguageMode.EN -> viewModel.string("lang_en")
                        }
                        Text(langLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CaminoBlue)
                },
                onClick = {
                    expandedMenu = false
                    showLangDialog = true
                },
                modifier = Modifier.testTag("menu_item_language")
            )
        }
    }

    // Language Selection Dialog
    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CaminoBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.string("language"))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptionRow(
                        title = viewModel.string("lang_es"),
                        subtitle = "Español (por defecto)",
                        isSelected = currentLang == LanguageMode.ES,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.ES)
                            showLangDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = viewModel.string("lang_en"),
                        subtitle = "English",
                        isSelected = currentLang == LanguageMode.EN,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.EN)
                            showLangDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text(viewModel.string("cancel"))
                }
            }
        )
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CaminoBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) CaminoBlue else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = CaminoBlue)
            )
        }
    }
}
