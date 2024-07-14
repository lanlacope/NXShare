package io.github.lanlacope.nxsharinghelper.activity.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DrawOutAnimated(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable() (AnimatedVisibilityScope.() -> Unit)
) {
    val enter: EnterTransition = fadeIn() + expandVertically()
    val exit: ExitTransition = shrinkVertically() + fadeOut()
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        label = label,
        content = content
    )
}

@Composable
fun SlideInAnimated(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable() (AnimatedVisibilityScope.() -> Unit)
) {
    val enter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn()

    val exit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut()

    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        label = label,
        content = content
    )
}