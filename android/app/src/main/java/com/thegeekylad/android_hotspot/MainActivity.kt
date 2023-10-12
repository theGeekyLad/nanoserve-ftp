package com.thegeekylad.android_hotspot

import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import com.thegeekylad.android_hotspot.ui.theme.Android_HotspotTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_HotspotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        val wifiManager: WifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                super.onStarted(reservation)
                Toast.makeText(applicationContext, "Started AP: " + reservation!!.softApConfiguration.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onStopped() {
                super.onStopped()
                Toast.makeText(applicationContext, "Stopped AP.", Toast.LENGTH_LONG).show()
            }

            override fun onFailed(reason: Int) {
                super.onFailed(reason)
                Toast.makeText(applicationContext, "Failed to start AP.", Toast.LENGTH_LONG).show()
            }
        }, null)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Android_HotspotTheme {
        Greeting("Android")
    }
}