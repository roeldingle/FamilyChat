package io.roeldingle.familychat.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.roeldingle.familychat.R
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage : ChatMessage) : Item<ViewHolder>(){
    /*
    * declare this to be able to get partner user
    * */
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.txtLatestMessage.text = chatMessage.text
        val chatPartnerId: String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }

        val chatPartnerRef = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        chatPartnerRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.txtLatestMessageUsername.text = chatPartnerUser?.username
                val target = viewHolder.itemView.imgLatestMessageProfile
                Picasso.get().load(chatPartnerUser?.profileImgUrl).into(target)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}