package com.txurdinaga.rednit_app.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Log.d("project|main", "ProfileActivity has started!")

        val globals = application as Globals

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        findViewById<Button>(R.id.logout_user_button).setOnClickListener {
            /**
             * @description: Clear stored credentials
             */
            val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", null)
            editor.putString("password", null)
            editor.apply()

            globals.current_user = null;

            finish()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}