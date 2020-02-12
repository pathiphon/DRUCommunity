package com.comsci.druchat.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.adedom.library.extension.exitApplication
import com.comsci.druchat.R
import com.comsci.druchat.ui.login.LoginActivity
import com.comsci.druchat.ui.main.chat.ChatsFragment
import com.comsci.druchat.ui.main.group.GroupsFragment
import com.comsci.druchat.ui.main.home.HomeFragment
import com.comsci.druchat.ui.main.map.MapsFragment
import com.comsci.druchat.ui.main.profile.ProfileFragment
import com.comsci.druchat.util.BaseActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocationRequest: LocationRequest

    companion object {
        lateinit var sLatLng: LatLng
        lateinit var sContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sContext = baseContext

        if (viewModel.currentUser() == null) {
            startActivity(
                Intent(sContext, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
            return
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.mFrameLayout,
                HomeFragment()
            )
                .commit()
        }
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdate()
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest,
            this
        )
    }

    private fun stopLocationUpdate() = LocationServices.FusedLocationApi
        .removeLocationUpdates(mGoogleApiClient, this)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_home -> selectedFragment =
                HomeFragment()
            R.id.nav_chats -> selectedFragment =
                ChatsFragment()
            R.id.nav_groups -> selectedFragment =
                GroupsFragment()
            R.id.nav_map -> selectedFragment =
                MapsFragment()
            R.id.nav_profile -> selectedFragment =
                ProfileFragment()
        }

        supportFragmentManager.beginTransaction().replace(R.id.mFrameLayout, selectedFragment!!)
            .commit()
        return true
    }

    override fun onLocationChanged(location: Location) {
        sLatLng = LatLng(location.latitude, location.longitude)

        val hashMap = HashMap<String, Any>()
        hashMap["latitude"] = sLatLng.latitude
        hashMap["longitude"] = sLatLng.longitude
        viewModel.setLatlng(hashMap)
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient.isConnected) {
            startLocationUpdate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mGoogleApiClient.isConnected) stopLocationUpdate()
    }

    override fun onBackPressed() = this.exitApplication()

}
