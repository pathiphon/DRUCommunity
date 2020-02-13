package com.comsci.druchat.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.adedom.library.extension.exitApplication
import com.adedom.library.extension.toast
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
        } else {
            viewModel.getUser(viewModel.currentUserId()!!).observe(this, Observer { user ->
                viewModel.updateProfile(user.name, user.status, user.imageURL, {}, {
                    baseContext.toast(it, Toast.LENGTH_LONG)
                })
            })
        }

        init(savedInstanceState)

    }

    private fun init(savedInstanceState: Bundle?) {
        mBottomNavigationView.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mFrameLayout, HomeFragment())
                .commit()
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        mGoogleApiClient.connect()

        mLocationRequest = LocationRequest()
            .setInterval(10000)
            .setFastestInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_home -> fragment = HomeFragment()
            R.id.nav_chats -> fragment = ChatsFragment()
            R.id.nav_groups -> fragment = GroupsFragment()
            R.id.nav_map -> fragment = MapsFragment()
            R.id.nav_profile -> fragment = ProfileFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mFrameLayout, fragment!!)
            .commit()
        return true
    }

    override fun onConnected(p0: Bundle?) = startLocationUpdate()

    override fun onConnectionSuspended(p0: Int) = mGoogleApiClient.connect()

    override fun onConnectionFailed(p0: ConnectionResult) {}

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

    override fun onLocationChanged(location: Location) {
        sLatLng = LatLng(location.latitude, location.longitude)

        val hashMap = HashMap<String, Any>()
        hashMap["latitude"] = sLatLng.latitude
        hashMap["longitude"] = sLatLng.longitude
        viewModel.setLatlng(hashMap) { baseContext.toast(it, Toast.LENGTH_LONG) }
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient.isConnected) startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        if (mGoogleApiClient.isConnected) stopLocationUpdate()
    }

    override fun onBackPressed() = this.exitApplication()

}
