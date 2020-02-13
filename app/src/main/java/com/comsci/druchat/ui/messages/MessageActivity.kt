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

class MessageActivity : BaseActivity() {

    private lateinit var mUserId: String
    private lateinit var mAdapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        init()

        fetchChats()

    }

    private fun init() {
        mUserId = intent.getStringExtra(KEY_USER_ID)!!

        mAdapter = MessagesAdapter(viewModel.currentUserId()!!)
        mRecyclerView.recyclerVertical(true) { it.adapter = mAdapter }

        viewModel.getUser(mUserId).observe(this, Observer {
            if (it.imageURL != KEY_DEFAULT) mImgProfile.loadCircle(it.imageURL)
            mTvName.text = it.name
            mTvStatus.text = it.status
            if (it.state != KEY_OFFLINE) {
                mImgState.setImageResource(R.drawable.shape_state_green)
            }
        })

        mIvSend.setOnClickListener { sendMessage() }
        mIvImage.setOnClickListener { viewModel.selectImage(false).start(this) }

        mAdapter.onClickImage = {
            val bundle = Bundle()
            bundle.putString(KEY_IMAGE, it)

            val dialog = ImageDialog()
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, null)
        }
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
        val messages = mEtSend.text.toString().trim()
        if (messages.isNotEmpty()) {
            val dateTime = System.currentTimeMillis()
            val chat = Messages(
                viewModel.currentUserId()!!, mUserId, messages, KEY_EMPTY, dateTime,
                MainActivity.sLatLng.latitude, MainActivity.sLatLng.longitude
            )
            viewModel.setMessages(mUserId, chat, {
                mEtSend.setText(KEY_EMPTY)
            }, {
                baseContext.toast(it, Toast.LENGTH_LONG)
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mProgressBar.visibility = View.VISIBLE
                val imageUri = result.uri
                viewModel.firebaseUploadImage(false, imageUri, { url ->
                    mProgressBar.visibility = View.INVISIBLE
                    val dateTime = System.currentTimeMillis()
                    val messages = Messages(
                        viewModel.currentUserId()!!, mUserId, KEY_EMPTY, url, dateTime,
                        MainActivity.sLatLng.latitude, MainActivity.sLatLng.longitude
                    )
                    viewModel.setMessages(mUserId, messages, onFailed = {
                        baseContext.toast(it, Toast.LENGTH_LONG)
                    })
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
