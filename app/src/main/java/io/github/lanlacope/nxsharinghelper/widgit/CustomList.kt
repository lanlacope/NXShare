package io.github.lanlacope.nxsharinghelper.widgit

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Suppress("unused")
@Composable
fun LazyHorizontalPager(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    reverseLayout: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: LazyListScope.() -> Unit
) {
    val flingBehavior = rememberLazyPagerFlingBehavior(state = state)

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        flingBehavior = flingBehavior,
        userScrollEnabled = true,
        content = content
    )
}

@Composable
fun rememberLazyPagerFlingBehavior(
    state: LazyListState,
    decayAnimationSpec: DecayAnimationSpec<Float> = remember {
        exponentialDecay(frictionMultiplier = 20.0f)
    },
    snapAnimationSpec: AnimationSpec<Float> = spring(
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = Int.VisibilityThreshold.toFloat()
    ),
): FlingBehavior {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    return remember(state, decayAnimationSpec, snapAnimationSpec, density, layoutDirection) {
        val snapLayoutInfoProvider = SnapLayoutInfoProvider(
            lazyListState = state,
            snapPosition = SnapPosition.Start
        )

        snapFlingBehavior(
            snapLayoutInfoProvider = snapLayoutInfoProvider,
            decayAnimationSpec = decayAnimationSpec,
            snapAnimationSpec = snapAnimationSpec
        )
    }
}

@Suppress("unused")
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
    androidx.compose.foundation.layout.Box(modifier = Modifier.animateItem()) {
        itemContent(items[index])
    }
}

@Suppress("unused")
inline fun <T> LazyListScope.animatedPagerItems(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index: Int -> contentType(items[index]) }
) { index ->
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .animateItem()
            .fillParentMaxSize()
    ) {
        itemContent(items[index])
    }
}
