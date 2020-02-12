package com.comsci.druchat.ui.main.map

import com.adedom.library.extension.loadBitmap
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.ui.main.MainActivity
import com.comsci.druchat.ui.main.map.MapsFragment.Companion.mMarkerPeople
import com.comsci.druchat.util.KEY_DEFAULT
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class People(googleMap: GoogleMap, users: ArrayList<User>, user_id: String) {

    init {
        for (marker in mMarkerPeople) {
            marker.remove()
        }
        mMarkerPeople.clear()

        for (user in users) {
            if (user.imageURL == KEY_DEFAULT) {
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

    private fun setMarker(googleMap: GoogleMap, user: User, icon: BitmapDescriptor) {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(user.latitude, user.longitude))
                .icon(icon)
        )
        marker.tag = user
        mMarkerPeople.add(marker)
    }
}
