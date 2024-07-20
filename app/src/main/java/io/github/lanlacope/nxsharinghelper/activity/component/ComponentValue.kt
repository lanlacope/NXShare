package io.github.lanlacope.nxsharinghelper.activity.component

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val recompositionKey: MutableState<Int> = mutableStateOf(0)

fun recomposition() {
    recompositionKey.value++
}

object ComponentValue {
    val DISPLAY_PADDING_START: Dp = 10.dp
    val DISPLAY_PADDING_TOP: Dp = 10.dp
    val DISPLAY_PADDING_END: Dp = 20.dp
}

@Composable
fun makeToast(
    text: String,
    duration: Int = Toast.LENGTH_SHORT
): Toast {
    return Toast.makeText(LocalContext.current, text, duration)
}