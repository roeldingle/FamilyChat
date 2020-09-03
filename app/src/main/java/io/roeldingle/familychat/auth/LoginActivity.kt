package io.roeldingle.familychat.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.roeldingle.familychat.messages.LatestMessageActivity
import io.roeldingle.familychat.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.txtLoginPassword

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*
       * will remove action bar
       * */
        if (supportActionBar != null)
            supportActionBar?.hide()

        btnLogin.setOnClickListener {
            initLogin()
        }

        txtGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initLogin(){
        val email = txtLoginEmail.text.toString()
        val password = txtLoginPassword.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please input your email/password", Toast.LENGTH_SHORT).show()
            return
        }
        /*
        * Firebase method to login using email and password
        * */
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "You logged in successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to login user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}