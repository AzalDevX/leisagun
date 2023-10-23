package com.txurdinaga.rednit_app.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import java.util.HashMap

data class Message(
    var messageText: String? = "",
    var senderId: String? = "",
    var timestamp: Long = 0
)

class MessageAdapter(private val messages: List<Message>, private val localUserId: String) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderName: TextView = itemView.findViewById(R.id.messageSenderName)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.drawable.chat_message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        
        var sender_name = if (message.senderId?.contains("@") == true) { message.senderId?.split("@")?.get(0) } else { message.senderId }

        holder.senderName.text = sender_name
        holder.messageText.text = message.messageText

        // Check if the message is from the local user
        if (sender_name == localUserId) {
            // Align to the right for local user
            val layoutParams = holder.messageText.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = Gravity.END
            holder.senderName.layoutParams = layoutParams
            holder.messageText.layoutParams = layoutParams
        } else {
            // Align to the left for others
            val layoutParams = holder.messageText.layoutParams as LinearLayout.LayoutParams
            layoutParams.gravity = Gravity.START
            holder.senderName.layoutParams = layoutParams
            holder.messageText.layoutParams = layoutParams
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}

class ChatActivity : AppCompatActivity() {

    val databaseRef = FirebaseDatabase.getInstance("https://adminapp-7da13-default-rtdb.europe-west1.firebasedatabase.app").reference
    val messagesRef = databaseRef.child("messages")

    val messageList: ArrayList<Message> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Log.d("project|main", "ChatActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        val messageAdapter = MessageAdapter(messageList, globals.current_user?.email?.split("@")?.get(0).toString())
        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            var message_input = findViewById<TextView>(R.id.messageInput)

            // Create a message object
            val message = Message()
            message.messageText = message_input.text.toString() // messageText;
            message.senderId = globals.current_user?.email.toString();
            message.timestamp = System.currentTimeMillis();

            // Push the message to the "messages" node
            val messageRef = databaseRef.child("messages").push()
            messageRef.setValue(message)
                .addOnSuccessListener {
                    // Message has been successfully sent
                    message_input.text = ""
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                    Log.e("project|main", "Error sending message: $e")
                }
        }

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // A new message has been added
                val message = dataSnapshot.getValue(Message::class.java)

                if (message != null)
                    messageList.add(message)

                Log.d("project|main", "onChildAdded has been called message: ${message?.messageText.toString()} and now there are ${messageList.size} has been added ${message != null}")
                messageAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Message data has changed
                val message = dataSnapshot.getValue(Message::class.java)
                if (message != null) {
                    // Find the index of the message to update
                    val index = messageList.indexOfFirst { it.timestamp == message.timestamp }
                    if (index != -1) {
                        messageList[index] = message
                        messageAdapter.notifyDataSetChanged() // Notify the adapter that data has changed
                    }
                }

                Log.d("project|main", "onChildChanged has been called message: ${message?.messageText.toString()} and now there are ${messageList.size} has been added ${message != null}")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Message has been removed
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Message has been moved
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error handling
            }
        })
    }
}