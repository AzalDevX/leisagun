package com.txurdinaga.rednit_app.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.txurdinaga.rednit_app.MainActivity
import com.txurdinaga.rednit_app.R
import com.txurdinaga.rednit_app.classes.Globals

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullname_edit_text: EditText
    private lateinit var age_edit_text: EditText
    private lateinit var email_edit_text: EditText
    private lateinit var password_edit_text: EditText
    private lateinit var repassword_edit_text: EditText
    private lateinit var submit_button: Button
    private lateinit var login_button: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Log.d("project|main", "RegisterActivity has started!")

        val globals = application as Globals

        if (globals.current_user != null)
            startActivity(Intent(this, HomeActivity::class.java))

        fullname_edit_text = findViewById(R.id.fullname_edit_text)
        age_edit_text = findViewById(R.id.age_edit_text)
        email_edit_text = findViewById(R.id.email_edit_text)
        password_edit_text = findViewById(R.id.password_edit_text)
        repassword_edit_text = findViewById(R.id.repassword_edit_text)
        submit_button = findViewById(R.id.submit_button)
        login_button = findViewById(R.id.login_button)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        submit_button.setOnClickListener {
            val fullname = fullname_edit_text.text.toString().trim()
            val age = age_edit_text.text.toString().trim()
            val email = email_edit_text.text.toString().trim()
            val password = password_edit_text.text.toString().trim()
            val repassword = repassword_edit_text.text.toString().trim()

            if (!password.equals(repassword)) {
                Toast.makeText(this, R.string.register_complete_error_message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.register_credential_error_message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        globals.current_user = user

                        val userMap = hashMapOf(
                            "fullname" to fullname,
                            "age" to age
                        )

                        /**
                         * @description: Store full name and age in firebase for the current user
                         */
                        if (uid != null) {
                            firestore.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    /**
                                     * @description: Store credentials locally
                                     */
                                    val sharedPreferences = getSharedPreferences("userCredentials", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("email", email)
                                    editor.putString("password", password)
                                    editor.apply()

                                    startActivity(Intent(this, FavouriteActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, R.string.register_additional_info, Toast.LENGTH_SHORT).show()
                                    Log.i("project|register", "Error saving the additional information. exception ${e.localizedMessage}")
                                }
                        }
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, R.string.register_mail_already_exists, Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, R.string.register_pass_weak, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "${R.string.register_unknown_error}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("register", "Exception caught while registering user: ${task.exception.toString()}")
                    }
                }
        }

        login_button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        Log.d("project|main", "RegisterActivity onBackPressed has been called!")

        val globals = application as Globals

        if (globals.current_user != null)
            super.onBackPressed()
    }
}
