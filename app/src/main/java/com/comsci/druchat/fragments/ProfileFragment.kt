package com.comsci.druchat.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.dialog.ChangeEmailDialog
import com.comsci.druchat.dialog.ChangePasswordDialog
import com.comsci.druchat.model.UserItem
import com.comsci.druchat.utility.MyCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage

class ProfileFragment : Fragment() {

    private var mImageUri: Uri? = null
    private lateinit var mUserItem: UserItem
    private lateinit var mUser: FirebaseUser
    private lateinit var mDatabaseUser: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mImgProfile: ImageView
    private lateinit var mEdtName: EditText
    private lateinit var mEdtStatus: EditText
    private lateinit var mBtnSave: Button
    private lateinit var mTvVerification: TextView
    private lateinit var mBtnChangeEmail: Button
    private lateinit var mBtnChangePassword: Button
    private lateinit var mBtnLogout: Button
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        mUser = FirebaseAuth.getInstance().currentUser!!

        bindWidgets(view)
        setWidgets()
        setEvents()

        mStorage = FirebaseStorage.getInstance().getReference("profile")
        mDatabaseUser = FirebaseDatabase.getInstance().getReference("Users")
        feedProfile()

        return view
    }

    private fun feedProfile() {
        mDatabaseUser.child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val name = dataSnapshot.child("name").value.toString()

                mUserItem = dataSnapshot.getValue(UserItem::class.java)!!
                mEdtName.setText(mUserItem.name)
                mEdtStatus.setText(mUserItem.status)
                if (mUserItem.imageURL != "default") {
                    Glide.with(MainActivity.mContext).load(mUserItem.imageURL).circleCrop().into(mImgProfile)
                }
            }
        })
    }

    private fun bindWidgets(view: View) {
        mImgProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mEdtName = view.findViewById(R.id.mEdtName) as EditText
        mEdtStatus = view.findViewById(R.id.mEdtStatus) as EditText
        mBtnSave = view.findViewById(R.id.mBtnSave) as Button
        mTvVerification = view.findViewById(R.id.mTvVerification) as TextView
        mBtnChangeEmail = view.findViewById(R.id.mBtnChangeEmail) as Button
        mBtnChangePassword = view.findViewById(R.id.mBtnChangePassword) as Button
        mBtnLogout = view.findViewById(R.id.mBtnLogout) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setWidgets() {
        if (mUser.isEmailVerified) {
            mBtnChangeEmail.isEnabled = false
            mTvVerification.isEnabled = false
            mTvVerification.text = "verification completed"
        } else {
            mTvVerification.text = "verification not completed (Click to Verify)"
            mTvVerification.error = ""
        }
    }

    private fun setEvents() {
        mImgProfile.setOnClickListener { selectImage() }
        mBtnSave.setOnClickListener { saveProfile() }
        mTvVerification.setOnClickListener { setVerification() }
        mBtnChangeEmail.setOnClickListener {
            if (!mUser.isEmailVerified) {
                ChangeEmailDialog().show(activity!!.supportFragmentManager, null)
            }
        }
        mBtnChangePassword.setOnClickListener { ChangePasswordDialog().show(activity!!.supportFragmentManager, null) }
        mBtnLogout.setOnClickListener { logout() }
    }

    private fun setVerification() {
        mUser.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseAuth.getInstance().signOut()
                activity!!.finish()
                startActivity(
                    Intent(MainActivity.mContext, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                Toast.makeText(MainActivity.mContext, "Please check your email", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun selectImage() {
        CropImage.activity()
            .setOutputCompressQuality(50)
            .setRequestedSize(150, 150)
            .setMinCropWindowSize(150, 150)
            .setAspectRatio(1, 1)
            .start(activity!!, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                Glide.with(context!!).load(mImageUri).circleCrop().into(mImgProfile)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(MainActivity.mContext, "${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveProfile() {
        val build = AlertDialog.Builder(activity!!)
        build.setTitle(R.string.profile)
            .setMessage("Do you want to edit the profile?")
            .setIcon(R.drawable.ic_user)
            .setNegativeButton(R.string.no) { dialogInterface, i ->
                dialogInterface.dismiss()
            }.setPositiveButton(R.string.yes) { dialogInterface, i ->
                mProgressBar.visibility = View.VISIBLE
                val name = mEdtName.text.toString()
                when {
                    name.isEmpty() -> {
                        mEdtName.error = "Please enter Name"
                        mEdtName.requestFocus()
                        mProgressBar.visibility = View.INVISIBLE
                        return@setPositiveButton
                    }
                    mImageUri == null -> {
                        updateProfile()
                    }
                    mImageUri != null -> {
                        val storage = mStorage.child("${System.currentTimeMillis()}.jpg")
                        val uploadTask = storage.putFile(mImageUri!!)
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception!!
                            }

                            storage.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                updateProfile(task.result.toString())
                            } else {
                                mProgressBar.visibility = View.INVISIBLE
                                Toast.makeText(MainActivity.mContext, "Failed!", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener { exception ->
                            mProgressBar.visibility = View.INVISIBLE
                            Toast.makeText(MainActivity.mContext, "${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.show()
    }

    private fun updateProfile(image: String = mUserItem.imageURL) {
        val hashMap = HashMap<String, Any>()
        hashMap["name"] = mEdtName.text.toString()
        hashMap["status"] = mEdtStatus.text.toString()
        hashMap["imageURL"] = image
        mDatabaseUser.child(mUser.uid).updateChildren(hashMap).addOnCompleteListener { task ->
            mProgressBar.visibility = View.INVISIBLE
            if (task.isSuccessful) {
                feedProfile()
                mImageUri = null
                Toast.makeText(MainActivity.mContext, "Profile update complete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        val build = AlertDialog.Builder(activity!!)
        build.setTitle(R.string.logout)
            .setMessage("Do you want to log out?")
            .setIcon(R.drawable.ic_exit_to_app_black)
            .setPositiveButton(R.string.no) { dialogInterface, i ->
                dialogInterface.dismiss()
            }.setNegativeButton(R.string.yes) { dialogInterface, i ->
                MyCode.setState(mDatabaseUser, mUser.uid, "offline")
                FirebaseAuth.getInstance().signOut()
                activity!!.finish()
                startActivity(
                    Intent(MainActivity.mContext, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }.show()
    }
}
