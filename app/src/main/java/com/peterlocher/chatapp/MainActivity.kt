package com.peterlocher.chatapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance();
    private val db = Firebase.firestore

    private var adapter: ChatFeedAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        // Show login if user isn't already authenticated
        if (mAuth.currentUser == null) {
            login()
        } else {
            initUser(mAuth.currentUser)
        }
        // Set up UI and listeners
        setUpChatFeed()
        setUpDataBaseListener()
    }

    private val RC_SIGN_IN: Int = 0
    private var displayName: String = "user"

    private fun login() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    /*  Handles authentication callback
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                initUser(user)
            } else {
                Log.w(getString(R.string.tag_firebase), "Sign in failed", response?.error)
            }
        }
    }


    private fun initUser(user: FirebaseUser?) {
        displayName = user?.displayName ?: getString(R.string.default_user_name)
        adapter?.displayName = displayName
        Log.d(getString(R.string.tag_firebase), "Signed in as $displayName")
    }

    private fun setUpChatFeed() {
        chatRecycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatFeedAdapter(this, displayName)
        chatRecycler.adapter = adapter
    }

    // Remembers document ids of messages already fetched
    private val messageDocumentNames: MutableList<String> = mutableListOf()

    /*  Listen for new documents from FireStore each containing a text message
    *   New documents are passed to the adapter
    * */
    private fun setUpDataBaseListener() {
        db.collection(getString(R.string.chatfeed_collection_name))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(getString(R.string.tag_firebase), "Listen failed", e)
                    return@addSnapshotListener
                }
                for (document in snapshot?.documents ?: listOf()) {
                    if (messageDocumentNames.contains(document.id)) continue
                    messageDocumentNames.add(document.id)
                    adapter?.add(document)
                }
                notifyAdapterDataChanged()
            }
    }

    fun onSendMessageClicked(view: View) {
        if (messageEditText.text.toString() != "")
            postTextMessageToServer(messageEditText.text.toString())
        messageEditText.text.clear()
    }


    private fun notifyAdapterDataChanged() {
        scrollChatFeedDown()
        adapter?.sort()
        adapter?.notifyDataSetChanged()
    }

    private fun scrollChatFeedDown() {
        chatRecycler.smoothScrollToPosition(chatRecycler.getAdapter()?.getItemCount() ?: 1 - 1)
    }

    /*  Posting message to FireStore server.
    *   The post will result in a trigger of the previously set up listener,
    *   which will update the GUI with it. This relies on the user always having
    *   internet connection. TODO: Handle no internet scenario
    * */
    private fun postTextMessageToServer(message: String) {
        val currentDateTime = LocalDateTime.now()
        val messagePost = hashMapOf(
            getString(R.string.key_user) to displayName,
            getString(R.string.key_message) to message,
            getString(R.string.key_time) to currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        Log.d(getString(R.string.tag_firebase), "Trying to post text message to server")
        db.collection(getString(R.string.chatfeed_collection_name))
            .add(messagePost)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    getString(R.string.tag_firebase),
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w(getString(R.string.tag_firebase), "Error adding document", e)
            }
    }
}