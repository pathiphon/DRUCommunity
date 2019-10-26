package com.comsci.druchat.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.model.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage

class InsertUserDialog : DialogFragment() {

    private var mImageUri: Uri? = null
    private lateinit var mEdtName: EditText
    private lateinit var mEdtEmail: EditText
    private lateinit var mEdtPassword: EditText
    private lateinit var mEdtRePassword: EditText
    private lateinit var mImgProfile: ImageView
    private lateinit var mBtnReg: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_insert_user, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setIcon(R.drawable.ic_user)
            .setTitle(com.comsci.druchat.R.string.register)

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mEdtName = view.findViewById(R.id.mEdtName) as EditText
        mEdtEmail = view.findViewById(R.id.mEdtEmail) as EditText
        mEdtPassword = view.findViewById(R.id.mEdtPassword) as EditText
        mEdtRePassword = view.findViewById(R.id.mEdtRePassword) as EditText
        mImgProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mBtnReg = view.findViewById(R.id.mBtnReg) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() {
        mImgProfile.setOnClickListener { selectImage() }
        mBtnReg.setOnClickListener { register() }
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
            if (resultCode == Activity.RESULT_OK) {
                mImageUri = result.uri
                Glide.with(context!!).load(mImageUri).circleCrop().into(mImgProfile)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(MainActivity.mContext, "${result.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun register() {
        val email = mEdtEmail.text.toString().trim()
        val password = mEdtPassword.text.toString().trim()
        val rePassword = mEdtRePassword.text.toString().trim()
        val name = mEdtName.text.toString().trim()

        when {
            name.isEmpty() -> {
                mEdtName.error = "Please enter Name"
                mEdtName.requestFocus()
                return
            }
            email.isEmpty() -> {
                mEdtEmail.error = "Please enter E-mail"
                mEdtEmail.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                mEdtEmail.error = "Please enter a valid email"
                mEdtEmail.requestFocus()
                return
            }
            password.isEmpty() -> {
                mEdtPassword.error = "Please enter Password"
                mEdtPassword.requestFocus()
                return
            }
            rePassword.isEmpty() -> {
                mEdtRePassword.error = "Please enter Re-password"
                mEdtRePassword.requestFocus()
                return
            }
            password.length < 8 -> {
                mEdtPassword.error = "Minimum length of password should be 8"
                mEdtPassword.requestFocus()
                return
            }
            password != rePassword -> {
                mEdtPassword.error = "Passwords do not match"
                mEdtPassword.requestFocus()
                return
            }
        }

        mProgressBar.visibility = View.VISIBLE
        dialog.setCanceledOnTouchOutside(false)
        mEdtName.isEnabled = false
        mEdtEmail.isEnabled = false
        mEdtPassword.isEnabled = false
        mEdtRePassword.isEnabled = false
        mImgProfile.isEnabled = false
        mBtnReg.isEnabled = false

        mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateProfile(name, mAuth.currentUser!!.uid)
            } else {
                dialog.dismiss()
                mProgressBar.visibility = View.GONE
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(context, "You are already registered", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateProfile(name: String, userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        if (mImageUri == null) {
            insertUser(databaseReference, name)
        } else {
            val storage = FirebaseStorage.getInstance().getReference("profile")
                .child("${System.currentTimeMillis()}.jpg")

            val uploadTask = storage.putFile(mImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }

                storage.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uri = task.result.toString()
                    insertUser(databaseReference, name, uri)
                } else {
                    Toast.makeText(context, "Failed!", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun insertUser(databaseReference: DatabaseReference, name: String, imgUri: String = "default") {
        val user = UserItem(mAuth.currentUser!!.uid, name, "", imgUri)
        databaseReference.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mProgressBar.visibility = View.INVISIBLE

                startActivity(
                    Intent(LoginActivity.mContext, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }
    }
}
