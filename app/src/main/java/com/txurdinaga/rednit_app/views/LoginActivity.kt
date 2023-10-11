package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.*
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class LoginActivity : AppCompatActivity() {

    private lateinit var email_edit_text : EditText
    private lateinit var password_edit_text : EditText
    private lateinit var submit_button : Button
    private lateinit var register_button : Button

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("project|main", "LoginActivity has started!")

        val globals = application as Globals

        if (globals.current_user != null)
            startActivity(Intent(this, HomeActivity::class.java))

        email_edit_text = findViewById(R.id.email_edit_text)
        password_edit_text = findViewById(R.id.password_edit_text)
        submit_button = findViewById(R.id.submit_button)
        register_button = findViewById(R.id.register_button)

        auth = FirebaseAuth.getInstance()

        submit_button.setOnClickListener{
            val email = email_edit_text.text.toString().trim()
            val password = password_edit_text.text.toString().trim()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.login_credential_error_message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        globals.current_user = user

                        /**
                         * @description: Store credentials locally
                         */
                        val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.apply()

                        Log.i("project|login", "User has been logged-in successfully, redirecting to home...")

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, R.string.login_first_error, Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, R.string.login_invalid_creds_error, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, R.string.login_complete_error_message, Toast.LENGTH_SHORT).show()
                        }
                        Log.e("login", "Exception caught while login-in user: ${task.exception.toString()}")
                    }
                }
        }

        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        Log.d("project|main", "LoginActivity onBackPressed has been called!")

        val globals = application as Globals

        if (globals.current_user != null)
            super.onBackPressed()
    }
}