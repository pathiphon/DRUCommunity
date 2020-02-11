package com.comsci.druchat.data.repositories

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adedom.library.extension.addListenerForSingleValueEvent
import com.adedom.library.extension.addValueEventListener
import com.comsci.druchat.data.models.ChatList
import com.comsci.druchat.data.models.Follow
import com.comsci.druchat.data.models.Messages
import com.comsci.druchat.data.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class BaseRepository {

    val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    val currentUser = firebaseAuth.currentUser
    val currentUserId = currentUser?.uid

    private val mUsers = firebaseDatabase.getReference("Users")
    private val mFollow = firebaseDatabase.getReference("Follow")
    private val mChats = firebaseDatabase.getReference("Chats")
    private val mChatList = firebaseDatabase.getReference("ChatList")

    private var storageProfile = firebaseStorage.getReference("profile")
    private var storageImage = firebaseStorage.getReference("image")

    private lateinit var mReadListener: ValueEventListener

    fun getUser(uId: String): LiveData<User> {
        val liveData = MutableLiveData<User>()
        mUsers.child(uId).addValueEventListener {
            liveData.value = it.getValue(User::class.java)
        }
        return liveData
    }

    fun getUsers(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        mUsers.addValueEventListener {
            val items = arrayListOf<User>()
            for (snapshot in it.children) {
                val item = snapshot.getValue(User::class.java)
                items.add(item!!)
            }
            liveData.value = items
        }
        return liveData
    }

    fun getSearch(name: String): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        mUsers.orderByChild("name").startAt(name).endAt(name + "\uf8ff")
            .addListenerForSingleValueEvent {
                val items = arrayListOf<User>()
                for (snapshot in it.children) {
                    val item = snapshot.getValue(User::class.java)
                    if (currentUserId != item!!.user_id) {
                        items.add(item)
                        liveData.value = items
                    }
                }
            }
        return liveData
    }

    fun getFollows(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        mFollow.child(currentUserId!!).addValueEventListener { dataSnapshot ->
            val items = arrayListOf<User>()

            for (snapshot in dataSnapshot.children) {
                val follow = snapshot.getValue(Follow::class.java)

                if (follow!!.type == "follow") {
                    mUsers.child(follow.user_id).addListenerForSingleValueEvent {
                        val item = it.getValue(User::class.java)
                        items.add(item!!)
                        liveData.value = items
                    }
                }
            }

        }
        return liveData
    }

    fun getChatListUsers(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        mChatList.child(currentUserId!!).addValueEventListener { dataSnapshot ->
            val chatList = arrayListOf<ChatList>()
            for (snapshot in dataSnapshot.children) {
                val item = snapshot.getValue(ChatList::class.java)!!
                chatList.add(item)
            }

            chatList.sortWith(compareBy(ChatList::key))
            chatList.reverse()

            val chatListUsers = arrayListOf<User>()
            for (item in chatList) {
                mUsers.child(item.user_id).addListenerForSingleValueEvent {
                    val user = it.getValue(User::class.java)
                    chatListUsers.add(user!!)
                    liveData.value = chatListUsers
                }
            }
        }
        return liveData
    }

    fun getChats(otherId: String): LiveData<List<Messages>> {
        val liveData = MutableLiveData<List<Messages>>()
        mChats.addValueEventListener {
            val messagesList = arrayListOf<Messages>()
            for (messages in it.children) {
                val m = messages.getValue(Messages::class.java)

                if (m!!.receiver == currentUserId && m.sender == otherId ||
                    m.receiver == otherId && m.sender == currentUserId
                ) {
                    messagesList.add(m)
                    liveData.value = messagesList
                }
            }
        }
        return liveData
    }

    fun setLatlng(hashMap: HashMap<String, Any>) {
        mUsers.child(currentUserId!!).updateChildren(hashMap)
    }

    fun setState(state: String) {
        mUsers.child(currentUserId!!).child("state").setValue(state)
    }

    fun setFollow(followId: String, friend: Follow) {
        mFollow.child(currentUserId!!).child(followId).setValue(friend)
    }

    fun setMessages(otherId: String, messages: Messages, onComplete: (() -> Unit)? = null) {
        val key = mChats.push().key
        mChats.child(key!!).setValue(messages).addOnCompleteListener { t ->
            if (t.isSuccessful) {
                onComplete?.invoke()

                mChatList.child(currentUserId!!).child(otherId)
                    .setValue(ChatList(key, otherId))
                mChatList.child(otherId).child(currentUserId)
                    .setValue(ChatList(key, currentUserId))
            }
        }
    }

    fun setRead(otherId: String) {
        mReadListener = mChats.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Messages::class.java)!!
                    if (chat.receiver == currentUserId && chat.sender == otherId)
                        snapshot.ref.child("isread").setValue(true)
                }
            }
        })
    }

    fun setRead() = mChats.removeEventListener(mReadListener)

    fun insertUser(
        name: String,
        imgUrl: String = "default",
        onComplete: () -> Unit
    ) {
        val user = User(currentUserId!!, name, imageURL = imgUrl)
        mUsers.child(currentUserId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful)
                onComplete.invoke()
        }
    }

    fun updateProfile(
        name: String,
        status: String,
        imageUrl: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val hashMap = HashMap<String, Any>()
        hashMap["name"] = name
        hashMap["status"] = status
        hashMap["imageURL"] = imageUrl
        mUsers.child(currentUserId!!).updateChildren(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete.invoke()
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseUpdateEmail(
        oldPassword: String,
        newEmail: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val credential = EmailAuthProvider.getCredential(currentUser!!.email!!, oldPassword)
        currentUser.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser.updateEmail(newEmail).addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        onComplete.invoke()
                    } else {
                        onFailed.invoke(t.exception!!.message!!)
                    }
                }
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseUpdatePassword(
        oldPassword: String,
        newPassword: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val credential = EmailAuthProvider
            .getCredential(currentUser!!.email!!, oldPassword)
        currentUser.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser.updatePassword(newPassword)
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            onComplete.invoke()
                        } else {
                            onFailed.invoke(t.exception!!.message!!)
                        }
                    }
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseSendPasswordResetEmail(
        email: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete.invoke()
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseCreateUserWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete.invoke()
            } else {
                if (task.exception !is FirebaseAuthUserCollisionException)
                    onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseSignInWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete.invoke()
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }
    }

    fun firebaseUploadImage(
        profile: Boolean,
        uri: Uri,
        imgUrl: (String) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val storage = if (profile) storageProfile else storageImage
        storage.child("${System.currentTimeMillis()}.jpg")
        val uploadTask = storage.putFile(uri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }

            storage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()
                imgUrl.invoke(url)
            } else {
                onFailed.invoke(task.exception!!.message!!)
            }
        }.addOnFailureListener { e ->
            onFailed.invoke(e.message!!)
        }
    }

}



