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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.comsci.druchat.MainActivity.Companion.mContext
import com.comsci.druchat.MainActivity.Companion.mLatLng
import com.comsci.druchat.R
import com.comsci.druchat.dialog.PublicChatsDialog
import com.comsci.druchat.model.UserItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapsFragment : Fragment(), OnMapReadyCallback {

    val TAG = "MapsFragment"
    private lateinit var mUserItem: ArrayList<UserItem>
    private lateinit var mUser: FirebaseUser
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

        mUser = FirebaseAuth.getInstance().currentUser!!
        if (!mUser.isEmailVerified) {
            Toast.makeText(mContext, "Click to Verify", Toast.LENGTH_LONG).show()
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.mFrameLayout, ProfileFragment()).commit()
        }

        mUserItem = arrayListOf<UserItem>()

        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        mMapView = view.findViewById(R.id.mMapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)

        initChat(view)

        return view
    }

    private fun initChat(view: View) {
        val mFloatingActionButton = view.findViewById(R.id.mFloatingActionButton) as FloatingActionButton
        mFloatingActionButton.setOnClickListener {
            PublicChatsDialog().show(fragmentManager, null)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.isMyLocationEnabled = true
//        mGoogleMap.uiSettings.setAllGesturesEnabled(false)

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mLatLng.latitude, mLatLng.longitude), 15.0F))
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

                val infoWindowData = marker.tag as UserItem
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
                        Glide.with(mContext).load(infoWindowData.imageURL).into(imgProfile)
                    }

                    //location
                    val list = Geocoder(mContext).getFromLocation(infoWindowData.latitude, infoWindowData.longitude, 1)
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
        marker.tag = UserItem("DRU", "DRU", "Dhonburi rajabhat university", "", "", latitude, longitude)
    }

    private fun setPeopleLocation() {
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUserItem.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(UserItem::class.java)!!
                    if (user.latitude != 0.0 && user.longitude != 0.0) {
                        mUserItem.add(user)
                    }
                }

                People(mGoogleMap, mUserItem, mUser.uid)
            }
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
