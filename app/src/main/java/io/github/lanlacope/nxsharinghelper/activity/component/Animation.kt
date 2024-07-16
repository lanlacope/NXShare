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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Suppress("unused")
@Composable
fun FadeInAnimated(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "AnimatedVisibility",
    content: @Composable() (AnimatedVisibilityScope.() -> Unit)
) {
    val enter: EnterTransition = fadeIn()
    val exit: ExitTransition = fadeOut()
    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        label = label,
        content = content
    )
}

@Suppress("unused")
@Composable
fun DrawDownAnimated(
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

@Suppress("unused")
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

inline fun <T> LazyListScope.animatedItems(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index: Int -> contentType(items[index]) }
) { index ->
    Box(modifier = Modifier.animateItem()) {
        itemContent(items[index])
    }
}