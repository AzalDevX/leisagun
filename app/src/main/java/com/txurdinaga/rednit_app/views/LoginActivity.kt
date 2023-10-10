package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.*
import com.txurdinaga.rednit_app.R

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
            val no_completed_error = R.string.login_credential_error_message
            val credential_error = R.string.login_complete_error_message

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, no_completed_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Iniciar MainActivity y pasar el correo electrónico y la contraseña como extras
                        intent.putExtra("email", email)
                        intent.putExtra("password", password)

                        /**
                         * @description: Store credentials locally
                         */

                        val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.apply()

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "El usuario no existe o está deshabilitado.", Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Credenciales inválidas. Por favor, verifica tu correo y contraseña.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // Toast.makeText(this, "Error al iniciar sesión: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            Toast.makeText(this, credential_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }
}