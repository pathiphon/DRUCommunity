package com.comsci.druchat.utility

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import com.google.firebase.database.DatabaseReference

class MyCode {

    companion object {
        fun locationSetting(context: Context): Boolean {
            val contentResolver = context.contentResolver
            return Settings.Secure.isLocationProviderEnabled(
                contentResolver,
                LocationManager.GPS_PROVIDER
            )
        }

        fun setState(databaseReference: DatabaseReference, user_id: String, state: String) {
            databaseReference.child(user_id).child("state").setValue(state)
        }

    }
}