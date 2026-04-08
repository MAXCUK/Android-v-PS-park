package com.maxcuk.xboardclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.maxcuk.xboardclient.app.XBoardClientApp
import com.maxcuk.xboardclient.app.XBoardClientApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as XBoardClientApplication).container
        setContent {
            XBoardClientApp(container = container)
        }
    }
}
