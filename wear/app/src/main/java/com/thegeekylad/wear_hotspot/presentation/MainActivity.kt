/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.thegeekylad.wear_hotspot.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.thegeekylad.wear_hotspot.presentation.theme.Wear_HotspotTheme
import io.javalin.Javalin
import j2html.TagCreator.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Android")
        }

        var uploadableName: String = ""

        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:com.thegeekylad.wear_hotspot"))
            startActivity(intent)
        }

        // run server
        val app = Javalin
            .create(/*config*/)
            .get("/") { ctx -> ctx.result("Hello World") }.start(7070)

//        TODO authenticate session
//        app.before("/list/*") { ctx ->
//            // runs before request to /path/*
//            Toast.makeText(applicationContext, "Intercepted!", Toast.LENGTH_LONG).show()
//        }

        app.get("/list/*") { ctx ->
            try {
                val path = ctx.path().replace("%20", " ")
                val indexPathStart = path.indexOf("list") + 5
                if (path.length > indexPathStart)
                    serveContentForPath(ctx, path.substring(indexPathStart))
                else
                    serveContentForPath(ctx)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        app.post("/upload/prepare") { ctx ->
            uploadableName = ctx.body()
            ctx.status(200)
        }

        app.post("/upload") { ctx ->
            try {
                var body = ctx.body()
                body = body.substring(body.indexOf(",") + 1)
                body.replace("\n", "")
                body.replace("\r", "")
                val decodedBytes = Base64.getDecoder().decode(body)
                val destinationFilePath = Paths.get(Environment.getExternalStorageDirectory().absolutePath, "/Download/$uploadableName")
                Files.write(destinationFilePath, decodedBytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ctx.status(201);
        }
    }

    fun serveContentForPath(ctx: io.javalin.Context, path: String = "") {
        // list all files
        val potentialDir = File(Environment.getExternalStorageDirectory(), path)

        if (potentialDir.isFile) {
            ctx.header("Content-Disposition", "filename=\"${potentialDir.name}\"")
            ctx.result(FileInputStream(potentialDir.absolutePath))
            return
        }

        val files = potentialDir.listFiles()

        // html content
        ctx.html(html(
            head(
                title("Watch FTP"),
                style("" +
                        "a {" +
                        "   text-decoration: underline;" +
                        "}" +
                        "a:hover {" +
                        "   cursor:pointer;" +
                        "}" +
                        "")
            ),
            body(
                h1("Rahul's Pixel Watch"),
                hr(),
                base().withHref("http://localhost:7070/list/"),
                table(
                    thead(
                        tr(
                            td(h3("File")),
                            td(h3("Size"))
                        )
                    ),
                    tbody(
                        *files.map { file ->
                            tr(
                                td(
                                    a(file.name)
                                        .withClass("file-item")
                                        .attr("data-file-name", file.name)
                                        .attr("style", "")
                                ),
                                if (file.isFile) td(file.length().toString()) else null
                            )
                        }.toTypedArray()
                    )
                ),
                hr(),
                div(
                    span("Upload File: "),
                    input().withType("file").withValue("Upload File").withId("input-file-upload")
                ),
                hr(),
                i(p("Made with <3 by theGeekyLad")),

                // all scripts

                script().withSrc("https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"),
                script("" +
                        "   function uploadFile(name, base64Data) {" +
                        "       axios.post('http://localhost:7070/upload/prepare', name)" +
                        "           .then(res => {" +
                        "              axios.post('http://localhost:7070/upload', base64Data)" +
                        "                  .then(function (res) {" +
                        "                      alert('Upload complete!')" +
                        "                  })" +
                        "                  .catch(function (err) {" +
                        "                      alert('Error: ' + err.message)" +
                        "                  });" +
                        "           })" +
                        "           .catch(err => {" +
                        "               alert('Error: Could not even share file name!')" +
                        "           })" +
                        "   }" +
                        "" +
                        "   var fileItems = document.getElementsByClassName('file-item');" +
                        "   Array.from(fileItems).forEach(fileItem => {" +
                        "       fileItem.addEventListener('click', e => {" +
                        "           window.location += e.target.getAttribute('data-file-name') + '/';" +
                        "       });" +
                        "   });" +
                        "" +
                        "   document.getElementById('input-file-upload').addEventListener('change', (e) => {" +
                        "       if (e.target.files.length === 0) return;" +
                        "       const file = e.target.files[0];" +
                        "       const reader = new FileReader();" +
                        "       reader.addEventListener('load', () => {" +
                        "           uploadFile(file.name, reader.result);" +
                        "       }, false);" +
                        "       reader.readAsDataURL(file);" +
                        "   }, false);" +
                        "").withType("text/javascript")
            )
        ).render())
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