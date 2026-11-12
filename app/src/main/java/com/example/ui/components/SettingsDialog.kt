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
import com.example.ui.settings.ThemeMode
import com.example.ui.theme.CaminoBlue

@Composable
fun SettingsActionMenu(
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }

    val currentTheme by viewModel.themeMode.collectAsState()
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
                        Text(viewModel.string("theme"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val themeLabel = when (currentTheme) {
                            ThemeMode.SYSTEM -> viewModel.string("theme_system")
                            ThemeMode.LIGHT -> viewModel.string("theme_light")
                            ThemeMode.DARK -> viewModel.string("theme_dark")
                        }
                        Text(themeLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (currentTheme) {
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = null,
                        tint = CaminoBlue
                    )
                },
                onClick = {
                    expandedMenu = false
                    showThemeDialog = true
                },
                modifier = Modifier.testTag("menu_item_theme")
            )

            HorizontalDivider()

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

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = CaminoBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.string("theme"))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptionRow(
                        title = viewModel.string("theme_system"),
                        subtitle = "Sigue la configuración de Android",
                        isSelected = currentTheme == ThemeMode.SYSTEM,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = viewModel.string("theme_light"),
                        subtitle = "Fondo claro y tonos frescos",
                        isSelected = currentTheme == ThemeMode.LIGHT,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = viewModel.string("theme_dark"),
                        subtitle = "Ideal para ahorro de batería y noche",
                        isSelected = currentTheme == ThemeMode.DARK,
                        onClick = {
                            viewModel.setThemeMode(ThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(viewModel.string("cancel"))
                }
            }
        )
    }

    // Language Selection Dialog
    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = CaminoBlue)
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.string("language"))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptionRow(
                        title = viewModel.string("lang_es"),
                        subtitle = "Español (Castellano)",
                        isSelected = currentLang == LanguageMode.ES,
                        onClick = {
                            viewModel.setLanguageMode(LanguageMode.ES)
                            showLangDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = viewModel.string("lang_en"),
                        subtitle = "English (International pilgrim)",
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) CaminoBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = CaminoBlue)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
