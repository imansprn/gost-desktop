package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.gobliggg.gost.ui.theme.Spacing

@Composable
fun ScreenScaffold(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    messages: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl)
    ) {
        header()
        messages()
        Spacer(Modifier.height(Spacing.lg))
        content()
    }
}

