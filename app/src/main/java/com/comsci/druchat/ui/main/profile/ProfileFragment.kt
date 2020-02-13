package com.comsci.druchat.ui.main.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.*
import com.adedom.library.util.BaseFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.login.LoginActivity
import com.comsci.druchat.ui.main.MainActivity
import com.comsci.druchat.util.KEY_DEFAULT
import com.comsci.druchat.util.KEY_EMPTY
import com.comsci.druchat.util.KEY_OFFLINE
import com.theartofdev.edmodo.cropper.CropImage

class ProfileFragment : BaseFragment<BaseViewModel>({ R.layout.fragment_profile }) {

    private var mImageUri: Uri? = null
    private lateinit var mUserItem: User
    private lateinit var mIvProfile: ImageView
    private lateinit var mEtName: EditText
    private lateinit var mEtStatus: EditText
    private lateinit var mBtSave: Button
    private lateinit var mTvVerification: TextView
    private lateinit var mBtChangeEmail: Button
    private lateinit var mBtChangePassword: Button
    private lateinit var mBtLogout: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initFragment(view: View) {
        super.initFragment(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mIvProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mEtName = view.findViewById(R.id.mEdtName) as EditText
        mEtStatus = view.findViewById(R.id.mEdtStatus) as EditText
        mBtSave = view.findViewById(R.id.mBtnSave) as Button
        mTvVerification = view.findViewById(R.id.mTvVerification) as TextView
        mBtChangeEmail = view.findViewById(R.id.mBtnChangeEmail) as Button
        mBtChangePassword = view.findViewById(R.id.mBtnChangePassword) as Button
        mBtLogout = view.findViewById(R.id.mBtnLogout) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        if (viewModel.currentUser()!!.isEmailVerified) {
            mBtChangeEmail.isEnabled = false
            mTvVerification.isEnabled = false
            mTvVerification.text = getString(R.string.verification_completed)
        } else {
            mTvVerification.text = getString(R.string.verification_not_completed)
            mTvVerification.error = KEY_EMPTY
        }

        viewModel.getUser().observe(this, Observer {
            mUserItem = it
            mEtName.setText(mUserItem.name)
            mEtStatus.setText(mUserItem.status)
            if (mUserItem.imageURL != KEY_DEFAULT) mIvProfile.loadCircle(mUserItem.imageURL)
        })

        mIvProfile.setOnClickListener { viewModel.selectImage(true).start(activity!!, this) }
        mBtSave.setOnClickListener { saveProfile() }
        mTvVerification.setOnClickListener { setVerification() }
        mBtChangeEmail.setOnClickListener {
            if (!viewModel.currentUser()!!.isEmailVerified)
                ChangeEmailDialog().show(activity!!.supportFragmentManager, null)
        }
        mBtChangePassword.setOnClickListener {
            ChangePasswordDialog()
                .show(activity!!.supportFragmentManager, null)
        }
        mBtLogout.setOnClickListener { logout() }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                mIvProfile.loadCircle(mImageUri.toString())
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                MainActivity.sContext.toast("${result.error}", Toast.LENGTH_LONG)
            }
        }
    }

    private fun saveProfile() {
        AlertDialog.Builder(activity!!)
            .dialogPositive(R.string.profile, R.string.edit_profile, R.drawable.ic_user) {
                val name = mEtName.getContent()
                val status = mEtStatus.getContent()

                mProgressBar.visibility = View.VISIBLE

                when {
                    mEtName.isEmpty(getString(R.string.enter_name)) -> {
                        mProgressBar.visibility = View.INVISIBLE
                        return@dialogPositive
                    }
                    mImageUri == null -> {
                        viewModel.updateProfile(name, status, mUserItem.imageURL, {
                            mProgressBar.visibility = View.INVISIBLE
                            MainActivity.sContext.toast(R.string.profile_update_complete)
                        }, {
                            mProgressBar.visibility = View.INVISIBLE
                            MainActivity.sContext.toast(it, Toast.LENGTH_LONG)
                        })
                    }
                    mImageUri != null -> {
                        viewModel.firebaseUploadImage(true, mImageUri!!, { url ->
                            mProgressBar.visibility = View.INVISIBLE
                            viewModel.updateProfile(name, status, url, {
                                mImageUri = null
                                mProgressBar.visibility = View.INVISIBLE
                                MainActivity.sContext.toast(R.string.profile_update_complete)
                            }, {
                                mProgressBar.visibility = View.INVISIBLE
                                MainActivity.sContext.toast(it, Toast.LENGTH_LONG)
                            })
                        }, {
                            mProgressBar.visibility = View.INVISIBLE
                            MainActivity.sContext.toast(it, Toast.LENGTH_LONG)
                        })
                    }
                }
            }
    }

    private fun logout() {
        AlertDialog.Builder(activity!!).dialogNegative(
            R.string.logout,
            R.string.logout_messages,
            R.drawable.ic_exit_to_app_black
        ) {
            viewModel.setState(KEY_OFFLINE) { MainActivity.sContext.toast(it, Toast.LENGTH_LONG) }
            viewModel.firebaseAuth().signOut()
            activity!!.finish()
            startActivity(
                Intent(MainActivity.sContext, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }
}
