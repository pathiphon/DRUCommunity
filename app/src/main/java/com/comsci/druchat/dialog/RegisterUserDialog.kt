package com.comsci.druchat.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.*
import com.adedom.library.extension.*
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.util.BaseDialogFragment
import com.comsci.druchat.util.extension.isEmail
import com.theartofdev.edmodo.cropper.CropImage

class RegisterUserDialog : BaseDialogFragment(
    { R.layout.dialog_register_user },
    { R.drawable.ic_user },
    { R.string.register }
) {

    private var mImageUri: Uri? = null
    private lateinit var mEtName: EditText
    private lateinit var mEtEmail: EditText
    private lateinit var mEtPassword: EditText
    private lateinit var mEtRePassword: EditText
    private lateinit var mIvProfile: ImageView
    private lateinit var mBtReg: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        mEtName = view.findViewById(R.id.mEdtName) as EditText
        mEtEmail = view.findViewById(R.id.mEdtEmail) as EditText
        mEtPassword = view.findViewById(R.id.mEdtPassword) as EditText
        mEtRePassword = view.findViewById(R.id.mEdtRePassword) as EditText
        mIvProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mBtReg = view.findViewById(R.id.mBtnReg) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mIvProfile.setOnClickListener { viewModel.selectImage(true).start(activity!!, this) }
        mBtReg.setOnClickListener { register() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mImageUri = result.uri
                mIvProfile.loadCircle(mImageUri.toString())
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                MainActivity.sContext.toast(result.error.message!!, Toast.LENGTH_LONG)
            }
        }
    }

    private fun register() {
        val name = mEtName.getContent()
        val email = mEtEmail.getContent()
        val password = mEtPassword.getContent()

        when {
            mEtName.isEmpty(getString(R.string.enter_name)) -> return
            mEtEmail.isEmpty(getString(R.string.enter_email)) -> return
            mEtPassword.isEmpty(getString(R.string.enter_password)) -> return
            mEtRePassword.isEmpty(getString(R.string.enter_re_password)) -> return
            mEtEmail.isEmail(getString(R.string.enter_valid_email)) -> return
            mEtPassword.isLength(8, getString(R.string.enter_length_password_8)) -> return
            mEtPassword.isMatching(mEtRePassword, getString(R.string.enter_not_match)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEtName.isEnabled = false
        mEtEmail.isEnabled = false
        mEtPassword.isEnabled = false
        mEtRePassword.isEnabled = false
        mIvProfile.isEnabled = false
        mBtReg.isEnabled = false

        viewModel.firebaseCreateUserWithEmailAndPassword(email, password, {
            if (mImageUri == null) {
                viewModel.insertUser(name, onComplete = {
                    mProgressBar.visibility = View.INVISIBLE

                    startActivity(
                        Intent(LoginActivity.mContext, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                })
            } else {
                viewModel.firebaseUploadImage(true, mImageUri!!, {
                    viewModel.insertUser(name, it) {
                        mProgressBar.visibility = View.INVISIBLE

                        startActivity(
                            Intent(LoginActivity.mContext, MainActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                    }
                }, { LoginActivity.mContext.toast(it, Toast.LENGTH_LONG) })
            }
        }, {
            mProgressBar.visibility = View.GONE
            dialog!!.dismiss()
            LoginActivity.mContext.toast(it, Toast.LENGTH_LONG)
        })
    }

}
