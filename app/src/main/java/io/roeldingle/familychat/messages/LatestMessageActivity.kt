package io.roeldingle.familychat.messages

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.roeldingle.familychat.R
import io.roeldingle.familychat.auth.RegisterActivity
import io.roeldingle.familychat.model.ChatMessage
import io.roeldingle.familychat.model.LatestMessageRow
import io.roeldingle.familychat.model.User
import kotlinx.android.synthetic.main.activity_latest_message.*


class LatestMessageActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
    }

    lateinit var  notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var  builder: Notification.Builder
    internal lateinit var profileImage: ImageView
    internal lateinit var profileNameText: TextView

    val channelId = "io.roeldingle.familychat"
    val description = "Family Chat"
    val adapter = GroupAdapter<ViewHolder>()

    /*
    * hashmap are array map like in js
    * */
    val latestMessagesMap = HashMap<String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)
        rvLatestMessages.adapter = adapter
        /*
        * put a line under row
        * */
        rvLatestMessages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserIsLoggedIn()
        fetchCurrentUser()
        listenToLatestMessages()

        /*
        * on click goto chat activity with partners data
        * */
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
    }
    /*
    * this will send notification to receiver when app needs tobe running
    * */
    private fun sendNoteMessage(text: String){
        notificationManager=getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
        val intent= Intent(applicationContext,LatestMessageActivity::class.java)
        val pendingIntent= PendingIntent.getActivity(this@LatestMessageActivity,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel= NotificationChannel(channelId,description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor= Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
            builder= Notification.Builder(this@LatestMessageActivity, channelId)
                .setContentTitle("You have a new message")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
        }else{
            builder= Notification.Builder(this@LatestMessageActivity)
                .setContentTitle("You have a new message")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)

        }
        notificationManager.notify(0,builder.build())

        playNotification(this@LatestMessageActivity)
    }

    /*
    * play notification sounds
    * */
    fun playNotification(context: Context){
        try{
            val defaultSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, defaultSoundUri)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    /*
    * will clear the hashmap and add the new array listenToLatestMessages->addChildEventListener
    * */
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        val sortedMap = HashMap<Long, ChatMessage>()//new hashMap

        latestMessagesMap.forEach {
            sortedMap[0 - it.value.timestamp] = it.value
            //add to new hashMap with 0 - timestamp as key
        }
        sortedMap.toSortedMap().values.forEach{
            adapter.add(LatestMessageRow(it))
        }

    }

    private fun listenToLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()

                if(fromId == chatMessage.toId){
                    sendNoteMessage(chatMessage.text)
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)

                val imageUri = currentUser!!.profileImgUrl
                profileImage = findViewById<View>(R.id.imgProfileImage) as ImageView
                Picasso.get().load(imageUri).into(profileImage)

                profileNameText = findViewById<View>(R.id.txtProfileName) as TextView
                profileNameText.text = currentUser!!.username

                //Log.d("LatestMessage", "User ${currentUser!!.username}")
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    /*
    * Check if user is login else push to registration screen
    * */
    private fun verifyUserIsLoggedIn(){
        val uid =FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    /*
    * option to logout or goto new message activity
    * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "You are signed out", Toast.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }
    /*
    * Create a menu directory in res
    * Create a menu resource file inside menu dir
    * Create menu items inside menu resource file
    * User R.menu.nav_menu menuInflater.inflate(R.menu.nav_menu, menu)
    *
    * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


}