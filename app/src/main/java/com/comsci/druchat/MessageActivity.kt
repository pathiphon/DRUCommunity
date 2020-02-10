package com.comsci.druchat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.adedom.library.extension.loadCircle
import com.adedom.library.extension.toast
import com.comsci.druchat.data.models.Messages
import com.comsci.druchat.util.BaseActivity
import com.comsci.druchat.util.MyCode
import com.comsci.druchat.util.extension.recyclerViewChats
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.item_user.*
import kotlinx.android.synthetic.main.template_chats.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : BaseActivity() {

    private lateinit var mUserId: String
    private lateinit var mAdapter: MessagesAdapter

    private lateinit var mLocationSwitchStateReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        mUserId = intent.getStringExtra("user_id")!!

        locationListener()
        locationSetting()

        init()

        fetchChats()

    }

    private fun init() {
        mAdapter = MessagesAdapter(viewModel.currentUserId()!!)
        mRecyclerView.recyclerViewChats { it.adapter = mAdapter }

        viewModel.getUser(mUserId).observe(this, Observer {
            if (it.imageURL != "default") mImgProfile.loadCircle(it.imageURL)
            mTvName.text = it.name
            mTvStatus.text = it.status
            if (it.state != "offline") {
                mImgState.setImageResource(R.drawable.shape_state_green)
            }
        })

        mImgSend.setOnClickListener { sendMessage() }
        mImgImage.setOnClickListener { viewModel.selectImage(false).start(this) }
    }

    private fun fetchChats() {
        viewModel.getChats(mUserId).observe(this, Observer {
            mAdapter.setList(it)
        })
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
        if (!MyCode.locationSetting(baseContext)) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1234)
        }
    }

    //endregion

    override fun onResume() {
        super.onResume()
        locationListener(true)

        viewModel.setRead(mUserId)

    }

    override fun onPause() {
        super.onPause()
        locationListener(false)

        viewModel.setRead()
    }

    private fun sendMessage() {
        val message = mEdtSend.text.toString().trim()
        if (message.isNotEmpty()) {
            val dateTime = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
                .format(Calendar.getInstance().time)
//            val dateTime = ServerValue.TIMESTAMP

            val chat = Messages(
                viewModel.currentUserId()!!, mUserId, message, "", dateTime,
                MainActivity.sLatLng.latitude, MainActivity.sLatLng.longitude
            )
            viewModel.setMessages(mUserId, chat) {
                mEdtSend.setText("")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //set app
        if (!MyCode.locationSetting(baseContext) && requestCode == 1234) {
            baseContext.toast(R.string.please_grant_location, Toast.LENGTH_LONG)
            finishAffinity()
        }

        //select image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mProgressBar.visibility = View.VISIBLE
                val imageUri = result.uri
                viewModel.firebaseUploadImage(viewModel.storageImage(), imageUri, { url ->
                    mProgressBar.visibility = View.INVISIBLE

                    val dateTime = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
                        .format(Calendar.getInstance().time)
                    val messages = Messages(
                        viewModel.currentUserId()!!, mUserId, "", url, dateTime,
                        MainActivity.sLatLng.latitude, MainActivity.sLatLng.longitude
                    )
                    viewModel.setMessages(mUserId, messages)
                }, {
                    mProgressBar.visibility = View.INVISIBLE
                    baseContext.toast(it, Toast.LENGTH_LONG)
                })
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                baseContext.toast("${result.error}", Toast.LENGTH_LONG)
            }
        }
    }
}
