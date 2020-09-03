package io.roeldingle.familychat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.roeldingle.familychat.R
import io.roeldingle.familychat.model.User
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        /*
        * this will change action bar title
        * */
        supportActionBar?.title = "Select User"
        /*
        * call function to get users
        * */
        fetchUsers()

    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                /*
               * implement groupie library for recyclerview adapter
               * */
                val adapter = GroupAdapter<ViewHolder>()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if(user != null){
                        if(user?.uid != FirebaseAuth.getInstance().uid){
                            adapter.add(UserItem(user))
                        }
                    }
                }
                /*
                * on click of users will go to Chat log activity
                * */
                adapter.setOnItemClickListener { item, view ->
                    /*
                    * connect item to obj UserItem and assign to val
                    * */
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    /*
                    * connect item to obj UserItem and assign to val
                    * */
                    intent.putExtra(USER_KEY, userItem.user)

                    startActivity(intent)
                    finish() /*prevent from backing to previous activity*/
                }
                /*
                * send data to recycler view
                * */
                rvNewMessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}


/*
* need this to create items for recyclerview adapter
* */
class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txtUserNameNewMessage.text = user.username
        Picasso.get().load(user.profileImgUrl).into(viewHolder.itemView.imgProfileNewMessage)
    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}