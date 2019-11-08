package com.comsci.druchat

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.comsci.druchat.model.ChatListItem
import com.comsci.druchat.model.ChatsItem
import com.comsci.druchat.model.UserItem
import com.comsci.druchat.utility.MyCode
import com.comsci.druchat.utility.MyMessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.item_user.*
import kotlinx.android.synthetic.main.template_chats.*
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {

    val TAG = "MessageActivity"
    private lateinit var mLocationSwitchStateReceiver: BroadcastReceiver
    private lateinit var mUser: FirebaseUser
    private lateinit var mDatabaseUsers: DatabaseReference
    private lateinit var mDatabaseChats: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mContext: Context
    private lateinit var mUserId: String
    private lateinit var mChatItem: ArrayList<ChatsItem>
    private lateinit var mReadListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        mContext = baseContext

        mUserId = intent.getStringExtra("user_id")

        locationListener()
        locationSetting()

        mUser = FirebaseAuth.getInstance().currentUser!!
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("Users")
        mDatabaseChats = FirebaseDatabase.getInstance().getReference("Chats")
        mStorage = FirebaseStorage.getInstance().getReference("image")

        feedData()

        mChatItem = arrayListOf<ChatsItem>()
        val linearLayoutManager = LinearLayoutManager(mContext)
        linearLayoutManager.stackFromEnd = true
        mRecyclerView.layoutManager = linearLayoutManager

        setEvents()
        setRead()
    }

    private fun feedData() {
        //follow
        mDatabaseUsers.child(mUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(UserItem::class.java)
                if (user!!.imageURL != "default") {
                    Glide.with(mContext).load(user.imageURL).circleCrop().into(mImgProfile)
                }
                mTvName.text = user.name
                mTvStatus.text = user.status
                if (user.state != "offline") {
                    mImgState.setImageResource(R.drawable.shape_state_green)
                }
            }
        })

        //chat
        mDatabaseChats.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mChatItem.clear()
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(ChatsItem::class.java)!!
                    if (chat.receiver == mUser.uid && chat.sender == mUserId ||
                        chat.receiver == mUserId && chat.sender == mUser.uid
                    ) {
                        mChatItem.add(chat)
                    }
                }
                mRecyclerView.adapter = CustomAdapter()
            }
        })
    }

    //region setApp
    private fun locationListener() {
        mLocationSwitchStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        if (!MyCode.locationSetting(mContext)) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1234)
        }
    }

    //endregion

    override fun onResume() {
        super.onResume()
        locationListener(true)
        MyCode.setState(mDatabaseUsers, mUser.uid, "online")
    }

    override fun onPause() {
        super.onPause()
        locationListener(false)
        MyCode.setState(mDatabaseUsers, mUser.uid, "offline")
        mDatabaseChats.removeEventListener(mReadListener)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun setEvents() {
        mImgSend.setOnClickListener { sendMessage() }
        mImgImage.setOnClickListener { selectImage() }
    }

    private fun setRead() {
        mReadListener = mDatabaseChats.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(ChatsItem::class.java)!!
                    if (chat.receiver == mUser.uid && chat.sender == mUserId) {
                        snapshot.ref.child("isread").setValue(true)
                    }
                }
            }
        })
    }

    private fun sendMessage() {
        val message = mEdtSend.text.toString().trim()
        if (message.isNotEmpty()) {
            val dateTime = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
                .format(Calendar.getInstance().time)
            val chat = ChatsItem(
                mUser.uid, mUserId, message, "", dateTime,
                MainActivity.mLatLng.latitude, MainActivity.mLatLng.longitude
            )
            val key = mDatabaseChats.push().key
            mDatabaseChats.child(key!!).setValue(chat).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mEdtSend.setText("")
                    addChatList(key)
                }
            }
        }
    }

    private fun addChatList(key: String) {
        val databaseChatList = FirebaseDatabase.getInstance().getReference("ChatList")
        databaseChatList.child(mUser.uid).child(mUserId).setValue(ChatListItem(key, mUserId))
        databaseChatList.child(mUserId).child(mUser.uid).setValue(ChatListItem(key, mUser.uid))
    }

    private fun selectImage() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setOutputCompressQuality(10)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //set app
        if (!MyCode.locationSetting(mContext) && requestCode == 1234) {
            Toast.makeText(mContext, "Please grant location", Toast.LENGTH_LONG).show()
            finishAffinity()
        }

        //select image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mProgressBar.visibility = View.VISIBLE
                val imageUri = result.uri

                val storage = mStorage.child("${System.currentTimeMillis()}.jpg")
                val uploadTask = storage.putFile(imageUri)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }

                    storage.downloadUrl
                }.addOnCompleteListener { task ->
                    mProgressBar.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        val uri = task.result.toString()

                        val dateTime = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
                            .format(Calendar.getInstance().time)
                        val chat = ChatsItem(
                            mUser.uid, mUserId, "", uri, dateTime,
                            MainActivity.mLatLng.latitude, MainActivity.mLatLng.longitude
                        )
                        val key = mDatabaseChats.push().key
                        mDatabaseChats.child(key!!).setValue(chat).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                addChatList(key)
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.mContext, "Failed!", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { exception ->
                    mProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(MainActivity.mContext, "${exception.message}", Toast.LENGTH_LONG).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(MainActivity.mContext, "${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class CustomAdapter : RecyclerView.Adapter<CustomHolder>() {
        private lateinit var MSG_TYPE: MyMessageType

        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): CustomHolder {
            val view: View = if (MSG_TYPE == MyMessageType.CHAT_RIGHT) {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.item_chat_right, viewGroup, false)
            } else {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.item_chat_left, viewGroup, false)
            }
            return CustomHolder(view)
        }

        override fun getItemCount(): Int {
            return mChatItem.size
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val chat = mChatItem[position]
            // message
            holder.mTvMessage.text = chat.message

            //time
            holder.mTvTime.text = datetimeFormat("time", chat.dateTime)

            //date
            if (position > 0 && datetimeFormat("date", chat.dateTime) ==
                datetimeFormat("date", mChatItem[position - 1].dateTime)
            ) {
                holder.mTvDate.visibility = View.GONE
            } else {
                holder.mTvDate.text = datetimeFormat("date", chat.dateTime)
            }

            //read
            if (chat.isread) {
                holder.mTvRead.visibility = View.VISIBLE
            }

            //image
            if (chat.message.isEmpty()) {
                holder.mTvMessage.visibility = View.GONE
                holder.mImgImage.visibility = View.VISIBLE

                Glide.with(mContext)
                    .asBitmap()
                    .load(chat.image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            if (resource.width > resource.height) {
                                holder.mImgImage.layoutParams.height = 300
                            }
                            Glide.with(mContext).load(chat.image).into(holder.mImgImage)
                        }
                    })
            }

            //location
            if (position > 0 && getLocality(chat.latitude, chat.longitude) ==
                getLocality(mChatItem[position - 1].latitude, mChatItem[position - 1].longitude)
            ) {
                holder.mTvLocation.visibility = View.GONE
            } else {
                holder.mTvLocation.text = getLocality(chat.latitude, chat.longitude)
            }
        }

        private fun getLocality(latitude: Double, longitude: Double): String {
            val list = Geocoder(mContext).getFromLocation(latitude, longitude, 1)
            return if (list[0].locality != null) {
                list[0].locality
            } else {
                "unknown"
            }
        }

        private fun datetimeFormat(format: String, dateTime: String): String {
//            "EEE, dd MMM yy HH:mm"
            return if (format == "date") {
                dateTime.substring(0, 15).trim()
            } else {
                dateTime.substring(15, dateTime.length).trim()
            }
        }

        override fun getItemViewType(position: Int): Int {
            MSG_TYPE = if (mChatItem[position].sender == mUser.uid) {
                MyMessageType.CHAT_RIGHT
            } else {
                MyMessageType.CHAT_LEFT
            }
            return position
        }
    }

    inner class CustomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImgImage: ImageView = itemView.findViewById(R.id.mImgImage)
        val mTvMessage: TextView = itemView.findViewById(R.id.mTvMessage)
        val mTvRead: TextView = itemView.findViewById(R.id.mTvRead)
        val mTvTime: TextView = itemView.findViewById(R.id.mTvTime)
        val mTvDate: TextView = itemView.findViewById(R.id.mTvDate)
        val mTvLocation: TextView = itemView.findViewById(R.id.mTvLocation)
    }
}