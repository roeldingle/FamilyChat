package io.roeldingle.familychat.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.roeldingle.familychat.messages.LatestMessageActivity
import io.roeldingle.familychat.R
import io.roeldingle.familychat.model.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        /*
        * will remove action bar
        * */
        if (supportActionBar != null)
            supportActionBar?.hide()

        btnChangeProfile.setOnClickListener {
            selectProfileImage()
        }

        btnRegister.setOnClickListener {
            initRegister()
        }

        txtGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRegister(){
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please input your email/password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Account credentials saved", Toast.LENGTH_SHORT).show()
                uploadImageToFBS()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create account credentials: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectProfileImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    /*
    * override background func triggered when select in image browser
    * this func is triggered when startActivityForResult method is called
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            imgCircleProfile.setImageBitmap(bitmap)
            btnChangeProfile.alpha = 0f
//            val bitMapDrawable = BitmapDrawable(bitmap)
//            btnChangeProfile.setBackgroundDrawable(bitMapDrawable)
        }
    }

    private fun uploadImageToFBS(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/profile/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show()
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFBD(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image ${it.message}")
                Toast.makeText(this, "Failed to upload image ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveUserToFBD(profileImgUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val username = txtUsername.text.toString()
        val user = User(uid,username,profileImgUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "You registered successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed save user in database ${it.message}", Toast.LENGTH_SHORT).show()
            }


    }
}


