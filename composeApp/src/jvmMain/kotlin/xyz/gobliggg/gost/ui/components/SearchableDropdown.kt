package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Long-list string picker: opens a [DropdownMenu] with a filter field and scrollable matches.
 */
@Composable
fun SearchableStringDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search…",
    menuWidthMin: Int = 220,
    menuMaxHeight: Int = 280,
    anchor: @Composable (openMenu: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("") }
    val filtered =
        remember(filter, options) {
            options.filter { it.contains(filter, ignoreCase = true) }
        }
    LaunchedEffect(expanded) {
        if (!expanded) filter = ""
    }

    Box(modifier) {
        anchor { expanded = true }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Column(Modifier.widthIn(min = menuWidthMin.dp)) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    placeholder = {
                        Text(
                            searchPlaceholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = saasTextFieldColors(),
                )
                HorizontalDivider()
                Column(Modifier.heightIn(max = menuMaxHeight.dp).verticalScroll(rememberScrollState())) {
                    if (filtered.isEmpty()) {
                        Text(
                            "No matches",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(16.dp),
                        )
                    } else {
                        filtered.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    onSelect(opt)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
