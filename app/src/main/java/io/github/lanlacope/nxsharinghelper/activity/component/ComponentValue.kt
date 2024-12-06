package io.github.lanlacope.nxsharinghelper.activity.component

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.ui.theme.Gray

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

fun <K, V> Map<K, V>.toMutableStateMap(): SnapshotStateMap<K, V> {
    return mutableStateMapOf<K, V>().apply { putAll(this) }
}

@Composable
fun DialogTextField(
    text: String,
    onTextChange: (text: String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    eroorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    useLabel: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = {
            if (useLabel && !hint.isNullOrEmpty()) {
                Text(
                    text = hint,
                    /*
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        color = Gray
                    ),
                    modifier = Modifier.wrapContentSize()

                     */
                )
            }
        },
        placeholder = {
            if (!useLabel && !hint.isNullOrEmpty()) {
                Text(
                    text = hint,
                    /*
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    color = Gray
                ),

                 */
                )
            }
        },
        supportingText = {
            if (isError && !eroorText.isNullOrEmpty()) {
                Text(
                    text = eroorText,
                    /*
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 16.dp)

                     */

                )
            }
        },
        singleLine = singleLine,
        modifier = modifier
    )
}