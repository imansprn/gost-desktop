package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import xyz.gobliggg.gost.ui.theme.GostRadius
import xyz.gobliggg.gost.ui.theme.GostSemantics

/**
 * Standard dropdown “field” wrapper with optional search. Use this instead of bespoke
 * `OutlinedButton` / `Surface` anchors to keep height, shape, colors, and a11y consistent.
 */
@Composable
fun DropdownField(
    label: String? = null,
    value: String?,
    placeholder: String = "Select…",
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchable: Boolean = options.size >= 10,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val sc = GostSemantics.colors
    val display = value?.takeIf { it.isNotBlank() } ?: ""

    if (searchable) {
        SearchableStringDropdown(
            selected = display,
            options = options,
            onSelect = onSelect,
            modifier = modifier,
            anchor = { openMenu ->
                DropdownFieldAnchor(
                    label = label,
                    display = display,
                    placeholder = placeholder,
                    enabled = enabled,
                    contentDescription = contentDescription,
                    onClick = openMenu,
                )
            }
        )
    } else {
        SearchableStringDropdown(
            selected = display,
            options = options,
            onSelect = onSelect,
            modifier = modifier,
            menuMaxHeight = 240,
            anchor = { openMenu ->
                DropdownFieldAnchor(
                    label = label,
                    display = display,
                    placeholder = placeholder,
                    enabled = enabled,
                    contentDescription = contentDescription,
                    onClick = openMenu,
                )
            }
        )
    }
}

@Composable
private fun DropdownFieldAnchor(
    label: String?,
    display: String,
    placeholder: String,
    enabled: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val sc = GostSemantics.colors
    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = label?.let { { Text(it) } },
        placeholder = {
            Text(
                placeholder,
                color = sc.textDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = sc.textMuted,
            )
        },
        singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GostRadius.md),
        colors = saasTextFieldColors(),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled) { onClick() },
    )
}

