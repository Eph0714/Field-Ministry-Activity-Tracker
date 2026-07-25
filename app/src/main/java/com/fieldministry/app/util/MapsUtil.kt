package com.fieldministry.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openInMaps(context: Context, latitude: Double, longitude: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(context, "No app available to show the map", Toast.LENGTH_SHORT).show()
        }
    }
}
