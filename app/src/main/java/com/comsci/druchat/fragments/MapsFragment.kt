package com.comsci.druchat.fragments

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.adedom.library.extension.loadImage
import com.comsci.druchat.MainActivity.Companion.sContext
import com.comsci.druchat.MainActivity.Companion.sLatLng
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.dialog.PublicChatsDialog
import com.comsci.druchat.util.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsFragment : BaseFragment({ R.layout.fragment_maps }), OnMapReadyCallback {

    private lateinit var mUserItem: ArrayList<User>
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mMapView: MapView

    companion object {
        val mMarkerPeople = arrayListOf<Marker>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!viewModel.currentUser()!!.isEmailVerified) {
            Toast.makeText(sContext, "Click to Verify", Toast.LENGTH_LONG).show()
            activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.mFrameLayout, ProfileFragment()).commit()
        }

        mUserItem = arrayListOf<User>()

        mMapView = v.findViewById(R.id.mMapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)

        v.findViewById<FloatingActionButton>(R.id.mFloatingActionButton).setOnClickListener {
            PublicChatsDialog().show(fragmentManager!!, null)
        }

        return v
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.isMyLocationEnabled = true
//        mGoogleMap.uiSettings.setAllGesturesEnabled(false)

        mGoogleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    sLatLng.latitude,
                    sLatLng.longitude
                ), 15.0F
            )
        )
        setMyUniversity(13.7337910, 100.4911897)
        setMyUniversity(13.5228417, 100.7525409)
        setPeopleLocation()

        mGoogleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.info_window, null)
                val imgProfile = view.findViewById(R.id.mImgProfile) as ImageView
                val imgState = view.findViewById(R.id.mImgState) as ImageView
                val tvName = view.findViewById(R.id.mTvName) as TextView
                val tvStatus = view.findViewById(R.id.mTvStatus) as TextView
                val tvLocation = view.findViewById(R.id.mTvLocation) as TextView

                val infoWindowData = marker.tag as User
                //name
                tvName.text = infoWindowData.name

                //status
                tvStatus.text = infoWindowData.status

                if (infoWindowData.user_id == "DRU") {
                    //dru
                    imgProfile.visibility = View.GONE
                    tvLocation.visibility = View.GONE
                    imgState.visibility = View.GONE
                } else {
                    //image
                    if (infoWindowData.imageURL != "default") {
                        imgProfile.loadImage(infoWindowData.imageURL)
                    }

                    //location
                    val list = Geocoder(sContext).getFromLocation(
                        infoWindowData.latitude,
                        infoWindowData.longitude,
                        1
                    )
                    val locality = if (list[0].locality != null) {
                        list[0].locality
                    } else {
                        "unknown"
                    }
                    tvLocation.text = locality

                    //state
                    if (infoWindowData.state != "offline") {
                        imgState.setImageResource(R.drawable.shape_state_green)
                    }
                }
                return view
            }

            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
        })
    }

    private fun setMyUniversity(latitude: Double, longitude: Double) {
        val marker = mGoogleMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dru_logo))
                .title("DRU")
                .snippet("Dhonburi rajabhat university")
        )
        marker.tag =
            User("DRU", "DRU", "Dhonburi rajabhat university", "", "", latitude, longitude)
    }

    private fun setPeopleLocation() {
        viewModel.getUsers().observe(this, Observer {
            mUserItem = it as ArrayList<User>
            People(mGoogleMap, mUserItem, viewModel.currentUserId()!!)
        })
    }

    override fun onResume() {
        mMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}
