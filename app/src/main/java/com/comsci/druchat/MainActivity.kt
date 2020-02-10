package com.comsci.druchat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adedom.library.extension.exitApplication
import com.comsci.druchat.fragments.*
import com.comsci.druchat.util.BaseActivity
import com.comsci.druchat.util.MyCode
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

    val TAG = "MainActivity"
    private lateinit var mLocationSwitchStateReceiver: BroadcastReceiver

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

        locationListener()
        locationSetting()

        mBottomNavigationView.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.mFrameLayout, HomeFragment())
                .commit()
        }
    }

    //region setApp
    private fun locationListener() {
        mLocationSwitchStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val isGpsEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) //NETWORK_PROVIDER

                    if (!isGpsEnabled) {
                        locationSetting()
                    }
                }
            }
        }
    }

    private fun locationListener(boolean: Boolean) {
        if (boolean) {
            val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
            registerReceiver(mLocationSwitchStateReceiver, filter)
        } else {
            unregisterReceiver(mLocationSwitchStateReceiver)
        }
    }

    private fun locationSetting() {
        if (!MyCode.locationSetting(sContext)) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1234)
        } else {
            mGoogleApiClient = GoogleApiClient.Builder(sContext)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!MyCode.locationSetting(sContext) && requestCode == 1234) {
            Toast.makeText(sContext, "Please grant location", Toast.LENGTH_LONG).show()
            finishAffinity()
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
    //endregion

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_chats -> selectedFragment = ChatsFragment()
            R.id.nav_groups -> selectedFragment = GroupsFragment()
            R.id.nav_map -> selectedFragment = MapsFragment()
            R.id.nav_profile -> selectedFragment = ProfileFragment()
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
        locationListener(true)
        if (mGoogleApiClient.isConnected) {
            startLocationUpdate()
        }
    }

    override fun onPause() {
        super.onPause()
        locationListener(false)
    }

    override fun onBackPressed() = this.exitApplication()

}
