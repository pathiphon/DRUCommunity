package com.comsci.druchat.ui.main.map

import com.adedom.library.extension.loadBitmap
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.ui.main.MainActivity
import com.comsci.druchat.util.KEY_DEFAULT
import com.comsci.druchat.util.KEY_OFFLINE
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class People(
    private val googleMap: GoogleMap,
    private val mMarkerPeople: ArrayList<Marker>,
    users: ArrayList<User>
) {

    init {
        for (marker in mMarkerPeople) {
            marker.remove()
        }
        mMarkerPeople.clear()

        for (user in users) {
            if (user.state == KEY_OFFLINE) continue
            val icUser = BitmapDescriptorFactory.fromResource(R.drawable.ic_user)
            if (user.imageURL == KEY_DEFAULT) {
                setMarker(user, icUser)
            } else {
                MainActivity.sContext.loadBitmap(user.imageURL, {
                    setMarker(user, BitmapDescriptorFactory.fromBitmap(it))
                }, {
                    setMarker(user, icUser)
                })
            }
        }
    }

    private fun setMarker(user: User, icon: BitmapDescriptor) {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(user.latitude, user.longitude))
                .icon(icon)
        )
        marker.tag = user
        mMarkerPeople.add(marker)
    }
}
