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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.dialogNegative
import com.adedom.library.extension.failed
import com.adedom.library.extension.loadCircle
import com.adedom.library.extension.toast
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Users
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.dialog.ChangeEmailDialog
import com.comsci.druchat.dialog.ChangePasswordDialog
import com.theartofdev.edmodo.cropper.CropImage

class ProfileFragment : Fragment() {

    private lateinit var viewModel: BaseViewModel

    private var mImageUri: Uri? = null
    private lateinit var mUserItem: Users
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

        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        init(view)

        return view
    }

    private fun init(view: View) {
        mImgProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mEdtName = view.findViewById(R.id.mEdtName) as EditText
        mEdtStatus = view.findViewById(R.id.mEdtStatus) as EditText
        mBtnSave = view.findViewById(R.id.mBtnSave) as Button
        mTvVerification = view.findViewById(R.id.mTvVerification) as TextView
        mBtnChangeEmail = view.findViewById(R.id.mBtnChangeEmail) as Button
        mBtnChangePassword = view.findViewById(R.id.mBtnChangePassword) as Button
        mBtnLogout = view.findViewById(R.id.mBtnLogout) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        if (viewModel.currentUser()!!.isEmailVerified) {
            mBtnChangeEmail.isEnabled = false
            mTvVerification.isEnabled = false
            mTvVerification.text = getString(R.string.verification_completed)
        } else {
            mTvVerification.text = getString(R.string.verification_not_completed)
            mTvVerification.error = ""
        }

        viewModel.getUser().observe(this, Observer {
            mUserItem = it
            mEdtName.setText(mUserItem.name)
            mEdtStatus.setText(mUserItem.status)
            if (mUserItem.imageURL != "default") mImgProfile.loadCircle(mUserItem.imageURL)
        })

        mImgProfile.setOnClickListener { selectImage() }
        mBtnSave.setOnClickListener { saveProfile() }
        mTvVerification.setOnClickListener { setVerification() }
        mBtnChangeEmail.setOnClickListener {
            if (!viewModel.currentUser()!!.isEmailVerified)
                ChangeEmailDialog().show(activity!!.supportFragmentManager, null)
        }
        mBtnChangePassword.setOnClickListener {
            ChangePasswordDialog().show(activity!!.supportFragmentManager, null)
        }
        mBtnLogout.setOnClickListener { logout() }
    }

    private fun setVerification() {
        viewModel.currentUser()!!.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModel.firebaseAuth().signOut()
                activity!!.finish()
                startActivity(
                    Intent(MainActivity.sContext, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                MainActivity.sContext.toast(R.string.check_email, Toast.LENGTH_LONG)
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
                mImgProfile.loadCircle(mImageUri.toString())
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                MainActivity.sContext.toast("${result.error}", Toast.LENGTH_LONG)
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
                        val storage = viewModel.storageProfile().child("${System.currentTimeMillis()}.jpg")
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
                                MainActivity.sContext.failed()
                            }
                        }.addOnFailureListener { exception ->
                            mProgressBar.visibility = View.INVISIBLE
                            MainActivity.sContext.toast(exception.message!!, Toast.LENGTH_LONG)
                        }
                    }
                }
            }.show()
    }

    private fun updateProfile(image: String = mUserItem.imageURL) {
//        val hashMap = HashMap<String, Any>()
//        hashMap["name"] = mEdtName.text.toString()
//        hashMap["status"] = mEdtStatus.text.toString()
//        hashMap["imageURL"] = image
//        mDatabaseUser.child(viewModel.currentUser().uid).updateChildren(hashMap)
//            .addOnCompleteListener { task ->
//                mProgressBar.visibility = View.INVISIBLE
//                if (task.isSuccessful) {
//                    feedProfile()
//                    mImageUri = null
//                    Toast.makeText(
//                        MainActivity.sContext,
//                        "Profile update complete",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
    }

    private fun logout() {
        AlertDialog.Builder(activity!!).dialogNegative(
            R.string.logout,
            R.string.logout_messages,
            R.drawable.ic_exit_to_app_black
        ) {
            viewModel.setState("offline")
            viewModel.firebaseAuth().signOut()
            activity!!.finish()
            startActivity(
                Intent(MainActivity.sContext, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }
}
