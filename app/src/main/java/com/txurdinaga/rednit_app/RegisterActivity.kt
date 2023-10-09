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
import com.google.firebase.firestore.FirebaseFirestore

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
                Toast.makeText(this, "La contraseña no es la misma.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro exitoso, redirige a la ventana Main
                        val user = auth.currentUser
                        val uid = user?.uid

                        val userMap = hashMapOf(
                            "fullname" to fullname,
                            "age" to age
                        )

                        // Guardar la información adicional en Firestore
                        if (uid != null) {
                            firestore.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("email", email)
                                    intent.putExtra("password", password)
                                    startActivity(intent)
                                    finish()

                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar la información adicional: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Este email ya está registrado.", Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, "La contraseña es demasiado débil.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error al registrar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        login_button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
