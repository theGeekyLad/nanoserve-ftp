/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.thegeekylad.wear_hotspot.presentation

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.thegeekylad.wear_hotspot.R
import com.thegeekylad.wear_hotspot.presentation.theme.Wear_HotspotTheme
import io.javalin.Javalin
import j2html.TagCreator.*

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Android")
        }

//        NanoServer()
        val app = Javalin.create(/*config*/).get("/") { ctx -> ctx.result("Hello World") }.start(7070)

        val content = html(
            head(
                title("Watch FTP")
            ),
            body(
                h1("Rahul's Pixel Watch"),
                table(
                    thead(
                        tr(
                            td("File"),
                            td("Size"),
                            td("Timestamp")
                        )
                    ),
                    tbody(
                        tr(
                            td(Environment.getExternalStorageDirectory().absolutePath),
                            td("2")
                        )
                    )
                )
            )
        )

        app.get("/list_files") { ctx ->
            ctx.html(content.render())
        }

    }
}

@Composable
fun WearApp(greetingName: String) {
    Wear_HotspotTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "Running nano server @ 7070"
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}