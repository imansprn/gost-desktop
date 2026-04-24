package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import xyz.gobliggg.gost.ui.theme.GostRadius
import xyz.gobliggg.gost.ui.theme.GostSemantics
import xyz.gobliggg.gost.ui.theme.Spacing

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

@OptIn(ExperimentalMaterial3Api::class)
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
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled) { onClick() }
    ) {
        if (label != null) {
            Text(
                text = label,
                color = sc.textMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
        }

        BasicTextField(
            value = display,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().height(40.dp),
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = sc.textPrimary),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = display,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = sc.textDisabled,
                            style = MaterialTheme.typography.bodySmall,
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
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = saasTextFieldColors(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(GostRadius.md),
                            focusedBorderThickness = 1.dp,
                            unfocusedBorderThickness = 1.dp,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = saasTextFieldColors(),
                )
            }
        )
    }
}

