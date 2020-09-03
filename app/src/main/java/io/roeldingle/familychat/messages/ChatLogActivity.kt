package io.roeldingle.familychat.messages

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.roeldingle.familychat.R
import io.roeldingle.familychat.model.ChatMessage
import io.roeldingle.familychat.model.User
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        rvChatLog.adapter = adapter

        /*
        * catch the array sent from NewMessageActivity
        * */
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username.toString()
        listenToMessages()
        btnSendMessage.setOnClickListener {
            initSendMessage()
        }

    }

    private fun listenToMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
               val chatMessages =  snapshot.getValue(ChatMessage::class.java)
                if(chatMessages != null){
                    if (chatMessages.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessageActivity.currentUser ?: return
                        adapter.add(ChatToItem(chatMessages.text, currentUser))
                    }else{
                        if(toUser == null) return
                        adapter.add(ChatFromItem(chatMessages.text, toUser!!))
                    }
                }
                rvChatLog.scrollToPosition(adapter.itemCount - 1)

            }
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    private  fun initSendMessage(){
        val text = txtNewMessage.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid.toString()
        val timestamp = System.currentTimeMillis() / 1000
        /*check if has value*/
        if(fromId == null) return
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val id = ref.key.toString()
        val chatMessage = ChatMessage(id, text, fromId, toId, timestamp)
        /*
        * save user-message for current user and the receiver
        * */
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                txtNewMessage.text.clear()
                rvChatLog.scrollToPosition(adapter.itemCount - 1)
            }
        toRef.setValue(chatMessage)

        /*
       * save latest-message for current user and the receiver
       * */
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }


}

/*
* need this to create items for recyclerview adapter
* */
class ChatFromItem(val text: String, val user: User): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txtFromChat.text = text

        val profileUri = user.profileImgUrl
        val targetImageView = viewHolder.itemView.imgFromChatMessage
        Picasso.get().load(profileUri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txtToChat.text= text

        val profileUri = user.profileImgUrl
        val targetImageView = viewHolder.itemView.imgToChatMessage
        Picasso.get().load(profileUri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}