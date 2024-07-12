package io.github.lanlacope.nxsharinghelper.widgit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
inline fun Row(
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .clickable(
                onClick = onClick
            ),

        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun Row(
    modifier: Modifier = Modifier,
    noinline onClick: (() -> Unit)? = null,
    noinline onLongClick: () -> Unit,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .combinedClickable(
                onClick = onClick?.let { onClick }?:{ },
                onLongClick = onLongClick
            ),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}