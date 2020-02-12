package com.comsci.druchat.ui.messages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.adedom.library.extension.loadCircle
import com.adedom.library.extension.recyclerVertical
import com.adedom.library.extension.toast
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Messages
import com.comsci.druchat.ui.main.MainActivity
import com.comsci.druchat.util.*
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.item_user.*
import kotlinx.android.synthetic.main.template_chats.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : BaseActivity() {

    private lateinit var mUserId: String
    private lateinit var mAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        mUserId = intent.getStringExtra(KEY_USER_ID)!!

        init()

        fetchChats()

    }

    private fun init() {
        mAdapter =
            MessagesAdapter(viewModel.currentUserId()!!)
        mRecyclerView.recyclerVertical(true) { it.adapter = mAdapter }

        viewModel.getUser(mUserId).observe(this, Observer {
            if (it.imageURL != KEY_DEFAULT) mImgProfile.loadCircle(it.imageURL)
            mTvName.text = it.name
            mTvStatus.text = it.status
            if (it.state != KEY_OFFLINE) {
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

    override fun onResume() {
        super.onResume()
        viewModel.setRead(mUserId)
    }

    override fun onPause() {
        super.onPause()
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
                mEdtSend.setText(KEY_EMPTY)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //select image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mProgressBar.visibility = View.VISIBLE
                val imageUri = result.uri
                viewModel.firebaseUploadImage(false, imageUri, { url ->
                    mProgressBar.visibility = View.INVISIBLE

                    val dateTime = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
                        .format(Calendar.getInstance().time)
                    val messages = Messages(
                        viewModel.currentUserId()!!, mUserId, KEY_EMPTY, url, dateTime,
                        MainActivity.sLatLng.latitude, MainActivity.sLatLng.longitude
                    )
                    viewModel.setMessages(mUserId, messages)
                }, {
                    mProgressBar.visibility = View.INVISIBLE
                    baseContext.toast(it, Toast.LENGTH_LONG)
                })
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                baseContext.toast(result.error.message!!, Toast.LENGTH_LONG)
            }
        }
    }
}
