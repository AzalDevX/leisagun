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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("project|startup", "App has been started, device information:")
        Log.i("project|model", "Device Model: ${android.os.Build.MODEL}")
        Log.i("project|version", "Android Version: ${android.os.Build.VERSION.RELEASE}")
        Log.i("project|screen", "Screen Size: ${resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK}")
        Log.i("project|resolution", "Screen Resolution: ${resources.displayMetrics.widthPixels.toString() + "x" + resources.displayMetrics.heightPixels.toString()}")

        Log.d("project|main", "MainActivity has started!")

        val globals = application as Globals

        if (globals.current_user != null)
            startActivity(Intent(this, HomeActivity::class.java))

        /**
         * @description: override startup page to your own, uncomment to avoid logging/register
         * */
        // startActivity(Intent(this, RegisterActivity::class.java))

        /**
         * @description: Check local credentials to automatically log in the user
         */
        val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        if (globals.enviroment == "development") {
            Log.i("project|main", "Reading stored login data, email: $email, password: $password")
        }

        val auth = FirebaseAuth.getInstance()

        if (email != null && password != null) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { // User successfully logged in
                        val user = auth.currentUser
                        Log.i("project|autologin", "Automatically log-in in the user: ${user?.displayName ?: user?.email}")
                        globals.current_user = user

                        Toast.makeText(this, "${getString(R.string.autologin_success)} ${user?.displayName ?: user?.email}!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                    } else { // Auto-login failed
                        Log.e("autologin", "Failed to auto-login the user, redirecting to login...")
                        Toast.makeText(this, R.string.autologin_error, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
        } else {
            /**
             * @description: Open login activity by default
             */
            Log.i("project|main", "No login data found, redirecting to login...")
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}

/**
 * @title: String literals
 * When you use the string from strings.xml inside another string concatenated by user thi example
 * @sample: "${R.string.autologin_success}" // it will treat is as an integer for some reason
 * use instead
 * @sample "${getString(R.string.autologin_success)}" // it will fix it by referencing the str
 */