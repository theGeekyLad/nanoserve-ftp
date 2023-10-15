/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.thegeekylad.wear_hotspot.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.thegeekylad.wear_hotspot.presentation.theme.Wear_HotspotTheme
import io.javalin.Javalin
import io.javalin.core.util.FileUtil
import io.javalin.http.Context
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

@ExperimentalUnitApi
class MainActivity : ComponentActivity() {

    var app: Javalin? = null
    var ipAddress = mutableStateOf("")

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wear_HotspotTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "File Server", fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                    Text(text = ipAddress.value, fontSize = TextUnit(25f, TextUnitType.Sp))
                    Text(text = "Port: 7070")
                }
            }
        }

        // keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // get ip
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
        val dhcpAddress = linkProperties?.dhcpServerAddress?.hostAddress
        val dhcpAddressType = dhcpAddress?.substring(0, dhcpAddress.lastIndexOf("."))
        val linkAddresses = linkProperties?.linkAddresses
        ipAddress.value = linkAddresses?.find { linkAddress ->
            linkAddress.address.hostAddress!!.startsWith(dhcpAddressType!!)
        }!!.address.hostAddress!!.toString()

        // run server
        app = Javalin
            .create { config ->
                config.enableCorsForAllOrigins()
            }
            .apply {
                get("/") { ctx -> ctx.result("Hello World") }.start(7070)

                get("/list/*") { ctx ->
                    try {
                        val path = ctx.path().replace("%20", " ")
                        val indexPathStart = path.indexOf("list") + 4
                        if (path.length > indexPathStart)
                            serveContentForPath(ctx, path.substring(indexPathStart))
                        else
                            serveContentForPath(ctx)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                post("/upload") { ctx ->
                    ctx.uploadedFiles("files").forEach { uploadedFile ->
                        try {
                            Log.e("UPLOAD_DIR", uploadedFile.filename)
                            FileUtil.streamToFile(uploadedFile.content, Environment.getExternalStorageDirectory().absolutePath + uploadedFile.filename)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

//        TODO authenticate session
//        app.before("/list/*") { ctx ->
//            // runs before request to /path/*
//            Toast.makeText(applicationContext, "Intercepted!", Toast.LENGTH_LONG).show()
//        }
    }

    override fun onPause() {
        super.onPause()
        app?.stop()
    }

    fun serveContentForPath(ctx: Context, path: String = "") {
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
                        ".file-item {" +
                        "   text-decoration: underline;" +
                        "   color: blue" +
                        "}" +
                        ".file-item:hover {" +
                        "   cursor:pointer;" +
                        "}" +
                        ".ml-1 {" +
                        "   margin-left: 1rem;" +
                        "}")
            ),
            body(
                h1("Rahul's Pixel Watch"),
                i(h3("Storage: ${Math.floor(getFreeSpace() * 1.0 / getTotalSpace() * 100).toInt()}% free (${getFreeSpace()} GB / ${getTotalSpace()} GB)")),
                hr(),
                base().withHref("http://$ipAddress:7070/list/"),
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
                                    div(file.name)
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
                    input()
                        .withId("input-file-upload")
                        .withName("files")
                        .withType("file")
                        .withValue("Upload File")
                        .withCondMultiple(true),
                    progress()
                        .withClass("ml-1")
                        .withId("progress-bar")
                        .withValue("0")
                        .withMax("100"),
                    span()
                        .withClass("ml-1")
                        .withId("upload-status")
                ),
                hr(),
                i(p("Made with <3 by theGeekyLad")),

                // all scripts

                script().withSrc("https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"),
                script("" +
                        "   function uploadFile() {" +
                        "       var files = document.getElementById('input-file-upload').files;" +
                        "       var formData = new FormData();" +
                        "       Array.from(files).forEach(file => {" +
                        "           console.log('file', file);" +
                        "           const url = window.location.href;" +
                        "           console.log('url', url);" +
                        "           const subUrl = url.substring(" +
                        "               url.indexOf('list') + 4" +
                        "           );" +
                        "           const newFile = new File(" +
                        "               [file]," +
                        "               subUrl + file.name," +
                        "               {" +
                        "                   type: file.type" +
                        "               }" +
                        "           );" +
                        "           formData.append('files', newFile);" +
                        "       });" +
                        "       var ajax = new XMLHttpRequest();" +
                        "       ajax.upload.addEventListener('progress', progressHandler, false);" +
                        "       ajax.addEventListener('load', completeHandler, false);" +
                        "       ajax.addEventListener('error', errorHandler, false);" +
                        "       ajax.addEventListener('abort', abortHandler, false);" +
                        "       ajax.open('POST', '/upload');" +
                        "       ajax.send(formData);" +
                        "   }" +
                        "" +
                        "   function progressHandler(event) {" +
                        "       var percent = (event.loaded / event.total) * 100;" +
                        "       document.getElementById('upload-status').innerHTML = 'Uploaded ' + event.loaded + ' / ' + event.total + ' bytes';" +
                        "       document.getElementById('progress-bar').value = Math.round(percent);" +
//                        "       document.getElementById('status').innerHTML = Math.round(percent) + '%';" +
                        "   }" +
                        "" +
                        "   function completeHandler(event) {" +
                        "       document.getElementById('upload-status').innerHTML = 'Upload complete!';" +
//                        "       document.getElementById('progressBar').value = 0;" +
                        "   }" +
                        "" +
                        "   function errorHandler(event) {" +
                        "       document.getElementById('upload-status').innerHTML = 'Upload failed!';" +
                        "   }" +
                        "" +
                        "   function abortHandler(event) {" +
                        "       document.getElementById('upload-status').innerHTML = 'Upload aborted!';" +
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
                        "       uploadFile();" +
                        "   }, false);" +
                        "").withType("text/javascript")
            )
        ).render())
    }

    fun getFreeSpace(): Long {
        return StatFs(Environment.getExternalStorageDirectory().absolutePath).freeBytes / 1024 / 1024 / 1024
    }

    fun getTotalSpace(): Long {
        return StatFs(Environment.getExternalStorageDirectory().absolutePath).totalBytes / 1024 / 1024 / 1024
    }
}
