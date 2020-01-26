package com.comsci.druchat.fragments

import com.adedom.library.extension.loadBitmap
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.fragments.MapsFragment.Companion.mMarkerPeople
import com.comsci.druchat.model.UserItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class People(googleMap: GoogleMap, users: ArrayList<UserItem>, user_id: String) {

    init {
        for (marker in mMarkerPeople) {
            marker.remove()
        }
        mMarkerPeople.clear()

        for (user in users) {
            if (user.imageURL == "default") {
                setMarker(googleMap, user, BitmapDescriptorFactory.fromResource(R.drawable.ic_user))
            } else {
                MainActivity.sContext.loadBitmap(user.imageURL, {
                    setMarker(googleMap, user, BitmapDescriptorFactory.fromBitmap(it))
                }, {
                    setMarker(
                        googleMap,
                        user,
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_user)
                    )
                })
            }
        }
    }

    private fun setMarker(googleMap: GoogleMap, user: UserItem, icon: BitmapDescriptor) {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(user.latitude, user.longitude))
                .icon(icon)
        )
        marker.tag = user
        mMarkerPeople.add(marker)
    }
}
