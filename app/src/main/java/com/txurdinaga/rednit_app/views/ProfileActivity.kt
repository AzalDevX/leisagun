package com.txurdinaga.rednit_app.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.txurdinaga.rednit_app.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Log.i("project|main", "ProfileActivity has started!")


        findViewById<Button>(R.id.logout_user_button).setOnClickListener {
            /**
             * @description: Clear stored credentials
             */
            val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", null)
            editor.putString("password", null)
            editor.apply()

            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}