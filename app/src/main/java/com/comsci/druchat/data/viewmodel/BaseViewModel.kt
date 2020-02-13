package com.comsci.druchat.data.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.comsci.druchat.data.models.Follow
import com.comsci.druchat.data.models.Messages
import com.comsci.druchat.data.repositories.BaseRepository
import com.google.android.gms.maps.model.Marker
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class BaseViewModel : ViewModel() {

    private val repo = BaseRepository()
    val markerPeople by lazy { ArrayList<Marker>() }
    var imageUri: Uri? = null

    fun firebaseAuth() = repo.firebaseAuth

    fun currentUser() = repo.currentUser

    fun currentUserId() = repo.currentUserId

    fun getUser(uId: String = repo.currentUserId!!) = repo.getUser(uId)

    fun getUsers() = repo.getUsers()

    fun getSearch(name: String) = repo.getSearch(name)

    fun getFollows() = repo.getFollows()

    fun getChatListUsers() = repo.getChatListUsers()

    fun getChats(otherId: String) = repo.getChats(otherId)

    fun setLatlng(hashMap: HashMap<String, Any>, onFailed: (String) -> Unit) =
        repo.setLatlng(hashMap, onFailed)

    fun setState(state: String, onFailed: (String) -> Unit) = repo.setState(state, onFailed)

    fun setFollow(followId: String, friend: Follow, onFailed: (String) -> Unit) =
        repo.setFollow(followId, friend, onFailed)

    fun setMessages(
        otherId: String,
        messages: Messages,
        onComplete: (() -> Unit)? = null,
        onFailed: (String) -> Unit
    ) = repo.setMessages(otherId, messages, onComplete, onFailed)

    fun setRead(otherId: String) = repo.setRead(otherId)

    fun setRead() = repo.setRead()

    fun insertUser(
        name: String,
        imgUrl: String = "default",
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.insertUser(name, imgUrl, onComplete, onFailed)

    fun updateProfile(
        name: String,
        status: String,
        imageUrl: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.updateProfile(name, status, imageUrl, onComplete, onFailed)

    fun firebaseUpdateEmail(
        oldPassword: String,
        newEmail: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseUpdateEmail(oldPassword, newEmail, onComplete, onFailed)

    fun firebaseUpdatePassword(
        oldPassword: String,
        newPassword: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseUpdatePassword(oldPassword, newPassword, onComplete, onFailed)

    fun firebaseSendPasswordResetEmail(
        email: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseSendPasswordResetEmail(email, onComplete, onFailed)

    fun firebaseCreateUserWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseCreateUserWithEmailAndPassword(email, password, onComplete, onFailed)

    fun firebaseSignInWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseSignInWithEmailAndPassword(email, password, onComplete, onFailed)

    fun firebaseUploadImage(
        profile: Boolean,
        uri: Uri,
        imgUrl: (String) -> Unit,
        onFailed: (String) -> Unit
    ) = repo.firebaseUploadImage(profile, uri, imgUrl, onFailed)

    fun selectImage(profile: Boolean): CropImage.ActivityBuilder {
        return if (profile) {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setOutputCompressQuality(50)
                .setRequestedSize(150, 150)
                .setMinCropWindowSize(150, 150)
                .setAspectRatio(1, 1)
        } else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setOutputCompressQuality(10)
                .setMaxCropResultSize(1500, 1500)
                .setMinCropWindowSize(150, 150)
        }
    }

}

