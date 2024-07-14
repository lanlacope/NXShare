package io.github.lanlacope.nxsharinghelper.activity

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.style.CharacterStyle
import android.text.style.URLSpan
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.HtmlCompat
import io.github.lanlacope.nxsharinghelper.R
import io.github.lanlacope.nxsharinghelper.ui.theme.AppTheme

class LicenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Licence()
                }
            }
        }
    }
}

@Composable
private fun Licence() {
    val context = LocalContext.current

    val rawText = stringResource(id = R.string.license)
    val textColor = MaterialTheme.colorScheme.onBackground
    val linkColor = MaterialTheme.colorScheme.primary
    val text = remember(rawText) {
        val parsedText = HtmlCompat.fromHtml(
            rawText.replace("\n", "<br>"),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )

        return@remember buildAnnotatedString {
            append(parsedText)

            parsedText.getSpans(0, parsedText.length, CharacterStyle::class.java).forEach { span ->
                if (span is URLSpan) {
                    addStyle(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = parsedText.getSpanStart(span),
                        end = parsedText.getSpanEnd(span)
                    )
                    addStringAnnotation(
                        tag = "URL",
                        annotation = span.url,
                        start = parsedText.getSpanStart(span),
                        end = parsedText.getSpanEnd(span)
                    )
                }
            }
        }
    }

    ClickableText(
        text = text,
        modifier = Modifier.fillMaxSize(),
        style = TextStyle(
            color = textColor
        ),
        onClick = {
            text.getStringAnnotations(
                tag = "URL",
                start = it,
                end = it
            )
                .firstOrNull()?.let {
                    val uri = Uri.parse(it.item)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun LicensePreViewLight() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Licence()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LicensePreViewDark() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Licence()
        }
    }
}