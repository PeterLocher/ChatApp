package com.peterlocher.chatapp

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.text_message_view.view.*

class ChatFeedAdapter(private val context: Context, var displayName: String) : RecyclerView.Adapter<ChatFeedAdapter.TextMessageHolder>() {

    /*  Class for holding messages from FireStore,
    *   which are used to bind views
    * */
    class TextMessage(val user: String, val message: String, val time: String)
    var messages: MutableList<TextMessage> = mutableListOf()

    init {
        sort()
    }

    /*  Sorts messages lexicographically on timestamp
    * */
    fun sort() {
        messages.sortWith(Comparator { m1, m2 -> m1.time.compareTo(m2.time) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextMessageHolder {
        val inflatedView = TextMessageView(parent.context)
        return TextMessageHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: TextMessageHolder, position: Int) {
        val message = messages[position]
        holder.textMessageView.messageText.text = message.message
        holder.textMessageView.messageHeader.text = "${message.user} - ${message.time}"
        // Style message as outgoing or incoming
        if (message.user == displayName) {
            holder.textMessageView.toRightMessage()
        } else {
            holder.textMessageView.toLeftMessage()
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    /*  Add a message to the list of the adapter.
    *   Takes a FireStore document snapshot as input and converts it.
    * */
    fun add(document: DocumentSnapshot?) {
        val message = TextMessage(
            document?.get(context.getString(R.string.key_user)) as? String ?: context.getString(R.string.default_user_name),
            document?.get(context.getString(R.string.key_message)) as? String ?: "",
            document?.get(context.getString(R.string.key_time)) as? String ?: ""
        )
        messages.add(message)
    }

    class TextMessageHolder(itemView: TextMessageView) : RecyclerView.ViewHolder(itemView) {
        val textMessageView: TextMessageView = itemView
    }
}