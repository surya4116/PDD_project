package com.simats.myapplication.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MapWebView(
    locationQuery: String,
    modifier: Modifier = Modifier
) {
    val encodedQuery = remember(locationQuery) {
        try {
            java.net.URLEncoder.encode(locationQuery.trim(), "UTF-8")
        } catch (e: Exception) {
            ""
        }
    }
    
    val mapsUrl = remember(encodedQuery) {
        if (encodedQuery.isEmpty()) {
            "https://maps.google.com/maps?q=Poonamallee%20Chennai&t=&z=14&ie=UTF8&iwloc=&output=embed"
        } else {
            "https://maps.google.com/maps?q=$encodedQuery&t=&z=14&ie=UTF8&iwloc=&output=embed"
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(mapsUrl)
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            webView.loadUrl(mapsUrl)
        }
    )
}
