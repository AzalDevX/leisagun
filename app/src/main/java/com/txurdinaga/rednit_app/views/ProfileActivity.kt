package com.txurdinaga.rednit_app.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals
import com.txurdinaga.rednit_app.classes.Utilities

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Log.d("project|main", "ProfileActivity has started!")

        val globals = application as Globals
        val utils = Utilities()

        if (globals.current_user == null)
            startActivity(Intent(this, LoginActivity::class.java))

        findViewById<TextView>(R.id.age_viewer).text = "Age 19"
        findViewById<TextView>(R.id.user_and_surname_view).text = globals.current_user?.displayName ?: "unknown"
        findViewById<TextView>(R.id.email_address_view).text = globals.current_user?.email ?: "unknown"

        val languagePicker = findViewById<Spinner>(R.id.language_picker)
        val languages = arrayOf("Español", "English", "Euskera")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languagePicker.adapter = adapter

        val defaultSelection = when (globals.app_language) {
            "es" -> 0 // Spanish
            "en" -> 1 // English
            "eu" -> 2 // Euskera
            else -> 0 // Default to Spanish if language is not recognized
        }

        languagePicker.setSelection(defaultSelection)

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

        languagePicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = when (position) {
                    0 -> "es" // Spanish
                    1 -> "en" // English
                    2 -> "eu" // Euskera
                    else -> "es" // Default to Spanish
                }

                if (selectedLanguage != globals.app_language) {
                    Log.d("project|main", "languagePicker changed to ${selectedLanguage}")

                    val sharedPreferences = getSharedPreferences("userSettings", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("lang", selectedLanguage)
                    editor.apply()

                    utils.setLocale(this@ProfileActivity, selectedLanguage)

                    globals.app_language = selectedLanguage;
                    recreate() // Restart activity to apply the new locale
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected
            }
        }

        /**
         * Go back to home page button
         */
        findViewById<AppCompatImageView>(R.id.go_back_btn).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}