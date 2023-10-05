package com.txurdinaga.rednit_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class LoginActivity : AppCompatActivity() {

    private lateinit var email_edit_text : EditText
    private lateinit var password_edit_text : EditText
    private lateinit var submit_button : Button
    private lateinit var register_button : Button

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_edit_text = findViewById(R.id.email_edit_text)
        password_edit_text = findViewById(R.id.password_edit_text)
        submit_button = findViewById(R.id.submit_button)
        register_button = findViewById(R.id.register_button)

        auth = FirebaseAuth.getInstance()

        submit_button.setOnClickListener{
            val email = email_edit_text.text.toString().trim()
            val password = password_edit_text.text.toString().trim()
            val no_completed_error = R.string.login_complete_error_message
            val credential_error = R.string.login_credential_error_message

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, no_completed_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Iniciar MainActivity y pasar el correo electrónico y la contraseña como extras
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("password", password)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, credential_error, Toast.LENGTH_SHORT).show()
                    }
                }


        }

        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }
}