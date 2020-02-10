package com.comsci.druchat.util

import android.content.Context
import android.location.LocationManager
import android.provider.Settings

class MyCode {

    companion object {
        fun locationSetting(context: Context): Boolean {
            val contentResolver = context.contentResolver
            return Settings.Secure.isLocationProviderEnabled(
                contentResolver,
                LocationManager.GPS_PROVIDER
            )
        }

    }
}
