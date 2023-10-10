package com.txurdinaga.rednit_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.views.HomeActivity
import com.txurdinaga.rednit_app.views.LoginActivity

class MainActivity : AppCompatActivity() {

//    private val globals = application as Globals
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * @description: override startup page to your own, uncomment to avoid logging/register
         * */
        // val intent = Intent(this, RegisterActivity::class.java)
        // startActivity(intent)

        /**
         * @description: Check local credentials to automatically log in the user
         */
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        if (email != null && password != null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { // User successfully logged in
                        val user = FirebaseAuth.getInstance().currentUser
                        Log.i("AUTOLOGIN", "USER: $user")
//                        globals.current_user = user
//                        Log.i("AUTOLOGIN","GLOBAL_USER: $globals.current_user")
                        intent.putExtra("user", user)

                        startActivity(Intent(this, HomeActivity::class.java))
                        Toast.makeText(this, resources.getString(R.string.autologin_success), Toast.LENGTH_SHORT).show()
                    } else { // Auto-login failed
                        Toast.makeText(this, resources.getString(R.string.autologin_error), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
        } else {
            /**
             * @description: Open login activity by default
             */
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}